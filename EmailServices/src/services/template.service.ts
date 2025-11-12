// src/services/template.service.ts
import { Injectable, Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { HttpService } from '@nestjs/axios';
import { firstValueFrom } from 'rxjs';
import { TemplateDataDto } from '../dto/template-data.dto';
import Redis from 'ioredis';
// const { default: Redis } = pkg; // ESM-compatible import.....Import works now(changed to ESNext in tsconfig.json)
import { TemplateNotFoundException } from '../exceptions/template-not-found.exception';

@Injectable()
export class TemplateService {
  private readonly logger = new Logger(TemplateService.name);
  private redis: InstanceType<typeof Redis>; // Correct Redis typing

  constructor(
    private readonly configService: ConfigService,
    private readonly httpService: HttpService,
  ) {
    // 1. Initialize Redis client
    this.redis = new Redis({
      host: this.configService.get<string>('REDIS_HOST') || 'localhost',
      port: parseInt(this.configService.get<string>('REDIS_PORT') || '6379'),
    });
  }

  async getTemplate(templateCode: string): Promise<TemplateDataDto> {
    // 2. Check Redis cache
    const cacheKey = `template:${templateCode}`;
    const cached = await this.redis.get(cacheKey);
    if (cached) {
      this.logger.log(`Template found in cache: ${templateCode}`);
      return JSON.parse(cached) as TemplateDataDto;
    }

    // 3. Fetch from Template Service if not cached
    const baseUrl = this.configService.get<string>('TEMPLATE_SERVICE_URL') || '';
    const url = `${baseUrl}/internal/templates/${templateCode}`;

    try {
      const response = await firstValueFrom(
        this.httpService.get<TemplateDataDto>(url),
      );
      const template = response.data;

      // 4. Cache the template for 2 hours
      await this.redis.set(cacheKey, JSON.stringify(template), 'EX', 7200);
      this.logger.log(`Template fetched and cached: ${templateCode}`);
      return template;
    } catch (error: any) {
      // 5. Throw custom exception if template not found
      this.logger.error(`Failed to fetch template: ${templateCode} → ${error.message}`);
      throw new TemplateNotFoundException(templateCode);
    }
  }
}
