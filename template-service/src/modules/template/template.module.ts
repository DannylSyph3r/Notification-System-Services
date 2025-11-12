import { Module } from '@nestjs/common';
import { TypeOrmModule } from '@nestjs/typeorm';
import { TemplateController } from './template.controller'
import { TemplateService } from './tenplate.service';
import { TemplateEntity } from './entities/template.entity';
import { TemplateVersionEntity } from './entities/template-version.entity';
import { CacheService } from '../../services/cache.service'; // Adjusted path

@Module({
  imports: [
    TypeOrmModule.forFeature([TemplateEntity, TemplateVersionEntity]),
  ],
  controllers: [TemplateController],
  // Provide CacheService here so TemplateService can inject it
  providers: [TemplateService, CacheService],
  exports: [TemplateService],
})
export class TemplateModule {}
