import { HttpClient } from '@angular/common/http';
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

@Injectable({ providedIn: 'root' })
export class AssetService {
  private readonly http = inject(HttpClient);

  list(): Observable<Asset[]> {
    return this.http.get<Asset[]>('/api/assets');
  }
}
