/**
 * <p>
 * This packages provides a simple implementation of OASIS XML Catalogs V1.1.
 * </p>
 * <p>
 * A catalog can be obtained by parsing a Catalog XML file like:
 * </p>
 * 
 * <pre>
 * URIResolver resolver = ...
 * CatalogParser parser = new CatalogParser(resolver);
 * Catalog catalog = parser.parse(catalogSystemId); // absolute systemId
 * </pre>
 * <p>
 * Remarks:
 * </p>
 * <ul>
 * <li>The CatalogParser needs a URIResolver to resolve the given systemId or
 * references to other catalogs from within the parsed catalog.</li>
 * <li>The CatalogParser can also take an optional CatalogCache parameter if caching is needed.</li>
 * <li>If you need to resolve catalogs from the classpath, this should be
 * handled in the URIResolver.</li>
 * </ul>
 * <p>
 * A catalog can also be builded programmatically:
 * </p>
 * 
 * <pre>
 * Catalog catalog = CatalogBuilder.newCatalog(Prefer.PUBLIC, null)
 *     .addGroup(null, "http://www.oasis-open.org/docbook/xml/4.1.2/")
 *         .addPublic("-//OASIS//DTD DocBook XML V4.1.2//EN", "docbookx.dtd", null)
 *         .addPublic("-//OASIS//ENTITIES DocBook XML Notations V4.1.2//EN", "dbnotnx.mod", null)
 *         .addPublic("-//OASIS//ENTITIES DocBook XML Character Entities V4.1.2//EN", "dbcentx.mod", null)
 *         .addPublic("-//OASIS//ELEMENTS DocBook XML Information Pool V4.1.2//EN", "dbpoolx.mod", null)
 *         .addPublic("-//OASIS//ELEMENTS DocBook XML Document Hierarchy V4.1.2//EN", "dbhierx.mod", null)
 *         .addPublic("-//OASIS//ENTITIES DocBook XML Additional General Entities V4.1.2//EN", "dbgenent.mod", null)
 *         .addPublic("-//OASIS//DTD DocBook XML CALS Table Model V4.1.2//EN","calstblx.dtd", null)
 *         .end()
 *     .addPublic("-//OASIS//DTD DocBook MathML Module V1.0//EN", "http://www.oasis-open.org/docbook/xml/mathml/1.0/dbmathml.dtd", null)
 *     .addNextCatalog(stylesheetCatalog) // previously loaded/builded
 *     .end();
 * </pre>
 */
package org.codegeny.xml.catalog;