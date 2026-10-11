import { HttpClient, HttpParams } from '@angular/common/http';
import { Injectable, inject } from '@angular/core';
import { Observable } from 'rxjs';

export type AssetShape = 'BOX' | 'SPHERE' | 'CYLINDER';
export type AssetStatus = 'OK' | 'WARNING' | 'ALARM';

export interface Asset {
  id: number;
  name: string;
  shape: AssetShape;
  posX: number;
  posY: number;
  posZ: number;
  color: string;
  status: AssetStatus;
  temperature: number;
  updatedAt: string;
}

/** Oldest and newest moment the timeline can show (ISO-8601 strings). */
export interface HistoryRange {
  earliest: string;
  latest: string;
}

@Injectable({ providedIn: 'root' })
export class AssetService {
  private readonly http = inject(HttpClient);

  /** Current assets, or the room as it was at `at` (ISO-8601). Same JSON shape either way. */
  list(at?: string): Observable<Asset[]> {
    const params = at ? new HttpParams().set('at', at) : undefined;
    return this.http.get<Asset[]>('/api/assets', { params });
  }

  range(): Observable<HistoryRange> {
    return this.http.get<HistoryRange>('/api/history/range');
  }
}
