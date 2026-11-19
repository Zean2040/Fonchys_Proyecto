import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE } from '../core/api-base';
import { ReporteVentas } from './reporte.model';

@Injectable({ providedIn: 'root' })
export class ReporteService {
  private readonly base = `${API_BASE}/api/reportes`;

  constructor(private readonly http: HttpClient) {}

  ventas(fechaInicio?: string, fechaFin?: string): Observable<ReporteVentas> {
    const params: Record<string, string> = {};
    if (fechaInicio && fechaFin) {
      params['fechaInicio'] = fechaInicio;
      params['fechaFin'] = fechaFin;
    }
    return this.http.get<ReporteVentas>(`${this.base}/ventas`, { params });
  }
}
