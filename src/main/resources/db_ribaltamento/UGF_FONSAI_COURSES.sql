drop view [TP2].[UGF_FONSAI_COURSES]
GO


create view [TP2].[UGF_FONSAI_COURSES] as
select t.id as Id
	, t.locale_id as LocaleId
	, t.title as Title
	, t.offering_template_no as CourseNumber
	, a.detail1 + a.detail2 + a.detail3 + a.detail4 as Abstract
	, b.detail1 + b.detail2 + b.detail3 + b.detail4 as [Description]
	, right('00' + cast(convert(integer, ISNULL(m.duration, 0) / 60) as varchar(2)), 2)
	  + ':' +
	  right('00' + cast(convert(integer, ISNULL(m.duration, 0) % 60) as varchar(2)), 2)
		as Duration
	, t.custom10 as PublicationYear
	, t.avail_from as AvailFrom
	, tp2.UGF_GET_SUBCATEGORY_NAMES(t.id, t.locale_id, 'Disciplina ISVAP') as IvassDisciplines
	, tp2.UGF_GET_SUBCATEGORY_IDS(t.id, t.locale_id, 'Disciplina ISVAP') as IvassDisciplineIds
	, tp2.UGF_GET_SUBCATEGORY_NAMES(t.id, t.locale_id, 'Codice Formativo') as TrainingCodes
	, tp2.UGF_GET_SUBCATEGORY_IDS(t.id, t.locale_id, 'Codice Formativo') as TrainingCodeIds
from LET_EXT_OFFERING_TEMPLATE t
	inner join FGT_DOMAIN d on d.id = t.split
	inner join FGT_DETAIL a on a.id = t.abstract
	inner join FGT_DETAIL b on b.id = t.description
	inner join TPT_EXT_DELIVERY_MODE m on m.offering_temp_id = t.id and m.locale_id = t.locale_id
	inner join LET_EXT_DELIVERY v on v.id = m.delivery_id and v.locale_id = m.locale_id and v.id = 'eqcat000000000000004' -- Instructor-Led
where
	-- course for FonSAI agents
	d.name = 'Reti' and t.custom13 = 'Ribaltabile'
	-- course is visible in catalog
	and t.avail_from <= GETDATE()
	and (t.disc_from is null or t.disc_from > GETDATE())
	and SUBSTRING(t.flags, 3, 1) = '1'			-- Display for Call Center
	-- delivery mode is visible in catalog
	and m.avail_from <= GETDATE()
	and (m.disc_from is null or m.disc_from > GETDATE())
	and SUBSTRING(m.flags, 2, 1) = '1'			-- Display for Call Center

GO
