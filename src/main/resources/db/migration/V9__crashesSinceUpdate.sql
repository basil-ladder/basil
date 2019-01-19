alter table bot add column crashes_since_update int4;

update bot b set crashes_since_update = (select count(*) from game_result r where r.botacrashed = TRUE and r.bota_id = b.id or r.botbcrashed = TRUE and r.botb_id = b.id and r.time >= b.last_updated);

ALTER TABLE bot ALTER COLUMN crashes_since_update SET NOT NULL;