import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE } from '../core/api-base';
import { Producto, ProductoRequest } from './producto.model';

@Injectable({ providedIn: 'root' })
export class ProductoService {
  private readonly base = `${API_BASE}/api/productos`;

  constructor(private readonly http: HttpClient) {}

  listar(): Observable<Producto[]> {
    return this.http.get<Producto[]>(this.base);
  }

  obtener(id: number): Observable<Producto> {
    return this.http.get<Producto>(`${this.base}/${id}`);
  }

  crear(dto: ProductoRequest): Observable<Producto> {
    return this.http.post<Producto>(this.base, dto);
  }

  actualizar(id: number, dto: ProductoRequest): Observable<Producto> {
    return this.http.put<Producto>(`${this.base}/${id}`, dto);
  }

  eliminar(id: number): Observable<void> {
    return this.http.delete<void>(`${this.base}/${id}`);
  }
}
