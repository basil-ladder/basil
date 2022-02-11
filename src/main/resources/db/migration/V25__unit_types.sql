create table unit_type (id int8 not null, name varchar(40), primary key (id));

insert into unit_type (name, id) VALUES
(            	'Terran Marine'	,	0),
(            	'Terran Ghost'	,	1),
(            	'Terran Vulture'	,	2),
(            	'Terran Goliath'	,	3),
(            	'Terran Goliath Turret'	,	4),
(            	'Terran Siege Tank Tank Mode'	,	5),
(            	'Terran Siege Tank Tank Mode Turret'	,	6),
(            	'Terran SCV'	,	7),
(            	'Terran Wraith'	,	8),
(            	'Terran Science Vessel'	,	9),
(            	'Hero Gui Montag'	,	10),
(            	'Terran Dropship'	,	11),
(            	'Terran Battlecruiser'	,	12),
(            	'Terran Vulture Spider Mine'	,	13),
(            	'Terran Nuclear Missile'	,	14),
(            	'Terran Civilian'	,	15),
(            	'Hero Sarah Kerrigan'	,	16),
(            	'Hero Alan Schezar'	,	17),
(            	'Hero Alan Schezar Turret'	,	18),
(            	'Hero Jim Raynor Vulture'	,	19),
(            	'Hero Jim Raynor Marine'	,	20),
(            	'Hero Tom Kazansky'	,	21),
(            	'Hero Magellan'	,	22),
(            	'Hero Edmund Duke Tank Mode'	,	23),
(            	'Hero Edmund Duke Tank Mode Turret'	,	24),
(            	'Hero Edmund Duke Siege Mode'	,	25),
(            	'Hero Edmund Duke Siege Mode Turret'	,	26),
(            	'Hero Arcturus Mengsk'	,	27),
(            	'Hero Hyperion'	,	28),
(            	'Hero Norad II'	,	29),
(            	'Terran Siege Tank Siege Mode'	,	30),
(            	'Terran Siege Tank Siege Mode Turret'	,	31),
(            	'Terran Firebat'	,	32),
(            	'Spell Scanner Sweep'	,	33),
(            	'Terran Medic'	,	34),
(            	'Zerg Larva'	,	35),
(            	'Zerg Egg'	,	36),
(            	'Zerg Zergling'	,	37),
(            	'Zerg Hydralisk'	,	38),
(            	'Zerg Ultralisk'	,	39),
(            	'Zerg Broodling'	,	40),
(            	'Zerg Drone'	,	41),
(            	'Zerg Overlord'	,	42),
(            	'Zerg Mutalisk'	,	43),
(            	'Zerg Guardian'	,	44),
(            	'Zerg Queen'	,	45),
(            	'Zerg Defiler'	,	46),
(            	'Zerg Scourge'	,	47),
(            	'Hero Torrasque'	,	48),
(            	'Hero Matriarch'	,	49),
(            	'Zerg Infested Terran'	,	50),
(            	'Hero Infested Kerrigan'	,	51),
(            	'Hero Unclean One'	,	52),
(            	'Hero Hunter Killer'	,	53),
(            	'Hero Devouring One'	,	54),
(            	'Hero Kukulza Mutalisk'	,	55),
(            	'Hero Kukulza Guardian'	,	56),
(            	'Hero Yggdrasill'	,	57),
(            	'Terran Valkyrie'	,	58),
(            	'Zerg Cocoon'	,	59),
(            	'Protoss Corsair'	,	60),
(            	'Protoss Dark Templar'	,	61),
(            	'Zerg Devourer'	,	62),
(            	'Protoss Dark Archon'	,	63),
(            	'Protoss Probe'	,	64),
(            	'Protoss Zealot'	,	65),
(            	'Protoss Dragoon'	,	66),
(            	'Protoss High Templar'	,	67),
(            	'Protoss Archon'	,	68),
(            	'Protoss Shuttle'	,	69),
(            	'Protoss Scout'	,	70),
(            	'Protoss Arbiter'	,	71),
(            	'Protoss Carrier'	,	72),
(            	'Protoss Interceptor'	,	73),
(            	'Hero Dark Templar'	,	74),
(            	'Hero Zeratul'	,	75),
(            	'Hero Tassadar Zeratul Archon'	,	76),
(            	'Hero Fenix Zealot'	,	77),
(            	'Hero Fenix Dragoon'	,	78),
(            	'Hero Tassadar'	,	79),
(            	'Hero Mojo'	,	80),
(            	'Hero Warbringer'	,	81),
(            	'Hero Gantrithor'	,	82),
(            	'Protoss Reaver'	,	83),
(            	'Protoss Observer'	,	84),
(            	'Protoss Scarab'	,	85),
(            	'Hero Danimoth'	,	86),
(            	'Hero Aldaris'	,	87),
(            	'Hero Artanis'	,	88),
(            	'Critter Rhynadon'	,	89),
(            	'Critter Bengalaas'	,	90),
(            	'Special Cargo Ship'	,	91),
(            	'Special Mercenary Gunship'	,	92),
(            	'Critter Scantid'	,	93),
(            	'Critter Kakaru'	,	94),
(            	'Critter Ragnasaur'	,	95),
(            	'Critter Ursadon'	,	96),
(            	'Zerg Lurker Egg'	,	97),
(            	'Hero Raszagal'	,	98),
(            	'Hero Samir Duran'	,	99),
(            	'Hero Alexei Stukov'	,	100),
(            	'Special Map Revealer'	,	101),
(            	'Hero Gerard DuGalle'	,	102),
(            	'Zerg Lurker'	,	103),
(            	'Hero Infested Duran'	,	104),
(            	'Spell Disruption Web'	,	105),
(            	'Terran Command Center'	,	106),
(            	'Terran Comsat Station'	,	107),
(            	'Terran Nuclear Silo'	,	108),
(            	'Terran Supply Depot'	,	109),
(            	'Terran Refinery'	,	110),
(            	'Terran Barracks'	,	111),
(            	'Terran Academy'	,	112),
(            	'Terran Factory'	,	113),
(            	'Terran Starport'	,	114),
(            	'Terran Control Tower'	,	115),
(            	'Terran Science Facility'	,	116),
(            	'Terran Covert Ops'	,	117),
(            	'Terran Physics Lab'	,	118),
(            	'Unused Terran1'	,	119),
(            	'Terran Machine Shop'	,	120),
(            	'Unused Terran2'	,	121),
(            	'Terran Engineering Bay'	,	122),
(            	'Terran Armory'	,	123),
(            	'Terran Missile Turret'	,	124),
(            	'Terran Bunker'	,	125),
(            	'Special Crashed Norad II'	,	126),
(            	'Special Ion Cannon'	,	127),
(            	'Powerup Uraj Crystal'	,	128),
(            	'Powerup Khalis Crystal'	,	129),
(            	'Zerg Infested Command Center'	,	130),
(            	'Zerg Hatchery'	,	131),
(            	'Zerg Lair'	,	132),
(            	'Zerg Hive'	,	133),
(            	'Zerg Nydus Canal'	,	134),
(            	'Zerg Hydralisk Den'	,	135),
(            	'Zerg Defiler Mound'	,	136),
(            	'Zerg Greater Spire'	,	137),
(            	'Zerg Queens Nest'	,	138),
(            	'Zerg Evolution Chamber'	,	139),
(            	'Zerg Ultralisk Cavern'	,	140),
(            	'Zerg Spire'	,	141),
(            	'Zerg Spawning Pool'	,	142),
(            	'Zerg Creep Colony'	,	143),
(            	'Zerg Spore Colony'	,	144),
(            	'Unused Zerg1'	,	145),
(            	'Zerg Sunken Colony'	,	146),
(            	'Special Overmind With Shell'	,	147),
(            	'Special Overmind'	,	148),
(            	'Zerg Extractor'	,	149),
(            	'Special Mature Chrysalis'	,	150),
(            	'Special Cerebrate'	,	151),
(            	'Special Cerebrate Daggoth'	,	152),
(            	'Unused Zerg2'	,	153),
(            	'Protoss Nexus'	,	154),
(            	'Protoss Robotics Facility'	,	155),
(            	'Protoss Pylon'	,	156),
(            	'Protoss Assimilator'	,	157),
(            	'Unused Protoss1'	,	158),
(            	'Protoss Observatory'	,	159),
(            	'Protoss Gateway'	,	160),
(            	'Unused Protoss2'	,	161),
(            	'Protoss Photon Cannon'	,	162),
(            	'Protoss Citadel of Adun'	,	163),
(            	'Protoss Cybernetics Core'	,	164),
(            	'Protoss Templar Archives'	,	165),
(            	'Protoss Forge'	,	166),
(            	'Protoss Stargate'	,	167),
(            	'Special Stasis Cell Prison'	,	168),
(            	'Protoss Fleet Beacon'	,	169),
(            	'Protoss Arbiter Tribunal'	,	170),
(            	'Protoss Robotics Support Bay'	,	171),
(            	'Protoss Shield Battery'	,	172),
(            	'Special Khaydarin Crystal Form'	,	173),
(            	'Special Protoss Temple'	,	174),
(            	'Special XelNaga Temple'	,	175),
(            	'Resource Mineral Field'	,	176),
(            	'Resource Mineral Field Type 2'	,	177),
(            	'Resource Mineral Field Type 3'	,	178),
(            	'Unused Cave'	,	179),
(            	'Unused Cave In'	,	180),
(            	'Unused Cantina'	,	181),
(            	'Unused Mining Platform'	,	182),
(            	'Unused Independant Command Center'	,	183),
(            	'Special Independant Starport'	,	184),
(            	'Unused Independant Jump Gate'	,	185),
(            	'Unused Ruins'	,	186),
(            	'Unused Khaydarin Crystal Formation'	,	187),
(            	'Resource Vespene Geyser'	,	188),
(            	'Special Warp Gate'	,	189),
(            	'Special Psi Disrupter'	,	190),
(            	'Unused Zerg Marker'	,	191),
(            	'Unused Terran Marker'	,	192),
(            	'Unused Protoss Marker'	,	193),
(            	'Special Zerg Beacon'	,	194),
(            	'Special Terran Beacon'	,	195),
(            	'Special Protoss Beacon'	,	196),
(            	'Special Zerg Flag Beacon'	,	197),
(            	'Special Terran Flag Beacon'	,	198),
(            	'Special Protoss Flag Beacon'	,	199),
(            	'Special Power Generator'	,	200),
(            	'Special Overmind Cocoon'	,	201),
(            	'Spell Dark Swarm'	,	202),
(            	'Special Floor Missile Trap'	,	203),
(            	'Special Floor Hatch'	,	204),
(            	'Special Upper Level Door'	,	205),
(            	'Special Right Upper Level Door'	,	206),
(            	'Special Pit Door'	,	207),
(            	'Special Right Pit Door'	,	208),
(            	'Special Floor Gun Trap'	,	209),
(            	'Special Wall Missile Trap'	,	210),
(            	'Special Wall Flame Trap'	,	211),
(            	'Special Right Wall Missile Trap'	,	212),
(            	'Special Right Wall Flame Trap'	,	213),
(            	'Special Start Location'	,	214),
(            	'Powerup Flag'	,	215),
(            	'Powerup Young Chrysalis'	,	216),
(            	'Powerup Psi Emitter'	,	217),
(            	'Powerup Data Disk'	,	218),
(            	'Powerup Khaydarin Crystal'	,	219),
(            	'Powerup Mineral Cluster Type 1'	,	220),
(            	'Powerup Mineral Cluster Type 2'	,	221),
(            	'Powerup Protoss Gas Orb Type 1'	,	222),
(            	'Powerup Protoss Gas Orb Type 2'	,	223),
(            	'Powerup Zerg Gas Sac Type 1'	,	224),
(            	'Powerup Zerg Gas Sac Type 2'	,	225),
(            	'Powerup Terran Gas Tank Type 1'	,	226),
(            	'Powerup Terran Gas Tank Type 2'	,	227),
(            	'None'	,	228),
(            	'AllUnits'	,	229),
(            	'Men'	,	230),
(            	'Buildings'	,	231),
(            	'Factories'	,	232),
(            	'Unknown'	,	233);