CREATE TABLE [dbo].[N2C2_TUI](
	[TUI] [nchar](4) NOT NULL,
	[CNT] [int] NULL,
PRIMARY KEY CLUSTERED 
(
	[TUI] ASC
)WITH (PAD_INDEX = OFF, STATISTICS_NORECOMPUTE = OFF, IGNORE_DUP_KEY = OFF, ALLOW_ROW_LOCKS = ON, ALLOW_PAGE_LOCKS = ON, OPTIMIZE_FOR_SEQUENTIAL_KEY = OFF) ON [PRIMARY]
) ON [PRIMARY]
GO

insert into N2C2_TUI (TUI,CNT) values
('T047',3227),
('T121',2114),
('T033',2019),
('T184',2001),
('T061',1833),
('T059',1247),
('T060',1141),
('T046',914),
('T191',607),
('T023',516),
('T195',376),
('T037',304),
('T082',287),
('T080',279),
('T058',261),
('T074',226),
('T079',171),
('T029',145),
('T197',142),
('T169',136),
('T200',135),
('T081',130),
('T048',127),
('T190',100),
('T196',100),
('T034',98),
('T127',76),
('T007',76),
('T031',73),
('T201',67),
('T042',63),
('T020',61),
('T019',54),
('T129',37),
('T024',32),
('T025',29),
('T004',28),
('T030',27),
('T039',26),
('T040',25),
('T167',24),
('T168',23),
('T122',20),
('T022',16),
('T078',15),
('T170',15),
('T017',14),
('T049',14),
('T054',14),
('T185',12),
('T055',12),
('T130',11),
('T038',9),
('T041',7),
('T067',7),
('T032',4),
('T051',4),
('T104',4),
('T073',4),
('T097',2)