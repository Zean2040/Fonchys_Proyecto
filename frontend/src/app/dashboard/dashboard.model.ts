import { Producto } from '../productos/producto.model';

export interface DashboardResumen {
  totalVentasHoy: number;
  cantVentasHoy: number;
  totalProductos: number;
  productosStockBajo: Producto[];
}
