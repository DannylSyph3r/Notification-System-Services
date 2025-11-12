// This class isn't used for validation, but for defining the
// shape of our API response, which is good practice.
export class TemplateResponseDto {
  template_id: string;
  template_code: string;
  content: string;
  subject: string;
  variables: string[];
  version: number;
  is_active: boolean;
  created_at: Date;
  updated_at: Date;
}

