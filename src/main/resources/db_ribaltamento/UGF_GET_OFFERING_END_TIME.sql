DROP FUNCTION [TP2].[UGF_GET_OFFERING_END_TIME]
GO


CREATE FUNCTION [TP2].[UGF_GET_OFFERING_END_TIME]
(
	@offeringID char(20)
)
RETURNS VARCHAR(50)
AS
BEGIN
	DECLARE @endTime nvarchar(50);
	
	select top 1 @endTime = end_time
	from FGT_TIME_ELEMENT
	where owner_id = @offeringID
	order by end_date desc, end_time desc
						
	RETURN @endTime
END

GO