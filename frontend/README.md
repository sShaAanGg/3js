# Frontend (Angular + Three.js)

Angular 19 app that draws the assets in a Three.js scene. Source is in `src/app/`. The main component is `scene/twin-scene.component.ts`, which owns the render loop and polls `/api/assets` every 2 seconds.

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
