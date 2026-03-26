/**
 * Modelos TypeScript para la autenticación.
 * Reflejan la estructura de las respuestas del backend Spring Boot.
 */

/** Payload enviado al endpoint POST /api/auth/login */
export interface LoginRequest {
  email: string;
  password: string;
}

/** Respuesta exitosa de POST /api/auth/login */
export interface LoginResponse {
  token: string;
  message: string;
  tokenType: string;
  expiresIn: number;
}

/** Respuesta de error del backend */
export interface ErrorResponse {
  error: boolean;
  status: number;
  message: string;
}

/** Respuesta del callback SSO (éxito o error) */
export interface SsoCallbackResponse {
  success: boolean;
  token?: string;
  tokenType?: string;
  message: string;
  expiresIn?: number;
  errorCode?: string;
}
