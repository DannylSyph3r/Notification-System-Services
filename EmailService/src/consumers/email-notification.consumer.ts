import { Injectable, OnModuleInit, Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import * as amqp from 'amqp-connection-manager';
import { Channel, ConsumeMessage } from 'amqplib';
import { EmailNotificationService } from '../services/email-notification.service';
import { NotificationMessageDto } from '../dto/notification-message.dto';

@Injectable()
export class EmailNotificationConsumer implements OnModuleInit {
  private readonly logger = new Logger(EmailNotificationConsumer.name);
  private connection!: amqp.AmqpConnectionManager;
  private channelWrapper!: amqp.ChannelWrapper;

  constructor(
    private readonly configService: ConfigService,
    private readonly emailService: EmailNotificationService,
  ) {}

  async onModuleInit() {
    await this.setupRabbitMQ();
  }

  private async setupRabbitMQ() {
    const host = this.configService.get<string>('RABBITMQ_HOST');
    const port = this.configService.get<number>('RABBITMQ_PORT');
    const user = this.configService.get<string>('RABBITMQ_USER');
    const pass = this.configService.get<string>('RABBITMQ_PASSWORD');

    // NEW: Get Exchange, Queue, and Routing Key from config
    const exchange = this.configService.get<string>('RABBITMQ_EXCHANGE');
    const queue = this.configService.get<string>('RABBITMQ_QUEUE_EMAIL');
    const routingKey = this.configService.get<string>('RABBITMQ_ROUTING_KEY_EMAIL');

    if (!exchange || !queue || !routingKey) {
      this.logger.error('RabbitMQ exchange, queue, or routing key is not defined in config');
      return;
    }

    this.connection = amqp.connect([`amqp://${user}:${pass}@${host}:${port}`]);

    this.channelWrapper = this.connection.createChannel({
      json: true,
      setup: async (channel: Channel) => {
        // NEW: Assert the direct exchange
        await channel.assertExchange(exchange, 'direct', { durable: true });
        
        // Assert the durable queue
        await channel.assertQueue(queue, { durable: true });

        // NEW: Bind the queue to the exchange with the routing key
        await channel.bindQueue(queue, exchange, routingKey);

        await channel.prefetch(10); // Set prefetch limit

        this.logger.log(`Waiting for messages in queue: ${queue}`);

        await channel.consume(
          queue,
          async (msg: ConsumeMessage | null) => {
            if (msg) {
              await this.handleMessage(msg, channel);
            }
          },
          { noAck: false },
        );
      },
    });

    this.logger.log('RabbitMQ consumer initialized and bound to exchange');
  }

  private async handleMessage(msg: ConsumeMessage, channel: Channel) {
    const message: NotificationMessageDto = JSON.parse(msg.content.toString());
    const correlationId = message.correlation_id;

    this.logger.log(`[${correlationId}] Received email notification: ${message.notification_id}`);

    try {
      await this.emailService.sendEmail(message);
      channel.ack(msg);
      this.logger.log(`[${correlationId}] Email sent successfully`);
    } catch (error: any) {
      this.logger.error(`[${correlationId}] Error processing email: ${error.message}`);
      await this.handleFailure(message, msg, channel, error);
    }
  }

  private async handleFailure(
    message: NotificationMessageDto,
    msg: ConsumeMessage,
    channel: Channel,
    error: any,
  ) {
    const retryCount = (message.metadata && message.metadata.retry_count) ? message.metadata.retry_count : 0;
    const maxRetries = parseInt(this.configService.get('MAX_RETRIES') || '5');

    if (retryCount < maxRetries) {
      if (!message.metadata) message.metadata = {}; // Ensure metadata exists
      message.metadata.retry_count = retryCount + 1;
      this.logger.log(`[${message.correlation_id}] Will retry (attempt ${retryCount + 1}/${maxRetries})`);
      channel.nack(msg, false, false); // Your original logic: drops or sends to DLQ
    } else {
      this.logger.error(`[${message.correlation_id}] Max retries reached, moving to DLQ`);
      await this.emailService.updateStatusAsFailed(message.notification_id, error.message);
      channel.nack(msg, false, false); // Discard or DLQ
    }
  }
}