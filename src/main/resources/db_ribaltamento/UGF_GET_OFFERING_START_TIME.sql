DROP FUNCTION [TP2].[UGF_GET_OFFERING_START_TIME]
GO


CREATE FUNCTION [TP2].[UGF_GET_OFFERING_START_TIME]
(
	@offeringID char(20)
)
RETURNS VARCHAR(50)
AS
BEGIN
	DECLARE @startTime nvarchar(50);
	
	select top 1 @startTime = start_time
	from FGT_TIME_ELEMENT
	where owner_id = @offeringID
	order by start_date, start_time
						
	RETURN @startTime
END

GO