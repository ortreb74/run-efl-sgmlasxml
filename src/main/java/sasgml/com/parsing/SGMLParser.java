package sasgml.com.parsing;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;

import sasgml.com.exception.SASgmlException;
import sasgml.com.handler.SGMLHandler;
import sasgml.com.model.Attribute;
import sasgml.com.model.Attributes;

public class SGMLParser extends DTDParser {

	private SGMLHandler sGMLHandler;

	public void parse(String str) throws SASgmlException, IOException {
		load(str);
		sGMLHandler.startDocument();
		parseToken();
		sGMLHandler.endDocument();
		close();
	}

	public void parse(File lFile) throws SASgmlException, IOException {
		load(lFile);
		sGMLHandler.startDocument();
		parseToken();
		sGMLHandler.endDocument();
		close();
	}

	public void parseToken() throws SASgmlException {
		skipWhiteSpace();
		if (hasNextIgnoreCase(Token.DOCTYPE_START)) {
			super.parseToken();
			sGMLHandler.doctype(dTDHandler.getDtdName(),
					dTDHandler.getDtdType(), dTDHandler.getSysId(),
					dTDHandler.getPubId());
		}
		while (!endOfData) {
			int charIndex = realIndex;
			char c = getCharAtIndex(0);
			if (c == Token.LESS_THAN) {
				nextCharacter();
				c = getCharAtIndex(0);
				if (c == Token.QUID) {
					// PI
					nextCharacter();
					parsePIToken();
				} else if (c == Token.SLASH) {
					nextCharacter();
					c = getCharAtIndex(0);
					// CLOSE ELEMENT
					if (isLetter(c)) {
						parseEndElementToken();
					} else {
						// cas </20 ...
						sGMLHandler.text("</");
					}
				} else if (c == Token.EXCLAMATION) {
					nextCharacter();
					c = getCharAtIndex(0);
					if (c == Token.GREAT_THAN) {
						// SPECIAL COMMENT
						nextCharacter();
						sGMLHandler.comment("");
					} else if (c == Token.MINUS) {
						nextCharacter();
						c = getCharAtIndex(0);
						if (c == Token.MINUS) {
							nextCharacter();
							parseCommentToken();
						} else {
							sGMLHandler.text("<!-");
						}
					} else if (c == Token.LEFT_SQUARE_BRACKET) {
						nextCharacter();
						parseMarkedSectionToken();
					} else {
						sGMLHandler.text("<!");
					}
				} else if (isLetter(getCharAtIndex(0))) {
					parseStartElementToken();
				} else {
					sGMLHandler.text("<");
				}
			} else {
				parseTextToken();
			}
			if (charIndex == realIndex) {
				throw new SASgmlException("Caractère [" + getCharAtIndex(0)
						+ "] inattendu à la position [" + realIndex + "]");
			}
		}
	}

	private void parseTextToken() throws SASgmlException {
		sGMLHandler.text(parseStringUnTillChar(Token.LESS_THAN));
	}

	private void parseStartElementToken() throws SASgmlException {
		ArrayList<String> grpList = new ArrayList<String>();
		char c = getCharAtIndex(0);
		if (c == Token.LEFT_PARENTHESIS) {
			nextCharacter();
			skipWhiteSpace();
			while (!endOfData) {
				String grpName = parseName().toUpperCase();
				grpList.add(grpName);
				skipWhiteSpace();
				c = getCharAtIndex(0);
				if (c == Token.RIGTH_PARENTHESIS) {
					nextCharacter();
					break;
				} else if (c == Token.PIPE || c == Token.ET || c == Token.COMA) {
					nextCharacter();
				} else {
					throw new SASgmlException(
							"Caractère ["
									+ c
									+ "] inattendu à la position ["
									+ realIndex
									+ "] : l'élement ["
									+ grpName
									+ "] dans ce NAMEGROUP doit être suivi de ')' ou de ('&',',','|') !");
				}
				skipWhiteSpace();
			}
		}
		String name = parseName().toUpperCase();
		skipWhiteSpace();

		// attspecset ::= /* nil */ // cf. 31
		// | attspecset attspec
		//
		// attspec ::= NAME '=' value // cf. 32
		// | value
		Attributes attributes = new Attributes();
		int index = 0;
		boolean match = false;
		while (!match) {
			int charIndex = realIndex;
			c = getCharAtIndex(0);
			if (c == Token.GREAT_THAN) {
				if (grpList.isEmpty()) {
					sGMLHandler.startElement(name, attributes);
				} else {
					sGMLHandler.startElement(grpList, name, attributes);
				}
				nextCharacter();
				match = true;
			} else if (c == Token.SLASH) {
				nextCharacter();
				c = getCharAtIndex(0);
				if (c == Token.GREAT_THAN) {
					if (grpList.isEmpty()) {
						sGMLHandler.startElement(name, attributes);
						sGMLHandler.endElement(name);
					} else {
						sGMLHandler.startElement(grpList, name, attributes);
						sGMLHandler.endElement(grpList, name);
					}
					nextCharacter();
					match = true;
				} else {
					throw new SASgmlException("Caractère [" + c
							+ "] inattendu à la position [" + realIndex
							+ "] : l'élement [" + name
							+ "] doit se terminer par '/>' ou par '>' !");
				}
			} else {
				String attName = parseName().toUpperCase();
				skipWhiteSpace();
				c = getCharAtIndex(0);
				if (c == Token.EQUAL) {
					nextCharacter();
					skipWhiteSpace();
					String attValue = parseValue();
					attributes.put(attName.toUpperCase(),
							new Attribute(attName.toUpperCase(), attValue));
					skipWhiteSpace();
				} else {
					attributes.put(
							String.valueOf(index),
							new Attribute(String.valueOf(index), attName
									.toUpperCase()));
					skipWhiteSpace();
					index++;
				}
				if (charIndex == realIndex) {
					throw new SASgmlException("Caractère [" + c
							+ "] inattendu à la position [" + realIndex
							+ "] lors du parse de l'attribut [" + attName + "]");
				}
			}

		}
	}

