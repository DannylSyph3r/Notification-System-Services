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

# --- THIS IS THE FIX ---
# Install prod deps AND ts-node (which is needed for ormconfig.ts) 
RUN npm ci --only=production && npm install ts-node

# Copy the built application from the 'build' stage
COPY --from=build /app/dist ./dist

# Copy migrations and TypeORM config from the build stage
COPY --from=build /app/migrations ./migrations
COPY --from=build /app/ormconfig.ts ./

# Expose the port the application will run on [cite: 111, 151]
EXPOSE 3001

# Command to run migrations and then start the application 
# This will now work because ts-node is installed
CMD ["sh", "-c", "npm run migration:run && node dist/main"]