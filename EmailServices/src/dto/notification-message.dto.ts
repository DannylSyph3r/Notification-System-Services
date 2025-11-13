export class NotificationMessageDto {
  notification_id!: string;
  request_id!: string;
  user_id!: string;
  notification_type!: 'email' | 'push';
  template_code!: string;
  variables!: Record<string, any>;
  priority!: number;
  metadata!: {
    retry_count?: number; // optional, tracks retries
  };
  user_preferences!: {
    email: boolean;
    push: boolean;
  };
  user_contact!: {
    email: string;
    push_token: string;
  };
  correlation_id!: string;
}
