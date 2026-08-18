import { useCallback, useEffect, useState } from 'react';
import { api, ApiError } from '../api';
import { IngestJob, Session } from '../types';

export function Diagnostics() {
  const [status, setStatus] = useState<Record<string, unknown> | null>(null);
  const [sessions, setSessions] = useState<Session[]>([]);
  const [jobs, setJobs] = useState<IngestJob[]>([]);
  const [error, setError] = useState<string | null>(null);

  const load = useCallback(async () => {
    setError(null);
    try {
      const [statusResult, sessionResult, jobResult] = await Promise.all([
        api.get<Record<string, unknown>>('/status'),
        api.get<Session[]>('/sessions'),
        api.get<IngestJob[]>('/ingest-jobs')
      ]);
      setStatus(statusResult);
      setSessions(sessionResult);
      setJobs(jobResult);
    } catch (problem) {
      setError(problem instanceof ApiError ? problem.describe() : String(problem));
    }
  }, []);

  useEffect(() => {
    void load();
  }, [load]);

  return (
    <section>
      <h2>Diagnose</h2>
      <p className="muted">
        Wat de app zelf ziet. De volledige waarheid staat in de logs onder <code>artifacts/logs</code>.
      </p>
      <button type="button" onClick={() => void load()}>Verversen</button>
      {error ? <p className="error">{error}</p> : null}

      <h3>Services</h3>
      <pre>{JSON.stringify(status, null, 2)}</pre>

      <h3>Actieve sessies ({sessions.length})</h3>
      <table>
        <thead>
        <tr>
          <th>Sessie</th>
          <th>Kijker</th>
          <th>Aflevering</th>
          <th>Apparaat</th>
          <th>Gestart</th>
          <th>Laatste heartbeat</th>
        </tr>
        </thead>
        <tbody>
        {sessions.map((session) => (
          <tr key={session.id}>
            <td>{session.id.slice(0, 8)}</td>
            <td>{session.viewerId.slice(0, 8)}</td>
            <td>{session.episodeId.slice(0, 8)}</td>
            <td>{session.deviceType ?? '-'}</td>
            <td>{session.startedAt?.slice(0, 19)}</td>
            <td>{session.lastHeartbeatAt?.slice(0, 19)}</td>
          </tr>
        ))}
        </tbody>
      </table>

      <h3>Transcodeerjobs</h3>
      <table>
        <thead>
        <tr>
          <th>Job</th>
          <th>Aflevering</th>
          <th>Status</th>
          <th>Worker</th>
          <th>Fout</th>
        </tr>
        </thead>
        <tbody>
        {jobs.map((job) => (
          <tr key={job.id}>
            <td>{job.id.slice(0, 8)}</td>
            <td>{job.episodeId.slice(0, 8)}</td>
            <td>{job.status}</td>
            <td>{job.worker}</td>
            <td className="stack">{job.errorMessage ? job.errorMessage.split('\n')[0] : ''}</td>
          </tr>
        ))}
        </tbody>
      </table>
    </section>
  );
}
