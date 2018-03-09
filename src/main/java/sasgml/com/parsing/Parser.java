package sasgml.com.parsing;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.StringReader;
import java.nio.charset.Charset;
import java.util.LinkedList;
import java.util.List;

import sasgml.com.exception.SASgmlException;

public class Parser {

	protected static String encoding = "ISO-8859-1";

	protected final int BUFFER_LENGTH = 1024;
	protected final int PARSER_BUFFER_LENGTH = 10; // must be less than buffer
													// length

	protected int realIndex;
	protected int index;

	protected String capture;
	protected StringBuilder captureSB;

	protected int nbRemainingChar;

	protected boolean endOfBuffer;
	protected boolean endOfData;

	protected BufferedReader buffer;

	protected List<char[]> charactersList;

	public Parser() {
		charactersList = new LinkedList<char[]>();
		realIndex = 0;
		index = 0;
		nbRemainingChar = 0;
		endOfBuffer = false;
		endOfData = false;
	}

	protected void load(File lFile) throws SASgmlException, IOException {
		buffer = new BufferedReader(new InputStreamReader(new FileInputStream(
				lFile), Charset.forName(encoding)));
		initCharacters();
	}

	protected void load(InputStream is) throws SASgmlException, IOException {
		buffer = new BufferedReader(new InputStreamReader(is));
		initCharacters();
	}

	protected void load(String str) throws IOException {
		buffer = new BufferedReader(new StringReader(str));
		initCharacters();
	}

	protected void close() throws IOException {
		buffer.close();
	}

	protected void initCharacters() throws IOException {
		char[] characters = new char[BUFFER_LENGTH];
		int nbCharacRead = buffer.read(characters, 0, BUFFER_LENGTH);
		if (nbCharacRead == -1) {
			endOfBuffer = true;
		} else {
			nbRemainingChar = nbCharacRead;
		}
		addCharacters(characters);
	}

	protected void addCharacters(char[] characters) {
		charactersList.add(characters);
	}

	protected void removeCharacters(int index) {
		charactersList.remove(index);
	}

	protected void nextCharacter() {
		if (endOfData) {
			return;
		} else if (endOfBuffer) {
			if (nbRemainingChar > 0) {
				realIndex++;
				index++;
				nbRemainingChar--;
				endOfData = (nbRemainingChar == 0);
			} else {
				return;
			}
		} else {
			realIndex++;
			if (PARSER_BUFFER_LENGTH < nbRemainingChar) {
				index++;
				nbRemainingChar--;
			} else {
				char[] characters = new char[BUFFER_LENGTH];
				try {
					int nbCharacRead = buffer
							.read(characters, 0, BUFFER_LENGTH);
					if (nbCharacRead != -1) {
						nbRemainingChar += nbCharacRead;
					}
					if (nbCharacRead < BUFFER_LENGTH) {
						endOfBuffer = true;
					}
					addCharacters(characters);
					nextCharacter();
				} catch (IOException e) {
					endOfData = true;
					endOfBuffer = true;
				}
			}
		}
	}

	protected void nextCharacters(int n) {
		for (int i = 0; i < n; i++) {
			nextCharacter();
		}
	}

	protected char getCharAtIndex(int i) {
		if (endOfData) {
			return '\0';
		} else {
			int cIndex = index + i;
			double quotient = new Double(cIndex) / new Double(BUFFER_LENGTH);
			int charactersListIndex = (int) quotient;
			int charactersIndex = (int) (cIndex - BUFFER_LENGTH
					* charactersListIndex);
			return charactersList.get(charactersListIndex)[charactersIndex];
		}
	}

	protected void startCapture() {
		captureSB = new StringBuilder();
	}

	protected void capture(char c) {
		captureSB.append(c);
	}

	protected void endCapture() {
		capture = captureSB.toString();
		captureSB = null;
		if (index > BUFFER_LENGTH) {
			index = index - BUFFER_LENGTH;
			charactersList.remove(0);
		}
	}

	protected String parseStringWithLength(int length) {
		startCapture();
		for (int i = 0; i < length; i++) {
			capture(getCharAtIndex(0));
			nextCharacter();
		}
		endCapture();
		return capture;
	}

