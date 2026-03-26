import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { AuthService } from '../../../core/services/auth.service';

/**
 * Componente de Login (Organismo - Atomic Design).
 *
 * Presenta el formulario de acceso con:
 * - Campo email con validación de formato
 * - Campo password requerido
 * - Botón "ENTRAR" deshabilitado mientras el formulario sea inválido
 * - Botón "Iniciar sesión con SSO" para el flujo OAuth2 simulado
 * - Mensajes de error en la interfaz (sin console.log ni alert)
 */
@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

  /** Formulario reactivo con los campos email y password */
  loginForm!: FormGroup;

  /** Indica si hay una petición HTTP en curso (para deshabilitar el botón) */
  isLoading = false;

  /** Mensaje de error global devuelto por el backend */
  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.buildForm();
  }

  /** Construye el formulario con sus validadores */
  private buildForm(): void {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]]
    });
  }

  // =============================================
  // Accesores para los controles del formulario
  // =============================================

  get emailControl() {
    return this.loginForm.get('email');
  }

  get passwordControl() {
    return this.loginForm.get('password');
  }

  // =============================================
  // Getters de errores de validación
  // =============================================

  /** Devuelve el mensaje de error para el campo email, o null si no hay error */
  get emailError(): string | null {
    if (!this.emailControl?.touched) return null;
    if (this.emailControl.hasError('required')) return 'El email es obligatorio.';
    if (this.emailControl.hasError('email')) return 'Introduce un email válido.';
    return null;
  }

  /** Devuelve el mensaje de error para el campo password, o null si no hay error */
  get passwordError(): string | null {
    if (!this.passwordControl?.touched) return null;
    if (this.passwordControl.hasError('required')) return 'La contraseña es obligatoria.';
    return null;
  }

  // =============================================
  // Envío del formulario
  // =============================================

  /**
   * Maneja el submit del formulario de login.
   * Llama al backend y navega a /dashboard en caso de éxito.
   */
  onSubmit(): void {
    if (this.loginForm.invalid) {
      this.loginForm.markAllAsTouched();
      return;
    }

    this.isLoading = true;
    this.errorMessage = '';

    const { email, password } = this.loginForm.value;

    this.authService.login(email, password).subscribe({
      next: () => {
        this.isLoading = false;
        this.router.navigate(['/dashboard']);
      },
      error: (err: Error) => {
        this.isLoading = false;
        this.errorMessage = err.message;
      }
    });
  }

  // =============================================
  // SSO
  // =============================================

  /**
   * Inicia el flujo SSO redirigiendo el navegador al backend,
   * que a su vez redirige al proveedor SSO simulado.
   */
  onSsoLogin(): void {
    this.authService.initiateSsoLogin();
  }
}
