import { useCallback, useEffect, useState } from 'react';
import { useParams } from 'react-router-dom';
import { api, ApiError } from '../api';
import { Episode, Session, Title } from '../types';
import { useViewer } from '../ViewerContext';

export function TitleDetail() {
  const { slug } = useParams();
  const { current } = useViewer();
  const [title, setTitle] = useState<Title | null>(null);
  const [session, setSession] = useState<Session | null>(null);
  const [message, setMessage] = useState<string | null>(null);

  const load = useCallback(async () => {
    if (!slug) {
      return;
    }
    try {
      setTitle(await api.get<Title>(`/titles/${slug}`));
    } catch (problem) {
      setMessage(problem instanceof ApiError ? problem.describe() : String(problem));
    }
  }, [slug]);

  useEffect(() => {
    void load();
  }, [load]);

  async function play(episode: Episode) {
    if (!current) {
      setMessage('Kies eerst een profiel.');
      return;
    }
    setMessage(null);
    try {
      const started = await api.post<Session>('/play', {
        viewerId: current.id,
        episodeId: episode.id,
        deviceType: 'BROWSER'
      });
      setSession(started);
      setMessage(`Sessie gestart in ${started.quality}.`);
    } catch (problem) {
      setSession(null);
      setMessage(problem instanceof ApiError ? problem.describe() : String(problem));
    }
  }

  async function heartbeat() {
    if (!session) {
      return;
    }
    try {
      const updated = await api.post<Session>(`/sessions/${session.id}/heartbeat`, {
        positionSeconds: session.positionSeconds + 30
      });
      setSession(updated);
      setMessage(`Positie ${updated.positionSeconds}s.`);
    } catch (problem) {
      setMessage(problem instanceof ApiError ? problem.describe() : String(problem));
    }
  }

  async function stop() {
    if (!session) {
      return;
    }
    try {
      await api.post(`/sessions/${session.id}/stop`);
      setSession(null);
      setMessage('Sessie gestopt.');
    } catch (problem) {
      setMessage(problem instanceof ApiError ? problem.describe() : String(problem));
    }
  }

  if (!title) {
    return <section><p className="muted">Laden...</p>{message ? <p className="error">{message}</p> : null}</section>;
  }

  return (
    <section>
      <h2>{title.name}</h2>
      <p className="muted">
        {title.releaseYear} &middot; {title.genre} &middot; {title.maturityRating}+ &middot;{' '}
        beschikbaar in {(title.availableRegions ?? []).join(', ')}
      </p>
      <p>{title.synopsis}</p>

      <table data-testid="episode-table">
        <thead>
        <tr>
          <th>Seizoen</th>
          <th>Aflevering</th>
          <th>Titel</th>
          <th>Duur</th>
          <th>Asset</th>
          <th/>
        </tr>
        </thead>
        <tbody>
        {title.episodes.map((episode) => (
          <tr key={episode.id}>
            <td>{episode.seasonNumber}</td>
            <td>{episode.episodeNumber}</td>
            <td>{episode.name}</td>
            <td>{Math.round(episode.durationSeconds / 60)} min</td>
            <td>{episode.assetStatus}</td>
            <td>
              <button type="button" data-testid={`play-${episode.seasonNumber}-${episode.episodeNumber}`}
                      onClick={() => void play(episode)}>
                Afspelen
              </button>
            </td>
          </tr>
        ))}
        </tbody>
      </table>

      {message ? <p className="notice" data-testid="playback-message">{message}</p> : null}

      {session ? (
        <div className="player" data-testid="player">
          <h3>Speelt af</h3>
          <p className="muted">{session.manifestUrl}</p>
          <p>Kwaliteit {session.quality} &middot; positie {session.positionSeconds}s &middot; sessie {session.id}</p>
          <div className="actions">
            <button type="button" onClick={() => void heartbeat()}>Heartbeat</button>
            <button type="button" onClick={() => void stop()}>Stoppen</button>
          </div>
        </div>
      ) : null}
    </section>
  );
}
