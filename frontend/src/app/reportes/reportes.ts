import { Component, OnInit, inject, signal } from '@angular/core';
import { DecimalPipe, DatePipe } from '@angular/common';
import { FormBuilder, ReactiveFormsModule } from '@angular/forms';
import { ReporteVentas } from './reporte.model';
import { ReporteService } from './reporte.service';

@Component({
  selector: 'app-reportes',
  standalone: true,
  imports: [DecimalPipe, DatePipe, ReactiveFormsModule],
  templateUrl: './reportes.html',
})
export class Reportes implements OnInit {
  private readonly reporteService = inject(ReporteService);
  private readonly fb = inject(FormBuilder);

  readonly reporte = signal<ReporteVentas | null>(null);
  readonly cargando = signal(true);
  readonly error = signal<string | null>(null);

  readonly filtros = this.fb.group({
    fechaInicio: [''],
    fechaFin: [''],
  });

  ngOnInit(): void {
    this.buscar();
  }

  buscar(): void {
    this.cargando.set(true);
    this.error.set(null);
    const { fechaInicio, fechaFin } = this.filtros.getRawValue();
    this.reporteService.ventas(fechaInicio || undefined, fechaFin || undefined).subscribe({
      next: (data) => {
        this.reporte.set(data);
        this.cargando.set(false);
      },
      error: () => {
        this.error.set('No se pudo cargar el reporte.');
        this.cargando.set(false);
      },
    });
  }

  limpiar(): void {
    this.filtros.reset({ fechaInicio: '', fechaFin: '' });
    this.buscar();
  }
}
