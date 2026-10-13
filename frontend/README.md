# Mr. White frontend

## Run locally

From this directory:

```bash
npm install
npm run dev
```

The client uses `http://localhost:8080` and `ws://localhost:8080/ws` by default. Override them with `VITE_API_BASE` and `VITE_WS_URL` in a `.env` file.

```bash
VITE_API_BASE=http://localhost:8080
VITE_WS_URL=ws://localhost:8080/ws
```

Create a production build with `npm run build`.
