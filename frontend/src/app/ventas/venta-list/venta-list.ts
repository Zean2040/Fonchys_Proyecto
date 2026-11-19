import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DecimalPipe, DatePipe } from '@angular/common';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { Venta } from '../venta.model';
import { VentaService } from '../venta.service';

@Component({
  selector: 'app-venta-list',
  standalone: true,
  imports: [DecimalPipe, DatePipe, RouterLink],
  templateUrl: './venta-list.html',
})
export class VentaList implements OnInit {
  private readonly ventaService = inject(VentaService);
  readonly auth = inject(AuthService);

  readonly ventas = signal<Venta[]>([]);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);
  readonly mensaje = signal<string | null>(null);

  /** Solo ADMIN puede anular ventas (igual que en el backend). */
  readonly puedeAnular = computed(() => this.auth.rol() === 'ADMIN');

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);
    this.ventaService.listar({ historial: true }).subscribe({
      next: (data) => {
        this.ventas.set(data);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar el historial de ventas.');
        this.cargando.set(false);
      },
    });
  }

  anular(venta: Venta): void {
    if (!confirm(`¿Anular la venta #${venta.id}?`)) {
      return;
    }
    this.ventaService.anular(venta.id).subscribe({
      next: () => {
        this.mensaje.set(`Venta #${venta.id} anulada.`);
        this.cargar();
      },
      error: () => this.error.set('No se pudo anular la venta.'),
    });
  }
}
