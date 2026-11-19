import { inject } from '@angular/core';
import { CanActivateFn, Router } from '@angular/router';
import { AuthService } from './auth.service';

/** Bloquea el acceso si el rol del usuario no esta en route.data['roles'] (mismo criterio que el backend). */
export const roleGuard: CanActivateFn = (route) => {
  const auth = inject(AuthService);
  const router = inject(Router);
  const rolesPermitidos = route.data['roles'] as string[] | undefined;

  if (!rolesPermitidos || rolesPermitidos.includes(auth.rol() ?? '')) {
    return true;
  }
  router.navigate(['/productos']);
  return false;
};
