alter table bot
add column publish_read boolean;

update bot bot set publish_read = FALSE;

alter table bot
alter column publish_read set not null;