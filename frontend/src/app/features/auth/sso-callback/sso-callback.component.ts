import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { AuthService } from '../../../core/services/auth.service';
import { SsoCallbackResponse } from '../../../core/models/auth.models';

/**
 * Componente SSO Callback.
 *
 * Se activa cuando el proveedor SSO redirige al usuario a /sso/callback?code=XXX.
 * Lee el parámetro ?code de la URL, llama al backend para validarlo
 * y muestra el resultado (éxito o error) al usuario.
 */
@Component({
  selector: 'app-sso-callback',
  templateUrl: './sso-callback.component.html',
  styleUrls: ['./sso-callback.component.scss']
})
export class SsoCallbackComponent implements OnInit {

  /** Estado del proceso: procesando, exito o error */
  state: 'loading' | 'success' | 'error' = 'loading';

  /** Mensaje que se muestra al usuario */
  message = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    // Leer el parámetro ?code de la URL
    const code = this.route.snapshot.queryParamMap.get('code');

    if (!code) {
      this.state = 'error';
      this.message = 'No se recibió el código de autorización SSO. Inicie el proceso de nuevo.';
      return;
    }

    // Llamar al backend con el código recibido
    this.authService.processSsoCallback(code).subscribe({
      next: (response: SsoCallbackResponse) => {
        if (response.success) {
          this.state = 'success';
          this.message = response.message || 'Autenticación SSO completada correctamente.';
          // Navegar al dashboard tras 2 segundos
          setTimeout(() => this.router.navigate(['/dashboard']), 2000);
        } else {
          this.state = 'error';
          this.message = response.message || 'El código SSO no es válido.';
        }
      },
      error: (err: Error) => {
        this.state = 'error';
        this.message = err.message;
      }
    });
  }

  /** Volver a la pantalla de login */
  goToLogin(): void {
    this.router.navigate(['/login']);
  }
}
