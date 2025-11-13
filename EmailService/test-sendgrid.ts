import { config } from 'dotenv';
config();
import { SendGridService } from './src/services/sendgrid.service';

async function test() {
  const sg = new SendGridService();
  await sg.sendMail(
    'sundayigboke@gmail.com',
    'Test Email from Mael (SendGrid)',
    '<p>✅ This is a test email sent via SendGrid API!</p>'
  );
}

test().catch(console.error);
