import {
  Entity,
  Column,
  PrimaryGeneratedColumn,
  CreateDateColumn,
  ManyToOne,
  JoinColumn,
} from 'typeorm';
import { TemplateEntity } from './template.entity';

@Entity('template_versions')
export class TemplateVersionEntity {
  @PrimaryGeneratedColumn('uuid')
  version_id: string;

  @Column({ type: 'uuid' })
  template_id: string;

  @Column({ type: 'text' })
  content: string;

  @Column({ type: 'varchar', length: 255, nullable: true })
  subject: string;

  @Column({ type: 'jsonb', nullable: true }) // Changed from 'json' in plan to 'jsonb' for consistency
  variables: string[];

  @Column({ type: 'integer' })
  version: number;

  @CreateDateColumn()
  created_at: Date;

  @ManyToOne(() => TemplateEntity, (template) => template.versions)
  @JoinColumn({ name: 'template_id' })
  template: TemplateEntity;
}
