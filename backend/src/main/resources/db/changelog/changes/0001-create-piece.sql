--liquibase formatted sql

--changeset practice-planning:0001-create-piece
create table piece
(
    id         uuid primary key,
    title      varchar(200) not null check (length(trim(title)) > 0),
    composer   varchar(200),
    created_at timestamptz  not null
);

create index piece_created_at_idx on piece (created_at desc);
--rollback drop table piece;
