FROM node:20-alpine AS build

WORKDIR /app

COPY package*.json ./

RUN npm ci

COPY . .

RUN npm run build


# ===========================
# Stage 2: Production Runtime
# ===========================
FROM node:20-alpine

WORKDIR /app

COPY package*.json ./
RUN npm ci --only=production

COPY --from=build /app/dist ./dist

EXPOSE 3002

ENV NODE_ENV=production
ENV NODE_OPTIONS="--enable-source-maps"

CMD ["node", "dist/main.js"]
