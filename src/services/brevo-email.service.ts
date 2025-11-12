// src/services/brevo-email.service.ts
import { Injectable, Logger } from '@nestjs/common';
import { ConfigService } from '@nestjs/config';
import * as nodemailer from 'nodemailer';
import { EmailPayloadDto } from '../dto/email-payload.dto.js';
import { BrevoException } from '../exceptions/brevo.exception.js';

@Injectable()
export class BrevoEmailService {
  private readonly logger = new Logger(BrevoEmailService.name);
  private transporter: any;

  constructor(private readonly configService: ConfigService) {
    // 1. Initialize SMTP transporter
    this.initializeTransporter();
  }

  private initializeTransporter() {
    this.transporter = nodemailer.createTransport({
      host: this.configService.get('BREVO_SMTP_HOST'), // 2. SMTP host
      port: parseInt(this.configService.get('BREVO_SMTP_PORT') || '587'), // 3. SMTP port defaults to 587
      secure: false, // 4. Use TLS
      auth: {
        user: this.configService.get('BREVO_API_KEY'), // 5. SMTP username
        pass: this.configService.get('BREVO_API_KEY'), // 6. SMTP password
      },
    });

    this.logger.log('Brevo SMTP transporter initialized');
  }

  async sendEmail(payload: EmailPayloadDto): Promise<void> {
    const fromEmail = this.configService.get('BREVO_FROM_EMAIL');
    const fromName = this.configService.get('BREVO_FROM_NAME');

    // 1. Build mail options
    const mailOptions = {
      from: `"${fromName}" <${fromEmail}>`,
      to: payload.to,
      subject: payload.subject,
      text: payload.textContent,
      html: payload.htmlContent,
    };

    try {
      // 2. Send email via Brevo
      const info = await this.transporter.sendMail(mailOptions);
      this.logger.log(`Email sent: ${info.messageId}`);
    } catch (err: any) {
      // 3. Throw custom exception on failure
      this.logger.error(`Brevo error: ${err.message}`);
      throw new BrevoException(err.message);
    }
  }

  async verifyConnection(): Promise<boolean> {
    try {
      // 1. Verify transporter connection
      await this.transporter.verify();
      return true;
    } catch (err: any) {
      this.logger.error(`Brevo connection failed: ${err.message}`);
      return false;
    }
  }

}

