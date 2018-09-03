package sasgml.com.catalog;

import runefl.casual.ConsoleOutput;

import java.io.FileInputStream;
import java.io.InputStream;
import java.util.HashMap;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

public class CatalogManager {

	// TODO CATALOG
	public static HashMap<String, HashMap<String, HashMap<String, String>>> items = new HashMap<String, HashMap<String, HashMap<String, String>>>();

	private static String DtdPath = "";

	public static void load() {

		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			InputStream lCatalogIs = CatalogManager.class
					.getResourceAsStream("/catalog/sgml.xml");
			SAXParser saxParser = factory.newSAXParser();
			CatalogHandler handler = new CatalogHandler("sgml");
			saxParser.parse(lCatalogIs, handler);
		} catch (Throwable err) {
			err.printStackTrace();
		}

		try {
			InputStream lCatalogIs = CatalogManager.class
					.getResourceAsStream("/catalog/xml.xml");
			SAXParser saxParser = factory.newSAXParser();
			CatalogHandler handler = new CatalogHandler("xml");
			saxParser.parse(lCatalogIs, handler);
		} catch (Throwable err) {
			err.printStackTrace();
		}

	}

	public static void load(String filePath) {

		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			InputStream lCatalogIs = new FileInputStream(filePath);
			SAXParser saxParser = factory.newSAXParser();
			CatalogHandler handler = new CatalogHandler(filePath);
			saxParser.parse(lCatalogIs, handler);
		} catch (Throwable err) {
			err.printStackTrace();
		}

	}

	public static String getDtdPath() {
		return DtdPath;
	}

	public static void setDtdPath(String dtdPath) {
		DtdPath = dtdPath;
	}

	public static void add(String group, CatalogItem lCatalogItem) {
		if (!items.containsKey(group)) {
			items.put(group, new HashMap<String, HashMap<String, String>>());
		}
		if (!items.get(group).containsKey(lCatalogItem.getType())) {
			items.get(group).put(lCatalogItem.getType(),
					new HashMap<String, String>());
		}
		items.get(group).get(lCatalogItem.getType())
				.put(lCatalogItem.getId(), lCatalogItem.getUri());
	}

	public static String get(String group, String type, String id) {
		if (items.containsKey(group)) {
			if (items.get(group).containsKey(type)) {
				if (items.get(group).get(type).containsKey(id)) {
					return items.get(group).get(type).get(id);
				}
			}
		}
		return null;
	}

	public static String get(String type, String id) {
		for (String group : items.keySet()) {
			if (items.get(group).containsKey(type)) {
				if (items.get(group).get(type).containsKey(id)) {
					return items.get(group).get(type).get(id);
				}
			}
		}
		return null;
	}
}
