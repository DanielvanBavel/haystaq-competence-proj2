'use strict';

const GATEWAY = process.env.GATEWAY_URL || 'http://localhost:8090';

/**
 * Doet een aanroep tegen de gateway en bewaart alles wat je later nodig hebt om
 * een storing te analyseren: correlatie-id, status, duur en de body.
 */
async function call(method, path, body) {
  const started = Date.now();
  const url = `${GATEWAY}${path}`;
  let response;
  let payload = null;
  let networkError = null;

  try {
    response = await fetch(url, {
      method,
      headers: { 'content-type': 'application/json' },
      body: body === undefined ? undefined : JSON.stringify(body)
    });
    const text = await response.text();
    try {
      payload = text ? JSON.parse(text) : null;
    } catch {
      payload = text;
    }
  } catch (error) {
    networkError = String(error);
  }

  return {
    method,
    url,
    requestBody: body ?? null,
    status: response ? response.status : 0,
    requestId: response ? response.headers.get('x-request-id') : null,
    durationMs: Date.now() - started,
    body: payload,
    networkError
  };
}

module.exports = {
  GATEWAY,
  get: (path) => call('GET', path),
  post: (path, body) => call('POST', path, body ?? {})
};
