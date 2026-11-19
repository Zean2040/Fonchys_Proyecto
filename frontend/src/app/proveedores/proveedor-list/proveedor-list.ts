import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Proveedor } from '../proveedor.model';
import { ProveedorService } from '../proveedor.service';

@Component({
  selector: 'app-proveedor-list',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './proveedor-list.html',
})
export class ProveedorList implements OnInit {
  private readonly proveedorService = inject(ProveedorService);

  readonly proveedores = signal<Proveedor[]>([]);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);
  readonly mensaje = signal<string | null>(null);

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);
    this.proveedorService.listar().subscribe({
      next: (data) => {
        this.proveedores.set(data);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar la lista de proveedores.');
        this.cargando.set(false);
      },
    });
  }

  eliminar(proveedor: Proveedor): void {
    if (!confirm(`¿Eliminar "${proveedor.nombre}"?`)) {
      return;
    }
    this.proveedorService.eliminar(proveedor.id!).subscribe({
      next: () => {
        this.mensaje.set(`Proveedor "${proveedor.nombre}" eliminado.`);
        this.cargar();
      },
      error: () => this.error.set('No se pudo eliminar el proveedor.'),
    });
  }
}