	protected String parseName() throws SASgmlException {
		// NAME ::= letter
		// | NAME & letter
		// | NAME & digit
		// | NAME & othernamech
		//

		boolean match = false;
		startCapture();

		while (!match && !endOfData) {
			char c = getCharAtIndex(0);
			if (isLetter(c)) {
				nextCharacter();
				capture(c);
			} else if (isDigit(c)) {
				nextCharacter();
				capture(c);
			} else if (isOtherMatch(c)) {
				nextCharacter();
				capture(c);
			} else {
				match = true;
			}
		}

		endCapture();

		if (capture.length() == 0) {
			throw new SASgmlException(
					"La chaine de type [NAME] attendue en position ["
							+ realIndex + "] doit être non vide.");
		} else {
			return capture;
		}
	}

	protected String parseNumber() throws SASgmlException {
		// NAME ::= letter
		// | NAME & letter
		// | NAME & digit
		// | NAME & othernamech
		//

		boolean match = false;
		startCapture();

		while (!match && !endOfData) {
			char c = getCharAtIndex(0);
			if (isDigit(c)) {
				capture(c);
				nextCharacter();
			} else {
				match = true;
			}
		}

		endCapture();

		if (capture.length() == 0) {
			throw new SASgmlException(
					"La chaine de type [NUMBER] attendue en position ["
							+ realIndex + "] doit être non vide.");
		} else {
			return capture;
		}

	}

	protected String parseNumtoken() throws SASgmlException {
		// NAME ::= letter
		// | NAME & letter
		// | NAME & digit
		// | NAME & othernamech
		//

		boolean match = false;
		startCapture();

		while (!match && !endOfData) {
			char c = getCharAtIndex(0);
			if (isLetter(c) || isDigit(c) || isOtherMatch(c)) {
				nextCharacter();
				capture(c);
			} else {
				match = true;
			}
		}

		endCapture();

		if (capture.length() == 0) {
			throw new SASgmlException(
					"La chaine de type [NUMTOKEN] attendue en position ["
							+ realIndex + "] doit être non vide.");
		} else {
			return capture;
		}

	}

	protected String parseNmtoken() throws SASgmlException {
		// Nmtoken ::= (NameChar)+

		boolean match = false;
		startCapture();

		while (!match && !endOfData) {
			char c = getCharAtIndex(0);
			if (isNameChar(c)) {
				nextCharacter();
				capture(c);
			} else {
				match = true;
			}
		}

		endCapture();

		if (capture.length() == 0) {
			throw new SASgmlException(
					"La chaine de type [NMTOKEN] attendue en position ["
							+ realIndex + "] doit être non vide.");
		} else {
			return capture;
		}

	}

	protected String parseNutoken() throws SASgmlException {
		// Nutoken ::= Number (NameChar)*

		boolean match = false;
		startCapture();

		char c = getCharAtIndex(0);
		if (isDigit(c)) {
			capture(c);
			nextCharacter();
			while (!match && !endOfData) {
				c = getCharAtIndex(0);
				if (isLetter(c) || isDigit(c) || isOtherMatch(c)) {
					nextCharacter();
					capture(c);
				} else {
					match = true;
				}
			}
		} else {
			throw new SASgmlException(
					"Erreur : la chaine de type [NUTOKEN] attendue en position ["
							+ realIndex + "] doit commencer par un digit.");
		}

		endCapture();

		if (capture.length() == 0) {
			throw new SASgmlException(
					"La chaine de type [NUTOKEN] attendue en position ["
							+ realIndex + "] doit être non vide.");
		} else {
			return capture;
		}

	}

	protected String parseStringUnTillString(String end) {
		boolean match = false;
		startCapture();

		while (!match && !endOfData) {
			if (hasNext(end)) {
				match = true;
			} else {
				capture(getCharAtIndex(0));
				nextCharacter();
			}
		}
		endCapture();
		return capture;
	}

	protected String parseValue() throws SASgmlException {
		// value ::= LITERAL // Cf. 33
		// | NAME
		// | NUMBER
		// | NUMTOKEN

		char c = getCharAtIndex(0);
		if (c == Token.QUOTE || c == Token.DOUBLE_QUOTE) {
			return parseLiteral();
		} else {
			return parseNameToken();
		}
	}

