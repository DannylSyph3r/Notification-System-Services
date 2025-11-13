import { DataSource } from 'typeorm';

const dbPort = parseInt(process.env.DB_PORT || '5432', 10);

if (isNaN(dbPort)) {
  throw new Error(
    `DB_PORT ("${process.env.DB_PORT}") is not a valid number.`,
  );
}

export default new DataSource({
  type: 'postgres',
  host: process.env.DB_HOST || 'localhost',
  port: dbPort,
  username: process.env.DB_USERNAME,
  password: process.env.DB_PASSWORD,
  database: process.env.DB_NAME,
  entities: ['dist/**/*.entity.js'],
  migrations: ['src/migrations/*.ts'],
  synchronize: false,
});