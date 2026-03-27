import { NgModule } from '@angular/core';
import { RouterModule, Routes } from '@angular/router';

import { LoginComponent } from './features/auth/login/login.component';
import { SsoCallbackComponent } from './features/auth/sso-callback/sso-callback.component';
import { DashboardComponent } from './features/dashboard/dashboard.component';

const routes: Routes = [
  { path: 'login', component: LoginComponent },
  { path: 'sso/callback', component: SsoCallbackComponent },
  { path: 'dashboard', component: DashboardComponent },
  { path: '', redirectTo: 'login', pathMatch: 'full' },
  { path: '**', redirectTo: 'login' }
];

@NgModule({
  imports: [RouterModule.forRoot(routes)],
  exports: [RouterModule]
})
export class AppRoutingModule { }
