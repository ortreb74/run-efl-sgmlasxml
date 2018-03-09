package sasgml.com.model;

import java.util.TreeMap;

public class Attributes extends TreeMap<String, Attribute> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public String toDtdString() {
		StringBuilder lStringBuilder = new StringBuilder();
		lStringBuilder.append('{');
		for (Attribute cAttribute : values()) {
			lStringBuilder.append("\"name\"\" : " + cAttribute.getName()
					+ "\",\"value\" : \"" + cAttribute.getValue() + "\"");
		}
		lStringBuilder.append('}');
		return lStringBuilder.toString();
	}
	
		
}
