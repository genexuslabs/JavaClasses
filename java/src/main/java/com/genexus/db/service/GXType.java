package com.genexus.db.service;

public enum GXType
{
	Number(0),
	Int16(1),
	Int32(2),
	Int64(3),
	Date(4),
	DateTime(5),
	DateTime2(17),
	Byte(6),
	NChar(7),
	NClob(8),
	NVarChar(9),
	Char(10),
	LongVarChar(11),
	Clob(12),
	VarChar(13),
	Raw(14),
	Blob(15),
	Undefined(16),
	Boolean(18),
	Decimal(19),
	NText(20),
	Text(21),
	Image(22),
	UniqueIdentifier(23),
	Xml(24),
	Geography(25),
	Geopoint(26),
	Geoline(27),
	Geopolygon(28),
	DateAsChar(29);

	private final int value;
	GXType(final int value)
	{
		this.value = value;
	}
}