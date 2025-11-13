import { Injectable, Logger } from '@nestjs/common';
import { NotificationMessageDto } from '../dto/notification-message.dto';
import { SendGridService } from './sendgrid.service';
import { TemplateService } from './template.service';
import { StatusService } from './status.service';
import { TemplateNotFoundException } from '../exceptions/template-not-found.exception';
import { fillTemplate } from '../utils/template-filler.util';

@Injectable()
export class EmailNotificationService {
  private readonly logger = new Logger(EmailNotificationService.name);

  constructor(
    private readonly sendGridService: SendGridService,
    private readonly templateService: TemplateService,
    private readonly statusService: StatusService,
  ) {}

  async sendEmail(message: NotificationMessageDto): Promise<void> {
    const { correlation_id, notification_id, user_preferences, user_contact, template_code, variables } = message;

    if (!user_preferences.email) {
      this.logger.log(`[${correlation_id}] Email disabled for user, skipping`);
      await this.statusService.updateStatus(notification_id, 'skipped', null);
      return;
    }

    if (!user_contact.email) {
      throw new Error('User email address not found');
    }

    let template;
    try {
      template = await this.templateService.getTemplate(template_code);
    } catch (error: any) {
      if (error instanceof TemplateNotFoundException) {
        this.logger.error(`[${correlation_id}] Template not found: ${template_code}`);
        await this.statusService.updateStatus(notification_id, 'failed', `Template not found: ${template_code}`);
        throw error;
      }
      throw error;
    }

    const emailSubject = fillTemplate(template.subject, variables);
    const emailBody = fillTemplate(template.body, variables);

    try {
      await this.sendGridService.sendMail(user_contact.email, emailSubject, emailBody);
      await this.statusService.updateStatus(notification_id, 'delivered', null);
      this.logger.log(`[${correlation_id}] Email sent successfully`);
    } catch (error: any) {
      this.logger.error(`[${correlation_id}] Failed to send email via SendGrid: ${error.message}`);
      await this.statusService.updateStatus(notification_id, 'failed', error.message);
      throw error;
    }
  }

  async updateStatusAsFailed(notificationId: string, error: string) {
    await this.statusService.updateStatus(notificationId, 'failed', error);
  }
}