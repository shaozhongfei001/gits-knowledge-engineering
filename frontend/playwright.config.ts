import { defineConfig } from '@playwright/test';

export default defineConfig({
  testDir: './e2e',
  timeout: 30000,
  retries: 1,
  use: {
    baseURL: 'http://localhost:80',
    trace: 'on-first-retry',
  },
  webServer: {
    command: 'cd .. && docker compose -f compose.local.yaml up --build',
    port: 80,
    reuseExistingServer: true,
    timeout: 120000,
  },
});
