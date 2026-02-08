import * as THREE from 'three';
import { OrbitControls } from 'three/examples/jsm/controls/OrbitControls.js';

const scene = new THREE.Scene();
scene.background = new THREE.Color(0xaaaaaa);

const camera = new THREE.PerspectiveCamera(75, window.innerWidth / window.innerHeight, 0.1, 1000);

const renderer = new THREE.WebGLRenderer({ antialias: true });
renderer.setSize(window.innerWidth, window.innerHeight);
renderer.shadowMap.enabled = true;
document.body.appendChild(renderer.domElement);

// Lights
const hemi = new THREE.HemisphereLight(0xffffff, 0x444444, 0.6);
hemi.position.set(0, 20, 0);
scene.add(hemi);

const dirLight = new THREE.DirectionalLight(0xffffff, 0.8);
dirLight.position.set(5, 10, 7.5);
dirLight.castShadow = true;
dirLight.shadow.camera.top = 10;
dirLight.shadow.camera.bottom = -10;
dirLight.shadow.camera.left = -10;
dirLight.shadow.camera.right = 10;
scene.add(dirLight);

// Plane (white horizontal)
const planeGeo = new THREE.PlaneGeometry(50, 50);
const planeMat = new THREE.MeshStandardMaterial({ color: 0xffffff });
const plane = new THREE.Mesh(planeGeo, planeMat);
plane.rotation.x = -Math.PI / 2;
plane.receiveShadow = true;
scene.add(plane);

// Blue rotating cube sitting on plane
const geometry = new THREE.BoxGeometry(1, 1, 1);
const cubeMaterial = new THREE.MeshStandardMaterial({ color: 0x0000ff });
const cube = new THREE.Mesh(geometry, cubeMaterial);
cube.castShadow = true;
cube.position.y = 0.5;
scene.add(cube);

// Still green sphere beside cube
const sphereGeo = new THREE.SphereGeometry(0.5, 32, 32);
const sphereMat = new THREE.MeshStandardMaterial({ color: 0x00ff00 });
const sphere = new THREE.Mesh(sphereGeo, sphereMat);
sphere.castShadow = true;
sphere.position.set(2, 0.5, 0);
scene.add(sphere);

camera.position.set(5, 3, 5);

// Orbit controls: left drag rotate, right drag pan, wheel zoom
const controls = new OrbitControls(camera, renderer.domElement);
controls.enableDamping = true;
controls.dampingFactor = 0.05;
controls.screenSpacePanning = false;
controls.minDistance = 1;
controls.maxDistance = 50;
controls.target.set(0, 0.5, 0);
controls.update();
controls.mouseButtons = {
  LEFT: THREE.MOUSE.ROTATE,
  MIDDLE: THREE.MOUSE.DOLLY,
  RIGHT: THREE.MOUSE.PAN
};

// Raycaster for hover detection
const raycaster = new THREE.Raycaster();
const pointer = new THREE.Vector2();
let isHovering = false;

function onPointerMove(event) {
  const rect = renderer.domElement.getBoundingClientRect();
  pointer.x = ((event.clientX - rect.left) / rect.width) * 2 - 1;
  pointer.y = -((event.clientY - rect.top) / rect.height) * 2 + 1;
}

renderer.domElement.addEventListener('pointermove', onPointerMove);
renderer.domElement.addEventListener('contextmenu', (e) => e.preventDefault());

// Arrow key 4-direction camera movement (XZ plane)
const moveStep = 0.3;
function onKeyDown(e) {
  const forward = new THREE.Vector3();
  camera.getWorldDirection(forward);
  forward.y = 0;
  forward.normalize();
  const right = new THREE.Vector3();
  right.crossVectors(camera.up, forward).normalize();

  if (e.code === 'ArrowUp') {
    camera.position.addScaledVector(forward, moveStep);
    controls.target.addScaledVector(forward, moveStep);
  } else if (e.code === 'ArrowDown') {
    camera.position.addScaledVector(forward, -moveStep);
    controls.target.addScaledVector(forward, -moveStep);
  } else if (e.code === 'ArrowLeft') {
    camera.position.addScaledVector(right, -moveStep);
    controls.target.addScaledVector(right, -moveStep);
  } else if (e.code === 'ArrowRight') {
    camera.position.addScaledVector(right, moveStep);
    controls.target.addScaledVector(right, moveStep);
  }
  controls.update();
}

window.addEventListener('keydown', onKeyDown);

window.addEventListener('resize', () => {
  camera.aspect = window.innerWidth / window.innerHeight;
  camera.updateProjectionMatrix();
  renderer.setSize(window.innerWidth, window.innerHeight);
});

function animate() {
  // rotate cube
  cube.rotation.x += 0.01;
  cube.rotation.y += 0.01;

  // raycast for hover
  raycaster.setFromCamera(pointer, camera);
  const intersects = raycaster.intersectObject(cube, false);
  if (intersects.length > 0) {
    if (!isHovering) {
      cube.material.color.set(0xff0000);
      isHovering = true;
    }
  } else {
    if (isHovering) {
      cube.material.color.set(0x0000ff);
      isHovering = false;
    }
  }

  controls.update();
  renderer.render(scene, camera);
}

renderer.setAnimationLoop(animate);
