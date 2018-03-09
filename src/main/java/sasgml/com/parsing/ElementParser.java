package sasgml.com.parsing;

import java.io.IOException;

import sasgml.com.exception.SASgmlException;
import sasgml.com.model.Element;

public class ElementParser extends Parser {

	private Element dTDElement;

	public void parse(String str) throws SASgmlException, IOException {
		load(str);
		parseToken();
		close();
	}

	public void parseToken() throws SASgmlException, IOException {
		String eName = "";
		String eMinimiz = "";
		String eContentDecl = "";

		skipWhiteSpaceAndLocalComment();

		if (getCharAtIndex(0) == Token.LEFT_PARENTHESIS) {
			nextCharacter();
			eName = parseStringUnTillChar(Token.RIGTH_PARENTHESIS)
					.toUpperCase();
			nextCharacter();
		} else {
			eName = parseName().toUpperCase();
		}

		skipWhiteSpaceAndLocalComment();

		char c = getCharAtIndex(0);
		if (c == Token.MINUS || c == Token.UPPER_CASE_O) {
			eMinimiz += c;
			nextCharacter();
			skipWhiteSpace();
			c = getCharAtIndex(0);
			if (c == Token.MINUS || c == Token.UPPER_CASE_O) {
				eMinimiz += ' ';
				eMinimiz += c;
				nextCharacter();
			}
		}

		skipWhiteSpaceAndLocalComment();

		eContentDecl = parseStringUnTillString(Token.SUB_COMMENT).toUpperCase();

		skipWhiteSpaceAndLocalComment();

		// TEST
		ElementModelParser lDTDElementModelParser = new ElementModelParser();
		lDTDElementModelParser.parse(eContentDecl);

		//

		dTDElement = new Element(eName, eContentDecl, eMinimiz);
		lDTDElementModelParser.getDtdElementModel().setName(eName);
		dTDElement.setModel(lDTDElementModelParser.getDtdElementModel());
	}

	public Element getdTDElement() {
		return dTDElement;
	}

	public void setdTDElement(Element dTDElement) {
		this.dTDElement = dTDElement;
	}

}
