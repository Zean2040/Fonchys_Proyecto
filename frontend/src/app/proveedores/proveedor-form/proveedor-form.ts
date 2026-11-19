import { Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ProveedorService } from '../proveedor.service';

@Component({
  selector: 'app-proveedor-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './proveedor-form.html',
})
export class ProveedorForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly proveedorService = inject(ProveedorService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly error = signal<string | null>(null);
  readonly guardando = signal(false);
  readonly modoEdicion = signal(false);
  private id: number | null = null;

  readonly form = this.fb.group({
    nombre: ['', [Validators.required, Validators.maxLength(150)]],
    ruc: [''],
    telefono: [''],
    email: [''],
    direccion: [''],
  });

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.id = Number(idParam);
      this.modoEdicion.set(true);
      this.proveedorService.obtener(this.id).subscribe({
        next: (proveedor) => this.form.patchValue(proveedor),
        error: () => this.error.set('No se pudo cargar el proveedor.'),
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
      ? this.proveedorService.actualizar(this.id, dto as any)
      : this.proveedorService.crear(dto as any);

    operacion.subscribe({
      next: () => {
        this.guardando.set(false);
        this.router.navigate(['/proveedores']);
      },
      error: (err) => {
        this.guardando.set(false);
        this.error.set(err.error?.message || 'No se pudo guardar el proveedor.');
      },
    });
  }
}
