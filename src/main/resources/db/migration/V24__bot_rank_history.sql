create table bot_rank (id int8 not null, time timestamp, bot_id int8, rank varchar(10), primary key (id));


alter table bot
add column previous_rank varchar(10);

update bot set previous_rank = rank;

alter table bot
alter column previous_rank set not null;
