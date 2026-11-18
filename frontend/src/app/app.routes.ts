import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'productos' },
  { path: 'login', loadComponent: () => import('./login/login').then((m) => m.Login) },
  {
    path: 'productos',
    canActivate: [authGuard],
    loadComponent: () => import('./productos/producto-list/producto-list').then((m) => m.ProductoList),
  },
  {
    path: 'productos/nuevo',
    canActivate: [authGuard],
    loadComponent: () => import('./productos/producto-form/producto-form').then((m) => m.ProductoForm),
  },
  {
    path: 'productos/editar/:id',
    canActivate: [authGuard],
    loadComponent: () => import('./productos/producto-form/producto-form').then((m) => m.ProductoForm),
  },
  { path: '**', redirectTo: 'productos' },
];
