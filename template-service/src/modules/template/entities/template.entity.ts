import {
  Entity,
  Column,
  PrimaryGeneratedColumn,
  CreateDateColumn,
  UpdateDateColumn,
  OneToMany,
} from 'typeorm';
import { TemplateVersionEntity } from './template-version.entity';

@Entity('templates')
export class TemplateEntity {
  @PrimaryGeneratedColumn('uuid')
  template_id: string;

  @Column({ type: 'varchar', length: 100, unique: true })
  template_code: string;

  @Column({ type: 'text' })
  content: string;

  @Column({ type: 'varchar', length: 255, nullable: true })
  subject: string;

  @Column({ type: 'jsonb', nullable: true })
  variables: string[];

  @Column({ type: 'integer', default: 1 })
  version: number;

  @Column({ type: 'boolean', default: true })
  is_active: boolean;

  @CreateDateColumn()
  created_at: Date;

  @UpdateDateColumn()
  updated_at: Date;

  @OneToMany(() => TemplateVersionEntity, (version) => version.template)
  versions: TemplateVersionEntity[];
}
