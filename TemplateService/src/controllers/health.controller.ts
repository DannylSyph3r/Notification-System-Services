import { Controller, Get } from '@nestjs/common';
import { HealthCheckService } from '../services/health-check.service';

@Controller('health')
export class HealthController {
  constructor(private readonly healthService: HealthCheckService) {}

  @Get()
  async checkHealth() {
    const health = await this.healthService.checkHealth();

    // As per your plan's API summary, we return a standard response object
    return {
      success: true,
      data: health,
      message: 'Health check completed',
    };
  }
}
