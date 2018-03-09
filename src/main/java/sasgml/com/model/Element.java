package sasgml.com.model;

import java.io.IOException;

import sasgml.com.exception.SASgmlException;
import sasgml.com.parsing.ElementParser;

public class Element {
	private String name;
	private String value;
	private String textContent;
	private String typeString;
	private ElementTypeEnum type;
	private ElementModel model;
	private boolean isEmpty;

	private String fileName;

	public Element(String name, String value, String type) {
		super();
		this.name = name;
		this.value = value;
		this.typeString = type;
	}

	public Element(String textContent, String fileName) {
		this.setTextContent(textContent);
		this.setFileName(fileName);
	}

	public Element(Element pDTDElement) {
		this.name = pDTDElement.getName();
		this.value = pDTDElement.getValue();
		this.typeString = pDTDElement.getTypeString();
		this.type = pDTDElement.getType();
		this.model = pDTDElement.getModel();
		this.isEmpty = pDTDElement.isEmpty();
		this.setFileName(pDTDElement.getFileName());

	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public String getTypeString() {
		return typeString;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setTypeString(String type) {
		this.typeString = type;
	}

	public ElementTypeEnum getType() {
		return type;
	}

	public void setType(ElementTypeEnum type) {
		this.type = type;
	}

	public void parseType() {
		if (typeString.equals("--")) {
			type = ElementTypeEnum.STARTENDTAG;
		} else if (typeString.equals("-O")) {
			type = ElementTypeEnum.EMPTYTAG;
		} else if (typeString.equals("OO")) {
			type = ElementTypeEnum.NOSTARTENDTAG;
		} else {
			type = ElementTypeEnum.NONE;
		}
	}

	public void parseValue() {

		// ElementModelParser lDTDElementModelParser = new
		// ElementModelParser();
		// lDTDElementModelParser.parse(value);
		// model = lDTDElementModelParser.getDTDElementModel();

		isEmpty = value.matches("\\s*EMPTY\\s*");

	}

	public ElementModel getModel() {
		return model;
	}

	public void setModel(ElementModel model) {
		this.model = model;
	}

	public boolean isEmpty() {
		return isEmpty;
	}

	public void setEmpty(boolean isEmpty) {
		this.isEmpty = isEmpty;
	}

	public String getDescription() {
		if (name.contains("|")) {
			return "<!ELEMENT (" + name + ") " + typeString.substring(0, 1)
					+ " " + typeString.substring(1) + " " + value + ">";
		} else {
			return "<!ELEMENT " + name + " " + typeString.substring(0, 1) + " "
					+ typeString.substring(1) + " " + value + ">";
		}
	}

	public String getXmlDescription() {
		StringBuilder lStringBuilder = new StringBuilder();

		/***
		 * TODO : Virer les inclusions/Exclusions TODO : Remplacer les "#PCDATA"
		 * par "TEXTE-LIBRE"
		 */

		String textValue = value.replaceAll("#PCDATA", "TEXTE-LIBRE");
		String[] textValueSplit = textValue.split("\\s+");
		
		
		for (String cName : name.split("[|]")) {
			lStringBuilder.append("<!ELEMENT " + cName + " ");
			for(String lString : textValueSplit)
			{
				if(!(lString.startsWith("-") || lString.startsWith("+")))
				{
					lStringBuilder.append(lString + " ");
				}
			}
			lStringBuilder.append(">\n");
		}

		return lStringBuilder.toString();
	}

	public void parseTextContent() throws SASgmlException, IOException {
		ElementParser lDTDElementParser = new ElementParser();
		lDTDElementParser.parse(textContent);
		Element lDTDElement = lDTDElementParser.getdTDElement();
		this.name = lDTDElement.getName();
		this.value = lDTDElement.getValue();
		this.typeString = lDTDElement.getTypeString().replaceAll("\\s", "");
		this.model = lDTDElement.getModel();
		parseType();
		parseValue();
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
