import { Module } from '@nestjs/common';
import { ConfigModule } from '@nestjs/config';
import { HttpModule } from '@nestjs/axios';

// Configs
import rabbitmqConfig from './config/rabbitmq.config';
import redisConfig from './config/redis.config';

// Consumers
import { EmailNotificationConsumer } from './consumers/email-notification.consumer';

// Services
import { EmailNotificationService } from './services/email-notification.service';
import { SendGridService } from './services/sendgrid.service';
import { TemplateService } from './services/template.service';
import { StatusService } from './services/status.service';
import { HealthCheckService } from './services/health-check.service';

// Controllers
import { HealthController } from './controllers/health.controller';

@Module({
  imports: [
    ConfigModule.forRoot({
      isGlobal: true,
      load: [rabbitmqConfig, redisConfig],
      envFilePath: ['.env'],
    }),
    HttpModule.register({
      timeout: 5000,
      maxRedirects: 3,
    }),
  ],
  controllers: [HealthController],
  providers: [
    EmailNotificationConsumer,
    EmailNotificationService,
    SendGridService,
    TemplateService,
    StatusService,
    HealthCheckService,
  ],
})
export class AppModule {}