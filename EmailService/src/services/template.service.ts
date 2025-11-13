import { HttpService } from '@nestjs/axios';
import { Injectable, Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { firstValueFrom } from 'rxjs';
import { TemplateNotFoundException } from '../exceptions/template-not-found.exception';

@Injectable()
export class TemplateService {
  private readonly logger = new Logger(TemplateService.name);
  private readonly templateUrl: string;

  constructor(
    private readonly httpService: HttpService,
    private readonly configService: ConfigService,
  ) {
    this.templateUrl = this.configService.get<string>('TEMPLATE_SERVICE_URL');
    if (!this.templateUrl) {
      throw new Error('TEMPLATE_SERVICE_URL is not defined in environment');
    }
  }

  async getTemplate(
    templateCode: string,
    language: string = 'en',
  ): Promise<{ subject: string; body: string }> {
    const url = `${this.templateUrl}/templates/code/${templateCode}/active?lang=${language}`;
    this.logger.log(`Fetching template from: ${url}`);

    try {
      const response = await firstValueFrom(this.httpService.get(url));
      return response.data;
    } catch (error) {
      if (error.response && error.response.status === 404) {
        this.logger.warn(
          `Template not found for code: ${templateCode}, lang: ${language}`,
        );
        throw new TemplateNotFoundException(templateCode, language);
      }
      this.logger.error(
        `Failed to fetch template: ${error.message}`,
        error.stack,
      );
      throw new Error(`Failed to fetch template: ${templateCode}`);
    }
  }
}