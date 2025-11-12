import {
  Controller,
  Get,
  Post,
  Patch,
  Delete,
  Body,
  Param,
  Query,
  HttpCode,
  HttpStatus,
  ValidationPipe,
  ParseIntPipe,
} from '@nestjs/common';
import { TemplateService } from './tenplate.service';
import { CreateTemplateDto } from './dto/create-template.dto';
import { UpdateTemplateDto } from './dto/update-template.dto'

@Controller('internal/templates')
export class TemplateController {
  constructor(private readonly templateService: TemplateService) {}

  // Get template by code (used by Email/Push services)
  @Get(':template_code')
  async getTemplate(@Param('template_code') templateCode: string) {
    const template = await this.templateService.getTemplateByCode(templateCode);
    return {
      success: true,
      data: template,
      message: 'Template retrieved successfully',
    };
  }

  // Create new template
  @Post()
  @HttpCode(HttpStatus.CREATED)
  async createTemplate(@Body(ValidationPipe) dto: CreateTemplateDto) {
    const template = await this.templateService.createTemplate(dto);
    return {
      success: true,
      data: template,
      message: 'Template created successfully',
    };
  }

  // Update template
  @Patch(':template_code')
  async updateTemplate(
    @Param('template_code') templateCode: string,
    @Body(ValidationPipe) dto: UpdateTemplateDto,
  ) {
    const template = await this.templateService.updateTemplate(templateCode, dto);
    return {
      success: true,
      data: template,
      message: 'Template updated successfully',
    };
  }

  // Get all templates (with pagination)
  @Get()
  async getAllTemplates(
    // Use ParseIntPipe for type safety and default values
    @Query('page', new ParseIntPipe({ optional: true })) page = 1,
    @Query('limit', new ParseIntPipe({ optional: true })) limit = 10,
  ) {
    const result = await this.templateService.getAllTemplates(page, limit);
    return {
      success: true,
      data: result.templates,
      message: 'Templates retrieved successfully',
      meta: {
        total: result.total,
        page: result.page,
        limit: result.limit,
        total_pages: result.total_pages,
        has_next: result.has_next,
        has_previous: result.has_previous,
      },
    };
  }

  // Get template versions
  @Get(':template_code/versions')
  async getVersions(@Param('template_code') templateCode: string) {
    const versions = await this.templateService.getTemplateVersions(
      templateCode,
    );
    return {
      success: true,
      data: versions,
      message: 'Template versions retrieved successfully',
    };
  }

  // Delete template (soft delete)
  @Delete(':template_code')
  @HttpCode(HttpStatus.NO_CONTENT)
  async deleteTemplate(@Param('template_code') templateCode: string) {
    await this.templateService.deleteTemplate(templateCode);
    // As per HTTP 204 No Content, we don't return a body
  }
}
