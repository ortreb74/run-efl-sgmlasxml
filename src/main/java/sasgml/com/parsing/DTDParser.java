package sasgml.com.parsing;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;

import sasgml.com.exception.SASgmlException;
import sasgml.com.handler.DTDHandler;
import sasgml.com.model.AttList;
import sasgml.com.model.Element;
import sasgml.com.model.Entity;
import sasgml.com.model.Notation;

public class DTDParser extends Parser {

	public DTDParser() {
		setDTDHandler(new DTDHandler());
	}

	protected DTDHandler dTDHandler;
	protected String fileName;

	public void parse(String str) throws SASgmlException, IOException {
		load(str);
		parseToken();
		close();
	}

	public void parse(InputStream is) throws IOException, SASgmlException {
		load(is);
		parseToken();
		close();
	}

	public void parse(File pFile) throws IOException, SASgmlException {
		// System.out.println("************" + pFile.getAbsolutePath());

		if (pFile.exists()) {
			load(pFile);
			fileName = pFile.getName();
			parseToken();
		}
	}

	public void parseToken() throws SASgmlException {
		skipWhiteSpace();
		if (hasNextIgnoreCase(Token.DOCTYPE_START)) {
			nextCharacters(9);
			parseDocTypeToken();
		} else {
			parseDocTypeContentToken();
		}
		skipWhiteSpace();
	}

	private void parseDocTypeContentToken() throws SASgmlException {
		while (!endOfData) {
			char c = getCharAtIndex(0);
			if (c == Token.PERCENTAGE) {
				parseIncludeToken();
			} else if (c == Token.LESS_THAN) {
				nextCharacter();
				c = getCharAtIndex(0);
				if (c == Token.EXCLAMATION) {
					nextCharacter();
					c = getCharAtIndex(0);
					if (c == Token.MINUS) {
						nextCharacter();
						c = getCharAtIndex(0);
						if (c == Token.MINUS) {
							// COMMENT
							nextCharacter();
							parseCommentToken();
						} else {
							throw new SASgmlException("Caractère ["
									+ getCharAtIndex(0)
									+ "] inattendu à la position [" + realIndex
									+ "]");
						}
					} else if (hasNextIgnoreCase(Token.ENTITY)) {
						// PI
						nextCharacters(6);
						parseEntityToken();
					} else if (hasNextIgnoreCase(Token.NOTATION)) {
						// COMMENT
						nextCharacters(8);
						parseNotationToken();
					} else if (hasNextIgnoreCase(Token.ATTLIST)) {
						// CDATA
						nextCharacters(7);
						parseAttListToken();
					} else if (hasNextIgnoreCase(Token.ELEMENT)) {
						// ELEMENT
						nextCharacters(7);
						parseElementToken();
					} else {
						throw new SASgmlException("Caractère ["
								+ getCharAtIndex(0)
								+ "] inattendu à la position [" + realIndex
								+ "]");
					}
				} else {
					throw new SASgmlException("Caract�re [" + getCharAtIndex(0)
							+ "] inattendu � la position [" + realIndex
							+ "] pour la d�finition de la DTD !");
				}
			} else if (c == Token.RIGTH_SQUARE_BRACKET) {
				break;
			} else {
				throw new SASgmlException("Caract�re [" + getCharAtIndex(0)
						+ "] inattendu � la position [" + realIndex
						+ "] pour la d�finition de la DTD !");
			}
		}
	}

	private void parseIncludeToken() throws SASgmlException {
		skipWhiteSpace();
		dTDHandler.includeEntity(parseStringUnTillChar(Token.ESCAPE));
		nextCharacter();
		skipWhiteSpace();
	}

	private void parseElementToken() throws SASgmlException {
		dTDHandler
				.element(new Element(parseStringUnTillCharAndIgnoreLocalComment(Token.GREAT_THAN),fileName));
		nextCharacter();
		skipWhiteSpaceAndLocalComment();
	}

	private void parseAttListToken() throws SASgmlException {
		dTDHandler
				.attlist(new AttList(parseStringUnTillCharAndIgnoreLocalComment(Token.GREAT_THAN),fileName));
		nextCharacter();
		skipWhiteSpaceAndLocalComment();
	}

