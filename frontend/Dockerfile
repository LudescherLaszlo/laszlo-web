# Stage 1: Build the React App
FROM node:20-alpine AS builder
WORKDIR /app
COPY package*.json ./
RUN npm install
COPY . .
RUN npm run build

# Stage 2: Serve with Nginx
FROM nginx:alpine
# Replace default Nginx config with our SPA config
COPY nginx.conf /etc/nginx/conf.d/default.conf
# Copy compiled Vite files from the builder (Vite outputs to /dist)
COPY --from=builder /app/dist /usr/share/nginx/html
EXPOSE 80
CMD ["nginx", "-g", "daemon off;"]
