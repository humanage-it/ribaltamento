drop view UGF_FONSAI_OFFERINGS

GO


create view UGF_FONSAI_OFFERINGS
as
select
	s.id as Id, s.locale_id as LocaleId, s.class_no as OfferingNumber,
	t.id as CourseId, t.title as Title,
	l.id as LocationId,
	l.loc_name as Location,
	s.start_date as StartDate,
	s.end_date as EndDate,
	tp2.UGF_GET_OFFERING_START_TIME(s.id) as StartTime,
	tp2.UGF_GET_OFFERING_END_TIME(s.id) as EndTime,
	s.custom0 as ExpiryDate,
	IsNull(s.custom2, 'false') as IsValidIvass,
	case s.custom6 when 'SI' then 'true' else IsNull(s.custom6, 'false') end as IsArchived,
	g.owner_id as OwnerId
from LET_EXT_OFFERING_SESSION s
inner join LET_EXT_OFFERING_TEMPLATE t on t.id = s.offering_temp_id and t.locale_id = s.locale_id
inner join TPT_LOCATIONS l on l.id = s.location_id
inner join FGT_DOMAIN d on d.id = s.split
inner join CMT_GOVERNANCE g on g.parent_id = s.id and g.owner_type = 100 and g.sequence = 1 and g.type = 100
where
	-- offerings for FonSAI agents
	d.name = 'Reti' and t.custom13 = 'Ribaltabile' and s.status = 100
GO

