import { Component, computed } from '@angular/core';
import { RouterLink, RouterLinkActive, RouterOutlet } from '@angular/router';
import { AuthService } from './core/auth.service';

@Component({
  selector: 'app-root',
  standalone: true,
  imports: [RouterOutlet, RouterLink, RouterLinkActive],
  templateUrl: './app.html',
  styleUrl: './app.css',
})
export class App {
  constructor(readonly auth: AuthService) {}

  readonly inicial = computed(() => (this.auth.email() ?? '?').charAt(0).toUpperCase());

  salir(): void {
    this.auth.logout();
  }
}
