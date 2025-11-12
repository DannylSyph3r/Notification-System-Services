import { MigrationInterface, QueryRunner } from "typeorm";

export class NewMigration1762930212488 implements MigrationInterface {
    name = 'NewMigration1762930212488'

    public async up(queryRunner: QueryRunner): Promise<void> {
        await queryRunner.query(`CREATE TABLE "template_versions" ("version_id" uuid NOT NULL DEFAULT uuid_generate_v4(), "template_id" uuid NOT NULL, "content" text NOT NULL, "subject" character varying(255), "variables" jsonb, "version" integer NOT NULL, "created_at" TIMESTAMP NOT NULL DEFAULT now(), CONSTRAINT "PK_96ef3ba5b1e36ee1d67d3e857fe" PRIMARY KEY ("version_id"))`);
        await queryRunner.query(`CREATE TABLE "templates" ("template_id" uuid NOT NULL DEFAULT uuid_generate_v4(), "template_code" character varying(100) NOT NULL, "content" text NOT NULL, "subject" character varying(255), "variables" jsonb, "version" integer NOT NULL DEFAULT '1', "is_active" boolean NOT NULL DEFAULT true, "created_at" TIMESTAMP NOT NULL DEFAULT now(), "updated_at" TIMESTAMP NOT NULL DEFAULT now(), CONSTRAINT "UQ_25fc4c4c600ee1a5b71bad6a95f" UNIQUE ("template_code"), CONSTRAINT "PK_cfafdab99c9325e084ebb3f8aa0" PRIMARY KEY ("template_id"))`);
        await queryRunner.query(`ALTER TABLE "template_versions" ADD CONSTRAINT "FK_d747f429f90a051a017094521b0" FOREIGN KEY ("template_id") REFERENCES "templates"("template_id") ON DELETE NO ACTION ON UPDATE NO ACTION`);
    }

    public async down(queryRunner: QueryRunner): Promise<void> {
        await queryRunner.query(`ALTER TABLE "template_versions" DROP CONSTRAINT "FK_d747f429f90a051a017094521b0"`);
        await queryRunner.query(`DROP TABLE "templates"`);
        await queryRunner.query(`DROP TABLE "template_versions"`);
    }

}
