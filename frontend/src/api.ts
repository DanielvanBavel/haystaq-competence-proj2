export interface ApiFailure {
  status: number;
  requestId: string | null;
  body: unknown;
}

export class ApiError extends Error {
  failure: ApiFailure;

  constructor(failure: ApiFailure) {
    super(`HTTP ${failure.status}`);
    this.failure = failure;
  }

  /** Alles wat de UI weet, staat hier. De oorzaak staat in de logs. */
  describe(): string {
    const body = this.failure.body as Record<string, unknown> | null;
    const parts = [`HTTP ${this.failure.status}`];
    if (body && typeof body === 'object') {
      if (body.code) parts.push(String(body.code));
      if (body.upstream) parts.push(`upstream ${body.upstream} (${body.upstreamStatus})`);
      if (body.message) parts.push(String(body.message));
    }
    if (this.failure.requestId) parts.push(`requestId ${this.failure.requestId}`);
    return parts.join(' | ');
  }
}

async function call<T>(method: string, path: string, body?: unknown): Promise<T> {
  const response = await fetch(`/api${path}`, {
    method,
    headers: { 'content-type': 'application/json' },
    body: body === undefined ? undefined : JSON.stringify(body)
  });

  const text = await response.text();
  let payload: unknown = null;
  try {
    payload = text ? JSON.parse(text) : null;
  } catch {
    payload = text;
  }

  if (!response.ok) {
    throw new ApiError({
      status: response.status,
      requestId: response.headers.get('x-request-id'),
      body: payload
    });
  }
  return payload as T;
}

export const api = {
  get: <T>(path: string) => call<T>('GET', path),
  post: <T>(path: string, body?: unknown) => call<T>('POST', path, body ?? {})
};
