import { IsString, IsOptional, IsArray, MaxLength } from 'class-validator';

export class UpdateTemplateDto {
  @IsString()
  @IsOptional()
  content?: string;

  @IsString()
  @IsOptional()
  @MaxLength(255)
  subject?: string;

  @IsArray()
  @IsString({ each: true }) // We validate that each item in the array is a string
  @IsOptional()
  variables?: string[];
}

