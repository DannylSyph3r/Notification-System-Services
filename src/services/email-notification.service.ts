// src/services/email-notification.service.ts
import { Injectable, Logger } from '@nestjs/common';
import { NotificationMessageDto } from '../dto/notification-message.dto.js';
import { BrevoEmailService } from './brevo-email.service.js';
import { TemplateService } from './template.service.js';
import { StatusService } from './status.service.js';
import { EmailPayloadDto } from '../dto/email-payload.dto.js';
import { fillTemplate } from '../utils/template-filler.util.js';
import { BrevoException } from '../exceptions/brevo.exception.js';
import { TemplateNotFoundException } from '../exceptions/template-not-found.exception.js';

@Injectable()
export class EmailNotificationService {
  private readonly logger = new Logger(EmailNotificationService.name);

  constructor(
    private readonly brevoService: BrevoEmailService,
    private readonly templateService: TemplateService,
    private readonly statusService: StatusService,
  ) {}

  async sendEmail(message: NotificationMessageDto): Promise<void> {
    const { correlation_id, notification_id, user_preferences, user_contact, template_code, variables } = message;

    // 1. Check user preferences: skip if email disabled
    if (!user_preferences.email) {
      this.logger.log(`[${correlation_id}] Email disabled for user, skipping`);
      await this.statusService.updateStatus(notification_id, 'skipped', null);
      return;
    }

    // 2. Validate email address
    if (!user_contact.email) {
      throw new Error('User email address not found');
    }

    // 3. Fetch and fill template
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

    const emailSubject = fillTemplate(template.subject, variables); // 4. Replace variables in subject
    const emailBody = fillTemplate(template.content, variables); // 5. Replace variables in body

    // 6. Build email payload
    const payload: EmailPayloadDto = {
      to: user_contact.email,
      subject: emailSubject,
      htmlContent: emailBody,
      textContent: this.stripHtml(emailBody),
    };

    // 7. Send via Brevo
    try {
      await this.brevoService.sendEmail(payload);
      // 8. Update status as delivered
      await this.statusService.updateStatus(notification_id, 'delivered', null);
      this.logger.log(`[${correlation_id}] Email sent successfully`);
    } catch (error: any) {
      // 9. Log and throw Brevo exception
      if (error instanceof BrevoException) {
        this.logger.error(`[${correlation_id}] Failed to send email via Brevo: ${error.message}`);
        await this.statusService.updateStatus(notification_id, 'failed', error.message);
      }
      throw error;
    }
  }

  // 10. Update status manually as failed
  async updateStatusAsFailed(notificationId: string, error: string) {
    await this.statusService.updateStatus(notificationId, 'failed', error);
  }

  // 11. Utility: Strip HTML tags for text version
  private stripHtml(html: string): string {
    return html.replace(/<[^>]*>/g, '');
  }
}
