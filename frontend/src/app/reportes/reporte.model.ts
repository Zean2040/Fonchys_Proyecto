import { Venta } from '../ventas/venta.model';

export interface ReporteVentas {
  ventas: Venta[];
  totalIngresos: number;
  ventasCompletadas: number;
  ventasAnuladas: number;
  ticketPromedio: number;
}
