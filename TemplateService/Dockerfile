# --- Stage 1: Build Stage ---
# Use a Node.js 20 Alpine image as the base for building the app
FROM node:20-alpine AS build

# Set the working directory inside the container
WORKDIR /app

# Copy package.json and package-lock.json
COPY package*.json ./

# Install all dependencies (including devDependencies)
RUN npm ci

# Copy the rest of the source code
COPY . .

# Build the TypeScript application
RUN npm run build

# --- Stage 2: Production Stage ---
# Use a slim Node.js 20 Alpine image for the final container
FROM node:20-alpine

WORKDIR /app

# Copy package.json and package-lock.json
COPY package*.json ./

# Install *only* production dependencies
RUN npm ci --only=production

# Copy the built application from the 'build' stage
COPY --from=build /app/dist ./dist

# Copy migrations and TypeORM config
COPY migrations ./migrations
COPY ormconfig.ts ./

# Expose the port the application will run on
EXPOSE 3001

# Command to run migrations and then start the application
# We use npm run migration:run (from package.json) and then start the app
CMD ["sh", "-c", "npm run migration:run && node dist/main"]
