package sasgml.com.parsing;

import java.io.IOException;

import sasgml.com.exception.SASgmlException;
import sasgml.com.log.LogManager;
import sasgml.com.model.Attribute;
import sasgml.com.model.Attributes;
import sasgml.com.model.Entity;

public class EntityParser extends Parser {

	private Entity cDtdEntity;

	public void parse(String str) throws IOException, SASgmlException {
		load(str);
		parseToken();
		close();
	}

	public void parseToken() throws SASgmlException {

		// enttext ::= LITERAL // cf. 105-08
		// | 'CDATA' LITERAL
		// | 'SDATA' LITERAL
		// | 'PI' LITERAL
		// | 'STARTTAG' LITERAL
		// | 'ENDTAG' LITERAL
		// | 'MS' LITERAL
		// | 'MD' LITERAL
		// | extid enttype
		//
		// enttype ::= /* */ // cf. 108-109, 149.2
		// | 'SUBDOC'
		// | 'CDATA' NAME
		// | 'CDATA' NAME '[' attspecset ']'
		// | 'NDATA' NAME
		// | 'NDATA' NAME '[' attspecset ']'
		// | 'SDATA' NAME
		// | 'SDATA' NAME '[' attspecset ']'
		// /* For attspecset, see Common Constructs below. */
		//
		// extid ::= 'SYSTEM' // cf. 73
		// | 'SYSTEM' sysid
		// | 'PUBLIC' pubid
		// | 'PUBLIC' pubid sysid
		// /* For pubid, see Common Constructs below */
		//
		// sysid ::= LITERAL // cf. 75

		String eValue = "";
		String eMode = "";
		String ePubId = "";
		String eSysId = "";
		String eType = "";
		String eTypeName = "";
		Attributes eAttributes = new Attributes();

		if (hasNext(Token.CDATA)) {
			eMode = Token.CDATA;
			nextCharacters(5);
			skipWhiteSpaceAndLocalComment();
			eValue = parseLiteral();
		} else if (hasNext(Token.SDATA)) {
			eMode = Token.SDATA;
			nextCharacters(5);
			skipWhiteSpaceAndLocalComment();
			eValue = parseLiteral();
		} else if (hasNext(Token.PI)) {
			eMode = Token.PI;
			nextCharacters(2);
			skipWhiteSpaceAndLocalComment();
			eValue = parseLiteral();
		} else if (hasNext(Token.STARTTAG)) {
			eMode = Token.STARTTAG;
			nextCharacters(8);
			skipWhiteSpaceAndLocalComment();
			eValue = parseLiteral();
		} else if (hasNext(Token.ENDTAG)) {
			eMode = Token.ENDTAG;
			nextCharacters(6);
			skipWhiteSpaceAndLocalComment();
			eValue = parseLiteral();
		} else if (hasNext(Token.MS)) {
			eMode = Token.MS;
			nextCharacters(2);
			skipWhiteSpaceAndLocalComment();
			eValue = parseLiteral();
		} else if (hasNext(Token.MD)) {
			eMode = Token.MD;
			nextCharacters(2);
			skipWhiteSpaceAndLocalComment();
			eValue = parseLiteral();
		} else if (hasNext(Token.SYSTEM)) {
			eMode = Token.SYSTEM;
			nextCharacters(6);
			skipWhiteSpaceAndLocalComment();
			char c = getCharAtIndex(0);
			if (c == Token.QUOTE || c == Token.DOUBLE_QUOTE) {
				eSysId = parseLiteral();
			}
			skipWhiteSpaceAndLocalComment();
			if (!endOfData) {
				if (hasNext(Token.SUBDOC)) {
					eType = Token.SUBDOC;
					nextCharacters(6);
				} else {
					if (hasNext(Token.CDATA) || hasNext(Token.NDATA)
							|| hasNext(Token.SDATA)) {
						eType = parseStringWithLength(5);
						skipWhiteSpaceAndLocalComment();
						eTypeName = parseName().toUpperCase();
						skipWhiteSpaceAndLocalComment();
						if (!endOfData) {
							if (getCharAtIndex(0) == Token.LEFT_SQUARE_BRACKET) {
								nextCharacter();
								eAttributes = parseAttributesToken();
								if (getCharAtIndex(0) == Token.RIGTH_SQUARE_BRACKET) {
									nextCharacter();
								} else {
									LogManager
											.writeWarning("La declaration d'attributs dans l'entité doivenet se terminer par ] !");
								}
							}
						}
					} else {
						throw new SASgmlException(
								"Le text dans l'entité n'est pas un [ENTTYPE] valide !");
					}
				}
			}
			skipWhiteSpaceAndLocalComment();
		} else if (hasNext(Token.PUBLIC)) {
			eMode = Token.PUBLIC;
			nextCharacters(6);
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
			if (!endOfData) {
				if (hasNext(Token.SUBDOC)) {
					eType = Token.SUBDOC;
					nextCharacters(6);
				} else {
					if (hasNext(Token.CDATA) || hasNext(Token.NDATA)
							|| hasNext(Token.SDATA)) {
						eType = parseStringWithLength(5);
						nextCharacters(5);
						skipWhiteSpaceAndLocalComment();
						eTypeName = parseName().toUpperCase();
						skipWhiteSpaceAndLocalComment();
						if (!endOfData) {
							if (getCharAtIndex(0) == Token.LEFT_SQUARE_BRACKET) {
								nextCharacter();
								eAttributes = parseAttributesToken();
								if (getCharAtIndex(0) == Token.RIGTH_SQUARE_BRACKET) {
									nextCharacter();
								} else {
									LogManager
											.writeWarning("La declaration d'attributs dans l'entité doivenet se terminer par ] !");
								}
							}
						}
					} else {
						throw new SASgmlException(
								"Le text dans l'entité n'est pas un [ENTTYPE] valide !");
					}
				}
			}
			skipWhiteSpaceAndLocalComment();
		} else {
			eValue = parseLiteral();
		}
		skipWhiteSpaceAndLocalComment();

		cDtdEntity = new Entity(eValue, eMode, ePubId, eSysId, eType,
				eTypeName, eAttributes);

	}

	private Attributes parseAttributesToken() throws SASgmlException {

		// attspecset ::= /* nil */ // cf. 31
		// | attspecset attspec
		//
		// attspec ::= NAME '=' value // cf. 32
		// | value

		Attributes cAttributes = new Attributes();

		int index = 0;
		boolean match = false;
		while (!match) {
			int charIndex = realIndex;
			if (getCharAtIndex(0) == Token.GREAT_THAN) {
				match = true;
			} else {
				String attName = parseName().toUpperCase();
				skipWhiteSpace();
				if (getCharAtIndex(0) == Token.EQUAL) {
					nextCharacter();
					skipWhiteSpace();
					String attValue = parseValue();
					cAttributes.put(attName.toUpperCase(), new Attribute(
							attName.toUpperCase(), attValue));
					skipWhiteSpace();
				} else {
					cAttributes.put(
							String.valueOf(index),
							new Attribute(String.valueOf(index), attName
									.toUpperCase()));
					skipWhiteSpace();
					index++;
				}
				if (charIndex == realIndex) {
					throw new SASgmlException("Caractère [" + getCharAtIndex(0)
							+ "] inattendu à la position [" + realIndex
							+ "] lors du parse des attributs de l'entité ["
							+ attName + "]");
				}
			}

		}

		return cAttributes;
	}

	public Entity getDtdEntity() {
		return cDtdEntity;
	}

	public void setDtdEntity(Entity value) {
		this.cDtdEntity = value;
	}

}
