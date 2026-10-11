import {
  AfterViewInit,
  Component,
  DestroyRef,
  ElementRef,
  NgZone,
  OnDestroy,
  ViewChild,
  computed,
  inject,
  signal,
} from '@angular/core';
import { takeUntilDestroyed } from '@angular/core/rxjs-interop';
import { Subject, catchError, merge, of, switchMap, tap, timer } from 'rxjs';
import * as THREE from 'three';
import { OrbitControls } from 'three/addons/controls/OrbitControls.js';
import { Asset, AssetService, AssetStatus } from '../services/asset.service';

const POLL_MS = 2000;

/** How many seconds of history one second of playback covers. */
const PLAYBACK_SPEEDS = [1, 10, 60, 600];

const STATUS_EMISSIVE: Record<AssetStatus, number> = {
  OK: 0x000000,
  WARNING: 0xff8800,
  ALARM: 0xff0000,
};

@Component({
  selector: 'app-twin-scene',
  templateUrl: './twin-scene.component.html',
  styleUrl: './twin-scene.component.css',
})
export class TwinSceneComponent implements AfterViewInit, OnDestroy {
  @ViewChild('canvasHost', { static: true }) private canvasHost!: ElementRef<HTMLDivElement>;

  private readonly assetService = inject(AssetService);
  private readonly zone = inject(NgZone);
  private readonly destroyRef = inject(DestroyRef);

  readonly assets = signal<Asset[]>([]);
  readonly error = signal<string | null>(null);

  // ---- Time machine state ----
  readonly playbackSpeeds = PLAYBACK_SPEEDS;
  /** The moment being shown, in epoch milliseconds, or null when following live data. */
  readonly viewTime = signal<number | null>(timeFromUrl());
  readonly playing = signal(false);
  readonly speed = signal(10);
  /** Oldest and newest moment the slider can reach, in epoch milliseconds. */
  readonly range = signal<{ earliest: number; latest: number } | null>(null);
  readonly isLive = computed(() => this.viewTime() === null);
  readonly sliderValue = computed(() => this.viewTime() ?? this.range()?.latest ?? 0);
  readonly timeLabel = computed(() => {
    const t = this.viewTime();
    return t === null ? 'LIVE' : new Date(t).toLocaleString();
  });
  /** Emits when the user scrubs, so the scene updates right away instead of on the next poll. */
  private readonly reload$ = new Subject<void>();

  private renderer!: THREE.WebGLRenderer;
  private scene!: THREE.Scene;
  private camera!: THREE.PerspectiveCamera;
  private controls!: OrbitControls;
  private resizeObserver?: ResizeObserver;
  private readonly meshes = new Map<number, THREE.Mesh<THREE.BufferGeometry, THREE.MeshStandardMaterial>>();

