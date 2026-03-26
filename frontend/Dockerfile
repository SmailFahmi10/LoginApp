# ---- Etapa 1: Compilación ----
# Imagen Node para instalar dependencias y hacer el build de producción
# Contexto de build: raíz del repositorio (para acceder a /assets/scss)
FROM node:18-alpine AS build

WORKDIR /app

# Copiamos package.json y package-lock.json primero para aprovechar caché de capas
COPY frontend/package*.json ./
RUN npm ci

# Copiamos el código fuente del frontend
COPY frontend/ .

# Copiamos la carpeta /assets de la raíz del repo al nivel que angular.json espera:
# includePaths = "../assets/scss" → relativo a /app → /assets/scss
COPY assets/ /assets/

RUN npm run build -- --configuration production

# ---- Etapa 2: Servidor web ----
# Nginx sirve los estáticos compilados (mucho más ligero que Node en producción)
FROM nginx:1.25-alpine

# Copiamos los estáticos generados por Angular
COPY --from=build /app/dist/frontend /usr/share/nginx/html

# Copiamos la configuración de nginx (necesaria para el enrutamiento SPA)
COPY frontend/nginx.conf /etc/nginx/conf.d/default.conf

EXPOSE 80

CMD ["nginx", "-g", "daemon off;"]
