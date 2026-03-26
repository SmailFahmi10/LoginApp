import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';

import { LoginRequest, LoginResponse, SsoCallbackResponse } from '../models/auth.models';

/**
 * Servicio de autenticación.
 *
 * Gestiona las llamadas HTTP al backend Spring Boot (puerto 8080) para:
 * - Login con email/password y almacenamiento del JWT en localStorage
 * - Flujo SSO: inicio y procesamiento del callback
 */
@Injectable({
  providedIn: 'root'
})
export class AuthService {

  /** URL base del backend */
  private readonly apiUrl = 'http://localhost:8080/api/auth';

  /** Clave bajo la que se guarda el JWT en localStorage */
  private readonly tokenKey = 'econocom_token';

  constructor(private http: HttpClient) {}

  // =============================================
  // Login con email / password
  // =============================================

  /**
   * Autenticar usuario con credenciales.
   * En caso de éxito guarda el token en localStorage.
   *
   * @param email    correo electrónico del usuario
   * @param password contraseña del usuario
   * @returns Observable con la respuesta de login (token + mensaje)
   */
  login(email: string, password: string): Observable<LoginResponse> {
    const payload: LoginRequest = { email, password };

    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, payload).pipe(
      tap((response: LoginResponse) => {
        // Guardar el token JWT en localStorage tras autenticación exitosa
        localStorage.setItem(this.tokenKey, response.token);
      }),
      catchError(this.handleError)
    );
  }

  // =============================================
  // SSO
  // =============================================

  /**
   * Inicia el flujo SSO.
   * Pide al backend la URL de autorización y redirige el navegador a ella.
   * El backend devuelve JSON { authUrl: "..." } porque Angular HttpClient
   * no puede seguir redirects 302 cross-origin por restricciones CORS.
   *
   * @returns Observable<void> — suscribirse para disparar la redirección
   */
  initiateSsoLogin(): void {
    // Llama al backend para obtener la URL del proveedor SSO y redirige el navegador.
    // El backend devuelve JSON { authUrl: "..." } porque Angular HttpClient
    // no puede seguir redirects 302 cross-origin por restricciones CORS.
    this.http.get<{ authUrl: string }>(`${this.apiUrl}/sso`).pipe(
      catchError(this.handleError)
    ).subscribe((response) => {
      window.location.href = response.authUrl;
    });
  }

  /**
   * Procesar el código de autorización recibido del proveedor SSO.
   * En caso de éxito guarda el token en localStorage.
   *
   * @param code código de autorización recibido como query param ?code=XXX
   * @returns Observable con la respuesta del callback SSO
   */
  processSsoCallback(code: string): Observable<SsoCallbackResponse> {
    return this.http.get<SsoCallbackResponse>(`${this.apiUrl}/sso/callback`, {
      params: { code }
    }).pipe(
      tap((response: SsoCallbackResponse) => {
        if (response.success && response.token) {
          localStorage.setItem(this.tokenKey, response.token);
        }
      }),
      catchError(this.handleError)
    );
  }

  // =============================================
  // Utilidades de sesión
  // =============================================

  /** Devuelve true si hay un token almacenado (usuario autenticado) */
  isAuthenticated(): boolean {
    return !!localStorage.getItem(this.tokenKey);
  }

  /** Elimina el token y cierra la sesión */
  logout(): void {
    localStorage.removeItem(this.tokenKey);
  }

  // =============================================
  // Manejo de errores HTTP
  // =============================================

  /**
   * Transforma errores HTTP en mensajes legibles para el usuario.
   */
  private handleError(error: HttpErrorResponse): Observable<never> {
    let message = 'Ha ocurrido un error inesperado. Por favor, inténtelo de nuevo.';

    if (error.error && error.error.message) {
      // El backend devuelve un mensaje descriptivo en el cuerpo del error
      message = error.error.message;
    } else if (error.status === 0) {
      // Error de red: el backend no está disponible
      message = 'No se puede conectar con el servidor. Verifique que el backend esté activo en el puerto 8080.';
    } else if (error.status === 401) {
      message = 'Credenciales inválidas. Verifique su email y contraseña.';
    }

    return throwError(() => new Error(message));
  }
}
