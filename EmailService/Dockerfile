# ===========================
# Stage 1: Build
# ===========================
FROM node:20-alpine AS build

WORKDIR /app

# Copy package files first (for better layer caching)
COPY package*.json ./

# Install all dependencies (including dev)
RUN npm ci

# Copy the full source code
COPY . .

# Build the NestJS project
RUN npm run build


# ===========================
# Stage 2: Production Runtime
# ===========================
FROM node:20-alpine

WORKDIR /app

# Copy only package files and install production deps
COPY package*.json ./
RUN npm ci --only=production

# Copy the compiled app from the build stage
COPY --from=build /app/dist ./dist

# Copy environment file if needed (optional, handled by docker-compose)
# COPY .env .env

# Expose the app port
EXPOSE 3002

# Environment configuration
ENV NODE_ENV=production
ENV NODE_OPTIONS="--enable-source-maps"

# Start the service
CMD ["node", "dist/main.js"]
