import { Component, OnInit, inject, signal } from '@angular/core';
import { RouterLink } from '@angular/router';
import { Empleado } from '../empleado.model';
import { EmpleadoService } from '../empleado.service';

@Component({
  selector: 'app-empleado-list',
  standalone: true,
  imports: [RouterLink],
  templateUrl: './empleado-list.html',
})
export class EmpleadoList implements OnInit {
  private readonly empleadoService = inject(EmpleadoService);

  readonly empleados = signal<Empleado[]>([]);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);
  readonly mensaje = signal<string | null>(null);

  ngOnInit(): void {
    this.cargar();
  }

  cargar(): void {
    this.cargando.set(true);
    this.error.set(null);
    this.empleadoService.listar().subscribe({
      next: (data) => {
        this.empleados.set(data);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar la lista de empleados.');
        this.cargando.set(false);
      },
    });
  }

  eliminar(empleado: Empleado): void {
    if (!confirm(`¿Desactivar a "${empleado.nombre} ${empleado.apellido}"?`)) {
      return;
    }
    this.empleadoService.eliminar(empleado.id!).subscribe({
      next: () => {
        this.mensaje.set(`Empleado "${empleado.nombre}" desactivado.`);
        this.cargar();
      },
      error: () => this.error.set('No se pudo desactivar al empleado.'),
    });
  }
}
