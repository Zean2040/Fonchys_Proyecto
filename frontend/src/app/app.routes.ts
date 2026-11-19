import { Routes } from '@angular/router';
import { authGuard } from './core/auth.guard';
import { roleGuard } from './core/role.guard';

export const routes: Routes = [
  { path: '', pathMatch: 'full', redirectTo: 'productos' },
  { path: 'login', loadComponent: () => import('./login/login').then((m) => m.Login) },
  {
    path: 'dashboard',
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] },
    loadComponent: () => import('./dashboard/dashboard').then((m) => m.Dashboard),
  },
  {
    path: 'productos',
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN', 'ALMACENERO'] },
    loadComponent: () => import('./productos/producto-list/producto-list').then((m) => m.ProductoList),
  },
  {
    path: 'productos/nuevo',
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN', 'ALMACENERO'] },
    loadComponent: () => import('./productos/producto-form/producto-form').then((m) => m.ProductoForm),
  },
  {
    path: 'productos/editar/:id',
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN', 'ALMACENERO'] },
    loadComponent: () => import('./productos/producto-form/producto-form').then((m) => m.ProductoForm),
  },
  {
    path: 'ventas',
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN', 'CAJERO'] },
    loadComponent: () => import('./ventas/venta-list/venta-list').then((m) => m.VentaList),
  },
  {
    path: 'ventas/nueva',
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN', 'CAJERO'] },
    loadComponent: () => import('./ventas/venta-pos/venta-pos').then((m) => m.VentaPos),
  },
  {
    path: 'reportes',
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] },
    loadComponent: () => import('./reportes/reportes').then((m) => m.Reportes),
  },
  {
    path: 'proveedores',
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] },
    loadComponent: () => import('./proveedores/proveedor-list/proveedor-list').then((m) => m.ProveedorList),
  },
  {
    path: 'proveedores/nuevo',
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] },
    loadComponent: () => import('./proveedores/proveedor-form/proveedor-form').then((m) => m.ProveedorForm),
  },
  {
    path: 'proveedores/editar/:id',
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] },
    loadComponent: () => import('./proveedores/proveedor-form/proveedor-form').then((m) => m.ProveedorForm),
  },
  {
    path: 'empleados',
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] },
    loadComponent: () => import('./empleados/empleado-list/empleado-list').then((m) => m.EmpleadoList),
  },
  {
    path: 'empleados/nuevo',
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] },
    loadComponent: () => import('./empleados/empleado-form/empleado-form').then((m) => m.EmpleadoForm),
  },
  {
    path: 'empleados/editar/:id',
    canActivate: [authGuard, roleGuard],
    data: { roles: ['ADMIN'] },
    loadComponent: () => import('./empleados/empleado-form/empleado-form').then((m) => m.EmpleadoForm),
  },
  { path: '**', redirectTo: 'productos' },
];
