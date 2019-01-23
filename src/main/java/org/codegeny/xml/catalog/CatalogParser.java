package org.codegeny.xml.catalog;

import static java.util.Objects.requireNonNull;

import java.util.stream.IntStream;

import javax.xml.transform.Source;
import javax.xml.transform.TransformerException;
import javax.xml.transform.TransformerFactory;
import javax.xml.transform.URIResolver;
import javax.xml.transform.dom.DOMResult;

import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.NodeList;

/**
 * This class is responsible for parsing catalogs for a given systemId. 
 * 
 * @author Xavier Dury
 */
public class CatalogParser {
	
	public static final String NAMESPACE = "urn:oasis:names:tc:entity:xmlns:xml:catalog";
	
	private final CatalogCache catalogCache;
	private final URIResolver uriResolver;
	
	public CatalogParser(URIResolver uriResolver) {
		this(uriResolver, CatalogCache.NO_CACHE);
	}
	
	public CatalogParser(URIResolver uriResolver, CatalogCache catalogCache) {
		this.uriResolver = requireNonNull(uriResolver, "uriResolver cannot be null");
		this.catalogCache = requireNonNull(catalogCache, "catalogCache cannot be null");
	}
	
	private void assertNamespace(Element element) {
		if (!element.getNamespaceURI().equals(NAMESPACE)) {
			throw new CatalogException(String.format("Catalog element '%s' must belong to the '%s' namespace", element.getTagName(), NAMESPACE));
		}
	}
	
	private String requiredAttribute(Element element, String name) {
		String value = element.getAttribute(name);
		if (value == null) {
			throw new CatalogException(String.format("Attribute '%s' of element '%s' cannot be null", name, element.getTagName()));
		}
		return value;
	}
	
	private <T> CatalogBuilder<T> children(CatalogBuilder<T> builder, Element element, String systemId) {
		NodeList children = element.getElementsByTagName("*");
		IntStream.range(0, children.getLength()).mapToObj(i -> (Element) children.item(i)).forEach(e -> element(builder, e, systemId));
		return builder;
	}
	
	private <T> CatalogBuilder<T> element(CatalogBuilder<T> builder, Element element, String systemId) {
		assertNamespace(element);
		switch (element.getTagName()) {
		case "group":
			return children(builder.addGroup(Prefer.valueOf(requiredAttribute(element, "prefer").toUpperCase()), element.getAttributeNS("http://www.w3.org/XML/1998/namespace", "base")), element, systemId).end();
		case "public":
			return builder.addPublic(requiredAttribute(element, "publicId"), requiredAttribute(element, "uri"), element.getAttributeNS("http://www.w3.org/XML/1998/namespace", "base"));
		case "system":
			return builder.addSystem(requiredAttribute(element, "systemId"), requiredAttribute(element, "uri"), element.getAttributeNS("http://www.w3.org/XML/1998/namespace", "base"));
		case "rewriteSystem":
			return builder.addRewriteSystem(requiredAttribute(element, "systemIdStartString"), requiredAttribute(element, "rewritePrefix"));
		case "systemSuffix":
			return builder.addSystemSuffix(requiredAttribute(element, "systemIdSuffix"), requiredAttribute(element, "uri"), element.getAttributeNS("http://www.w3.org/XML/1998/namespace", "base"));
		case "delegatePublic":
			return builder.addDelegatePublic(requiredAttribute(element, "publicIdStartString"), parse(requiredAttribute(element, "catalog"), systemId));
		case "delegateSystem":
			return builder.addDelegateSystem(requiredAttribute(element, "systemIdStartString"), parse(requiredAttribute(element, "catalog"), systemId));
		case "uri":
			return builder.addURI(requiredAttribute(element, "name"), requiredAttribute(element, "uri"), element.getAttributeNS("http://www.w3.org/XML/1998/namespace", "base"));
		case "rewriteURI":
			return builder.addRewriteURI(requiredAttribute(element, "uriStartString"), requiredAttribute(element, "rewritePrefix"));
		case "uriSuffix":
			return builder.addURISuffix(requiredAttribute(element, "uriSuffix"), requiredAttribute(element, "uri"), element.getAttributeNS("http://www.w3.org/XML/1998/namespace", "base"));
		case "delegateURI":
			return builder.addDelegateURI(requiredAttribute(element, "uriStartString"), parse(requiredAttribute(element, "catalog"), systemId));
		case "nextCatalog":
			return builder.addNextCatalog(parse(requiredAttribute(element, "catalog"), systemId));
		default:
			throw new CatalogException(String.format("Unknown tag '%s'", element.getTagName()));
		}
	}
	
	private Catalog parse(Source source) {
		try {
			DOMResult result = new DOMResult();
			TransformerFactory.newInstance().newTransformer().transform(source, result);
			Document document = (Document) result.getNode();
			return rootElement(document.getDocumentElement(), source.getSystemId());
		} catch (TransformerException transformerException) {
			throw new CatalogException(String.format("Could not parse catalog with systemId '%s'", source.getSystemId()), transformerException);
		}
	}
	
	public Catalog parse(String systemId) {
		return parse(requireNonNull(systemId, "systemId cannot be null"), null);
	}
	
	private Catalog parse(String systemId, String base) {
		try {
			Source source = uriResolver.resolve(systemId, base);
			if (source == null) {
				throw new CatalogException(String.format("Could not resolve catalog with systemId '%s' and base '%s'", systemId, base));
			}
			return this.catalogCache.resolve(source.getSystemId(), () -> parse(source));
		} catch (TransformerException transformerException) {
			throw new CatalogException(String.format("Could not resolve catalog with systemId '%s' and base '%s'", systemId, base), transformerException);
		}
	}
		
	private Catalog rootElement(Element element, String systemId) {
		assertNamespace(element);
		switch (element.getTagName()) {
		case "catalog":
			return children(CatalogBuilder.newCatalog(Prefer.valueOf(requiredAttribute(element, "prefer").toUpperCase()), element.getAttributeNS("http://www.w3.org/XML/1998/namespace", "base")), element, systemId).end();
		default:
			throw new CatalogException(String.format("Root element must be 'catalog' and not '%s'", element.getTagName()));
		}
	}
}