	protected String parseNameToken() throws SASgmlException {
		boolean match = false;
		startCapture();

		while (!match && !endOfData) {
			char c = getCharAtIndex(0);
			if (isLetter(c) || isDigit(c) || isOtherMatch(c)) {
				nextCharacter();
				capture(c);
			} else {
				match = true;
			}
		}

		endCapture();

		if (capture.length() == 0) {
			throw new SASgmlException(
					"La chaine de type [NAMETOKEN] attendue en position ["
							+ realIndex + "] doit être non vide.");
		} else {
			return capture;
		}

	}

	protected String parseLiteral() throws SASgmlException {
		// LITERAL ::= "'" & STRING & "'" // Cf. 66, 76, 34
		// | '"' & STRING & '"'
		char toStopCapture = getCharAtIndex(0);

		if (toStopCapture == Token.QUOTE || toStopCapture == Token.DOUBLE_QUOTE) {
			nextCharacter();
			String result = parseStringUnTillChar(toStopCapture);
			nextCharacter();
			return result;
		} else {
			throw new SASgmlException(
					"La chaine de type [LITERAL] n'est pas valide : elle doit commencer par un \" ou un '");
		}
	}

	protected String parseStringUnTillChar(char c) {
		boolean match = false;
		startCapture();

		while (!match && !endOfData) {
			if (getCharAtIndex(0) == c) {
				match = true;
			} else {
				capture(getCharAtIndex(0));
				nextCharacter();
			}
		}
		endCapture();
		return capture;
	}

	protected String parseStringUnTillCharAndIgnoreLocalComment(char toStopChar) throws SASgmlException {
		boolean match = false;
		StringBuilder lStringBuilder = new StringBuilder();
		
		while (!match && !endOfData) {
			char c = getCharAtIndex(0);
			if (c == Token.QUOTE || c == Token.DOUBLE_QUOTE) {
				lStringBuilder.append(c + parseLiteral() + c);
			} else if (hasNext("--")) {
				skipWhiteSpaceAndLocalComment();
			} else if (c == toStopChar) {
				match = true;
			} else {
				lStringBuilder.append(c);
				nextCharacter();
			}
		}
		
		return lStringBuilder.toString();
	}

	protected String parseStringUntillEnd() {
		startCapture();
		while (!endOfData) {
			capture(getCharAtIndex(0));
			nextCharacter();
		}
		endCapture();
		return capture;
	}

	protected void skipLocalComment() throws SASgmlException {
		if (hasNext("--")) {
			nextCharacters(2);
			while (!hasNext("--") && !endOfData) {
				nextCharacter();
			}
			nextCharacters(2);
		}
	}

	protected void skipWhiteSpaceAndLocalComment() throws SASgmlException {
		skipWhiteSpace();
		skipLocalComment();
		skipWhiteSpace();
	}

	protected void skipWhiteSpace() {
		char c = getCharAtIndex(0);
		while (c == ' ' || c == '\n' || c == '\t' || c == '\r') {
			nextCharacter();
			c = getCharAtIndex(0);
		}
	}

	protected boolean hasNext(char c) {
		return getCharAtIndex(0) == c;
	}

	protected boolean hasNext(char[] chars) {
		for (int i = 0; i < chars.length; i++) {
			if (getCharAtIndex(i) != chars[i]) {
				return false;
			}
		}
		return true;
	}

	protected boolean hasNextIgnoreCase(char[] chars) {
		for (int i = 0; i < chars.length; i++) {
			if (Character.toLowerCase(getCharAtIndex(i)) != chars[i]) {
				return false;
			}
		}
		return true;
	}

	protected boolean hasNext(String value) {
		return hasNext(value.toCharArray());
	}

	protected boolean hasNextIgnoreCase(String value) {
		return hasNextIgnoreCase(value.toLowerCase().toCharArray());
	}

	protected static boolean isLetter(char ch) {
		return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z');
	}

