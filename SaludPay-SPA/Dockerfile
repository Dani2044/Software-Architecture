# --- build stage ---
FROM node:20-alpine AS builder
WORKDIR /app
COPY package.json ./
RUN npm install
COPY . .
RUN npm run build
RUN echo "=== Contenido de /app/dist ===" && ls -la /app/dist && \
    echo "=== Buscando index.html ===" && find /app/dist -name "index.html" -type f

# --- runtime stage ---
FROM nginx:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
RUN rm -f /usr/share/nginx/html/index.html /usr/share/nginx/html/50x.html
COPY --from=builder /app/dist/saludpay-spa/browser/ /usr/share/nginx/html/
EXPOSE 80
