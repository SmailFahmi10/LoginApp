import { Component, OnInit } from '@angular/core';
import { ActivatedRoute, Router } from '@angular/router';

import { AuthService } from '../../../core/services/auth.service';
import { SsoCallbackResponse } from '../../../core/models/auth.models';

@Component({
  selector: 'app-sso-callback',
  templateUrl: './sso-callback.component.html',
  styleUrls: ['./sso-callback.component.scss']
})
export class SsoCallbackComponent implements OnInit {

  state: 'loading' | 'success' | 'error' = 'loading';

  message = '';

  constructor(
    private route: ActivatedRoute,
    private router: Router,
    private authService: AuthService
  ) {}

  ngOnInit(): void {
    const code = this.route.snapshot.queryParamMap.get('code');

    if (!code) {
      this.state = 'error';
      this.message = 'No se recibió el código de autorización SSO. Inicie el proceso de nuevo.';
      return;
    }

    this.authService.processSsoCallback(code).subscribe({
      next: (response: SsoCallbackResponse) => {
        if (response.success) {
          this.state = 'success';
          this.message = response.message || 'Autenticación SSO completada correctamente.';
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

  goToLogin(): void {
    this.router.navigate(['/login']);
  }
}
