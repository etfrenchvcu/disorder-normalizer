USE [nlp]
GO

/****** Object:  View [dbo].[UMLS]    Script Date: 10/26/2021 11:56:38 AM ******/
SET ANSI_NULLS ON
GO

SET QUOTED_IDENTIFIER ON
GO

CREATE view [dbo].[UMLS]
as 
	SELECT distinct
		a.*,
		b.STY
	FROM MRCONSO a
	INNER JOIN MRSTY b on a.CUI = b.CUI
	INNER JOIN N2C2_TUI c on b.TUI = c.TUI
	WHERE 
		LAT = 'ENG' AND
		SAB in ('RXNORM', 'SNOMEDCT_US') AND
		SUPPRESS = 'N'
GO


