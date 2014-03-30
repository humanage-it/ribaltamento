-- create new periodic notification event
insert into FGT_ID_NOTIFY_EVENT (id, time_stamp)
values('nevnt000000090000001', 'unipol')

insert into FGT_EXT_NOTIFY_EVENT (
	id,
	locale_id,
	inherited,
	priority,
	flags,
	name,
	event_no,
	object_id,
	description)
values (
	'nevnt000000090000001',
	'local000000000000001',
	'0',
	0,
	'1010000001',
	'Formazione Aula UnipolSai - Trash Edizioni senza Owner',
	9000001,
	'cpobj000000000001039',	-- ILT Offering
	'Formazione Aula UnipolSai - Trash Edizioni senza Owner'
)


-- create the event in the other available locales
insert into FGT_EXT_NOTIFY_EVENT (
	id,
	locale_id,
	inherited,
	priority,
	flags,
	name,
	event_no,
	object_id,
	description)
select
	'nevnt000000090000001',
	l.id,
	'1',
	0,
	'1010000001',
	'Formazione Aula UnipolSai - Trash Edizioni senza Owner',
	9000001,
	'cpobj000000000001039',	-- ILT Offering
	'Formazione Aula UnipolSai - Trash Edizioni senza Owner'
from FGT_LOCALE l
where l.id != 'local000000000000001'


-- assign the event to a notification category
insert into FGT_NOTIFY_EVENT_CATEGORY (
	id,
	time_stamp,
	event_id,
	category)
values (
	'nevtc000000090000001',
	'unipol',
	'nevnt000000090000001',
	'Instructor-Led'
)

select Id, DomainId from UGF_FONSAI_OFFERINGS_NO_OWNER

insert into FGT_MESG_TABLE(mesg_id, mesg_seq, mesg_text, mesg_type)
values (
	90000001,
	1,
	'select Id, DomainId from UGF_FONSAI_OFFERINGS_NO_OWNER',
	1
)


-- create the named query
insert into FGT_NOTIFY_FUNC(id, time_stamp, type, name, sql_id, description)
values (
	'nffnc000000090000001',
	'unipol',
	1,
	'Formazione Aula UnipolSai - Trash Edizioni senza Owner',
	90000001,
	'Formazione Aula UnipolSai - Trash Edizioni senza Owner'
)

insert into FGT_PERIODIC_FUNCTION(id, time_stamp, statement_id, event_id, flags, frequency, name, start_time, trigger_time, start_date, weekly_mask, STORE_ID)
values('prdfn000000090000001', 'unipol', 'nffnc000000090000001', 'nevnt000000090000001',
	'1111100000', 1, 'Formazione Aula UnipolSai - Trash Edizioni senza Owner', '1900-01-01 01:00:00.000', '2013-03-21 01:00:00.000', '2013-03-21 01:00:00.000', '1000000',
	'store000000000001000')
	
