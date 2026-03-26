import { Component } from '@angular/core';
import { Router } from '@angular/router';

import { AuthService } from '../../core/services/auth.service';

/**
 * Componente Dashboard.
 *
 * Pantalla de destino tras un login exitoso.
 * En una aplicación real contendría el contenido principal del usuario.
 */
@Component({
  selector: 'app-dashboard',
  templateUrl: './dashboard.component.html',
  styleUrls: ['./dashboard.component.scss']
})
export class DashboardComponent {

  constructor(
    private authService: AuthService,
    private router: Router
  ) {}

  /** Cerrar sesión y volver al login */
  logout(): void {
    this.authService.logout();
    this.router.navigate(['/login']);
  }
}
