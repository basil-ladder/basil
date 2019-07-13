update game_result set frame_timeout = false where frame_timeout is null;

ALTER TABLE game_result
    ALTER COLUMN time SET NOT NULL,
    ALTER COLUMN bota_id SET NOT NULL,
    ALTER COLUMN botb_id SET NOT NULL,
    ALTER COLUMN game_hash SET NOT NULL,
    ALTER COLUMN map_pool SET NOT NULL,
    ALTER COLUMN frame_timeout SET NOT NULL,
    ALTER COLUMN map SET NOT NULL;

CREATE INDEX ix_game_time on game_result (time);