	private void parseEntityToken() throws SASgmlException {

		skipWhiteSpaceAndLocalComment();

		String eName = "";
		char c = getCharAtIndex(0);
		if (c == Token.PERCENTAGE) {
			nextCharacter();
			skipWhiteSpaceAndLocalComment();
			eName = Token.PERCENTAGE + parseName();
			nextCharacter();
		} else if (c == Token.DIEZ) {
			nextCharacter();
			if (hasNext(Token.DEFAULT)) {
				eName = Token.DIEZ + Token.DEFAULT;
				nextCharacters(7);
			} else {
				throw new SASgmlException("Caract�re [" + getCharAtIndex(0)
						+ "] inattendu � la position [" + realIndex
						+ "] pour la d�claration de l'entit� !");
			}
		} else {
			eName = parseName();
		}

		// if (eName.startsWith("%")) {
		// System.out.println(eName);
		//
		// }

		skipWhiteSpaceAndLocalComment();

		boolean match = false;
		StringBuilder lStringBuilder = new StringBuilder();
		while (!match && !endOfData) {
			c = getCharAtIndex(0);
			if (c == Token.QUOTE || c == Token.DOUBLE_QUOTE) {
				lStringBuilder.append(c + parseLiteral() + c);
			} else if (hasNext("--")) {
				skipWhiteSpaceAndLocalComment();
			} else if (c == Token.GREAT_THAN) {
				match = true;
			} else {
				lStringBuilder.append(c);
				nextCharacter();
			}
		}

		String eContentDecl = lStringBuilder.toString();

//		if (eName.startsWith("%")) {
//			System.out.println(eName + "\n" + eContentDecl);
//		}

		dTDHandler.entity(new Entity(eName, eContentDecl,fileName));
		nextCharacter();

		skipWhiteSpaceAndLocalComment();

	}

	private void parseNotationToken() throws SASgmlException {

		skipWhiteSpaceAndLocalComment();

		String eContentDecl = parseStringUnTillCharAndIgnoreLocalComment(Token.GREAT_THAN);

		dTDHandler.notation(new Notation(eContentDecl));
		nextCharacter();

		skipWhiteSpaceAndLocalComment();

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
					dTDHandler.comment(capture);
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
		skipWhiteSpace();
	}

	private void parseDocTypeToken() throws SASgmlException {
		skipWhiteSpace();
		String dtdName = parseName().toUpperCase();
		String dtdType = "";
		String sysid = "";
		String pubid = "";
		skipWhiteSpace();
		if (hasNextIgnoreCase(Token.SYSTEM)) {
			nextCharacters(6);
			dtdType = Token.SYSTEM;
			skipWhiteSpace();
			char c = getCharAtIndex(0);
			if (c == Token.QUOTE || c == Token.DOUBLE_QUOTE) {
				sysid = parseLiteral();
			}
		} else if (hasNextIgnoreCase(Token.PUBLIC)) {
			nextCharacters(6);
			dtdType = Token.PUBLIC;
			skipWhiteSpace();
			char c = getCharAtIndex(0);
			if (c == Token.QUOTE || c == Token.DOUBLE_QUOTE) {
				pubid = parseLiteral();
			}
			skipWhiteSpace();
			c = getCharAtIndex(0);
			if (c == Token.QUOTE || c == Token.DOUBLE_QUOTE) {
				sysid = parseLiteral();
			}
		}

		skipWhiteSpace();

		char c = getCharAtIndex(0);
		if (c == Token.LEFT_SQUARE_BRACKET) {
			nextCharacter();
			skipWhiteSpace();
			parseDocTypeContentToken();
			c = getCharAtIndex(0);
			if (c == Token.RIGTH_SQUARE_BRACKET) {
				nextCharacter();
				skipWhiteSpace();
				c = getCharAtIndex(0);
				if (c == Token.GREAT_THAN) {
					nextCharacter();
				} else {
					throw new SASgmlException("Caract�re [" + getCharAtIndex(0)
							+ "] inattendu � la position [" + realIndex
							+ "] pour la d�claration du DOCTYPE !");
				}
			} else {
				throw new SASgmlException("Caract�re [" + getCharAtIndex(0)
						+ "] inattendu � la position [" + realIndex
						+ "] pour la d�claration du DOCTYPE !");
			}
		} else if (c == Token.GREAT_THAN) {
			nextCharacter();
		} else {
			throw new SASgmlException("Caract�re [" + getCharAtIndex(0)
					+ "] inattendu � la position [" + realIndex
					+ "] pour la d�claration du DOCTYPE !");
		}

		dTDHandler.doctype(dtdName, dtdType, sysid, pubid);
	}

	public DTDHandler getDTDHandler() {
		return dTDHandler;
	}

	public void setDTDHandler(DTDHandler handler) {
		this.dTDHandler = handler;
	}

}
