import { Component } from '@angular/core';
import { TwinSceneComponent } from './scene/twin-scene.component';

@Component({
  selector: 'app-root',
  imports: [TwinSceneComponent],
  template: `<app-twin-scene />`,
})
export class AppComponent {}
