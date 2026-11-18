import { HttpInterceptorFn } from '@angular/common/http';
import { inject } from '@angular/core';
import { Router } from '@angular/router';
import { catchError, throwError } from 'rxjs';
import { API_BASE } from './api-base';
import { AuthService } from './auth.service';

/**
 * Agrega "Authorization: Bearer <token>" a toda peticion hacia la API, y si el
 * backend responde 401 (token ausente/invalido/expirado) limpia la sesion y
 * redirige al login en vez de dejar al usuario viendo una pantalla rota.
 */
export const authInterceptor: HttpInterceptorFn = (req, next) => {
  const auth = inject(AuthService);
  const router = inject(Router);

  const esApi = req.url.startsWith(`${API_BASE}/api/`);
  const token = auth.getToken();
  const peticion = esApi && token ? req.clone({ setHeaders: { Authorization: `Bearer ${token}` } }) : req;

  return next(peticion).pipe(
    catchError((error) => {
      if (esApi && error.status === 401) {
        auth.logout();
        router.navigate(['/login'], { queryParams: { expirada: '1' } });
      }
      return throwError(() => error);
    }),
  );
};
