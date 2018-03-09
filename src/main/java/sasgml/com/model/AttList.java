package sasgml.com.model;

import java.io.IOException;

import sasgml.com.exception.SASgmlException;
import sasgml.com.parsing.AttListParser;

public class AttList {
	private String name;
	private String value;
	private String textContent;
	private Attributes attributes;
	private boolean isNotation;

	
	private String fileName;

	public String getName() {
		return name;
	}

	public Attributes getAttributes() {
		return attributes;
	}

	public boolean isNotation() {
		return isNotation;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setAttributes(Attributes attributes) {
		this.attributes = attributes;
	}

	public void setNotation(boolean isNotation) {
		this.isNotation = isNotation;
	}

	public AttList(String name, Attributes attributes, boolean isNotation) {
		super();
		this.name = name;
		this.attributes = attributes;
		this.isNotation = isNotation;
	}

	public AttList(String textContent,String fileName) {
		this.setTextContent(textContent);
		attributes = new Attributes();
		this.setFileName(fileName);
	}

	public AttList(AttList pDTDATTList) {
		this.name = pDTDATTList.getName();
		this.isNotation = pDTDATTList.isNotation();
		this.attributes = pDTDATTList.getAttributes();
		this.fileName = pDTDATTList.getFileName();
	}

	public String getDescription() {
		String str = "<!ATTLIST " + name + " ";
		if (isNotation) {
			str += "#NOTATION ";
		}
		for (Attribute cAttribute : attributes.values()) {
			str += "\n\t\t\t" + cAttribute.getDescription();
		}
		return str + ">";
	}

	public String getXmlDescription() {
		StringBuilder lStringBuilder = new StringBuilder();

		for (String cName : name.split("[|]")) {

			lStringBuilder.append("<!ATTLIST " + cName + " ");

			if (isNotation) {
				lStringBuilder.append("#NOTATION ");
			}
			for (Attribute cAttribute : attributes.values()) {
				lStringBuilder.append("\n\t\t\t"
						+ cAttribute.getXmlDescription());
			}

			lStringBuilder.append(">\n");

		}
		return lStringBuilder.toString();

	}

	public String getValue() {
		return value;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void parseTextContent() throws SASgmlException, IOException {
		AttListParser lDTDATTListParser = new AttListParser();
		// System.out.println(textContent);
		lDTDATTListParser.parse(textContent);
		AttList lDTDATTList = lDTDATTListParser.getDtdAttList();
		setName(lDTDATTList.getName());
		setNotation(lDTDATTList.isNotation());
		setAttributes(lDTDATTList.getAttributes());
	}

	public String getTextContent() {
		return textContent;
	}

	public void setTextContent(String textContent) {
		this.textContent = textContent;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

}
