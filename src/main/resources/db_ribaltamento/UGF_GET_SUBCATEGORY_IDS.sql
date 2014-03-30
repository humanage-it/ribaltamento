DROP FUNCTION [TP2].[UGF_GET_SUBCATEGORY_IDS]
GO


CREATE FUNCTION [TP2].[UGF_GET_SUBCATEGORY_IDS]
(
	@ownerID char(20),
	@localeID char(20),
	@parentCategory nvarchar(1000)
)
RETURNS VARCHAR(MAX)
AS
BEGIN
	DECLARE @AllValues varchar(max);
	
	select @AllValues = coalesce(@AllValues + '/', '') + c.id
	from FGT_GEN g
	inner join (
		select id
		from TPT_EXT_NLEVEL_FOLDER
		where folder_type = 200 and flags like '1%'
		and locale_id = @localeID
		and parent_id = (
			select id
			from TPT_EXT_NLEVEL_FOLDER
			where folder_type = 200 and flags like '1%'
			and name = @parentCategory
			and locale_id = @localeID
		)
	) c on c.id = g.id1
	where g.[type] = 304  -- Category
	and g.id2 = @ownerID
						
	RETURN @AllValues
END

GO