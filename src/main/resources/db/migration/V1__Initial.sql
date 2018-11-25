create sequence hibernate_sequence start 1 increment 1;
create table bot (id int8 not null, bot_type varchar(40), crashed int4 not null, enabled boolean not null, last_updated timestamp, name varchar(255), played int4 not null, race varchar(30), rating int4 not null, primary key (id));
create table bot_elo (id int8 not null, rating int4 not null, time timestamp, bot_id int8, game_hash varchar(30), primary key (id));
create table game_result (id int8 not null, botacrashed boolean not null, botbcrashed boolean not null, game_realtime float8 not null, map varchar(255), realtime_timeout boolean not null, time timestamp, bota_id int8, botb_id int8, loser_id int8, winner_id int8, game_hash varchar(30), primary key (id));
