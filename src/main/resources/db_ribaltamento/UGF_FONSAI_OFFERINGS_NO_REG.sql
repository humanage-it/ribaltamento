drop view UGF_FONSAI_OFFERINGS_NO_REG

GO


create view UGF_FONSAI_OFFERINGS_NO_REG
as
select
	s.id as Id, s.start_date as StartDate, s.class_no as OfferingNo,
	t.title as Title, l.loc_name as Location, s.split as DomainId
from LET_EXT_OFFERING_SESSION s
inner join LET_EXT_OFFERING_TEMPLATE t on t.id = s.offering_temp_id and t.locale_id = s.locale_id
inner join FGT_DOMAIN d on d.id = s.split
inner join TPT_LOCATIONS l on l.id = s.location_id
left outer join TPT_REGISTRATION r on r.class_id = s.id and r.status != 100
where
	-- offerings for FonSAI agents
	d.name = 'Reti' and t.custom13 = 'Ribaltabile' and s.locale_id = 'local000000000000010'
	
group by s.id, s.start_date, s.class_no, t.title, l.loc_name, s.split
having COUNT(r.id) = 0
	and datediff(day, s.start_date, GETDATE()) > 30

GO