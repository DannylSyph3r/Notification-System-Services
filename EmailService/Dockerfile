FROM node:20-alpine AS build

WORKDIR /app

COPY package*.json ./

# --- FIX IS HERE ---
RUN npm ci --legacy-peer-deps

COPY . .

RUN npm run build


# ===========================
# Stage 2: Production Runtime
# ===========================
FROM node:20-alpine

WORKDIR /app

# Copy only package files and install production deps
COPY package*.json ./
# --- AND HERE ---
RUN npm ci --only=production --legacy-peer-deps

# Copy the compiled app from the build stage
COPY --from=build /app/dist ./dist

# Expose the app port
EXPOSE 3002

# Environment configuration
ENV NODE_ENV=production
ENV NODE_OPTIONS="--enable-source-maps"

# Start the service
CMD ["node", "dist/main.js"]