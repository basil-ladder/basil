UPDATE game_result
    set raceA = (select race from bot b where  b.id = bota_id),
        raceB = (select race from bot b where  b.id = botb_id);