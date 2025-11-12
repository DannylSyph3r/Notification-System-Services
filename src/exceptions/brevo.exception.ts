// src/exceptions/brevo.exception.ts
export class BrevoException extends Error {
  constructor(message: string) {
    super(`Brevo Error: ${message}`);
    this.name = 'BrevoException';
  }
}
