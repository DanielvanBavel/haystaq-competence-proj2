import { FormEvent, useEffect, useState } from 'react';
import { Link } from 'react-router-dom';
import { api, ApiError } from '../api';
import { Title } from '../types';

export function Browse() {
  const [titles, setTitles] = useState<Title[]>([]);
  const [query, setQuery] = useState('');
  const [error, setError] = useState<string | null>(null);
  const [busy, setBusy] = useState(false);

  useEffect(() => {
    api.get<Title[]>('/browse')
      .then(setTitles)
      .catch((problem) => setError(problem instanceof ApiError ? problem.describe() : String(problem)));
  }, []);

  async function search(event: FormEvent) {
    event.preventDefault();
    setError(null);
    setBusy(true);
    const started = Date.now();
    try {
      const path = query.trim() ? `/search?query=${encodeURIComponent(query.trim())}` : '/browse';
      setTitles(await api.get<Title[]>(path));
    } catch (problem) {
      setError(`${problem instanceof ApiError ? problem.describe() : String(problem)} (na ${Date.now() - started}ms)`);
      setTitles([]);
    } finally {
      setBusy(false);
    }
  }

  return (
    <section>
      <form className="searchbar" onSubmit={search}>
        <input
          type="search"
          placeholder="Zoek een titel"
          value={query}
          data-testid="search-input"
          onChange={(event) => setQuery(event.target.value)}
        />
        <button type="submit" data-testid="search-submit">Zoeken</button>
      </form>

      {busy ? <p className="muted">Bezig met zoeken...</p> : null}
      {error ? <p className="error" data-testid="search-error">{error}</p> : null}

      <div className="grid">
        {titles.map((title) => (
          <Link className="card" to={`/titel/${title.slug}`} key={title.id}>
            <span className="badge">{title.genre}</span>
            <h3>{title.name}</h3>
            <p className="muted">{title.releaseYear} &middot; {title.maturityRating}+</p>
            <p className="regions">{(title.availableRegions ?? []).join(' ')}</p>
          </Link>
        ))}
      </div>
      {titles.length === 0 && !busy && !error ? <p className="muted">Geen titels gevonden.</p> : null}
    </section>
  );
}
