package sasgml.com.parsing;

import java.io.IOException;
import java.util.regex.Pattern;

import sasgml.com.exception.SASgmlException;
import sasgml.com.model.ElementExpression;
import sasgml.com.model.ElementModel;
import sasgml.com.model.ElementOccurenceTypeEnum;
import sasgml.com.model.ElementOperationTypeEnum;

public class ElementModelParser extends Parser {

	private ElementModel cDtdElementmodel = new ElementModel();

	public void parse(String str) throws IOException, SASgmlException {
		if (Pattern.compile("[%&][\\w-_\\.]+;?").matcher(str).find()) {
			return;
		}
		load(str);
		parseToken();
		cDtdElementmodel.loadInclusionAndExclusion();
		cDtdElementmodel.getContent().loadRule();
		close();
		// cDtdElementmodel.getContent().getEntry()
		// .print(0,new ArrayList<ElementItem>());
	}

	public void parseToken() throws SASgmlException {
		while (!endOfData) {
			skipWhiteSpace();
			switch (getCharAtIndex(0)) {
			case Token.PLUS:
				nextCharacter();
				cDtdElementmodel
						.setPlusExpression(parseStringUnTillChar(Token.RIGTH_PARENTHESIS)
								+ Token.RIGTH_PARENTHESIS);
				nextCharacter();
				skipWhiteSpaceAndLocalComment();
				break;
			case Token.MINUS:
				nextCharacter();
				cDtdElementmodel
						.setMinusExpression(parseStringUnTillChar(Token.RIGTH_PARENTHESIS)
								+ Token.RIGTH_PARENTHESIS);
				nextCharacter();
				skipWhiteSpaceAndLocalComment();
				break;
			default:
				cDtdElementmodel.setContent(parseExpressionToken());
				break;
			}
		}
	}

	private ElementExpression parseExpressionToken() throws SASgmlException {
		boolean match = false;

		ElementExpression cDTDElementExpression = new ElementExpression();
		cDTDElementExpression.setOpType(ElementOperationTypeEnum.EXPRESSION);
		ElementExpression lDTDElementExpression;

		while (!endOfData && !match) {
			skipWhiteSpace();
			switch (getCharAtIndex(0)) {
			case Token.LEFT_PARENTHESIS:
				lDTDElementExpression = new ElementExpression();
				lDTDElementExpression
						.setOpType(ElementOperationTypeEnum.GROUP);
				cDTDElementExpression.addChild(lDTDElementExpression);
				cDTDElementExpression = lDTDElementExpression;
				nextCharacter();
				break;
			case Token.RIGTH_PARENTHESIS:
				cDTDElementExpression = cDTDElementExpression.getParent();
				nextCharacter();
				break;
			case Token.COMA:
				cDTDElementExpression = cDTDElementExpression.getParent();
				cDTDElementExpression
						.setOpType(ElementOperationTypeEnum.SEQUENCE);
				nextCharacter();
				break;
			case Token.PIPE:
				cDTDElementExpression = cDTDElementExpression.getParent();
				cDTDElementExpression
						.setOpType(ElementOperationTypeEnum.CHOICE);
				nextCharacter();
				break;
			case Token.ET:
				cDTDElementExpression = cDTDElementExpression.getParent();
				cDTDElementExpression
						.setOpType(ElementOperationTypeEnum.AND);
				nextCharacter();
				break;
			case Token.ASTERIX:
			case Token.PLUS:
			case Token.MINUS:
			case Token.QUID:
				if (hasNext(Token.START_INCLUSION) || hasNext(Token.START_EXCLUSION)) {
					match = true;
				} else if (hasNext(Token.SUB_COMMENT)) {
					skipLocalComment();
				} else {
					cDTDElementExpression.setOcType(ElementOccurenceTypeEnum
							.getValue(Character.toString(getCharAtIndex(0))));
					nextCharacter();
				}
				break;
			default:
				if (cDTDElementExpression.getOpType() == ElementOperationTypeEnum.EXPRESSION) {
					lDTDElementExpression = new ElementExpression();
					lDTDElementExpression
							.setOpType(ElementOperationTypeEnum.GROUP);
					cDTDElementExpression.addChild(lDTDElementExpression);
					cDTDElementExpression = lDTDElementExpression;
				}
				if (hasNext(Token.DIEZ_PCDATA)) {
					lDTDElementExpression = new ElementExpression();
					lDTDElementExpression.setName(Token.DIEZ_PCDATA);
					cDTDElementExpression.addChild(lDTDElementExpression);
					cDTDElementExpression = lDTDElementExpression;
					nextCharacters(7);
					break;
				} else {
					lDTDElementExpression = new ElementExpression();
					lDTDElementExpression.setName(parseName());
					cDTDElementExpression.addChild(lDTDElementExpression);
					cDTDElementExpression = lDTDElementExpression;
					break;
				}
			}
			skipWhiteSpace();
		}

		while (cDTDElementExpression.getParent() != null) {
			cDTDElementExpression = cDTDElementExpression.getParent();
		}
		
		skipWhiteSpace();
		return cDTDElementExpression;
	}

	public ElementModel getDtdElementModel() {
		return cDtdElementmodel;
	}

	public void setDtdElementDef(ElementModel dTDElementDef) {
		this.cDtdElementmodel = dTDElementDef;
	}

}
