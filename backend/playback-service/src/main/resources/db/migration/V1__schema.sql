create table playback_session (
    id                uuid primary key,
    viewer_id         uuid        not null,
    episode_id        uuid        not null,
    status            varchar(10) not null check (status in ('ACTIVE', 'ENDED')),
    -- Oudere clients sturen geen device_type mee.
    device_type       varchar(20),
    quality           varchar(10) not null,
    manifest_url      varchar(300) not null,
    position_seconds  integer     not null default 0,
    started_at        timestamptz not null,
    last_heartbeat_at timestamptz not null,
    ended_at          timestamptz
);

create index playback_session_viewer_idx on playback_session (viewer_id, status);
