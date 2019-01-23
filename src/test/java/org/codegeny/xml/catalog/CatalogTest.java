package org.codegeny.xml.catalog;

import static org.codegeny.xml.catalog.CatalogBuilder.newCatalog;
import static org.codegeny.xml.catalog.Prefer.PUBLIC;
import static org.junit.Assert.assertEquals;

import org.junit.Test;

public class CatalogTest {
	
	@Test
	public void simpleTest() {
		
		Catalog catalog = newCatalog(PUBLIC, null)
			.addGroup(null, "http://www.oasis-open.org/docbook/xml/4.1.2/")
				.addPublic("-//OASIS//DTD DocBook XML V4.1.2//EN", "docbookx.dtd", null)
				.addPublic("-//OASIS//ENTITIES DocBook XML Notations V4.1.2//EN", "dbnotnx.mod", null)
				.addPublic("-//OASIS//ENTITIES DocBook XML Character Entities V4.1.2//EN", "dbcentx.mod", null)
				.addPublic("-//OASIS//ELEMENTS DocBook XML Information Pool V4.1.2//EN", "dbpoolx.mod", null)
				.addPublic("-//OASIS//ELEMENTS DocBook XML Document Hierarchy V4.1.2//EN", "dbhierx.mod", null)
				.addPublic("-//OASIS//ENTITIES DocBook XML Additional General Entities V4.1.2//EN", "dbgenent.mod", null)
				.addPublic("-//OASIS//DTD DocBook XML CALS Table Model V4.1.2//EN","calstblx.dtd", null)
				.end()
			.addPublic("-//OASIS//DTD DocBook MathML Module V1.0//EN", "http://www.oasis-open.org/docbook/xml/mathml/1.0/dbmathml.dtd", null)
			.addNextCatalog(PUBLIC, null).end() // empty for example
			.end();
		
		assertEquals("http://www.oasis-open.org/docbook/xml/4.1.2/docbookx.dtd", catalog.mapExternalIdentifier("-//OASIS//DTD DocBook XML V4.1.2//EN", null).get());
		assertEquals("http://www.oasis-open.org/docbook/xml/mathml/1.0/dbmathml.dtd", catalog.mapExternalIdentifier("-//OASIS//DTD DocBook MathML Module V1.0//EN", null).get());
	}
}
