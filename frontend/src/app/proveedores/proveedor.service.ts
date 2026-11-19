import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE } from '../core/api-base';
import { Proveedor } from './proveedor.model';

@Injectable({ providedIn: 'root' })
export class ProveedorService {
  private readonly base = `${API_BASE}/api/proveedores`;

  constructor(private readonly http: HttpClient) {}

  listar(): Observable<Proveedor[]> {
    return this.http.get<Proveedor[]>(this.base);
  }

  obtener(id: number): Observable<Proveedor> {
    return this.http.get<Proveedor>(`${this.base}/${id}`);
  }

  crear(dto: Proveedor): Observable<Proveedor> {
    return this.http.post<Proveedor>(this.base, dto);
  }

  actualizar(id: number, dto: Proveedor): Observable<Proveedor> {
    return this.http.put<Proveedor>(`${this.base}/${id}`, dto);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
