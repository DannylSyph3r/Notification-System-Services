import { Injectable, Logger } from '@nestjs/common';
const sgMail = require('@sendgrid/mail'); // ✅ Keep require for CommonJS

@Injectable()
export class SendGridService {
  private readonly logger = new Logger(SendGridService.name);
  private readonly fromEmail: string;

  constructor() {
    const apiKey = process.env.SENDGRID_API_KEY;
    const from = process.env.FROM_EMAIL;

    if (!apiKey) throw new Error('SENDGRID_API_KEY is missing in environment variables');
    if (!from) throw new Error('FROM_EMAIL is missing in environment variables');

    sgMail.setApiKey(apiKey);
    this.fromEmail = from;

    this.logger.log('✅ SendGrid client initialized');
  }

  async sendMail(to: string, subject: string, html: string, text?: string) {
    const msg = {
      to,
      from: this.fromEmail,
      subject,
      html,
      text,
    };

    try {
      const [response] = await sgMail.send(msg);
      this.logger.log(`✅ Email sent successfully! Status: ${response.statusCode}`);
    } catch (error: any) {
      this.logger.error(`❌ SendGrid error: ${error.message}`);
      if (error.response?.body) this.logger.error(JSON.stringify(error.response.body, null, 2));
      throw error;
    }
  }
}


