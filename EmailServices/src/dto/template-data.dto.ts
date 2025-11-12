export class TemplateDataDto {
  template_id!: string;
  template_code!: string;
  content!: string; // HTML content
  subject!: string;
  version!: number;
  variables!: string[]; // List of expected variable names
}
