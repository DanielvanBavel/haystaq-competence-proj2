export interface Viewer {
  id: string;
  email: string;
  displayName: string;
  region: string;
  maturityLimit: number;
  plan: string | null;
  subscriptionStatus: string | null;
  maxConcurrentStreams: number;
  maxQuality: string | null;
}

export interface Episode {
  id: string;
  titleId: string;
  titleName: string;
  seasonNumber: number;
  episodeNumber: number;
  name: string;
  durationSeconds: number;
  assetStatus: string;
  manifestUrl: string | null;
  playable: boolean;
  availableRegions: string[];
  maturityRating: number;
}

export interface Title {
  id: string;
  slug: string;
  name: string;
  synopsis: string | null;
  releaseYear: number;
  genre: string;
  maturityRating: number;
  popularity: number;
  availableRegions: string[];
  similarTitles: number | null;
  episodes: Episode[];
}

export interface Session {
  id: string;
  viewerId: string;
  episodeId: string;
  status: string;
  deviceType: string | null;
  quality: string;
  manifestUrl: string;
  positionSeconds: number;
  startedAt: string;
  lastHeartbeatAt: string;
  endedAt: string | null;
}

export interface IngestJob {
  id: string;
  episodeId: string;
  status: string;
  startedAt: string;
  finishedAt: string | null;
  worker: string;
  errorMessage: string | null;
}
