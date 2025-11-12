// src/exceptions/template-not-found.exception.ts
export class TemplateNotFoundException extends Error {
  constructor(templateCode: string) {
    super(`Template not found: ${templateCode}`);
    this.name = 'TemplateNotFoundException';
  }
}
