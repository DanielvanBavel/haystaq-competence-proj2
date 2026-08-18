-- Twee sessies van Chris die nooit netjes zijn afgesloten. De client is
-- gecrasht, de opruimjob draait sinds release 5.0 niet meer.
insert into playback_session (id, viewer_id, episode_id, status, device_type, quality, manifest_url,
                              position_seconds, started_at, last_heartbeat_at, ended_at)
values ('60000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000003',
        '20000000-0000-0000-0000-000000000201', 'ACTIVE', 'SMART_TV', 'HD',
        'https://cdn.streamforge.example/kanaalkoorts/s01e01.m3u8', 1420,
        now() - interval '3 days', now() - interval '3 days' + interval '24 minutes', null),
       ('60000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000003',
        '20000000-0000-0000-0000-000000000301', 'ACTIVE', 'TABLET', 'HD',
        'https://cdn.streamforge.example/deltawacht/s01e01.m3u8', 300,
        now() - interval '2 days', now() - interval '2 days' + interval '5 minutes', null);

-- Een normale, afgesloten sessie van Anne.
insert into playback_session (id, viewer_id, episode_id, status, device_type, quality, manifest_url,
                              position_seconds, started_at, last_heartbeat_at, ended_at)
values ('60000000-0000-0000-0000-000000000003', '40000000-0000-0000-0000-000000000001',
        '20000000-0000-0000-0000-000000000101', 'ENDED', 'BROWSER', 'UHD',
        'https://cdn.streamforge.example/noorderlicht/s01e01.m3u8', 2820,
        now() - interval '1 day', now() - interval '1 day' + interval '47 minutes',
        now() - interval '1 day' + interval '47 minutes');
