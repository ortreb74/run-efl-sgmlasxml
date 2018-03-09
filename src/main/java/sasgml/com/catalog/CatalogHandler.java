package sasgml.com.catalog;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

/**

 */

public class CatalogHandler extends DefaultHandler {

	private String group;

	public CatalogHandler(String group) {
		this.group = group;
	}

	@Override
	public void endDocument() throws SAXException {
	}

	@Override
	public void startDocument() throws SAXException {
	}

	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {

		if (qName.equals("item")) {
			CatalogItem lConfigItem = new CatalogItem(
					attributes.getValue("id"), attributes.getValue("type"),
					attributes.getValue("uri"));
			CatalogManager.add(group,lConfigItem);
		}
	}

	public void endElement(String uri, String localName, String qName)
			throws SAXException {
	}

	public void characters(char ch[], int start, int length)
			throws SAXException {
	}
}