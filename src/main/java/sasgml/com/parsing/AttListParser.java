package sasgml.com.parsing;

import java.io.IOException;
import java.util.HashSet;

import sasgml.com.exception.SASgmlException;
import sasgml.com.model.AttList;
import sasgml.com.model.Attribute;
import sasgml.com.model.Attributes;

public class AttListParser extends Parser {

	private AttList cDtdAttList;

	public void parse(String str) throws SASgmlException, IOException {
		load(str);
		parseToken();
		close();
	}

	private void parseToken() throws SASgmlException {
		boolean isNotation = false;
		String eType = "";

		skipWhiteSpaceAndLocalComment();

		char c = getCharAtIndex(0);
		if (c == Token.DIEZ) {
			nextCharacter();
			if (hasNext(Token.NOTATION)) {
				nextCharacters(8);
				isNotation = true;
			} else {
				throw new SASgmlException("Caract�re [" + getCharAtIndex(0)
						+ "] inattendu � la position [" + realIndex
						+ "] : le mot clef 'NOTATION' est attendu !");
			}
		}

		skipWhiteSpaceAndLocalComment();

		if (getCharAtIndex(0) == Token.LEFT_PARENTHESIS) {
			nextCharacter();
			eType = parseStringUnTillChar(Token.RIGTH_PARENTHESIS)
					.toUpperCase();
			nextCharacter();
		} else {
			eType = parseName().toUpperCase();
		}

		skipWhiteSpaceAndLocalComment();

		Attributes attributes = new Attributes();

		while (!endOfData) {
			String attName = "";
			String attType = "";
			String attDefault = "";
			HashSet<String> values;
			attName = parseName().toUpperCase();
			skipWhiteSpaceAndLocalComment();
			attType = parseValType();
			skipWhiteSpaceAndLocalComment();
			values = parseValues();
			skipWhiteSpaceAndLocalComment();
			attDefault = parseDefault();
			skipWhiteSpaceAndLocalComment();

			if (attType.equals("") && attDefault.equals("")) {
				cDtdAttList = new AttList(eType, attributes, isNotation);
				return;
			} else {
				if (attDefault.startsWith(String.valueOf(Token.DIEZ))) {
					String[] splits = attDefault.split("\\s");
					if (splits.length == 2) {
						attributes.put(attName, new Attribute(attName, attType,
								splits[0].substring(1), splits[1], values));
					} else {
						attributes.put(attName, new Attribute(attName, attType,
								splits[0].substring(1), "", values));
					}

				} else {
					attributes.put(attName, new Attribute(attName, attType,
							"NONE", attDefault, values));
				}

			}
			skipWhiteSpaceAndLocalComment();
		}

		cDtdAttList = new AttList(eType, attributes, isNotation);

	}

	private String parseValType() throws SASgmlException {
		char c = getCharAtIndex(0);
		if (c == Token.LEFT_PARENTHESIS) {
			return Token.CHOICE_LIST;
		} else {
			String valType = parseName();
			if (valType.equals(Token.NOTATION)
					|| valType.equals(Token.NUTOKENS)
					|| valType.equals(Token.NUTOKEN)
					|| valType.equals(Token.NUMBERS)
					|| valType.equals(Token.NUMBER)
					|| valType.equals(Token.NMTOKENS)
					|| valType.equals(Token.NMTOKEN)
					|| valType.equals(Token.NAMES)
					|| valType.equals(Token.NAME)
					|| valType.equals(Token.IDREFS)
					|| valType.equals(Token.IDREF) || valType.equals(Token.ID)
					|| valType.equals(Token.ENTITIES)
					|| valType.equalsIgnoreCase(Token.ENTITY)
					|| valType.equalsIgnoreCase(Token.CDATA)) {
				return valType;
			} else {
				throw new SASgmlException(
						"Erreur : La chaine de type VALTYPE [" + valType
								+ "] n'est pas valide.");
			}
		}
	}

	private HashSet<String> parseValues() throws SASgmlException {
		HashSet<String> values = new HashSet<String>();
		if (getCharAtIndex(0) == Token.LEFT_PARENTHESIS) {
			nextCharacter();
			while (getCharAtIndex(0) != Token.RIGTH_PARENTHESIS) {
				skipWhiteSpaceAndLocalComment();
				String nToken = parseNameToken();
				values.add(nToken);
				skipWhiteSpaceAndLocalComment();
				char c = getCharAtIndex(0);
				if (c == '&' | c == '|' | c == ',') {
					nextCharacter();
				} else if (c == Token.RIGTH_PARENTHESIS) {

				} else {
					throw new SASgmlException(
							"La chaine de type [NAMETOKENS] n'est pas valide.");
				}
				skipWhiteSpaceAndLocalComment();
			}
			nextCharacter();
		}
		return values;
	}

	private String parseDefault() throws SASgmlException {
		if (getCharAtIndex(0) == Token.DIEZ) {
			nextCharacter();
			String type = parseName();
			if (type.toUpperCase().equals("IMPLIED")) {
				return "#IMPLIED";
			} else if (type.equals(Token.REQUIRED) || type.equals(Token.IMPLIED)
					|| type.equals(Token.CURRENT) || type.equals(Token.CONREF)) {
				return '#' + type;
			} else if (type.equals(Token.FIXED)) {
				skipWhiteSpaceAndLocalComment();
				return '#' + type + ' ' + parseValue();
			} else {
				throw new SASgmlException("La chaine [" + type
						+ "] ne repr�sente pas un type DEFAULT  valide !");
			}
		} else {
			return parseValue();
		}
	}

	public AttList getDtdAttList() {
		return cDtdAttList;
	}

	public void setDttAttList(AttList value) {
		this.cDtdAttList = value;
	}

}
