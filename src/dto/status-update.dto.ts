export class StatusUpdateDto {
  notification_id!: string;
  status!: 'delivered' | 'pending' | 'failed' | 'skipped';
  timestamp!: string;       // ISO timestamp
  error!: string | null;    // error message if failed
}
