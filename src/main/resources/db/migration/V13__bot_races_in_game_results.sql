alter table game_result
    ADD COLUMN raceA varchar(30),
    ADD COLUMN raceB varchar(30);

update game_result gr set
    raceA = (select race from bot where id = gr.bota_id),
    raceB = (select race from bot where id = gr.botb_id);


ALTER TABLE game_result
    ALTER COLUMN raceA SET NOT NULL,
    ALTER COLUMN raceB SET NOT NULL;

ALTER TABLE bot
    ALTER COLUMN race SET NOT NULL,
    ALTER COLUMN bot_type SET NOT NULL;