create table title (
    id              uuid primary key,
    slug            varchar(120) not null unique,
    name            varchar(200) not null,
    synopsis        varchar(1000),
    release_year    integer      not null,
    genre           varchar(40)  not null,
    maturity_rating integer      not null,
    popularity      integer      not null default 0
);

create table title_region (
    title_id uuid       not null references title (id) on delete cascade,
    region   varchar(2) not null,
    primary key (title_id, region)
);

create table episode (
    id               uuid primary key,
    title_id         uuid        not null references title (id) on delete cascade,
    season_number    integer     not null,
    episode_number   integer     not null,
    name             varchar(200) not null,
    duration_seconds integer     not null,
    asset_status     varchar(10) not null check (asset_status in ('READY', 'PENDING', 'FAILED')),
    manifest_url     varchar(300),
    unique (title_id, season_number, episode_number)
);

create table ingest_job (
    id            uuid primary key,
    episode_id    uuid        not null,
    status        varchar(20) not null,
    started_at    timestamptz not null,
    finished_at   timestamptz,
    worker        varchar(60) not null,
    error_message varchar(4000)
);

-- Bewust geen index op genre: de zoekverrijking valt daardoor op in de logs.
create index episode_title_idx on episode (title_id);
create index ingest_job_episode_idx on ingest_job (episode_id);
