# Frontend (Angular + Three.js)

Angular 19 app that draws the assets in a Three.js scene. Source is in `src/app/`. The main component is `scene/twin-scene.component.ts`, which owns the render loop and polls `/api/assets` every 2 seconds.

## Timeline (time machine)

The bar at the bottom of the screen controls which moment the scene shows:

- **Drag the slider** to jump to any time between the oldest stored event and now. The bar gets a blue edge while you're looking at the past.
- **Play / Pause** replays from there at the chosen speed (1×, 10×, 60× or 600× real time). Playback switches back to live when it reaches the present.
- **Live** returns to current data.
- The address bar keeps `?t=<ISO time>` in sync, so you can copy the URL to share that moment. Opening a URL with `?t=` starts there.

How it works, briefly: the component keeps `viewTime` (a signal). When it's `null`, the scene is live. Otherwise each poll calls `/api/assets?at=<viewTime>`. The backend returns the same JSON either way, so the Three.js code doesn't know or care whether it's drawing the past. The slider bounds come from `/api/history/range`, which is polled alongside the assets.

## How requests reach the backend

- **In Docker**, the browser loads the app from nginx on port 4200. `nginx.conf` forwards any `/api/` request to `http://backend:8080`.
- **Under `npm start`**, the Angular dev server on port 4200 does the same job, using `proxy.conf.json` to forward `/api` to `localhost:8080`.

The frontend has no hard-coded backend host, so both setups work without code changes.

## Logs

In Docker, the nginx access and error logs:

```bash
docker compose logs -f frontend
```

Each `/api/` request shows up as an access-log line with its status code. A line with `502 Bad Gateway` means nginx couldn't reach the backend. Check the backend is running.

Under `npm start`, build and proxy errors appear in the terminal where you ran it.

## Browser debugging

Most frontend problems are easiest to see in the browser:

1. Open the dev tools (F12).
2. **Console** tab: JavaScript and Angular errors, such as a failing `bootstrapApplication`.
3. **Network** tab: each request to `/api/assets`, its status, and its response. Filter by `api` to narrow the list.
4. The on-screen panel shows "Backend unreachable (status)" when the poll fails, with the HTTP status or "network error".

If the page is blank with no errors, check that WebGL works. Open `chrome://gpu` (or `about:support` in Firefox) and look for "WebGL: Hardware accelerated". Software-only WebGL usually still works, but it can be slow.

## Rebuilding after changes

```bash
docker compose up -d --build frontend     # production build served by nginx
```

For faster iteration, run the dev server instead. It rebuilds on save and reloads the browser:

```bash
npm install
npm start                                 # http://localhost:4200
```

Stop the Docker frontend first (`docker compose stop frontend`) so it doesn't hold port 4200.

## Building

```bash
npm run build                             # output in dist/frontend/browser
```

The production build warns if the bundle goes over 1 MB and fails over 2 MB (see `angular.json`).
