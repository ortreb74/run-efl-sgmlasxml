package sasgml.com.model;

import java.util.HashSet;
import java.util.LinkedHashMap;

public class Attribute implements Cloneable {
	private String name;
	private String value;
	private String type;
	private AttributeTypeEnum typeEnum;
	private String defaultType;
	private AttributeDefaultTypeEnum defaultTypeEnum;
	private String defaultValue;
	private LinkedHashMap<String, String> values;
	private HashSet<String> originalValues;

	public Attribute(String name, String value) {
		super();
		this.name = name;
		this.value = value;
		values = new LinkedHashMap<String, String>();
	}

	public Attribute(String name, String type, String defaultType,
			String defaultValue, HashSet<String> originalValues) {
		super();
		this.name = name;
		this.type = type;
		this.defaultValue = defaultValue;
		this.defaultType = defaultType;
		this.originalValues = originalValues;
		performTypeEnum();
		performDefaultTypeEnum();
		performValues();
	}

	private void performValues() {
		values = new LinkedHashMap<String, String>();
		for (String cValue : originalValues) {
			values.put(cValue, cValue);
			values.put(cValue.toUpperCase(), cValue);
		}
	}

	private void performTypeEnum() {
		try {
			typeEnum = AttributeTypeEnum.valueOf(type);
		} catch (Exception ex) {
			typeEnum = AttributeTypeEnum.CHOICE_LIST;
		}
	}

	private void performDefaultTypeEnum() {
		try {
			defaultTypeEnum = AttributeDefaultTypeEnum.valueOf(defaultType);
		} catch (Exception ex) {
			defaultTypeEnum = AttributeDefaultTypeEnum.NONE;
		}
	}

	public String getName() {
		return name;
	}

	public String getValue() {
		return value;
	}

	public void setName(String name) {
		this.name = name;
	}

	public void setValue(String value) {
		this.value = value;
	}

	public String getType() {
		return type;
	}

	public void setType(String type) {
		this.type = type;
	}

	public String getDefaultValue() {
		return defaultValue;
	}

	public void setDefaultValue(String defaultValue) {
		this.defaultValue = defaultValue;
	}

	public String getDescription() {
		String str = name + " ";
		if (typeEnum == AttributeTypeEnum.CHOICE_LIST) {
			str += originalValues.toString().replace(",", "|")
					.replace("[", "(").replace("]", ")");
		} else {
			str += type;
		}
		str += " ";

		if (!defaultValue.equals("")) {
			str += "\"" + defaultValue + "\" ";
		}

		if (defaultTypeEnum != AttributeDefaultTypeEnum.NONE) {
			str += "#" + defaultType;
		}

		return str;
	}

	public String getXmlDescription() {
		String str = name + " ";
		switch (typeEnum) {
		case CHOICE_LIST:
			str += originalValues.toString().replace(",", "|")
					.replace("[", "(").replace("]", ")");
			break;
		case NAME:
		case NAMES:
		case NUMBER:
		case NUMBERS:
		case NMTOKEN:
		case NMTOKENS:
		case NOTATION:
		case NUTOKEN:
		case NUTOKENS:
		case ENTITIES:
		case ENTITY:
			str += "CDATA";
			break;
		case CDATA:
		case ID:
		case IDREF:
		case IDREFS:
		default:
			str += type;
		}
		str += " ";

		if (defaultValue.equals("")
				&& defaultTypeEnum == AttributeDefaultTypeEnum.NONE) {
			str += "#IMPLIED";
		}

		if (!defaultValue.equals("")) {
			str += "\"" + defaultValue + "\" ";
		}

		if (defaultTypeEnum != AttributeDefaultTypeEnum.NONE) {
			str += "#" + defaultType;
		}

		return str;
	}

	public AttributeTypeEnum getTypeEnum() {
		return typeEnum;
	}

	public void setTypeEnum(AttributeTypeEnum typeEnum) {
		this.typeEnum = typeEnum;
	}

	public LinkedHashMap<String, String> getValues() {
		return values;
	}

	public void setValues(LinkedHashMap<String, String> values) {
		this.values = values;
	}

	public String getDefaultType() {
		return defaultType;
	}

	public void setDefaultType(String defaultType) {
		this.defaultType = defaultType;
	}

	public AttributeDefaultTypeEnum getDefaultTypeEnum() {
		return defaultTypeEnum;
	}

	public void setDefaultTypeEnum(AttributeDefaultTypeEnum defaultTypeEnum) {
		this.defaultTypeEnum = defaultTypeEnum;
	}

	public boolean hasDefaultValue() {
		// TODO Auto-generated method stub
		return false;
	}

	public HashSet<String> getOriginalValues() {
		return originalValues;
	}

	public void setOriginalValues(HashSet<String> originalValues) {
		this.originalValues = originalValues;
	}

}
