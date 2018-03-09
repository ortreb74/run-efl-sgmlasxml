package sasgml.com.model;
public enum ElementOccurenceTypeEnum {
	ONE, PLUS, QUID, ASTERIX;

	public static ElementOccurenceTypeEnum getValue(String value) {
		if (value.equals("*")) {
			return ASTERIX;
		} else if (value.equals("+")) {
			return PLUS;
		} else if (value.equals("?")) {
			return QUID;
		} else {
			return ONE;
		}
	}
}
