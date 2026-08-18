create table viewer (
    id             uuid primary key,
    email          varchar(160) not null unique,
    display_name   varchar(80)  not null,
    region         varchar(2)   not null,
    maturity_limit integer      not null check (maturity_limit in (6, 12, 16, 18))
);

create table subscription (
    id         uuid primary key,
    viewer_id  uuid        not null unique references viewer (id) on delete cascade,
    plan       varchar(10) not null check (plan in ('BASIC', 'STANDARD', 'PREMIUM')),
    status     varchar(10) not null check (status in ('ACTIVE', 'PAUSED', 'CANCELLED')),
    started_on date        not null,
    renews_on  date
);
