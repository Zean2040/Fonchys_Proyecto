import { Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { EmpleadoService } from '../empleado.service';

@Component({
  selector: 'app-empleado-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './empleado-form.html',
})
export class EmpleadoForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly empleadoService = inject(EmpleadoService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly error = signal<string | null>(null);
  readonly guardando = signal(false);
  readonly modoEdicion = signal(false);
  private id: number | null = null;

  readonly form = this.fb.group({
    nombre: ['', [Validators.required, Validators.maxLength(100)]],
    apellido: ['', [Validators.required, Validators.maxLength(100)]],
    cargo: ['', [Validators.required, Validators.maxLength(100)]],
    telefono: [''],
    email: ['', [Validators.email]],
  });

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.id = Number(idParam);
      this.modoEdicion.set(true);
      this.empleadoService.obtener(this.id).subscribe({
        next: (empleado) => this.form.patchValue(empleado),
        error: () => this.error.set('No se pudo cargar el empleado.'),
      });
    }
  }

  submit(): void {
    this.error.set(null);
    if (this.form.invalid) {
      this.form.markAllAsTouched();
      return;
    }

    this.guardando.set(true);
    const dto = this.form.getRawValue();
    const operacion = this.id
      ? this.empleadoService.actualizar(this.id, dto as any)
      : this.empleadoService.crear(dto as any);

    operacion.subscribe({
      next: () => {
        this.guardando.set(false);
        this.router.navigate(['/empleados']);
      },
      error: (err) => {
        this.guardando.set(false);
        this.error.set(err.error?.message || 'No se pudo guardar el empleado.');
      },
    });
  }
}
