import { NestFactory } from '@nestjs/core';
import { AppModule } from './app.module.js';
import { ConfigService } from '@nestjs/config';
import { Logger, ValidationPipe } from '@nestjs/common';

async function bootstrap() {
  const app = await NestFactory.create(AppModule, { bufferLogs: true });
  const logger = new Logger('Bootstrap');
  const configService = app.get(ConfigService);

  // Global pipes (for DTO validation if you add class-validator later)
  app.useGlobalPipes(
    new ValidationPipe({
      whitelist: true, // strips unknown fields
      forbidNonWhitelisted: false,
      transform: true, // auto-transform types
    }),
  );

  // Port configuration
  const port = configService.get<number>('PORT') || 3002;

  await app.listen(port);

  logger.log(`✅ Email Service is running on port ${port}`);
}

bootstrap().catch((err) => {
  // Ensures clean logging on startup errors
  console.error('❌ Failed to start Email Service', err);
  process.exit(1);
});
