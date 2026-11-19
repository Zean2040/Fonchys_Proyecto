import { Component, OnInit, computed, inject, signal } from '@angular/core';
import { DecimalPipe } from '@angular/common';
import { FormsModule } from '@angular/forms';
import { Router, RouterLink } from '@angular/router';
import { Producto } from '../../productos/producto.model';
import { ProductoService } from '../../productos/producto.service';
import { VentaItemRequest } from '../venta.model';
import { VentaService } from '../venta.service';

interface LineaCarrito {
  producto: Producto;
  cantidad: number;
}

@Component({
  selector: 'app-venta-pos',
  standalone: true,
  imports: [DecimalPipe, FormsModule, RouterLink],
  templateUrl: './venta-pos.html',
})
export class VentaPos implements OnInit {
  private readonly productoService = inject(ProductoService);
  private readonly ventaService = inject(VentaService);
  private readonly router = inject(Router);

  readonly productos = signal<Producto[]>([]);
  readonly carrito = signal<LineaCarrito[]>([]);
  readonly error = signal<string | null>(null);
  readonly registrando = signal(false);

  productoSeleccionadoId: number | null = null;
  cantidadSeleccionada = 1;
  clienteNombre = '';
  clienteDni = '';

  readonly total = computed(() =>
    this.carrito().reduce((acc, linea) => acc + linea.producto.precio * linea.cantidad, 0),
  );

  ngOnInit(): void {
    this.productoService.listar().subscribe({
      next: (data) => this.productos.set(data.filter((p) => p.activo !== false)),
      error: () => this.error.set('No se pudo cargar la lista de productos.'),
    });
  }

  agregarAlCarrito(): void {
    this.error.set(null);
    const producto = this.productos().find((p) => p.id === Number(this.productoSeleccionadoId));
    if (!producto || this.cantidadSeleccionada < 1) {
      return;
    }

    const existente = this.carrito().find((l) => l.producto.id === producto.id);
    if (existente) {
      existente.cantidad += this.cantidadSeleccionada;
      this.carrito.set([...this.carrito()]);
    } else {
      this.carrito.set([...this.carrito(), { producto, cantidad: this.cantidadSeleccionada }]);
    }
    this.cantidadSeleccionada = 1;
  }

  quitarLinea(linea: LineaCarrito): void {
    this.carrito.set(this.carrito().filter((l) => l !== linea));
  }

  registrarVenta(): void {
    this.error.set(null);
    if (this.carrito().length === 0) {
      this.error.set('Agrega al menos un producto al carrito.');
      return;
    }

    const items: VentaItemRequest[] = this.carrito().map((l) => ({
      productoId: l.producto.id!,
      cantidad: l.cantidad,
    }));

    this.registrando.set(true);
    this.ventaService
      .registrar({ items, clienteNombre: this.clienteNombre || null, clienteDni: this.clienteDni || null })
      .subscribe({
        next: () => {
          this.registrando.set(false);
          this.router.navigate(['/ventas']);
        },
        error: (err) => {
          this.registrando.set(false);
          this.error.set(err.error?.message || 'No se pudo registrar la venta.');
        },
      });
  }
}
