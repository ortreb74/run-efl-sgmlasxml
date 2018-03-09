package sasgml.com.model;

import java.io.IOException;

import sasgml.com.exception.SASgmlException;
import sasgml.com.parsing.EntityParser;

public class Entity {
	private String name;
	private String textContent;

	private String value;
	private String mode;
	private ModeEnum modeEnum;
	private String pubId;
	private String sysId;
	private String type;
	private TypeEnum typeEnum;
	private String typeName;
	private Attributes attributes;

	private String fileName;

	public Entity(String value, String mode, String pubId, String sysId, String type, String typeName,
			Attributes attributes) {
		super();
		this.value = value;
		this.mode = mode;
		this.pubId = pubId;
		this.sysId = sysId;
		this.type = type;
		this.typeName = typeName;
		this.attributes = attributes;
	}

	public Entity(String name, String textContent, String fileName) {
		super();
		this.name = name;
		this.textContent = textContent;
		this.setFileName(fileName);
	}

	// private CharsetEncoder encoder =
	// Charset.forName("iso-8859-1").newEncoder();

	public void parseTextContent() throws IOException, SASgmlException {
		EntityParser lDTDEntityParser = new EntityParser();
		lDTDEntityParser.parse(textContent);
		Entity lDtdEntity = lDTDEntityParser.getDtdEntity();

		this.value = lDtdEntity.value;
		this.mode = lDtdEntity.mode;
		this.pubId = lDtdEntity.pubId;
		this.sysId = lDtdEntity.sysId;
		this.type = lDtdEntity.type;
		this.typeName = lDtdEntity.typeName;
		this.attributes = lDtdEntity.attributes;

		try {
			typeEnum = TypeEnum.valueOf(type);
		} catch (Exception ex) {
			typeEnum = TypeEnum.NONE;
		}

		try {
			modeEnum = ModeEnum.valueOf(mode);
		} catch (Exception ex) {
			modeEnum = ModeEnum.NONE;
		}

		if (modeEnum == ModeEnum.SDATA) {
			String sdataValue = value.replaceAll("[\\[\\]\\s]", "");

			if (Entities.getXMLDTDEntities() != null) {
				if (Entities.getXMLDTDEntities().containsKey(sdataValue)) {
					value = Entities.getXMLDTDEntities().get(sdataValue).getValue();
				} else if (Entities.getXMLDTDEntities().containsKey(sdataValue.toUpperCase())) {
					value = Entities.getXMLDTDEntities().get(sdataValue.toUpperCase()).getValue();
				} else if (sdataValue.matches("^#[0-9]+;")) {
					value = decodeString('&' + value);
				}
			}

			// if (value.length() == 1) {
			// if (!encoder.canEncode(value)) {
			// System.out.println("<entity name=\"" + name + "\" value=\""
			// + String.format("%04x", (int) value.charAt(0))
			// + "\"/>");
			// }
			// }

		} else if (modeEnum == ModeEnum.NONE) {
			value = decodeString(value);
		}

	}

	public static String decodeString(String s) {
		StringBuffer result = new StringBuffer(s.length());
		int ampInd = s.indexOf("&");
		int lastEnd = 0;
		while (ampInd >= 0) {
			int nextAmp = s.indexOf("&", ampInd + 1);
			int nextSemi = s.indexOf(";", ampInd + 1);
			if (nextSemi != -1 && (nextAmp == -1 || nextSemi < nextAmp)) {
				int value = -1;
				String escape = s.substring(ampInd + 1, nextSemi);
				try {
					if (escape.startsWith("#x")) {
						value = Integer.parseInt(escape.substring(2), 16);
					} else if (escape.startsWith("#")) {
						value = Integer.parseInt(escape.substring(1), 10);
					} else {

					}
				} catch (NumberFormatException x) {
				}
				result.append(s.substring(lastEnd, ampInd));
				lastEnd = nextSemi + 1;
				if (value >= 0 && value <= 0xffff) {
					result.append((char) value);
				} else {
					result.append("&").append(escape).append(";");
					// result.append("?");
				}
			}
			ampInd = nextAmp;
		}
		result.append(s.substring(lastEnd));

		return result.toString();
	}

	public String getName() {
		return name;
	}

	public String getTextContent() {
		return textContent;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setTextContent(String value) {
		this.textContent = value;
	}

	public TypeEnum getTypeEnum() {
		return typeEnum;
	}

	public void setTypeEnum(TypeEnum type) {
		this.typeEnum = type;
	}

	public String toDtdString() {
		return "{\"entity\":{\"mode\"\" : " + mode + "\",\"public_id\" : \"" + pubId + "\",\"system_id\" : \"" + sysId
				+ "\",\"type\" : \"" + type + "\",\"type_name\" : \"" + typeName + "\",\"att_set\" : \""
				+ attributes.toDtdString() + "\"}}";
	}

	public String getType() {
		return type;
	}

	public String getValue() {
		return value;
	}

	public String getMode() {
		return mode;
	}

	public String getPubId() {
		return pubId;
	}

	public String getSysId() {
		return sysId;
	}

	public String getTypeName() {
		return typeName;
	}

	public Attributes getAttributes() {
		return attributes;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public void setMode(String mode) {
		this.mode = mode;
	}

	public void setPubId(String pubId) {
		this.pubId = pubId;
	}

	public void setSysId(String sysId) {
		this.sysId = sysId;
	}

	public void setType(String type) {
		this.type = type;
	}

	public void setTypeName(String typeName) {
		this.typeName = typeName;
	}

	public void setAttributes(Attributes attributes) {
		this.attributes = attributes;
	}

	public ModeEnum getModeEnum() {
		return modeEnum;
	}

	public void setModeEnum(ModeEnum modeEnum) {
		this.modeEnum = modeEnum;
	}

	public String getFileName() {
		return fileName;
	}

	public void setFileName(String fileName) {
		this.fileName = fileName;
	}

	public String getDescription() {
		if (value.length() == 1) {
			return "<!-" + fileName + "--><!ENTITY " + name + " SDATA \""
					+ String.format("%04x", (int) value.toCharArray()[0]) + "\">";
		} else {
			return "<!-" + fileName + "--><!ENTITY " + name + " SDATA \"" + value + "\">";
		}
	}
}
