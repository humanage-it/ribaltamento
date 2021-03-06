-- create new periodic notification event
insert into FGT_ID_NOTIFY_EVENT (id, time_stamp)
values('nevnt000000090000002', 'unipol')

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
	'nevnt000000090000002',
	'local000000000000001',
	'0',
	0,
	'1010000001',
	'Formazione Aula UnipolSai - Trash edizioni senza registrazioni',
	9000002,
	'cpobj000000000001039',	-- ILT Offering
	'Formazione Aula UnipolSai - Trash edizioni senza registrazioni'
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
	'nevnt000000090000002',
	l.id,
	'1',
	0,
	'1010000001',
	'Formazione Aula UnipolSai - Trash edizioni senza registrazioni',
	9000002,
	'cpobj000000000001039',	-- ILT Offering
	'Formazione Aula UnipolSai - Trash edizioni senza registrazioni'
from FGT_LOCALE l
where l.id != 'local000000000000001'


-- assign the event to a notification category
insert into FGT_NOTIFY_EVENT_CATEGORY (
	id,
	time_stamp,
	event_id,
	category)
values (
	'nevtc000000090000002',
	'unipol',
	'nevnt000000090000002',
	'Instructor-Led'
)

select Id, DomainId from UGF_FONSAI_OFFERINGS_NO_REG

insert into FGT_MESG_TABLE(mesg_id, mesg_seq, mesg_text, mesg_type)
values (
	90000002,
	1,
	'select Id, DomainId from UGF_FONSAI_OFFERINGS_NO_REG',
	1
)


-- create the named query
insert into FGT_NOTIFY_FUNC(id, time_stamp, type, name, sql_id, description)
values (
	'nffnc000000090000002',
	'unipol',
	1,
	'Formazione Aula UnipolSai - Trash edizioni senza registrazioni',
	90000002,
	'Formazione Aula UnipolSai - Trash edizioni senza registrazioni'
)

insert into FGT_PERIODIC_FUNCTION(id, time_stamp, statement_id, event_id, flags, frequency, name, start_time, trigger_time, start_date, weekly_mask, STORE_ID)
values('prdfn000000090000002', 'unipol', 'nffnc000000090000002', 'nevnt000000090000002',
	'1111100000', 1, 'Formazione Aula UnipolSai - Trash edizioni senza registrazioni', '1900-01-01 01:00:00.000', '2013-03-21 01:00:00.000', '2013-03-21 01:00:00.000', '1000000',
	'store000000000001000')
	
