import { Injectable, Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import Redis from 'ioredis';
import * as amqp from 'amqplib';
import { SendGridService } from './sendgrid.service';

@Injectable()
export class HealthCheckService {
  private readonly logger = new Logger(HealthCheckService.name);
  private redis: Redis;

  constructor(
    private readonly configService: ConfigService,
    private readonly sendGridService: SendGridService,
  ) {
    this.redis = new Redis({
      host: this.configService.get<string>('REDIS_HOST'),
      port: this.configService.get<number>('REDIS_PORT'),
      username: this.configService.get<string>('REDIS_USER'),
      password: this.configService.get<string>('REDIS_PASSWORD'),
    });
  }

  async checkHealth() {
    const services: Record<string, string> = {};

    // Check Redis
    try {
      await this.redis.ping();
      services.redis = 'connected';
    } catch (error) {
      services.redis = 'disconnected';
    }

    // Check RabbitMQ
    try {
      // Use full URL if provided, otherwise fall back to individual components
      const rabbitmqUrl = this.configService.get('RABBITMQ_URL');
      
      let connectionUrl: string;
      
      if (rabbitmqUrl) {
        connectionUrl = rabbitmqUrl;
      } else {
        const host = this.configService.get('RABBITMQ_HOST') || 'rabbitmq';
        const port = this.configService.get('RABBITMQ_PORT') || '5672';
        const user = this.configService.get('RABBITMQ_USER') || 'guest';
        const pass = this.configService.get('RABBITMQ_PASSWORD') || 'guest';
        connectionUrl = `amqp://${user}:${pass}@${host}:${port}`;
      }

      const connection = await amqp.connect(connectionUrl);
      await connection.close();
      services.rabbitmq = 'connected';
    } catch (error) {
      services.rabbitmq = 'disconnected';
    }

    const allConnected = Object.values(services).every(
      (status) => status === 'connected',
    );
    const status = allConnected ? 'healthy' : 'degraded';

    return {
      status,
      services,
      timestamp: new Date().toISOString(),
    };
  }
}