# --- build stage ---
FROM node:20-alpine AS builder
WORKDIR /app
COPY package.json ./
RUN npm install
COPY . .
# Build con outputPath por defecto de angular.json (dist/sps-spa)
RUN npm run build
# Debug: imprime contenido para que `docker compose build` deje rastro
RUN echo "=== Contenido de /app/dist ===" && ls -la /app/dist && \
    echo "=== Buscando index.html ===" && find /app/dist -name "index.html" -type f

# --- runtime stage: nginx solo sirve los estaticos del bundle Angular ---
FROM nginx:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
# Borrar el index.html default de nginx para que no haya colisiones
RUN rm -f /usr/share/nginx/html/index.html /usr/share/nginx/html/50x.html
# Angular 17/18 con application builder genera dist/sps-spa/browser/
COPY --from=builder /app/dist/sps-spa/browser/ /usr/share/nginx/html/
EXPOSE 80
