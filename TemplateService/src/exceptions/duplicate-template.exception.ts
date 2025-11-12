import { ConflictException } from '@nestjs/common';

/**
 * Custom exception to be thrown when attempting to create a template
 * with a 'template_code' that already exists.
 */
export class DuplicateTemplateException extends ConflictException {
  constructor(templateCode: string) {
    super(`Template with code '${templateCode}' already exists`);
  }
}
