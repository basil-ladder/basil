DROP INDEX temp_game_hash;
DROP INDEX unit_events_game_multi_index;
CREATE INDEX unit_events_multi_index on unit_event (event, unit_type);
CREATE INDEX unit_events_game_id on unit_event (game_id);