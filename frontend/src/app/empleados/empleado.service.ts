import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE } from '../core/api-base';
import { Empleado } from './empleado.model';

@Injectable({ providedIn: 'root' })
export class EmpleadoService {
  private readonly base = `${API_BASE}/api/empleados`;

  constructor(private readonly http: HttpClient) {}

  listar(): Observable<Empleado[]> {
    return this.http.get<Empleado[]>(this.base);
  }

  obtener(id: number): Observable<Empleado> {
    return this.http.get<Empleado>(`${this.base}/${id}`);
  }

  crear(dto: Empleado): Observable<Empleado> {
    return this.http.post<Empleado>(this.base, dto);
  }

  actualizar(id: number, dto: Empleado): Observable<Empleado> {
    return this.http.put<Empleado>(`${this.base}/${id}`, dto);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
