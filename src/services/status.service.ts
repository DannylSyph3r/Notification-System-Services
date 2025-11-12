// src/services/status.service.ts
import { Injectable, Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import pkg from 'ioredis';
const { default: Redis } = pkg; // ✅ ESM-compatible import
import { StatusUpdateDto } from '../dto/status-update.dto.js';

@Injectable()
export class StatusService {
  private readonly logger = new Logger(StatusService.name);
  private redis: InstanceType<typeof Redis>; // Proper typing

  constructor(private readonly configService: ConfigService) {
    // Initialize Redis safely
    this.redis = new Redis({
      host: this.configService.get<string>('REDIS_HOST') || 'localhost',
      port: parseInt(this.configService.get<string>('REDIS_PORT') || '6379'),
    });
  }

  async updateStatus(
    notificationId: string,
    status: 'delivered' | 'pending' | 'failed' | 'skipped',
    error: string | null,
  ): Promise<void> {
    const key = `notification:status:${notificationId}`;

    const statusUpdate: StatusUpdateDto = {
      notification_id: notificationId,
      status,
      timestamp: new Date().toISOString(),
      error,
    };

    try {
      // Store in Redis with 24-hour TTL
      await this.redis.set(key, JSON.stringify(statusUpdate), 'EX', 86400);
      this.logger.log(
        `Status updated: notificationId=${notificationId}, status=${status}`,
      );
    } catch (err: any) {
      this.logger.error(`Failed to update status in Redis: ${err.message}`);
      throw err;
    }
  }
}