	protected static boolean isNameStartChar(char ch) {
		return (ch >= 'a' && ch <= 'z') || (ch >= 'A' && ch <= 'Z')
				|| (ch > 0xC0 && ch <= 0xD6) || (ch > 0xD8 && ch <= 0xF6)
				|| (ch > 0xF8 && ch <= 0x2FF) || (ch > 0x370 && ch <= 0x37D)
				|| (ch > 0x37F && ch <= 0x1FFF)
				|| (ch > 0x200C && ch <= 0x200D)
				|| (ch > 0x2070 && ch <= 0x218F)
				|| (ch > 0x2C00 && ch <= 0x2FEF)
				|| (ch > 0x3001 && ch <= 0xD7FF)
				|| (ch > 0xF900 && ch <= 0xFDCF)
				|| (ch > 0xFDF0 && ch <= 0xFFFD)
				|| (ch > 0x10000 && ch <= 0xEFFFF) || ch == ':' || ch == '_';
	}

	protected static boolean isNameChar(char ch) {
		return isNameStartChar(ch) || ch == '-' || ch == '.' || isDigit(ch)
				|| ch == 0xB7 || (ch > 0x0300 && ch <= 0x036F)
				|| (ch > 0x203F && ch <= 0x2040);
	}

	protected static boolean isDigit(char ch) {
		return (ch >= '0' && ch <= '9');
	}

	protected static boolean isOtherMatch(char ch) {
		//|| ch == '/'
		return (ch == '-' || ch == '_' || ch == '.' || ch == ':' 
				|| ch == '{' || ch == '}');
	}

	protected int getcRealIndex() {
		return realIndex;
	}

	protected int getcIndex() {
		return index;
	}

	public int getNbRemainingChar() {
		return nbRemainingChar;
	}

	public boolean isEndOfFile() {
		return endOfBuffer;
	}

	public void setcRealIndex(int cRealIndex) {
		this.realIndex = cRealIndex;
	}

	public void setcIndex(int cIndex) {
		this.index = cIndex;
	}

	public void setNbRemainingChar(int nbRemainingChar) {
		this.nbRemainingChar = nbRemainingChar;
	}

	public void setEndOfFile(boolean endOfFile) {
		this.endOfBuffer = endOfFile;
	}

	public static boolean isValidName(String value) {
		try {
			Parser lParser = new Parser();
			lParser.load(value);
			boolean result = lParser.parseName().equals(value);
			lParser.close();
			return result;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (SASgmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	public static boolean isValidNames(String value) {
		try {
			Parser lParser = new Parser();
			lParser.load(value);
			lParser.skipWhiteSpace();
			while (!lParser.endOfBuffer) {
				lParser.parseName();
				lParser.skipWhiteSpace();
			}
			lParser.close();
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (SASgmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	public static boolean isValidNumber(String value) {
		// try {
		// Parser lParser = new Parser();
		// lParser.Load(value);
		// boolean result = lParser.parseNumber().equals(value);
		// lParser.Close();
		// return result;
		// } catch (IOException | SASgmlException e) {
		// e.printStackTrace();
		// return false;
		// }

		return value.matches("[0-9]+");
	}

	public static boolean isValidNumbers(String value) {
		// try {
		// Parser lParser = new Parser();
		// lParser.Load(value);
		// boolean result = lParser.parseNumber().equals(value);
		// lParser.Close();
		// return result;
		// } catch (IOException | SASgmlException e) {
		// e.printStackTrace();
		// return false;
		// }

		return value.matches("([0-9]+\\s*)+");
	}

	public static boolean isValidNmtoken(String value) {
		try {
			Parser lParser = new Parser();
			lParser.load(value);
			boolean result = lParser.parseNmtoken().equals(value);
			lParser.close();
			return result;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (SASgmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	public static boolean isValidNmtokens(String value) {
		try {
			Parser lParser = new Parser();
			lParser.load(value);
			lParser.skipWhiteSpace();
			while (!lParser.endOfBuffer) {
				lParser.parseNmtoken();
				lParser.skipWhiteSpace();
			}
			lParser.close();
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (SASgmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	public static boolean isValidNutoken(String value) {
		try {
			Parser lParser = new Parser();
			lParser.load(value);
			boolean result = lParser.parseNutoken().equals(value);
			lParser.close();
			return result;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (SASgmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}

	public static boolean isValidNutokens(String value) {
		try {
			Parser lParser = new Parser();
			lParser.load(value);
			lParser.skipWhiteSpace();
			while (!lParser.endOfBuffer) {
				lParser.parseNutoken();
				lParser.skipWhiteSpace();
			}
			lParser.close();
			return true;
		} catch (IOException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		} catch (SASgmlException e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
			return false;
		}
	}
}
