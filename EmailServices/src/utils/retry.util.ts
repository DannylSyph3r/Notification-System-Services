// src/utils/retry.util.ts

/**
 * Calculates an exponential backoff delay based on retry count.
 * Example: 0 → 2s, 1 → 4s, 2 → 8s, up to 32s max.
 */
export function calculateBackoff(retryCount: number): number {
  const baseDelay = 2000; // 2 seconds
  const maxDelay = 32000; // 32 seconds
  const delay = baseDelay * 2 ** retryCount;
  return Math.min(delay, maxDelay);
}
