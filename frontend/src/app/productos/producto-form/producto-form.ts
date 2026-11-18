import { Component, OnInit, inject, signal } from '@angular/core';
import { ReactiveFormsModule, FormBuilder, Validators } from '@angular/forms';
import { ActivatedRoute, Router, RouterLink } from '@angular/router';
import { ProductoService } from '../producto.service';

@Component({
  selector: 'app-producto-form',
  standalone: true,
  imports: [ReactiveFormsModule, RouterLink],
  templateUrl: './producto-form.html',
})
export class ProductoForm implements OnInit {
  private readonly fb = inject(FormBuilder);
  private readonly productoService = inject(ProductoService);
  private readonly route = inject(ActivatedRoute);
  private readonly router = inject(Router);

  readonly error = signal<string | null>(null);
  readonly guardando = signal(false);
  readonly modoEdicion = signal(false);
  private id: number | null = null;

  readonly form = this.fb.group({
    nombre: ['', [Validators.required, Validators.maxLength(150)]],
    descripcion: [''],
    precio: [0, [Validators.required, Validators.min(0.01)]],
    precioCompra: [null as number | null],
    stock: [0, [Validators.required, Validators.min(0)]],
    stockMinimo: [5],
  });

  ngOnInit(): void {
    const idParam = this.route.snapshot.paramMap.get('id');
    if (idParam) {
      this.id = Number(idParam);
      this.modoEdicion.set(true);
      this.productoService.obtener(this.id).subscribe({
        next: (producto) => this.form.patchValue(producto),
        error: () => this.error.set('No se pudo cargar el producto.'),
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
      ? this.productoService.actualizar(this.id, dto as any)
      : this.productoService.crear(dto as any);

    operacion.subscribe({
      next: () => {
        this.guardando.set(false);
        this.router.navigate(['/productos']);
      },
      error: (err) => {
        this.guardando.set(false);
        this.error.set(
          err.status === 403
            ? 'No tienes permiso para esta acción con tu rol actual.'
            : err.error?.message || 'No se pudo guardar el producto.',
        );
      },
    });
  }
}
