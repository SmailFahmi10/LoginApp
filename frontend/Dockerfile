FROM node:18-alpine AS build

WORKDIR /app

COPY frontend/package*.json ./
RUN npm ci

COPY frontend/ .

COPY assets/ /assets/

RUN npm run build -- --configuration production

FROM nginx:1.25-alpine

COPY --from=build /app/dist/frontend /usr/share/nginx/html

COPY frontend/nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
