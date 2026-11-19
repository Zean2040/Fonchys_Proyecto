export interface VentaItemRequest {
  productoId: number;
  cantidad: number;
}

export interface VentaRequest {
  items: VentaItemRequest[];
  clienteNombre?: string | null;
  clienteDni?: string | null;
}

export interface VentaDetalle {
  productoId: number;
  productoNombre: string;
  cantidad: number;
  precioUnitario: number;
  subtotal: number;
}

export interface Venta {
  id: number;
  fecha: string;
  total: number;
  estado: 'COMPLETADA' | 'ANULADA';
  clienteNombre?: string | null;
  clienteDni?: string | null;
  usuarioEmail?: string | null;
  detalles: VentaDetalle[];
}
