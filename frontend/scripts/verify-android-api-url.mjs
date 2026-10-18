import { existsSync, readFileSync } from 'node:fs';
import { resolve } from 'node:path';

const envFile = resolve('.env.android');

if (!existsSync(envFile)) {
  console.error('Missing .env.android. Copy .env.android.example and set VITE_API_URL to your public HTTPS API.');
  process.exit(1);
}

const contents = readFileSync(envFile, 'utf8');
const match = contents.match(/^\s*VITE_API_URL\s*=\s*([^\r\n#]+)\s*$/m);
const apiUrl = match?.[1]?.trim();

if (!apiUrl) {
  console.error('VITE_API_URL is required in .env.android.');
  process.exit(1);
}

try {
  const url = new URL(apiUrl);
  const isLocalHost = ['localhost', '127.0.0.1', '::1'].includes(url.hostname);

  if (url.protocol !== 'https:' || isLocalHost || !url.pathname.replace(/\/$/, '').endsWith('/api')) {
    throw new Error();
  }
} catch {
  console.error('VITE_API_URL must be a public HTTPS URL ending in /api, for example https://api.example.com/api.');
  process.exit(1);
}
