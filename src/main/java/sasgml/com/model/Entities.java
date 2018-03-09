package sasgml.com.model;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.LinkedHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import sasgml.com.catalog.CatalogManager;
import sasgml.com.exception.SASgmlException;
import sasgml.com.handler.DTDHandler;
import sasgml.com.log.LogManager;
import sasgml.com.parsing.DTDParser;

public class Entities extends LinkedHashMap<String, Entity> {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	private static Entities XMLDTDEntities = null;

	private static Pattern ParametricEntities = Pattern
			.compile("[%&]\\w([-_.]*\\w)*;?");

	private static Pattern ReferenceEntities = Pattern.compile("&[^\\s&;]+;");

	/*
	 * TODO entity reference ";" can be omitted if there is space after or
	 * record end or start ... e.g <, > ,<? ..
	 */

	private HashSet<String> memory = new HashSet<String>();

//	private CharsetEncoder encoder = Charset.forName("iso-8859-1").newEncoder();

	public void arrange() throws IOException, SASgmlException {
		ArrayList<String> upperCaseKeys = new ArrayList<String>();
		for (String cName : keySet()) {
			if (!containsKey(cName.toUpperCase())) {
				upperCaseKeys.add(cName);
			}
		}

		for (String cName : upperCaseKeys) {
			put(cName.toUpperCase(), get(cName));
		}

		for (String cName : keySet()) {
			Entity cDTDEntity = get(cName);
			arrange(cDTDEntity);
//			if (cDTDEntity.getModeEnum() == ModeEnum.SDATA
//					&& !cDTDEntity.getName().startsWith("%")
//					&& cDTDEntity.getValue().length() == 1) {
//				if (!encoder.canEncode(cDTDEntity.getValue().charAt(0))) {
//					System.out.println("<Entity name=\""
//							+ cDTDEntity.getName()
//							+ "\" value=\""
//							+ String.format("%04x", (int) cDTDEntity.getValue()
//									.charAt(0)) + "\"/>");
//				}
//			}
		}

	}

	public void putAll(Entities pDTDEntities) {
		for (String cDTDEntityName : pDTDEntities.keySet()) {
			if (!containsKey(cDTDEntityName)) {
				put(cDTDEntityName, pDTDEntities.get(cDTDEntityName));
			}
		}
	}

	// Attention au cas d'entity avec la casse : exemple Prime et prime

	public void arrange(Entity pDtdEntity) throws IOException,
			SASgmlException {
		if (!memory.contains(pDtdEntity.getName())) {

			String rValue = "";
			String lValue = pDtdEntity.getTextContent();
			Matcher mEntity = ParametricEntities.matcher(lValue);

			int start = 0;
			while (mEntity.find()) {
				String cEntityRefName = lValue
						.substring(mEntity.start(), mEntity.end())
						.replace(";", "").replace('&', '%');

				boolean isSolved = false;

				if (containsKey(cEntityRefName)) {
					Entity refEntity = get(cEntityRefName);
					arrange(refEntity);
					rValue = rValue + lValue.substring(start, mEntity.start())
							+ refEntity.getValue();
					isSolved = true;
				} else if (containsKey(cEntityRefName.substring(1))) {
					// cas & sdata
					Entity refEntity = get(cEntityRefName.substring(1));
					arrange(refEntity);
					rValue = rValue + lValue.substring(start, mEntity.start())
							+ refEntity.getValue();
					isSolved = true;
				} else if (XMLDTDEntities != null) {
					if (XMLDTDEntities.containsKey(cEntityRefName)) {
						Entity refEntity = XMLDTDEntities
								.get(cEntityRefName);
						rValue = rValue
								+ lValue.substring(start, mEntity.start())
								+ refEntity.getValue();
						isSolved = true;
					} else if (XMLDTDEntities.containsKey(cEntityRefName
							.substring(1))) {
						// cas & sdata
						Entity refEntity = XMLDTDEntities.get(cEntityRefName
								.substring(1));
						rValue = rValue
								+ lValue.substring(start, mEntity.start())
								+ refEntity.getValue();
						isSolved = true;
					}
				}

				if (!isSolved) {
					cEntityRefName = cEntityRefName.toUpperCase();
					if (containsKey(cEntityRefName)) {
						Entity refEntity = get(cEntityRefName);
						arrange(refEntity);
						rValue = rValue
								+ lValue.substring(start, mEntity.start())
								+ refEntity.getValue();
						isSolved = true;
					} else if (containsKey(cEntityRefName.substring(1))) {
						// cas & sdata
						Entity refEntity = get(cEntityRefName.substring(1));
						arrange(refEntity);
						rValue = rValue
								+ lValue.substring(start, mEntity.start())
								+ refEntity.getValue();
						isSolved = true;
					} else if (XMLDTDEntities != null) {
						if (XMLDTDEntities.containsKey(cEntityRefName)) {
							Entity refEntity = XMLDTDEntities
									.get(cEntityRefName);
							rValue = rValue
									+ lValue.substring(start, mEntity.start())
									+ refEntity.getValue();
							isSolved = true;
						} else if (XMLDTDEntities.containsKey(cEntityRefName
								.substring(1))) {
							// cas & sdata
							Entity refEntity = XMLDTDEntities
									.get(cEntityRefName.substring(1));
							rValue = rValue
									+ lValue.substring(start, mEntity.start())
									+ refEntity.getValue();
							isSolved = true;
						}
					}
				}

				if (!isSolved) {
					rValue = rValue + lValue.substring(start, mEntity.end());
					// System.out.println("Alerte : entité ["
					// + cEntityRefName + "] introuvable !");
				}

				start = mEntity.end();

			}

			rValue += lValue.substring(start);
			pDtdEntity.setTextContent(rValue);

			pDtdEntity.parseTextContent();

		}
	}

