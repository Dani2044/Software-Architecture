# --- build stage ---
FROM node:20-alpine AS builder
WORKDIR /app
COPY package.json ./
RUN npm install
COPY . .
RUN npm run build -- --output-path=dist

# --- runtime stage: nginx solo sirve los estaticos del bundle Angular ---
FROM nginx:alpine
COPY nginx.conf /etc/nginx/conf.d/default.conf
COPY --from=builder /app/dist/browser /usr/share/nginx/html
EXPOSE 80
