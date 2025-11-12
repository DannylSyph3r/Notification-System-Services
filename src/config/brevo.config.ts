// src/config/brevo.config.ts
import { registerAs } from '@nestjs/config';

export default registerAs('brevo', () => ({
  apiKey: process.env.BREVO_API_KEY || '',
  smtpHost: process.env.BREVO_SMTP_HOST || 'smtp-relay.brevo.com',
  smtpPort: parseInt(process.env.BREVO_SMTP_PORT || '587'),
  fromEmail: process.env.BREVO_FROM_EMAIL || 'noreply@yourapp.com',
  fromName: process.env.BREVO_FROM_NAME || 'Your App Name',
}));
