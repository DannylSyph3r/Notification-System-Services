import { Module } from '@nestjs/common';
import { ConfigModule, ConfigService } from '@nestjs/config';
import { TypeOrmModule } from '@nestjs/typeorm';
import { TemplateModule } from './modules/template/template.module';
import { HealthController } from './controllers/health.controller';
import { HealthCheckService } from './services/health-check.service';
import { CacheService } from './services/cache.service';
import { TemplateEntity } from './modules/template/entities/template.entity';
import { TemplateVersionEntity } from './modules/template/entities/template-version.entity';

@Module({
  imports: [
    ConfigModule.forRoot({
      isGlobal: true, // Makes ConfigModule available globally
      envFilePath: '.env', // Specify the env file
    }),
    TypeOrmModule.forRootAsync({
      imports: [ConfigModule],
      useFactory: (configService: ConfigService) => ({
        type: 'postgres',
        host: configService.get<string>('DB_HOST'),
        port: configService.get<number>('DB_PORT'),
        username: configService.get<string>('DB_USERNAME'),
        password: configService.get<string>('DB_PASSWORD'),
        database: configService.get<string>('DB_NAME'),
        entities: [__dirname + '/**/*.entity{.ts,.js}'],
        synchronize: false, // Never use true in production. Use migrations.
      }),
      inject: [ConfigService],
    }),
    TemplateModule,
    // We need to import the entities here for the HealthCheckService
    TypeOrmModule.forFeature([TemplateEntity, TemplateVersionEntity]),
  ],
  controllers: [HealthController],
  providers: [HealthCheckService, CacheService],
})
export class AppModule {}

