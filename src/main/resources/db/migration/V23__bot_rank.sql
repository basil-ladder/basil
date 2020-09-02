alter table bot
add column rank varchar(10),
add column rank_since int4;

update bot set rank = 'UNRANKED', rank_since = 0;

alter table bot
alter column rank set not null,
alter column rank_since set not null;
