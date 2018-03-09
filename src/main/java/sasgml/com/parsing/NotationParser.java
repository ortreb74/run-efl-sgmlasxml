package sasgml.com.parsing;

import java.io.IOException;

import sasgml.com.exception.SASgmlException;
import sasgml.com.model.Notation;

public class NotationParser extends Parser {

	private Notation cDtdNotation;

	public void parse(String str) throws IOException, SASgmlException {
		load(str);
		parseToken();
		close();
	}

	public void parseToken() throws SASgmlException {

		skipWhiteSpaceAndLocalComment();
		String eName = parseName().toUpperCase();
		String eMode = "";
		String ePubId = "";
		String eSysId = "";
		skipWhiteSpaceAndLocalComment();
		if (hasNext(Token.SYSTEM)) {
			eMode = Token.SYSTEM;
			nextCharacters(6);
			skipWhiteSpaceAndLocalComment();
			char c = getCharAtIndex(0);
			if (c == Token.QUOTE || c == Token.DOUBLE_QUOTE) {
				eSysId = parseLiteral();
			}
			skipWhiteSpaceAndLocalComment();
		} else if (hasNext(Token.PUBLIC)) {
			eMode = Token.PUBLIC;
			nextCharacters(6);
			skipWhiteSpaceAndLocalComment();
			char c = getCharAtIndex(0);
			if (c == Token.QUOTE || c == Token.DOUBLE_QUOTE) {
				ePubId = parseLiteral();
			}
			skipWhiteSpaceAndLocalComment();
			c = getCharAtIndex(0);
			if (c == Token.QUOTE || c == Token.DOUBLE_QUOTE) {
				eSysId = parseLiteral();
			}
			skipWhiteSpaceAndLocalComment();
		} else {
			throw new SASgmlException(
					"La déclaration de [" + eName + "] n'est pas une NOTATION valide !");
		}
		skipWhiteSpaceAndLocalComment();

		cDtdNotation = new Notation(eName, eMode, ePubId, eSysId);

	}

	public Notation getDtdNotation() {
		return cDtdNotation;
	}

	public void setDtdNotation(Notation value) {
		this.cDtdNotation = value;
	}

}
