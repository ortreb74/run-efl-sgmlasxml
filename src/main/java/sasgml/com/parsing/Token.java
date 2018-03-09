package sasgml.com.parsing;

import java.util.ArrayList;

public class Token {
	public static final char LEFT_SQUARE_BRACKET = '[';
	public static final char RIGTH_SQUARE_BRACKET = ']';
	public static final char LEFT_PARENTHESIS = '(';
	public static final char RIGTH_PARENTHESIS = ')';
	public static final char LESS_THAN = '<';
	public static final char GREAT_THAN = '>';
	public static final char EQUAL = '=';
	public static final char QUOTE = '\'';
	public static final char DOUBLE_QUOTE = '"';
	public static final char MINUS = '-';
	public static final char DIEZ = '#';
	
	public static final char SLASH = '/';
	public static final char EXCLAMATION = '!';
	public static final char PLUS = '+';
	public static final char PIPE = '|';
	public static final char COMA = ',';
	public static final char ET = '&';
	public static final char ASTERIX = '*';
	public static final char QUID = '?';
	public static final char ESCAPE = ';';
	
	public static final char PERCENTAGE = '%';
	
	public static final String START_INCLUSION = "+(";
	public static final String START_EXCLUSION = "-(";
	
	public static final char UPPER_CASE_O = 'O';
		
	public static final String CDATA = "CDATA";
	public static final String SDATA = "SDATA";
	public static final String NDATA = "NDATA";
	public static final String PI = "PI";
	public static final String STARTTAG = "STARTTAG";
	public static final String ENDTAG = "ENDTAG";
	public static final String MS = "MS";
	public static final String MD = "MD";
	public static final String SYSTEM = "SYSTEM";
	public static final String PUBLIC = "PUBLIC";
	public static final String SUBDOC = "SUBDOC";
	
	public static final String DEFAULT = "DEFAULT";
	
	public static final String DIEZ_PCDATA = "#PCDATA";
	
	public static final String MARK_SECTION_END = "]]>";
	
	public static final String RIGTH_SQUARE_BRACKET_GREAT_THAN = "]>";
	public static final String DOCTYPE_START = "<!DOCTYPE";

	public static final String SUB_COMMENT = "--";
	public static final String INCLUDE = "INCLUDE";
	public static final String TEMP = "TEMP";
	public static final String RCDATA = "RCDATA";
	public static final String IGNORE = "IGNORE";
	public static final String DIEZNOTATION = "#NOTATION";
	public static final String NOTATION_START = "<!NOTATION";
	public static final String CHOICE_LIST = "CHOICE_LIST";
	public static final String ENTITY = "ENTITY";
	public static final String ENTITIES = "ENTITIES";
	public static final String ATTLIST = "ATTLIST";
	public static final String ELEMENT = "ELEMENT";
	public static final String DOCTYPE = "DOCTYPE";
	
	public static final String ID = "ID";
	public static final String IDREF = "IDREF";
	public static final String IDREFS = "IDREFS";
	public static final String NAME = "NAME";
	public static final String NAMES = "NAMES";
	public static final String NMTOKEN = "NMTOKEN";
	public static final String NMTOKENS = "NMTOKENS";
	public static final String NUMBER = "NUMBER";
	public static final String NUMBERS = "NUMBERS";
	public static final String NUTOKEN = "NUTOKEN";
	public static final String NUTOKENS = "NUTOKENS";
	public static final String NOTATION = "NOTATION";

	public static final Object REQUIRED = "REQUIRED";
	public static final Object IMPLIED = "IMPLIED";
	public static final Object CONREF = "CONREF";
	public static final Object CURRENT = "CURRENT";
	public static final Object FIXED = "FIXED";
	
	public static final ArrayList<String> TEXT_TAGS = new ArrayList<String>() {
		/**
		 * 
		 */
		private static final long serialVersionUID = 1L;

		{
			add("#PCDATA");
			add("CDATA");
			add("RCDATA");
		}
	};
	

}