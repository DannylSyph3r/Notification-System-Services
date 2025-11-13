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
      host: this.configService.get<string>('REDIS_HOST') || 'redis',
      port: parseInt(this.configService.get<string>('REDIS_PORT') || '6379', 10),
    });
  }

  async checkHealth() {
    const services: Record<string, string> = {};

    try {
      await this.redis.ping();
      services.redis = 'connected';
    } catch (error) {
      services.redis = 'disconnected';
    }

    try {
      const rabbitHost = this.configService.get<string>('RABBITMQ_HOST') || 'rabbitmq';
      const rabbitPort = this.configService.get<string>('RABBITMQ_PORT') || '5672';
      const rabbitUser = this.configService.get<string>('RABBITMQ_USER') || 'guest';
      const rabbitPass = this.configService.get<string>('RABBITMQ_PASSWORD') || 'guest';

      const connection = await amqp.connect(
        `amqp://${rabbitUser}:${rabbitPass}@${rabbitHost}:${rabbitPort}`,
      );
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