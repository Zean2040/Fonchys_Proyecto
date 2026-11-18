import { Injectable, computed, signal } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Router } from '@angular/router';
import { Observable, tap } from 'rxjs';
import { API_BASE } from './api-base';

export interface LoginResponse {
  token: string;
  tipo: string;
  email: string;
  rol: string;
  expiraEnMs: number;
}

const TOKEN_KEY = 'fonchys_token';
const ROL_KEY = 'fonchys_rol';
const EMAIL_KEY = 'fonchys_email';

@Injectable({ providedIn: 'root' })
export class AuthService {
  private readonly tokenSignal = signal<string | null>(localStorage.getItem(TOKEN_KEY));
  private readonly rolSignal = signal<string | null>(localStorage.getItem(ROL_KEY));
  private readonly emailSignal = signal<string | null>(localStorage.getItem(EMAIL_KEY));

  readonly isAuthenticated = computed(() => !!this.tokenSignal());
  readonly rol = computed(() => this.rolSignal());
  readonly email = computed(() => this.emailSignal());

  constructor(
    private readonly http: HttpClient,
    private readonly router: Router,
  ) {}

  login(email: string, password: string): Observable<LoginResponse> {
    return this.http
      .post<LoginResponse>(`${API_BASE}/api/auth/login`, { email, password })
      .pipe(tap((resp) => this.guardarSesion(resp)));
  }

  private guardarSesion(resp: LoginResponse): void {
    localStorage.setItem(TOKEN_KEY, resp.token);
    localStorage.setItem(ROL_KEY, resp.rol);
    localStorage.setItem(EMAIL_KEY, resp.email);
    this.tokenSignal.set(resp.token);
    this.rolSignal.set(resp.rol);
    this.emailSignal.set(resp.email);
  }

  logout(): void {
    localStorage.removeItem(TOKEN_KEY);
    localStorage.removeItem(ROL_KEY);
    localStorage.removeItem(EMAIL_KEY);
    this.tokenSignal.set(null);
    this.rolSignal.set(null);
    this.emailSignal.set(null);
    this.router.navigate(['/login']);
  }

  getToken(): string | null {
    return this.tokenSignal();
  }
}
