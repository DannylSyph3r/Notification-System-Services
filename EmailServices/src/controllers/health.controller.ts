// src/controllers/health.controller.ts
import { Controller, Get } from '@nestjs/common';
import { HealthCheckService } from '../services/health-check.service';

@Controller('health')
export class HealthController {
  constructor(private readonly healthService: HealthCheckService) {}

  @Get()
  async checkHealth() {
    const health = await this.healthService.checkHealth();
    return {
      success: true,
      data: health,
      message: 'Health check completed',
    };
  }
}
