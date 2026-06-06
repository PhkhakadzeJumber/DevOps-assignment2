alter table if exists orders
    add column if not exists expires_at timestamp(6);
