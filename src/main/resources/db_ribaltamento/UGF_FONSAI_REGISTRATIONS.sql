drop view UGF_FONSAI_REGISTRATIONS

GO


create view UGF_FONSAI_REGISTRATIONS
as
select r.id as RegistrationId, r.custom0 as Presence, p.id as StudentId, r.class_id as OfferingId, p.fname as FirstName, p.lname as LastName, p.custom0 as FiscalCode, p.person_type as TrainingCode
from TPT_REGISTRATION r
inner join CMT_PERSON p on p.id = r.student_id
where r.status = 100 or (r.status = 600 and substring(r.flags, 1, 1) = '1') /* Cancel - No Show */

GO

