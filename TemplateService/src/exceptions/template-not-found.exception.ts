import { NotFoundException } from '@nestjs/common';

/**
 * Custom exception to be thrown when a template is not found.
 */
export class TemplateNotFoundException extends NotFoundException {
  constructor(templateCode: string) {
    super(`Template with code '${templateCode}' not found`);
  }
}
