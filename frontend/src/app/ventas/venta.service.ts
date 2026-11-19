import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE } from '../core/api-base';
import { Venta, VentaRequest } from './venta.model';

@Injectable({ providedIn: 'root' })
export class VentaService {
  private readonly base = `${API_BASE}/api/ventas`;

  constructor(private readonly http: HttpClient) {}

  listar(opts: { historial?: boolean; fechaInicio?: string; fechaFin?: string } = {}): Observable<Venta[]> {
    const params: Record<string, string> = {};
    if (opts.fechaInicio && opts.fechaFin) {
      params['fechaInicio'] = opts.fechaInicio;
      params['fechaFin'] = opts.fechaFin;
    } else if (opts.historial) {
      params['historial'] = 'true';
    }
    return this.http.get<Venta[]>(this.base, { params });
  }

  obtener(id: number): Observable<Venta> {
    return this.http.get<Venta>(`${this.base}/${id}`);
  }

  registrar(dto: VentaRequest): Observable<Venta> {
    return this.http.post<Venta>(this.base, dto);
  }

  anular(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
