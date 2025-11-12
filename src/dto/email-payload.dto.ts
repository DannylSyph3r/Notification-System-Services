export class EmailPayloadDto {
  to!: string;            // recipient email
  subject!: string;       // email subject
  htmlContent!: string;   // HTML body
  textContent!: string;   // plain text body
}
