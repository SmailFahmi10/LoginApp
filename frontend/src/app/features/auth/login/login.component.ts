import { Component, OnInit } from '@angular/core';
import { FormBuilder, FormGroup, Validators } from '@angular/forms';
import { Router } from '@angular/router';

import { AuthService } from '../../../core/services/auth.service';

@Component({
  selector: 'app-login',
  templateUrl: './login.component.html',
  styleUrls: ['./login.component.scss']
})
export class LoginComponent implements OnInit {

  loginForm!: FormGroup;

  isLoading = false;

  errorMessage = '';

  constructor(
    private fb: FormBuilder,
    private authService: AuthService,
    private router: Router
  ) {}

  ngOnInit(): void {
    this.buildForm();
  }

  private buildForm(): void {
    this.loginForm = this.fb.group({
      email: ['', [Validators.required, Validators.email]],
      password: ['', [Validators.required]]
    });
  }

  get emailControl() {
    return this.loginForm.get('email');
  }

  get passwordControl() {
    return this.loginForm.get('password');
  }

  get emailError(): string | null {
    if (!this.emailControl?.touched) return null;
    if (this.emailControl.hasError('required')) return 'El email es obligatorio.';
    if (this.emailControl.hasError('email')) return 'Introduce un email válido.';
    return null;
  }

  get passwordError(): string | null {
    if (!this.passwordControl?.touched) return null;
    if (this.passwordControl.hasError('required')) return 'La contraseña es obligatoria.';
    return null;
  }

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

  onSsoLogin(): void {
    this.authService.initiateSsoLogin();
  }
}
