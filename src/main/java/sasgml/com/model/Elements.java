package sasgml.com.model;

import java.io.IOException;
import java.util.HashMap;
import java.util.LinkedHashMap;

import sasgml.com.exception.SASgmlException;

public class Elements extends LinkedHashMap<String, Element> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	private int size = 0;

	public void arrange(Entities pDTDEntities) throws SASgmlException,
			IOException {
		for (Element cDTDElement : values()) {
			String rContentText = pDTDEntities.getRealString(cDTDElement
					.getTextContent());
			cDTDElement.setTextContent(rContentText);
			cDTDElement.parseTextContent();
		}

	}

	public void perform() {
		HashMap<String, Element> map = new HashMap<String, Element>();

		for (Element cDTDElement : values()) {
			String cName = cDTDElement.getName();
			for (String cNormalizedName : cName.split("[\\|&,]", -1)) {
				map.put(cNormalizedName.trim(), new Element(cDTDElement));
			}
		}

		clear();
		putAll(map);
	}

	public void add(Element lElement) {
		put(String.valueOf(size), lElement);
		size++;
	}

	public void addAll(Elements lElements) {
		for (Element lElement : lElements.values()) {
			add(lElement);
		}
	}

	public int getSize() {
		return size;
	}

	public void setSize(int size) {
		this.size = size;
	}

}
