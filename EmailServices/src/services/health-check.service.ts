// src/services/health-check.service.ts
import { Injectable, Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import Redis from 'ioredis';
import * as amqp from 'amqplib';
import { SendGridService } from './sendgrid.service';

@Injectable()
export class HealthCheckService {
  private readonly logger = new Logger(HealthCheckService.name);
  private redis: InstanceType<typeof Redis>;

  constructor(
    private readonly configService: ConfigService,
    private readonly sendGridService: SendGridService, // ✅ Use SendGrid instead of Brevo
  ) {
    this.redis = new Redis({
      host: this.configService.get<string>('redis.host') || 'localhost',
      port: parseInt(this.configService.get<string>('redis.port') || '6379'),
    });
  }

  async checkHealth() {
    const services: Record<string, string> = {};

    // 1. Redis
    try {
      await this.redis.ping();
      services.redis = 'connected';
    } catch (err: any) {
      services.redis = 'disconnected';
      this.logger.error(`Redis connection failed: ${err.message}`);
    }

    // 2. RabbitMQ
    try {
      const host = this.configService.get<string>('rabbitmq.host') || 'localhost';
      const port = parseInt(this.configService.get<string>('rabbitmq.port') || '5672');
      const user = this.configService.get<string>('rabbitmq.user') || 'guest';
      const pass = this.configService.get<string>('rabbitmq.password') || 'guest';

      const connection = await amqp.connect(`amqp://${user}:${pass}@${host}:${port}`);
      await connection.close();
      services.rabbitmq = 'connected';
    } catch (err: any) {
      services.rabbitmq = 'disconnected';
      this.logger.error(`RabbitMQ connection failed: ${err.message}`);
    }

    // 3. SendGrid
    try {
      // We can do a quick test by sending to our own address or just check env variables
      const testEmail = this.configService.get<string>('FROM_EMAIL');
      services.sendgrid = testEmail ? 'ready' : 'disconnected';
    } catch (err: any) {
      services.sendgrid = 'disconnected';
      this.logger.error(`SendGrid check failed: ${err.message}`);
    }

    const allConnected = Object.values(services).every(
      status => status === 'connected' || status === 'ready'
    );
    const status = allConnected ? 'healthy' : 'degraded';

    return {
      status,
      services,
      timestamp: new Date().toISOString(),
    };
  }
}
