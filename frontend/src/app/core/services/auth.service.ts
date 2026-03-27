import { Injectable } from '@angular/core';
import { HttpClient, HttpErrorResponse } from '@angular/common/http';
import { Observable, throwError } from 'rxjs';
import { catchError, tap } from 'rxjs/operators';

import { LoginRequest, LoginResponse, SsoCallbackResponse } from '../models/auth.models';

@Injectable({
  providedIn: 'root'
})
export class AuthService {

  private readonly apiUrl = 'http://localhost:8080/api/auth';

  private readonly tokenKey = 'econocom_token';

  constructor(private http: HttpClient) {}

  login(email: string, password: string): Observable<LoginResponse> {
    const payload: LoginRequest = { email, password };

    return this.http.post<LoginResponse>(`${this.apiUrl}/login`, payload).pipe(
      tap((response: LoginResponse) => {
        localStorage.setItem(this.tokenKey, response.token);
      }),
      catchError(this.handleError)
    );
  }

  initiateSsoLogin(): void {
    this.http.get<{ authUrl: string }>(`${this.apiUrl}/sso`).pipe(
      catchError(this.handleError)
    ).subscribe((response) => {
      window.location.href = response.authUrl;
    });
  }

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

  isAuthenticated(): boolean {
    return !!localStorage.getItem(this.tokenKey);
  }

  logout(): void {
    localStorage.removeItem(this.tokenKey);
  }

  private handleError(error: HttpErrorResponse): Observable<never> {
    let message = 'Ha ocurrido un error inesperado. Por favor, inténtelo de nuevo.';

    if (error.error && error.error.message) {
      message = error.error.message;
    } else if (error.status === 0) {
      message = 'No se puede conectar con el servidor. Verifique que el backend esté activo en el puerto 8080.';
    } else if (error.status === 401) {
      message = 'Credenciales inválidas. Verifique su email y contraseña.';
    }

    return throwError(() => new Error(message));
  }
}
