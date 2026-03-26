import { NgModule } from '@angular/core';
import { BrowserModule } from '@angular/platform-browser';
import { BrowserAnimationsModule } from '@angular/platform-browser/animations';
import { HttpClientModule } from '@angular/common/http';
import { ReactiveFormsModule } from '@angular/forms';

// Angular Material
import { MatFormFieldModule } from '@angular/material/form-field';
import { MatInputModule } from '@angular/material/input';
import { MatButtonModule } from '@angular/material/button';
import { MatIconModule } from '@angular/material/icon';
import { MatProgressSpinnerModule } from '@angular/material/progress-spinner';

// Routing
import { AppRoutingModule } from './app-routing.module';

// Componentes de la aplicación
import { AppComponent } from './app.component';
import { LoginComponent } from './features/auth/login/login.component';
import { SsoCallbackComponent } from './features/auth/sso-callback/sso-callback.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';

/**
 * Módulo raíz de la aplicación.
 *
 * Declara todos los componentes e importa los módulos necesarios:
 * - BrowserAnimationsModule: requerido por Angular Material
 * - HttpClientModule: para las llamadas al backend
 * - ReactiveFormsModule: para el formulario de login
 * - Módulos de Angular Material para los componentes de UI
 */
@NgModule({
  declarations: [
    AppComponent,
    LoginComponent,
    SsoCallbackComponent,
    DashboardComponent
  ],
  imports: [
    BrowserModule,
    BrowserAnimationsModule,
    HttpClientModule,
    ReactiveFormsModule,
    AppRoutingModule,
    // Angular Material
    MatFormFieldModule,
    MatInputModule,
    MatButtonModule,
    MatIconModule,
    MatProgressSpinnerModule
  ],
  providers: [],
  bootstrap: [AppComponent]
})
export class AppModule { }
