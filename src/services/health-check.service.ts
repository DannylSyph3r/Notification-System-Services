// src/services/health-check.service.ts
import { Injectable, Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import pkg from 'ioredis';
const { default: Redis } = pkg; // ✅ Works with ESM
import * as amqp from 'amqplib';
import { BrevoEmailService } from './brevo-email.service.js';

@Injectable()
export class HealthCheckService {
  private readonly logger = new Logger(HealthCheckService.name);
  private redis: InstanceType<typeof Redis>;

  constructor(
    private readonly configService: ConfigService,
    private readonly brevoService: BrevoEmailService,
  ) {
    // ✅ Proper initialization for ESM
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

    // 3. Brevo
    try {
      const connected = await this.brevoService.verifyConnection();
      services.brevo = connected ? 'connected' : 'disconnected';
    } catch (err: any) {
      services.brevo = 'disconnected';
      this.logger.error(`Brevo connection failed: ${err.message}`);
    }

    const allConnected = Object.values(services).every(status => status === 'connected');
    const status = allConnected ? 'healthy' : 'degraded';

    return {
      status,
      services,
      timestamp: new Date().toISOString(),
    };
  }
}