	private void parseEndElementToken() throws SASgmlException {
		ArrayList<String> grpList = new ArrayList<String>();
		char c = getCharAtIndex(0);
		if (c == Token.LEFT_PARENTHESIS) {
			nextCharacter();
			skipWhiteSpace();
			while (!endOfData) {
				String grpName = parseName().toUpperCase();
				grpList.add(grpName);
				skipWhiteSpace();
				c = getCharAtIndex(0);
				if (c == Token.RIGTH_PARENTHESIS) {
					nextCharacter();
					break;
				} else if (c == Token.PIPE || c == Token.ET || c == Token.COMA) {
					nextCharacter();
				} else {
					throw new SASgmlException(
							"Caractère ["
									+ c
									+ "] inattendu à la position ["
									+ realIndex
									+ "] : l'élement ["
									+ grpName
									+ "] dans ce NAMEGROUP doit être suivi de ')' ou de ('&',',','|') !");
				}
				skipWhiteSpace();
			}
		}
		String name = parseName().toUpperCase();
		skipWhiteSpace();
		c = getCharAtIndex(0);
		if (c == Token.GREAT_THAN) {
			if (grpList.isEmpty()) {
				sGMLHandler.endElement(name);
			} else {
				sGMLHandler.endElement(grpList, name);
			}
			nextCharacters(1);
		} else {
			throw new SASgmlException("Caractère [" + c
					+ "] inattendu à la position [" + realIndex
					+ "] : l'élement [" + name + "] doit se terminer par '>' !");
		}
	}

	private void parsePIToken() throws SASgmlException {
		sGMLHandler.PI("", parseStringUnTillChar(Token.GREAT_THAN));
		nextCharacter();
	}

	private void parseCommentToken() throws SASgmlException {
		boolean match = false;
		startCapture();
		while (!match && !endOfData) {
			char c = getCharAtIndex(0);
			if (c == Token.MINUS) {
				nextCharacter();
				c = getCharAtIndex(0);
				if (c == Token.MINUS) {
					endCapture();
					sGMLHandler.comment(capture);
					nextCharacter();
					c = getCharAtIndex(0);
					if (c == Token.GREAT_THAN) {
						match = true;
						nextCharacter();
					} else {
						startCapture();
					}
				} else {
					capture(Token.MINUS);
				}
			} else {
				capture(c);
				nextCharacter();
			}
		}
	}

	private void parseMarkedSectionToken() throws SASgmlException {
		skipWhiteSpace();
		String decl = parseStringUnTillChar(Token.LEFT_SQUARE_BRACKET);
		nextCharacter();
		skipWhiteSpace();
		String data = parseStringUnTillString(Token.MARK_SECTION_END);
		sGMLHandler.markedSection(decl, data);
		nextCharacters(3);
	}

	public SGMLHandler getSGMLHandler() {
		return sGMLHandler;
	}

	public void setSGMLHandler(SGMLHandler handler) {
		sGMLHandler = handler;
		sGMLHandler.setSubDTDHandler(dTDHandler);
	}

}
