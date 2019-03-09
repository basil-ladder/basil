CREATE extension if not exists "uuid-ossp";

alter table game_result
    DROP COLUMN ID,
    ADD COLUMN ID UUID PRIMARY KEY DEFAULT uuid_generate_v4();

alter table bot_elo
    ADD COLUMN game_id UUID;

create index temp_game_hash on game_result (game_hash);

update bot_elo be set game_id = (select id from game_result where game_hash = be.game_hash FETCH FIRST 1 ROW ONLY);

alter table bot_elo
    DROP COLUMN game_hash,
    ALTER COLUMN game_id SET NOT NULL;

