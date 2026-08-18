-- Vaste UUID's voor de titels en afleveringen waar de tests naar verwijzen.

insert into title (id, slug, name, synopsis, release_year, genre, maturity_rating, popularity)
values ('10000000-0000-0000-0000-000000000001', 'noorderlicht', 'Noorderlicht',
        'Een poolonderzoeker keert terug naar het dorp dat haar ooit liet vallen.', 2024, 'drama', 12, 98),
       ('10000000-0000-0000-0000-000000000002', 'kanaalkoorts', 'Kanaalkoorts',
        'Twee douaniers raken verstrikt in een smokkelroute onder de Noordzee.', 2023, 'thriller', 16, 91),
       ('10000000-0000-0000-0000-000000000003', 'deltawacht', 'Deltawacht',
        'De stormvloedkering begeeft het, en niemand wil verantwoordelijk zijn.', 2025, 'drama', 12, 87),
       ('10000000-0000-0000-0000-000000000004', 'stadslicht', 'Stadslicht',
        'Nachtelijke verhalen uit een stad die nooit helemaal donker wordt.', 2022, 'documentaire', 6, 74),
       ('10000000-0000-0000-0000-000000000005', 'het-zilveren-uur', 'Het Zilveren Uur',
        'Een kok zonder restaurant probeert een oude ster terug te winnen.', 2021, 'komedie', 12, 66),
       ('10000000-0000-0000-0000-000000000006', 'windkracht-elf', 'Windkracht Elf',
        'Reddingswerkers op de Waddenzee, seizoen na seizoen.', 2020, 'actie', 12, 58),
       ('10000000-0000-0000-0000-000000000007', 'de-laatste-trein', 'De Laatste Trein',
        'Een nachtconducteur ontdekt dat zijn trein nergens meer aankomt.', 2024, 'mysterie', 16, 81),
       ('10000000-0000-0000-0000-000000000008', 'nachtploeg', 'Nachtploeg',
        'In een distributiecentrum draait alles door, ook de mensen.', 2025, 'thriller', 18, 70);

-- Beschikbaarheid per regio. Let op wat hier NIET staat.
insert into title_region (title_id, region)
values ('10000000-0000-0000-0000-000000000001', 'NL'),
       ('10000000-0000-0000-0000-000000000002', 'NL'),
       ('10000000-0000-0000-0000-000000000002', 'BE'),
       ('10000000-0000-0000-0000-000000000002', 'DE'),
       ('10000000-0000-0000-0000-000000000003', 'NL'),
       ('10000000-0000-0000-0000-000000000003', 'BE'),
       ('10000000-0000-0000-0000-000000000004', 'NL'),
       ('10000000-0000-0000-0000-000000000004', 'BE'),
       ('10000000-0000-0000-0000-000000000004', 'DE'),
       ('10000000-0000-0000-0000-000000000004', 'FR'),
       ('10000000-0000-0000-0000-000000000005', 'NL'),
       ('10000000-0000-0000-0000-000000000006', 'NL'),
       ('10000000-0000-0000-0000-000000000006', 'BE'),
       ('10000000-0000-0000-0000-000000000007', 'NL'),
       ('10000000-0000-0000-0000-000000000007', 'BE'),
       ('10000000-0000-0000-0000-000000000007', 'DE'),
       ('10000000-0000-0000-0000-000000000008', 'NL');

