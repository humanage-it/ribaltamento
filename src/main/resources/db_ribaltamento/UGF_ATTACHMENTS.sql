drop view UGF_ATTACHMENTS

GO


create view UGF_ATTACHMENTS as
select h.id as Id, h.owner_id as OwnerId, h.description as Name, h.doc_type as Description, substring(h.flags, 1, 1) as IsPrivate
	, d.id as AttachId, d.description as Filename, d.doc_type as DocType, d.attach_type as AttachType, d.url as Url
from FGT_NOT_DOCS_HEADER h
inner join FGT_NOT_DOCS d on d.owner_id = h.id

GO

