# ==============================================================================
# Multi-stage Dockerfile for React Vite Frontend
# ==============================================================================

# Stage 1: Build stage
FROM node:20-alpine AS builder
WORKDIR /app

# Cache package dependencies
COPY package*.json ./
RUN npm ci

# Copy source and build static bundle
COPY . .
RUN npm run build

# Stage 2: Minimal Production Web Server stage
FROM nginx:1.27-alpine AS runner

# Security: Upgrade Alpine OS packages to patch any base CVEs
RUN apk update && apk upgrade --no-cache

# Copy built bundle to Nginx standard directory
COPY --from=builder /app/dist /usr/share/nginx/html

# Custom Nginx configuration for SPA routing & reverse proxy
COPY nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

HEALTHCHECK --interval=30s --timeout=3s --retries=3 \
  CMD wget --quiet --tries=1 --spider http://127.0.0.1:80/ || exit 1

CMD ["nginx", "-g", "daemon off;"]
