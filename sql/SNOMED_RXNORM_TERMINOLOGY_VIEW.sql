USE [nlp]
GO

/****** Object:  View [dbo].[SNOMED_RXNORM_TERMINOLOGY]    Script Date: 10/26/2021 11:56:28 AM ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE view [dbo].[SNOMED_RXNORM_TERMINOLOGY]
as
select 
	cui + '||' + names [entry]
from (
	select 
		cui, 
		string_agg(cast([name] as nvarchar(max)), '|') within group (order by [name]) names
	from (
		select distinct
			CUI cui,
			trim(lower(replace(replace(replace([STR],'(s)',''),')',''),'(',''))) [name]
		from UMLS
	) x
	group by cui
) y
--order by cui + '||' + names
GO


