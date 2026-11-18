export interface Producto {
  id?: number;
  nombre: string;
  descripcion?: string | null;
  precio: number;
  precioCompra?: number | null;
  stock: number;
  stockMinimo?: number | null;
  activo?: boolean;
  codigoBarras?: string | null;
}

/** Forma exacta que espera ProductoRequestDTO en el backend. */
export interface ProductoRequest {
  nombre: string;
  descripcion?: string | null;
  precio: number;
  precioCompra?: number | null;
  stock: number;
  stockMinimo?: number | null;
}