	public String getRealString(String pString) {
		String rValue = "";
		String lValue = pString;
		Matcher mEntity = ParametricEntities.matcher(lValue);

		int start = 0;
		while (mEntity.find()) {
			String cEntityRefName = lValue
					.substring(mEntity.start(), mEntity.end()).replace(";", "")
					.replace('&', '%');
			if (containsKey(cEntityRefName)) {
				Entity refEntity = get(cEntityRefName);
				rValue = rValue + lValue.substring(start, mEntity.start())
						+ refEntity.getValue();
			} else if (containsKey(cEntityRefName.toUpperCase())) {
				Entity refEntity = get(cEntityRefName.toUpperCase());
				rValue = rValue + lValue.substring(start, mEntity.start())
						+ refEntity.getTextContent();
			} else {
				LogManager.writeWarning("Entité [" + cEntityRefName
						+ "] introuvable !");
				rValue = rValue + lValue.substring(start, mEntity.end());
			}
			start = mEntity.end();
		}

		rValue += lValue.substring(start);

		return rValue;
	}

	public String decodeString(String s) {
		StringBuilder sb = new StringBuilder();
		Matcher m = ReferenceEntities.matcher(s);
		int start = 0;

		while (m.find()) {
			String cEntityRefName = s.substring(m.start() + 1, m.end() - 1);
			boolean isSolved = false;

			if (containsKey(cEntityRefName)) {
				sb.append(s.substring(start, m.start()));
				sb.append(get(cEntityRefName).getValue());
				isSolved = true;
			} else if (XMLDTDEntities != null) {
				if (XMLDTDEntities.containsKey(cEntityRefName)) {
					sb.append(s.substring(start, m.start()));
					sb.append(XMLDTDEntities.get(cEntityRefName)
							.getTextContent());
					isSolved = true;
				}
			}

			if (!isSolved) {
				cEntityRefName = cEntityRefName.toUpperCase();
				if (containsKey(cEntityRefName)) {
					sb.append(s.substring(start, m.start()));
					sb.append(get(cEntityRefName).getTextContent());
					isSolved = true;
				} else if (XMLDTDEntities != null) {
					if (XMLDTDEntities.containsKey(cEntityRefName)) {
						sb.append(s.substring(start, m.start()));
						sb.append(XMLDTDEntities.get(cEntityRefName)
								.getTextContent());
						isSolved = true;
					}
				}
			}

			if (!isSolved) {
				sb.append(s.substring(start, m.start()));
				sb.append(Entity.decodeString(s.substring(m.start(), m.end())));
			}

			start = m.end();
		}

		sb.append(s.substring(start));
		return sb.toString();
	}

	public static void load() {
		Entities cDTDEntities = new Entities();
		Entities.setXMLDTDEntities(cDTDEntities);
		try {
			for (String uri : CatalogManager.items.get("xml").get("SYSTEM").values()) {
				InputStream lIS = DTDParser.class
						.getResourceAsStream(uri);
				DTDParser parser = new DTDParser();
				parser.setDTDHandler(new DTDHandler());
				parser.parse(lIS);
				cDTDEntities.putAll(((DTDHandler) parser.getDTDHandler())
						.getDtdDocument().getEntities());
				cDTDEntities.arrange();
			}

		} catch (Exception e) {
			// TODO Auto-generated catch block
			e.printStackTrace();
		}
	}

	public static Entities getXMLDTDEntities() {
		return XMLDTDEntities;
	}

	public static void setXMLDTDEntities(Entities xMLDTDEntities) {
		XMLDTDEntities = xMLDTDEntities;
	}

}
