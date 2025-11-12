import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { HttpModule } from '@nestjs/axios';

// Configs
import rabbitmqConfig from './config/rabbitmq.config.js';
import redisConfig from './config/redis.config.js';
import brevoConfig from './config/brevo.config.js';

// Consumers
import { EmailNotificationConsumer } from './consumers/email-notification.consumer.js';

// Services
import { EmailNotificationService } from './services/email-notification.service.js';
import { BrevoEmailService } from './services/brevo-email.service.js';
import { TemplateService } from './services/template.service.js';
import { StatusService } from './services/status.service.js';
import { HealthCheckService } from './services/health-check.service.js';

// Controllers
import { HealthController } from './controllers/health.controller.js';

@Module({
  imports: [
    ConfigModule.forRoot({
      isGlobal: true,
      load: [rabbitmqConfig, redisConfig, brevoConfig],
      envFilePath: ['.env'], // ensures local .env is loaded in dev
    }),
    HttpModule.register({
      timeout: 5000, // Prevent long HTTP hangs
      maxRedirects: 3,
    }),
  ],
  controllers: [HealthController],
  providers: [
    EmailNotificationConsumer,
    EmailNotificationService,
    BrevoEmailService,
    TemplateService,
    StatusService,
    HealthCheckService,
  ],
})
export class AppModule {}



