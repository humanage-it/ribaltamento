drop view UGF_FONSAI_OFFERINGS_NO_OWNER

GO


create view UGF_FONSAI_OFFERINGS_NO_OWNER
as
select
	s.id as Id, s.start_date as StartDate, s.class_no as OfferingNo,
	t.title as Title, l.loc_name as Location, s.split as DomainId
from LET_EXT_OFFERING_SESSION s
inner join LET_EXT_OFFERING_TEMPLATE t on t.id = s.offering_temp_id and t.locale_id = s.locale_id
inner join FGT_DOMAIN d on d.id = s.split
inner join TPT_LOCATIONS l on l.id = s.location_id
left outer join CMT_GOVERNANCE g on g.parent_id = s.id and g.owner_type = 100 and g.sequence = 1 and g.type = 100
where
	-- offerings for FonSAI agents
	d.name = 'Reti' and t.custom13 = 'Ribaltabile' and s.locale_id = 'local000000000000010'
	and g.owner_id is null

GO