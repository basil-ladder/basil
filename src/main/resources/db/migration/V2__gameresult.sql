alter table bot
add column won int4,
add column lost int4;

update bot bot set won = (select count(*) from game_result result where bot.id = result.winner_id);
update bot bot set lost = (select count(*) from game_result result where bot.id = result.loser_id);

alter table bot
alter column won set not null,
alter column lost set not null;