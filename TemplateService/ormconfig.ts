import { DataSource } from 'typeorm';
import * as dotenv from 'dotenv'; // Import all of dotenv
import * as path from 'path';

// --- THIS IS THE NEW FIX ---
// Use process.cwd() (current working directory) to build a reliable path
const envPath = path.resolve(process.cwd(), '.env');

// Manually load the .env file
const result = dotenv.config({ path: envPath });

// If dotenv fails to load, throw a clear error
if (result.error) {
  throw new Error(
    `Failed to load .env file from: ${envPath}. Error: ${result.error}`,
  );
}
// -----------------------------

// --- VALIDATION (from previous fix) ---
// 1. First, check if the variable exists at all.
if (!process.env.DB_PORT) {
  throw new Error(
    `DB_PORT is not defined in your .env file (${envPath}). Please check it.`,
  );
}
// ------------------------------

// 2. Now that TypeScript knows DB_PORT is a string, we can parse it.
const dbPort = parseInt(process.env.DB_PORT, 10);

// 3. This check is still valid, in case the value is "not-a-number"
if (isNaN(dbPort)) {
  throw new Error(
    `DB_PORT ("${process.env.DB_PORT}") in your .env file is not a valid number.`,
  );
}

export default new DataSource({
  type: 'postgres',
  host: process.env.DB_HOST,
  port: dbPort, // Use the parsed variable
  username: process.env.DB_USERNAME,
  password: process.env.DB_PASSWORD,
  database: process.env.DB_NAME,
  entities: ['src/**/*.entity.{ts,js}'],
  // Updated path to match your package.json generate script
  migrations: ['src/migrations/*.{ts,js}'],
  synchronize: false, // We are using migrations, so this must be false
});