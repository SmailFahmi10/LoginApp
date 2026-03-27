# Prueba Técnica Econocom — Autenticación JWT + SSO

Implementación full-stack de un sistema de autenticación con Spring Boot (backend) y Angular (frontend).

---

## Requisitos previos

| Herramienta | Versión mínima |
|-------------|----------------|
| Java JDK    | 1.8            |
| Maven       | 3.6+           |
| Node.js     | 16+            |
| npm         | 8+             |
| Angular CLI | 16.2.16        |

---

## Levantar el Backend (Spring Boot)

```bash
cd backend
mvn spring-boot:run
```

El servidor arranca en **http://localhost:8080**.

### Endpoints disponibles

| Método | Ruta                        | Descripción                              |
|--------|-----------------------------|------------------------------------------|
| POST   | `/api/auth/login`           | Login con email/password → devuelve JWT  |
| GET    | `/api/auth/sso`             | Inicio flujo SSO → devuelve `authUrl`    |
| GET    | `/api/auth/sso/callback`    | Procesa código de autorización SSO       |

### Credenciales de prueba

| Email                    | Password   | Rol   |
|--------------------------|------------|-------|
| `admin@econocom.com`     | `admin123` | ADMIN |
| `user@econocom.com`      | `user123`  | USER  |

### Código SSO válido

```
AUTH_CODE_ECONOCOM_2024
```

Para probar el callback SSO manualmente navega a:
```
http://localhost:4200/sso/callback?code=AUTH_CODE_ECONOCOM_2024
```

---

## Levantar el Frontend (Angular)

```bash
cd frontend
npm install
npm start
```

La aplicación arranca en **http://localhost:4200**.

### Rutas de la aplicación

| Ruta             | Componente           | Descripción                          |
|------------------|----------------------|--------------------------------------|
| `/login`         | `LoginComponent`     | Formulario de acceso (ruta por defecto) |
| `/sso/callback`  | `SsoCallbackComponent` | Resultado del flujo SSO             |
| `/dashboard`     | `DashboardComponent` | Panel post-login                     |

---

## Arrancar con Docker

### Requisitos

