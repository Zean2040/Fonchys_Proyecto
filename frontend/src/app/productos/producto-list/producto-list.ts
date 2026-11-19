import { Component, OnInit, computed, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { RouterLink } from '@angular/router';
import { AuthService } from '../../core/auth.service';
import { Producto } from '../producto.model';
import { ProductoService } from '../producto.service';

@Component({
  selector: 'app-producto-list',
  standalone: true,
  imports: [RouterLink, DecimalPipe, FormsModule],
  templateUrl: './producto-list.html',
})
export class ProductoList implements OnInit {
  readonly productos = signal<Producto[]>([]);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);
  readonly mensaje = signal<string | null>(null);
  buscar = '';

  /** Solo ADMIN y ALMACENERO pueden crear/editar/eliminar (igual que en el backend). */
  readonly puedeEscribir = computed(() => ['ADMIN', 'ALMACENERO'].includes(this.auth.rol() ?? ''));

  constructor(
    private readonly productoService: ProductoService,
    readonly auth: AuthService,
  ) {}

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);
    this.productoService.listar(this.buscar || undefined).subscribe({
      next: (data) => {
        this.productos.set(data);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar la lista de productos.');
        this.cargando.set(false);
      },
    });
  }

  limpiarBusqueda(): void {
    this.buscar = '';
    this.cargar();
  }

  eliminar(producto: Producto): void {
    if (!confirm(`¿Eliminar "${producto.nombre}"?`)) {
      return;
    }
    this.productoService.eliminar(producto.id!).subscribe({
      next: () => {
        this.mensaje.set(`Producto "${producto.nombre}" eliminado.`);
        this.cargar();
      },
      error: (err) => {
        this.error.set(
          err.status === 403
            ? 'No tienes permiso para eliminar productos con tu rol actual.'
            : 'No se pudo eliminar el producto.',
        );
      },
    });
  }
}
