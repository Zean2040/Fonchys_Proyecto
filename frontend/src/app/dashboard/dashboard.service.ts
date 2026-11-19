import { Injectable } from '@angular/core';
import { HttpClient } from '@angular/common/http';
import { Observable } from 'rxjs';
import { API_BASE } from '../core/api-base';
import { DashboardResumen } from './dashboard.model';

@Injectable({ providedIn: 'root' })
export class DashboardService {
  private readonly base = `${API_BASE}/api/dashboard`;

  constructor(private readonly http: HttpClient) {}

  resumen(): Observable<DashboardResumen> {
    return this.http.get<DashboardResumen>(this.base);
  }
}
