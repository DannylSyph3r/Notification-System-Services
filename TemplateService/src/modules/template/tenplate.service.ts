import { Injectable, Logger } from '@nestjs/common';
import { InjectRepository } from '@nestjs/typeorm';
import { Repository } from 'typeorm';
import { TemplateEntity } from './entities/template.entity';
import { TemplateVersionEntity } from './entities/template-version.entity';
import { CreateTemplateDto } from './dto/create-template.dto';
import { UpdateTemplateDto } from './dto/update-template.dto';
import { CacheService } from '../../services/cache.service';
import { ConfigService } from '@nestjs/config';
import { TemplateNotFoundException } from '../../exceptions/template-not-found.exception';
import { DuplicateTemplateException } from '../../exceptions/duplicate-template.exception';

@Injectable()
export class TemplateService {
  private readonly logger = new Logger(TemplateService.name);
  private readonly cacheTtl: number;

  constructor(
    @InjectRepository(TemplateEntity)
    private templateRepository: Repository<TemplateEntity>,
    @InjectRepository(TemplateVersionEntity)
    private versionRepository: Repository<TemplateVersionEntity>,
    private cacheService: CacheService,
    private configService: ConfigService,
  ) {
    // Get cache TTL from config service
    this.cacheTtl = this.configService.get<number>('CACHE_TTL') || 7200;
  }

  /**
   * Get a single active template by its unique code.
   * Checks cache first, then falls back to database.
   */
  async getTemplateByCode(templateCode: string): Promise<TemplateEntity> {
    const cacheKey = `template:${templateCode}`;

    // 1. Check cache first
    try {
      const cachedTemplate = await this.cacheService.get<TemplateEntity>(cacheKey);
      if (cachedTemplate) {
        this.logger.log(`Template found in cache: ${templateCode}`);
        return cachedTemplate;
      }
    } catch (error) {
      this.logger.error(`Cache get error for ${templateCode}: ${error.message}`);
    }

    // 2. Fetch from database if not in cache
    this.logger.log(`Template not in cache, fetching from DB: ${templateCode}`);
    const template = await this.templateRepository.findOne({
      where: { template_code: templateCode, is_active: true },
    });

    if (!template) {
      throw new TemplateNotFoundException(templateCode);
    }

    // 3. Cache for 2 hours (or configured TTL)
    try {
      await this.cacheService.set(cacheKey, template, this.cacheTtl);
    } catch (error) {
      this.logger.error(`Cache set error for ${templateCode}: ${error.message}`);
    }

    return template;
  }

  /**
   * Create a new template and its initial version.
   */
  async createTemplate(dto: CreateTemplateDto): Promise<TemplateEntity> {
    // Check if template code already exists
    const existing = await this.templateRepository.findOne({
      where: { template_code: dto.template_code },
    });

    if (existing) {
      throw new DuplicateTemplateException(dto.template_code);
    }

    // Create template
    const template = this.templateRepository.create({
      ...dto,
      version: 1,
      is_active: true,
    });
    const savedTemplate = await this.templateRepository.save(template);

    // Create initial version
    await this.createVersion(savedTemplate);

    this.logger.log(`Template created: ${savedTemplate.template_code}`);
    return savedTemplate;
  }

  /**
   * Update a template. This creates a new version.
   */
  async updateTemplate(
    templateCode: string,
    dto: UpdateTemplateDto,
  ): Promise<TemplateEntity> {
    const template = await this.templateRepository.findOne({
      where: { template_code: templateCode },
    });

    if (!template) {
      throw new TemplateNotFoundException(templateCode);
    }

    // Update template fields
    Object.assign(template, dto);
    template.version += 1; // Increment version
    template.updated_at = new Date();

    const updatedTemplate = await this.templateRepository.save(template);

    // Create new version
    await this.createVersion(updatedTemplate);

    // Invalidate cache
    const cacheKey = `template:${templateCode}`;
    try {
      await this.cacheService.delete(cacheKey);
    } catch (error) {
      this.logger.error(`Cache delete error for ${templateCode}: ${error.message}`);
    }

    this.logger.log(
      `Template updated: ${templateCode}, new version: ${updatedTemplate.version}`,
    );
    return updatedTemplate;
  }

  /**
   * Get all active templates with pagination.
   */
  async getAllTemplates(
    page = 1,
    limit = 10,
  ): Promise<{
    templates: TemplateEntity[];
    total: number;
    page: number;
    limit: number;
    total_pages: number;
    has_next: boolean;
    has_previous: boolean;
  }> {
    const [templates, total] = await this.templateRepository.findAndCount({
      where: { is_active: true },
      order: { created_at: 'DESC' },
      skip: (page - 1) * limit,
      take: limit,
    });

    const total_pages = Math.ceil(total / limit);
    return {
      templates,
      total,
      page,
      limit,
      total_pages,
      has_next: page * limit < total,
      has_previous: page > 1,
    };
  }

  /**
   * Get all version history for a specific template.
   */
  async getTemplateVersions(
    templateCode: string,
  ): Promise<TemplateVersionEntity[]> {
    const template = await this.templateRepository.findOne({
      where: { template_code: templateCode },
    });

    if (!template) {
      throw new TemplateNotFoundException(templateCode);
    }

    return this.versionRepository.find({
      where: { template_id: template.template_id },
      order: { version: 'DESC' },
    });
  }

  /**
   * Soft delete a template (set is_active to false).
   */
  async deleteTemplate(templateCode: string): Promise<void> {
    const template = await this.templateRepository.findOne({
      where: { template_code: templateCode },
    });

    if (!template) {
      throw new TemplateNotFoundException(templateCode);
    }

    template.is_active = false;
    await this.templateRepository.save(template);

    // Invalidate cache
    const cacheKey = `template:${templateCode}`;
    try {
      await this.cacheService.delete(cacheKey);
    } catch (error) {
      this.logger.error(`Cache delete error for ${templateCode}: ${error.message}`);
    }

    this.logger.log(`Template soft-deleted: ${templateCode}`);
  }

  /**
   * Helper function to create a new version record.
   */
  private async createVersion(template: TemplateEntity): Promise<void> {
    const version = this.versionRepository.create({
      template_id: template.template_id,
      content: template.content,
      subject: template.subject,
      variables: template.variables,
      version: template.version,
    });
    await this.versionRepository.save(version);
  }
}
