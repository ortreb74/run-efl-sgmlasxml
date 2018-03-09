package sasgml.com.model;

import java.io.IOException;

import sasgml.com.exception.SASgmlException;
import sasgml.com.parsing.NotationParser;

public class Notation {
	private String textContent;

	private String name;
	private String mode;
	private ModeEnum modeEnum;
	private String pubId;
	private String sysId;

	public Notation(String name, String mode, String pubId, String sysId) {
		super();
		this.name = name;
		this.mode = mode;
		this.pubId = pubId;
		this.sysId = sysId;
	}

	public Notation(String textContent) {
		super();
		this.textContent = textContent;
	}

	public Notation(Notation pDtdNotation) {
		this.name = pDtdNotation.name;
		this.mode = pDtdNotation.mode;
		this.pubId = pDtdNotation.pubId;
		this.sysId = pDtdNotation.sysId;
		this.modeEnum = pDtdNotation.modeEnum;
	}

	public void parseTextContent() throws IOException, SASgmlException {
		NotationParser lDtdNotationParser = new NotationParser();
		lDtdNotationParser.parse(textContent);
		Notation lDtdNotation = lDtdNotationParser.getDtdNotation();

		this.name = lDtdNotation.name;
		this.mode = lDtdNotation.mode;
		this.pubId = lDtdNotation.pubId;
		this.sysId = lDtdNotation.sysId;

		try {
			modeEnum = ModeEnum.valueOf(mode);
		} catch (Exception ex) {
			modeEnum = ModeEnum.NONE;
		}

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

	public ModeEnum getTypeEnum() {
		return modeEnum;
	}

	public void setTypeEnum(ModeEnum mode) {
		this.modeEnum = mode;
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

	public void setMode(String mode) {
		this.mode = mode;
	}

	public void setPubId(String pubId) {
		this.pubId = pubId;
	}

	public void setSysId(String sysId) {
		this.sysId = sysId;
	}

	public ModeEnum getModeEnum() {
		return modeEnum;
	}

	public void setModeEnum(ModeEnum modeEnum) {
		this.modeEnum = modeEnum;
	}

	public String toDtdString() {
		return "{\"notation\":{\"mode\"\"" + mode + "\",\"public_id\" : \""
				+ pubId + "\",\"system_id\" : \"" + sysId + "\"}}";
	}

}
