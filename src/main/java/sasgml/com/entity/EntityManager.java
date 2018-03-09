package sasgml.com.entity;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class EntityManager {

	// TODO Entity
	public static Map<String, Map<String, String>> items = new HashMap<String, Map<String, String>>();

	public static void load(String filePath) {

		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			InputStream lEntityIs = new FileInputStream(filePath);
			SAXParser saxParser = factory.newSAXParser();
			EntityHandler handler = new EntityHandler();
			saxParser.parse(lEntityIs, handler);
		} catch (Throwable err) {
			err.printStackTrace();
		}
	}

}
