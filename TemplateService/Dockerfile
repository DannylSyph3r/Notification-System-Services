# Stage 1: Build
FROM node:20-alpine AS build

WORKDIR /app

COPY package*.json ./

RUN npm install --legacy-peer-deps

COPY . .

RUN npm run build

# Stage 2: Production
FROM node:20-alpine

WORKDIR /app

COPY package*.json ./

RUN npm install --production --legacy-peer-deps

# Copy compiled application
COPY --from=build /app/dist ./dist

# Copy source migrations (needed for typeorm-ts-node-commonjs)
COPY --from=build /app/src/migrations ./src/migrations

# Copy ormconfig
COPY --from=build /app/ormconfig.ts ./

# Install ts-node for running migrations
RUN npm install --save-dev ts-node @types/node --legacy-peer-deps

EXPOSE 3001

CMD ["sh", "-c", "npm run migration:run && node dist/src/main.js"]