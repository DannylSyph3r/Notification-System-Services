import { Injectable, Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import { HttpService } from '@nestjs/axios';
import { firstValueFrom } from 'rxjs';
import { TemplateNotFoundException } from '../exceptions/template-not-found.exception';

@Injectable()
export class TemplateService {
  private readonly logger = new Logger(TemplateService.name);
  private readonly templateUrl: string;

  constructor(
    private readonly configService: ConfigService,
    private readonly httpService: HttpService,
  ) {
    this.templateUrl = this.configService.get<string>('TEMPLATE_SERVICE_URL') || 'http://template-service:3001';
  }

  async getTemplate(templateCode: string): Promise<{ subject: string; body: string }> {
    const url = `${this.templateUrl}/internal/templates/${templateCode}`;

    try {
      this.logger.log(`Fetching template: ${templateCode} from ${url}`);
      const response = await firstValueFrom(
        this.httpService.get(url),
      );

      return {
        subject: response.data.data.subject,
        body: response.data.data.content,
      };
    } catch (error: any) {
      if (error.response && error.response.status === 404) {
        this.logger.error(`Template not found: ${templateCode}`);
        throw new TemplateNotFoundException(templateCode);
      }

      this.logger.error(
        `Failed to fetch template: ${error.message || 'Unknown error'}`,
        error.stack || '',
      );
      throw error;
    }
  }
}