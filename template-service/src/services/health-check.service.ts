import { Injectable, Logger } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { TemplateEntity } from '../modules/template/entities/template.entity';
import { CacheService } from './cache.service';

@Injectable()
export class HealthCheckService {
  private readonly logger = new Logger(HealthCheckService.name);

  constructor(
    // We inject the TemplateEntity repository to check the DB connection
    @InjectRepository(TemplateEntity)
    private templateRepository: Repository<TemplateEntity>,
    private cacheService: CacheService,
  ) {}

  async checkHealth() {
    const services: Record<string, string> = {};

    // 1. Check PostgreSQL
    try {
      await this.templateRepository.query('SELECT 1');
      services.database = 'connected';
    } catch (error) {
      this.logger.error(`Health check: Database connection failed: ${error.message}`);
      services.database = 'disconnected';
    }

    // 2. Check Redis
    try {
      await this.cacheService.ping();
      services.redis = 'connected';
    } catch (error) {
      this.logger.error(`Health check: Redis connection failed: ${error.message}`);
      services.redis = 'disconnected';
    }

    // Determine overall status
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
