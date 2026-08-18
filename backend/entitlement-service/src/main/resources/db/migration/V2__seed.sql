-- Vijf kijkers met bewust verschillende profielen.
insert into viewer (id, email, display_name, region, maturity_limit)
values ('40000000-0000-0000-0000-000000000001', 'anne@example.com', 'Anne', 'NL', 18),
       ('40000000-0000-0000-0000-000000000002', 'bea@example.com', 'Bea', 'BE', 18),
       ('40000000-0000-0000-0000-000000000003', 'chris@example.com', 'Chris', 'NL', 18),
       ('40000000-0000-0000-0000-000000000004', 'dirk@example.com', 'Dirk', 'NL', 12),
       ('40000000-0000-0000-0000-000000000005', 'eva@example.com', 'Eva', 'NL', 16);

insert into subscription (id, viewer_id, plan, status, started_on, renews_on)
values ('50000000-0000-0000-0000-000000000001', '40000000-0000-0000-0000-000000000001',
        'PREMIUM', 'ACTIVE', date '2023-02-01', current_date + 12),
       ('50000000-0000-0000-0000-000000000002', '40000000-0000-0000-0000-000000000002',
        'STANDARD', 'ACTIVE', date '2024-06-15', current_date + 3),
       ('50000000-0000-0000-0000-000000000003', '40000000-0000-0000-0000-000000000003',
        'STANDARD', 'ACTIVE', date '2022-11-30', current_date + 21),
       ('50000000-0000-0000-0000-000000000004', '40000000-0000-0000-0000-000000000004',
        'BASIC', 'ACTIVE', date '2025-01-09', current_date + 8),
       ('50000000-0000-0000-0000-000000000005', '40000000-0000-0000-0000-000000000005',
        'PREMIUM', 'PAUSED', date '2021-08-20', null);
