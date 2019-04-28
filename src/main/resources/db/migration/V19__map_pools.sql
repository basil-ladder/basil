ALTER TABLE bot_history
    ADD COLUMN map_pools varchar(100) default '';
ALTER TABLE bot
    ADD COLUMN map_pools varchar(100) default '';
ALTER TABLE game_result
    ADD COLUMN map_pool varchar(30) default 'SSCAIT';