  ngAfterViewInit(): void {
    this.initScene();
    // Render loop runs outside Angular so it doesn't trigger change detection every frame
    this.zone.runOutsideAngular(() => this.renderer.setAnimationLoop((t) => this.animate(t)));

    // Every poll tick (and every scrub) fetches the assets for the current view time.
    // switchMap cancels a request that is still running when a newer one starts.
    merge(timer(0, POLL_MS).pipe(tap(() => this.advancePlayback())), this.reload$)
      .pipe(
        switchMap(() =>
          this.assetService.list(this.atParam()).pipe(
            catchError((err) => {
              this.error.set(`Backend unreachable (${err.status ?? 'network error'})`);
              return of(null);
            }),
          ),
        ),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((assets) => {
        if (!assets) return;
        this.error.set(null);
        this.assets.set(assets);
        this.syncMeshes(assets);
      });

    timer(0, POLL_MS)
      .pipe(
        switchMap(() => this.assetService.range().pipe(catchError(() => of(null)))),
        takeUntilDestroyed(this.destroyRef),
      )
      .subscribe((range) => {
        if (range) this.range.set({ earliest: Date.parse(range.earliest), latest: Date.parse(range.latest) });
      });
  }

  /** Slider moved: jump to that moment, or back to live if it was dragged to the end. */
  onScrub(event: Event): void {
    const value = Math.round((event.target as HTMLInputElement).valueAsNumber);
    const latest = this.range()?.latest;
    if (latest !== undefined && value >= latest) {
      this.goLive();
    } else {
      this.showTime(value);
    }
  }

  onSpeedChange(event: Event): void {
    this.speed.set(Number((event.target as HTMLSelectElement).value));
  }

  togglePlay(): void {
    this.playing.update((p) => !p);
  }

  goLive(): void {
    this.playing.set(false);
    this.showTime(null);
  }

  /** Called on each poll tick: while playing, move the view time forward by speed × tick. */
  private advancePlayback(): void {
    const t = this.viewTime();
    if (!this.playing() || t === null) return;
    const next = t + POLL_MS * this.speed();
    if (next >= (this.range()?.latest ?? Date.now())) {
      this.goLive();
    } else {
      this.viewTime.set(next);
      updateUrl(next);
    }
  }

  private showTime(t: number | null): void {
    this.viewTime.set(t);
    updateUrl(t);
    this.reload$.next();
  }

  private atParam(): string | undefined {
    const t = this.viewTime();
    return t === null ? undefined : new Date(t).toISOString();
  }

  ngOnDestroy(): void {
    this.renderer?.setAnimationLoop(null);
    this.resizeObserver?.disconnect();
    this.controls?.dispose();
    this.meshes.forEach((m) => {
      m.geometry.dispose();
      m.material.dispose();
    });
    this.renderer?.dispose();
  }

  private initScene(): void {
    const host = this.canvasHost.nativeElement;

    this.scene = new THREE.Scene();
    this.scene.background = new THREE.Color(0xaaaaaa);

    this.camera = new THREE.PerspectiveCamera(60, host.clientWidth / host.clientHeight, 0.1, 1000);
    this.camera.position.set(6, 6, 8);

    this.renderer = new THREE.WebGLRenderer({ antialias: true });
    this.renderer.setPixelRatio(window.devicePixelRatio);
    this.renderer.setSize(host.clientWidth, host.clientHeight);
    this.renderer.shadowMap.enabled = true;
    host.appendChild(this.renderer.domElement);

    this.controls = new OrbitControls(this.camera, this.renderer.domElement);
    this.controls.enableDamping = true;
    this.controls.target.set(0, 0.5, 1);

    const hemi = new THREE.HemisphereLight(0xffffff, 0x444444, 0.6);
    hemi.position.set(0, 20, 0);
    this.scene.add(hemi);

    const dirLight = new THREE.DirectionalLight(0xffffff, 0.8);
    dirLight.position.set(5, 10, 7.5);
    dirLight.castShadow = true;
    Object.assign(dirLight.shadow.camera, { top: 10, bottom: -10, left: -10, right: 10 });
    this.scene.add(dirLight);

    const floor = new THREE.Mesh(
      new THREE.PlaneGeometry(50, 50),
      new THREE.MeshStandardMaterial({ color: 0xffffff }),
    );
    floor.rotation.x = -Math.PI / 2;
    floor.receiveShadow = true;
    this.scene.add(floor);
    this.scene.add(new THREE.GridHelper(50, 50, 0x888888, 0xcccccc));

    this.resizeObserver = new ResizeObserver(() => {
      const { clientWidth: w, clientHeight: h } = host;
      this.camera.aspect = w / h;
      this.camera.updateProjectionMatrix();
      this.renderer.setSize(w, h);
    });
    this.resizeObserver.observe(host);
  }

  /** Create/update/remove meshes so the scene mirrors the latest backend state. */
  private syncMeshes(assets: Asset[]): void {
    const seen = new Set<number>();
    for (const asset of assets) {
      seen.add(asset.id);
      let mesh = this.meshes.get(asset.id);
      if (!mesh) {
        mesh = new THREE.Mesh(this.geometryFor(asset), new THREE.MeshStandardMaterial());
        mesh.castShadow = true;
        mesh.name = asset.name;
        this.scene.add(mesh);
        this.meshes.set(asset.id, mesh);
      }
      mesh.position.set(asset.posX, asset.posY, asset.posZ);
      mesh.material.color.set(asset.color);
      mesh.material.emissive.setHex(STATUS_EMISSIVE[asset.status]);
      mesh.userData['status'] = asset.status;
    }
    for (const [id, mesh] of this.meshes) {
      if (!seen.has(id)) {
        this.scene.remove(mesh);
        mesh.geometry.dispose();
        mesh.material.dispose();
        this.meshes.delete(id);
      }
    }
  }

  private geometryFor(asset: Asset): THREE.BufferGeometry {
    switch (asset.shape) {
      case 'SPHERE':
        return new THREE.SphereGeometry(0.5, 32, 32);
      case 'CYLINDER':
        return new THREE.CylinderGeometry(0.4, 0.4, 1, 32);
      case 'BOX':
      default:
        return new THREE.BoxGeometry(1, 1, 1);
    }
  }

  private animate(time: number): void {
    for (const mesh of this.meshes.values()) {
      // Spin everything slowly; pulse alarms so they stand out
      mesh.rotation.y += 0.01;
      if (mesh.userData['status'] === 'ALARM') {
        mesh.material.emissiveIntensity = 0.5 + 0.5 * Math.sin(time / 150);
      } else {
        mesh.material.emissiveIntensity = 0.6;
      }
    }
    this.controls.update();
    this.renderer.render(this.scene, this.camera);
  }
}

/** Reads `?t=<ISO time>` so a link can open the room at a given moment. */
function timeFromUrl(): number | null {
  const t = new URLSearchParams(window.location.search).get('t');
  const ms = t ? Date.parse(t) : NaN;
  return Number.isNaN(ms) ? null : ms;
}

/** Keeps `?t=` in the address bar in sync, so the current view can be shared as a link. */
function updateUrl(t: number | null): void {
  const url = new URL(window.location.href);
  if (t === null) {
    url.searchParams.delete('t');
  } else {
    url.searchParams.set('t', new Date(t).toISOString());
  }
  window.history.replaceState(null, '', url);
}
