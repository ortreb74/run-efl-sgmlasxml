package sasgml.com.entity;

import java.util.HashMap;
import java.util.Map;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.helpers.DefaultHandler;

public class EntityHandler extends DefaultHandler {

	Map<String,String> replacementMap;
	
	public void startElement(String uri, String localName, String qName,
			Attributes attributes) throws SAXException {

		if (qName.equals("dtd")) {
			String dtd = attributes.getValue("doctype");
			replacementMap = new HashMap<String,String>();
			
			EntityManager.items.put(dtd,replacementMap);
		}
		
		if (qName.equals("replacement")) {
			String value = attributes.getValue("value");
			String name = attributes.getValue("name");
			
			replacementMap.put(value, name);
		}		
	}

}