insert into episode (id, title_id, season_number, episode_number, name, duration_seconds, asset_status, manifest_url)
values ('20000000-0000-0000-0000-000000000101', '10000000-0000-0000-0000-000000000001', 1, 1,
        'Aankomst', 2820, 'READY', 'https://cdn.streamforge.example/noorderlicht/s01e01.m3u8'),
       ('20000000-0000-0000-0000-000000000102', '10000000-0000-0000-0000-000000000001', 1, 2,
        'Zwart ijs', 2760, 'READY', 'https://cdn.streamforge.example/noorderlicht/s01e02.m3u8'),
       ('20000000-0000-0000-0000-000000000103', '10000000-0000-0000-0000-000000000001', 1, 3,
        'De vondst', 2940, 'PENDING', null),
       ('20000000-0000-0000-0000-000000000201', '10000000-0000-0000-0000-000000000002', 1, 1,
        'Hoogwater', 3000, 'READY', 'https://cdn.streamforge.example/kanaalkoorts/s01e01.m3u8'),
       ('20000000-0000-0000-0000-000000000202', '10000000-0000-0000-0000-000000000002', 1, 2,
        'Containerdans', 2880, 'READY', 'https://cdn.streamforge.example/kanaalkoorts/s01e02.m3u8'),
       ('20000000-0000-0000-0000-000000000301', '10000000-0000-0000-0000-000000000003', 1, 1,
        'Alarmfase een', 3120, 'READY', 'https://cdn.streamforge.example/deltawacht/s01e01.m3u8'),
       ('20000000-0000-0000-0000-000000000401', '10000000-0000-0000-0000-000000000004', 1, 1,
        'Middernacht', 2400, 'READY', 'https://cdn.streamforge.example/stadslicht/s01e01.m3u8'),
       ('20000000-0000-0000-0000-000000000501', '10000000-0000-0000-0000-000000000005', 1, 1,
        'Mise en place', 2520, 'READY', 'https://cdn.streamforge.example/zilveren-uur/s01e01.m3u8'),
       ('20000000-0000-0000-0000-000000000601', '10000000-0000-0000-0000-000000000006', 1, 1,
        'Vlak voor de storm', 2700, 'READY', 'https://cdn.streamforge.example/windkracht/s01e01.m3u8'),
       ('20000000-0000-0000-0000-000000000701', '10000000-0000-0000-0000-000000000007', 1, 1,
        'Spoor 4', 2640, 'READY', 'https://cdn.streamforge.example/laatste-trein/s01e01.m3u8'),
       ('20000000-0000-0000-0000-000000000801', '10000000-0000-0000-0000-000000000008', 1, 1,
        'Ploegwissel', 2580, 'READY', 'https://cdn.streamforge.example/nachtploeg/s01e01.m3u8');

-- Transcodeerjobs. Eén ervan is stukgelopen en nooit opnieuw gestart.
insert into ingest_job (id, episode_id, status, started_at, finished_at, worker, error_message)
values ('30000000-0000-0000-0000-000000000101', '20000000-0000-0000-0000-000000000101', 'COMPLETED',
        now() - interval '9 days', now() - interval '9 days' + interval '22 minutes', 'transcoder-02', null),
       ('30000000-0000-0000-0000-000000000102', '20000000-0000-0000-0000-000000000102', 'COMPLETED',
        now() - interval '9 days', now() - interval '9 days' + interval '25 minutes', 'transcoder-02', null),
       ('30000000-0000-0000-0000-000000000103', '20000000-0000-0000-0000-000000000103', 'FAILED',
        now() - interval '6 days', now() - interval '6 days' + interval '3 minutes', 'transcoder-04',
        'java.io.UncheckedIOException: unable to read source asset s3://ingest/noorderlicht/s01e03.mxf' || chr(10) ||
        E'\tat nl.haystaq.streamforge.ingest.SourceReader.open(SourceReader.java:64)' || chr(10) ||
        E'\tat nl.haystaq.streamforge.ingest.TranscodeJob.run(TranscodeJob.java:118)' || chr(10) ||
        E'Caused by: java.nio.file.NoSuchFileException: /mnt/ingest/noorderlicht/s01e03.mxf' || chr(10) ||
        E'\tat sun.nio.fs.UnixException.translateToIOException(UnixException.java:92)'),
       ('30000000-0000-0000-0000-000000000201', '20000000-0000-0000-0000-000000000201', 'COMPLETED',
        now() - interval '14 days', now() - interval '14 days' + interval '19 minutes', 'transcoder-01', null);

-- Vulling: 8000 extra titels, zodat de catalogus realistisch groot is.
insert into title (id, slug, name, synopsis, release_year, genre, maturity_rating, popularity)
select gen_random_uuid(),
       'serie-' || n,
       (array['Havenlicht', 'Zandvliet', 'Polderkoorts', 'Nachtdienst', 'Kustwacht', 'Binnenvaart',
              'Wisselspoor', 'Duinrand', 'Grensgeval', 'Laagwater'])[1 + (n % 10)] || ' ' || n,
       'Automatisch gegenereerde vulling voor de catalogus.',
       2000 + (n % 26),
       (array['drama', 'thriller', 'komedie', 'documentaire', 'actie', 'mysterie'])[1 + (n % 6)],
       (array[6, 12, 16, 18])[1 + (n % 4)],
       n % 100
from generate_series(1, 8000) as n;

insert into title_region (title_id, region)
select id, 'NL' from title where slug like 'serie-%';