- [Docker Desktop](https://www.docker.com/products/docker-desktop/) instalado y en marcha
- No es necesario tener Java, Maven ni Node instalados localmente

### Primera vez — arrancar todo

Ejecutar desde la **raíz del proyecto** (donde está el `docker-compose.yml`):

```bash
docker compose up --build
```

Docker construirá las dos imágenes y arrancará los contenedores. La primera vez tarda
varios minutos porque descarga las imágenes base y compila el código. Las siguientes
veces es mucho más rápido gracias a la caché de capas.

Cuando veas estas líneas en la consola, todo está listo:

```
econocom-backend   | Started AuthApiApplication in X seconds
econocom-frontend  | ... start worker process ...
```

| Servicio  | URL                                              |
|-----------|--------------------------------------------------|
| Frontend  | http://localhost:4200                            |
| Backend   | http://localhost:8080                            |
| Swagger   | http://localhost:8080/swagger-ui/index.html      |

### Arrancar en segundo plano

```bash
docker compose up --build -d
```

Los contenedores corren en background y la terminal queda libre.

### Ver el estado de los contenedores

```bash
docker compose ps
```

### Ver los logs

```bash
# Logs de ambos servicios en tiempo real
docker compose logs -f

# Solo el backend (incluye logs de la aplicación Spring Boot)
docker compose logs -f backend

# Solo el frontend (logs de nginx)
docker compose logs -f frontend
```

### Parar los contenedores

```bash
# Parar y eliminar los contenedores (las imágenes se conservan)
docker compose down

# Parar sin eliminar los contenedores
docker compose stop
```

### Reconstruir tras cambios en el código

```bash
# Reconstruir y reiniciar solo el backend
docker compose build backend && docker compose up -d backend

# Reconstruir y reiniciar solo el frontend
docker compose build frontend && docker compose up -d frontend

# Reconstruir todo
docker compose up --build -d
```

### Limpiar completamente

```bash
# Eliminar contenedores, imágenes y caché de build del proyecto
docker compose down --rmi all
```

---

## Despliegue en producción con Traefik + Cloudflare

Arquitectura de red (igual que el resto de apps del servidor):
```
Internet → Cloudflare (DNS + SSL) → Servidor Linux → Traefik → econocom-frontend:80
                                                                      ├── /        → Angular SPA
                                                                      └── /api/*   → econocom-backend:8080
```

El backend **nunca está expuesto** al exterior. Traefik solo ve el frontend (Nginx),
que a su vez proxea las llamadas a la API al backend por la red interna Docker.

### 1. Prerrequisito: red `traefik-public` en el servidor

Si Traefik ya está corriendo para otras apps (como `shop.smailfahmi.com`),
la red ya existe. Verificar:

```bash
docker network ls | grep traefik-public
```

Si no existe, crearla:

```bash
docker network create traefik-public
```

### 2. Preparar el .env en el servidor

```bash
cp .env.example .env
nano .env
```

Contenido del `.env`:

```env
MY_DOMAIN=login.smailfahmi.com

JWT_SECRET=<resultado de: openssl rand -base64 32>
JWT_EXPIRATION=3600000

CORS_ALLOWED_ORIGINS=https://login.smailfahmi.com
SSO_REDIRECT_URI=https://login.smailfahmi.com/sso/callback
SSO_PROVIDER_URL=https://login.smailfahmi.com/api/auth/sso/provider
```

### 3. Arrancar

```bash
docker compose up --build -d
```

Traefik detecta automáticamente los labels del contenedor `econocom-frontend`
y empieza a enrutar `login.smailfahmi.com` hacia él.

Verificar:
```bash
docker compose ps
docker compose logs -f
```

### Solución de problemas

**El puerto 8080 o 4200 ya está en uso**

```bash
# Ver qué proceso ocupa el puerto (ejemplo con 8080)
netstat -ano | findstr :8080   # Windows
lsof -i :8080                  # Mac/Linux
```

Detén el proceso o cambia el puerto en `docker-compose.yml`.

**El frontend no conecta con el backend**

En producción Nginx hace de proxy: `/api/*` → `backend:8080`. Verificar:

```bash
# ¿El backend está sano?
docker compose --profile prod exec frontend curl -s http://backend:8080/api/auth/login
# En dev:
curl http://localhost:8080/api/auth/login
```

**Error de memoria durante el build del frontend**

El build de Angular puede consumir bastante RAM. En Docker Desktop aumenta
la memoria asignada: **Settings → Resources → Memory** (recomendado: 4 GB).

---

## Tests del backend (JUnit 5 + Mockito)

### Ejecutar todos los tests

```bash
cd backend
mvn test
```

Resultado esperado:

```
Tests run: 28, Failures: 0, Errors: 0, Skipped: 0
BUILD SUCCESS
```

### Ejecutar una clase concreta

```bash
# Tests de integración del controller (MockMvc + Spring Security)
mvn test -Dtest=AuthControllerTest

# Tests unitarios del servicio de autenticación (Mockito)
mvn test -Dtest=AuthServiceTest

# Tests unitarios del servicio JWT
mvn test -Dtest=JwtServiceTest
```

### Ejecutar un test individual

```bash
mvn test -Dtest=AuthServiceTest#authenticate_EmailNull_DevuelveNull
```

### Cobertura de tests

| Clase | Tipo | Tests | Qué se verifica |
|-------|------|-------|-----------------|
| `AuthControllerTest` | Integración (MockMvc) | 11 | Login válido/inválido, campos vacíos, SSO callback y provider |
| `AuthServiceTest` | Unitario (Mockito) | 9 | Autenticación, búsqueda por email, casos null y case-insensitive |
| `JwtServiceTest` | Unitario (Spring context) | 8 | Generación, validación, extracción de email, refresco y expiración |

### Informes detallados

Los resultados en XML y TXT se guardan automáticamente en:

```
backend/target/surefire-reports/
```

---

## Swagger UI

### Abrir la documentación

Con el backend en marcha, accede a:

```
http://localhost:8080/swagger-ui/index.html
```

### Probar el login

1. Despliega **POST /api/auth/login** > **Try it out**
2. Usa alguna de las credenciales de la tabla de arriba
3. Pulsa **Execute** — la respuesta incluye el campo `token`

### Usar el token JWT en Swagger

1. Copia el valor del campo `token` de la respuesta
2. Pulsa el botón **Authorize** (candado, arriba a la derecha)
3. Pega el token en **Value** (sin escribir "Bearer", solo el token)
4. Pulsa **Authorize** > **Close**

A partir de aquí todas las peticiones llevan el header `Authorization: Bearer <token>`.

### Probar el flujo SSO desde Swagger

| Paso | Endpoint | Acción |
|------|----------|--------|
| 1 | `GET /api/auth/sso` | Execute → copia el valor de `authUrl` |
| 2 | Pega la `authUrl` en el navegador | Te redirige al proveedor simulado y de ahí al callback |
| 3 | `GET /api/auth/sso/callback` | `code = AUTH_CODE_ECONOCOM_2024` → devuelve JWT |
| 3b | `GET /api/auth/sso/callback` | Cualquier otro código → devuelve 401 |

### JSON spec de la API (sin UI)

```
http://localhost:8080/v3/api-docs
```

---

## Estructura del proyecto

```
/
├── backend/                  # Spring Boot API (Java 1.8)
│   └── src/main/java/com/econocom/authapi/
│       ├── controller/       # AuthController
│       ├── service/          # AuthService, JwtService
│       └── model/            # LoginRequest, LoginResponse, User
│
├── frontend/                 # Angular 16
│   └── src/app/
│       ├── core/
│       │   ├── models/       # Interfaces TypeScript
│       │   └── services/     # AuthService
│       └── features/
│           ├── auth/
│           │   ├── login/          # LoginComponent
│           │   └── sso-callback/   # SsoCallbackComponent
│           └── dashboard/          # DashboardComponent
│
└── assets/                   # Sistema de diseño compartido
    ├── fonts/                # Fuente Lato
    ├── images/               # SVGs y logos
    └── scss/                 # ITCSS: settings, tools, generic, elements...
```

---

## Tecnologías

**Backend**
- Spring Boot 2.7.18 + Java 1.8
- Spring Security (stateless, JWT)
- jjwt 0.11.5 (HS256)
- CORS configurado para `http://localhost:4200`

**Frontend**
- Angular 16.2.16 + TypeScript
- Angular Material 16.2.14
- Reactive Forms con validaciones
- SCSS con metodología BEM + Atomic Design (ITCSS)
