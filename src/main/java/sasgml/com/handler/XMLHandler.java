package sasgml.com.handler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.nio.charset.Charset;
import java.nio.charset.CharsetEncoder;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;
import java.util.TreeMap;

import org.xml.sax.Attributes;
import org.xml.sax.SAXException;
import org.xml.sax.ext.DefaultHandler2;

import sasgml.com.catalog.CatalogManager;
import sasgml.com.entity.EntityManager;
import sasgml.com.log.LogManager;
import sasgml.com.model.AttList;
import sasgml.com.model.Attribute;
import sasgml.com.model.AttributeDefaultTypeEnum;
import sasgml.com.model.DocumentTypeDefinition;
import sasgml.com.model.Element;
import sasgml.com.model.ElementItem;
import sasgml.com.model.ElementObserver;
import sasgml.com.model.ElementTypeEnum;
import sasgml.com.model.Entity;
import sasgml.com.model.ModeEnum;
import sasgml.com.parsing.CaseInsensitiveFileSearcher;
import sasgml.com.parsing.Parser;
import sasgml.com.util.FileUtil;
import sasgml.com.xml.SASgml;

/**

 */

public class XMLHandler extends DefaultHandler2 {

	// For State
	private HandlerState state;

	// For special character encoding from UTF to ISO
	private HashMap<String, String> reverseSdataEntities;

	// Dtd Document
	private DocumentTypeDefinition dtdDocument;
	private String rootDtdName;
	private boolean isDtdLoaded;
	private boolean isDocumentElement;

	// ID and IDREF coherence
	private LinkedHashSet<String> ids;
	private LinkedHashSet<String> idrefs;

	// CURRENT Attribute
	private HashMap<String, String> currentAttributes;

	// For DTD Validation tree
	private LinkedList<String> ancestors;
	private LinkedList<ElementObserver> ancestorsObservers;

	// For Output
	private String outSystemId;
	private OutputStreamWriter out;
	private OutputStream outputStream;

	// For Input
	private String systemId;

	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	private void initialize() {
		state = new HandlerState();
		reverseSdataEntities = new HashMap<String, String>();
		dtdDocument = new DocumentTypeDefinition();
		ids = new LinkedHashSet<String>();
		idrefs = new LinkedHashSet<String>();
		currentAttributes = new HashMap<String, String>();
		ancestors = new LinkedList<String>();
		ancestorsObservers = new LinkedList<ElementObserver>();
		rootDtdName = "NONE";
		isDtdLoaded = false;
		isDocumentElement = true;
	}

	public XMLHandler(String pFilePath) throws Exception {
		super();
		initialize();
		outSystemId = pFilePath;
		File lOutFile = FileUtil.createFile(pFilePath);
		outputStream = new FileOutputStream(lOutFile, false);
		out = new OutputStreamWriter(outputStream, "iso-8859-1");
	}

	public void loadDtdFromFile(String pFileName) {
		CaseInsensitiveFileSearcher lCaseInsensitiveFile = new CaseInsensitiveFileSearcher(CatalogManager.getDtdPath()
				+ "/" + pFileName);
		if (lCaseInsensitiveFile.find()) {
			File lFile = lCaseInsensitiveFile.getFile();
			try {
				dtdDocument.load(lFile, true);
				isDtdLoaded = true;
			} catch (Exception e) {
				writeError("Erreur lors du chargement de la DTD : " + e.getMessage());
			}
		} else {
			writeWarning("Impossible de trouver le fichier [" + CatalogManager.getDtdPath() + "/" + pFileName + "].");
		}
	}

	private void loadEntities() {
		String doctype = rootDtdName;
		for (Entity cDTDEntity : dtdDocument.getEntities().values()) {
			String value = cDTDEntity.getValue();
			if (cDTDEntity.getModeEnum().equals(ModeEnum.SDATA) && !reverseSdataEntities.containsKey(value)) {
				if (EntityManager.items.containsKey(doctype)) {
					if (EntityManager.items.get(doctype).containsKey(value)) {
						reverseSdataEntities.put(value, EntityManager.items.get(doctype).get(value));
					} else {
						reverseSdataEntities.put(value, cDTDEntity.getName());
					}
				} else {
					reverseSdataEntities.put(value, cDTDEntity.getName());
				}
			}
		}
	}

	@Override
	public void processingInstruction(String target, String data) {
		try {
			out.write("<?" + data + ">");
		} catch (IOException e) {
			writeError("Erreur lors de l'écriture : " + e.getMessage());
		}
	}

	@Override
	public void endDocument() throws SAXException {
		try {
			out.close();
		} catch (IOException e) {
			writeError("Erreur lors de l'écriture : " + e.getMessage());
		}

		idrefs.removeAll(ids);

		if (idrefs.size() != 0) {
			LogManager.writeWarning("certains IDREF sont inconnus dans le document  " + idrefs.toString() + " !");
		}

	}

	@Override
	public void startDocument() throws SAXException {
	}

	public void startElement(String uri, String localName, String name, Attributes attributes) throws SAXException {
		try {

			TreeMap<String, String> lAttributes = new TreeMap<String, String>();
			for (Integer i = 0; i < attributes.getLength(); i++) {
				lAttributes.put(attributes.getQName(i).toUpperCase(), attributes.getValue(i));
			}

			String qName = name.toUpperCase();

			if (isDocumentElement) {

				if (lAttributes.containsKey("XSI:NONAMESPACESCHEMALOCATION")) {
					rootDtdName = lAttributes.get("XSI:NONAMESPACESCHEMALOCATION");
					lAttributes.remove("XSI:NONAMESPACESCHEMALOCATION");
					lAttributes.remove("XMLNS:XSI");
				} else {
					rootDtdName = qName;
				}

				String dtdUri = CatalogManager.get("sgml", "DOCTYPE", rootDtdName);
				if (dtdUri == null) {
					throw new SAXException("Impossible de charger la dtd du document [" + qName + "]");
				}

				loadDtdFromFile(dtdUri);

				if (!isDtdLoaded) {
					throw new SAXException("Impossible de charger la dtd du document [" + qName + "]");
				}

				loadEntities();

				out.write("<!DOCTYPE " + rootDtdName + " SYSTEM \"" + dtdUri + "\">");

			}

			if (!isDocumentElement && !state.isActive()) {

				/*
				 * Validation du fichier vs dtd
				 */

				ElementObserver pDTDElementObserver = ancestorsObservers.getLast();

				/*
				 * Acceptation normale
				 */
				boolean accepted = pDTDElementObserver.acceptNext(qName);

				if (!accepted) {
					/*
					 * Acceptation en tant que inclusion
					 */
					for (int i = ancestorsObservers.size() - 1; i >= 0; i--) {
						ElementObserver lDTDElementObserver = ancestorsObservers.get(i);
						accepted = lDTDElementObserver.acceptAsInclusion(qName);
						if (accepted) {
							break;
						}
					}

					if (!accepted) {

						// FOR INCLUSION WHEN DOCTYPE != ROOT ELEMENT NAME :
						// exemple FBV and SUPERFBV

						Element rootElement = dtdDocument.getElements().get(rootDtdName);
						ElementObserver rootElementObserver = new ElementObserver(rootElement.getModel());
						accepted = rootElementObserver.acceptAsInclusion(qName);
					}
				}

				/*
				 * Cas des element en OO (OMIT TAG)
				 */
				if (!accepted) {
					for (ElementItem cDTDElementItem : pDTDElementObserver.getPlausibleNextName()) {
						if (dtdDocument.getElements().containsKey(cDTDElementItem.getName())) {
							String pQName = cDTDElementItem.getName();
							Element lDTDElement = dtdDocument.getElements().get(pQName);
							if (lDTDElement.getType() == ElementTypeEnum.NOSTARTENDTAG) {
								pDTDElementObserver.acceptNext(pQName);
								ancestors.add(pQName);
								pDTDElementObserver = new ElementObserver(lDTDElement.getModel());
								ancestorsObservers.add(pDTDElementObserver);

								out.write("<" + pQName + ">");

								writeAndCheckAttributes(qName, new TreeMap<String, String>());

								startElement(uri, localName, qName, attributes);
								return;
							}
						}
					}

				}

				/*
				 * Cas ou le parent est un empty tag : tentative de fermer le
				 * parent qui est en réaité l'element precedent
				 */
				if (!accepted) {
					String pQName = ancestors.getLast();
					if (dtdDocument.getElements().containsKey(pQName)) {
						Element lDTDElement = dtdDocument.getElements().get(pQName);
						if (lDTDElement.getType() == ElementTypeEnum.EMPTYTAG) {
							out.write("</" + pQName + ">");
							ancestorsObservers.removeLast();
							ancestors.removeLast();
							accepted = true;

							// on devrait recommencer pour accepter ou pas le
							// qName

							startElement(uri, localName, qName, attributes);
							return;

						}
					}

				}

				if (!accepted) {
					String message = "Erreur";
					if (systemId != null) {
						message += " dans le fichier [" + systemId + "]";
					}

					switch (SASgml.getConversionMode()) {
					case Soft:
						writeError(message + " : Element [" + qName + "] inattendu après ["
								+ pDTDElementObserver.getState().getName() + "] dans [" + ancestors.getLast() + "]");
						state.setActive(true);
						break;
					case Strict:
						throw new SAXException(message + " : Element [" + qName + "] inattendu après ["
								+ pDTDElementObserver.getState().getName() + "] dans [" + ancestors.getLast() + "]");
					default:
						break;
					}

				}
			}

			if (state.isActive()) {
				state.incrementLength();
			}

			if (state.isActive()) {
				out.write("<" + qName);
				if (lAttributes.size() > 0) {
					for (String cAttName : lAttributes.keySet()) {
						String cAttValue = encode(lAttributes.get(cAttName));
						out.write(" " + cAttName + "=\"" + cAttValue + "\"");
					}
				}
				out.write(">");
			} else {

				ElementObserver lDTDElementObserver = null;

				if (dtdDocument.getElements().containsKey(qName)) {
					Element lDTDElement = dtdDocument.getElements().get(qName);

					lDTDElementObserver = new ElementObserver(lDTDElement.getModel());

					out.write("<" + qName);

					if (dtdDocument.getAttLists().containsKey(qName)) {
						writeAndCheckAttributes(qName, lAttributes);
					} else if (lAttributes.size() > 0) {
						for (String cAttName : lAttributes.keySet()) {
							String cAttValue = encode(lAttributes.get(cAttName));
							out.write(" " + cAttName + "=\"" + cAttValue + "\"");
						}
						LogManager.writeWarning("la liste d'attribut est inconnue pour l'element [" + qName + "]");
					}

					// TO CHECK
					if (!lDTDElement.isEmpty()) {
						ancestors.add(qName);
						ancestorsObservers.add(lDTDElementObserver);
					}

					out.write(">");
				} else {
					out.write("<" + qName);
					if (lAttributes.size() > 0) {
						for (String cAttName : lAttributes.keySet()) {
							String cAttValue = encode(lAttributes.get(cAttName));
							out.write(" " + cAttName + "=\"" + cAttValue + "\"");
						}
					}
					out.write(">");

					ancestors.add(qName);
					ancestorsObservers.add(lDTDElementObserver);

					writeWarning("Element [" + qName + "] inconnu !");
				}
			}
			if (isDocumentElement) {
				isDocumentElement = false;
			}

		} catch (IOException e) {
			writeError("Erreur lors de l'écriture : " + e.getMessage());
		}
	}

	private void writeAndCheckAttributes(String qName, TreeMap<String, String> lAttributes) throws IOException {
		AttList lDTDATTList = dtdDocument.getAttLists().get(qName);

		// check for default value in attlist

		for (String cAttName : lDTDATTList.getAttributes().keySet()) {
			if (!lAttributes.containsKey(cAttName)) {
				Attribute cAttribute = lDTDATTList.getAttributes().get(cAttName);
				switch (cAttribute.getDefaultTypeEnum()) {
				case CONREF:
					break;
				case CURRENT:
					if (currentAttributes.containsKey(qName + "%" + cAttName)) {
						lAttributes.put(cAttName, currentAttributes.get(qName + "%" + cAttName));
					} else {
						LogManager.writeWarning("L'attribut [" + cAttribute.getName() + "] de l'element [" + qName
								+ "] est en mode [CURRENT] mais il n'y a pas d'enregistrement courant !");
					}
					break;
				case FIXED:
					lAttributes.put(cAttName, cAttribute.getDefaultValue());
					break;
				case IMPLIED:
					break;
				case NONE:
					lAttributes.put(cAttName, cAttribute.getDefaultValue());
					break;
				case REQUIRED:
					writeWarning("L'attribut [" + cAttribute.getName() + "] de l'element [" + qName
							+ "] est manquant !");
					break;
				default:
					break;

				}
			}
		}

		// check for real value in sgml
		for (String cAttName : lAttributes.keySet()) {
			String cAttValue = encode(lAttributes.get(cAttName));
			if (lDTDATTList.getAttributes().containsKey(cAttName)) {
				Attribute cAttribute = lDTDATTList.getAttributes().get(cAttName);

				boolean isOk = true;
				// Test du mode CONREF : if att specified, the element qName
				// must be empty
				if (cAttribute.getDefaultTypeEnum().equals(AttributeDefaultTypeEnum.CONREF)) {
					Element lDTDElement = dtdDocument.getElements().get(qName);
					lDTDElement.setEmpty(true);
				}

				// Ajout du mode CURRENT
				if (cAttribute.getDefaultTypeEnum().equals(AttributeDefaultTypeEnum.CURRENT)) {
					currentAttributes.put(qName + "%" + cAttName, cAttValue);
				}

				switch (cAttribute.getTypeEnum()) {
				case CDATA:
					out.write(" " + cAttName + "=\"" + cAttValue + "\"");
					break;
				case CHOICE_LIST:
					if (cAttribute.getValues().containsKey(cAttValue)) {
						out.write(" " + cAttName + "=\"" + cAttValue + "\"");
					} else if (cAttribute.getValues().containsKey(cAttValue.toUpperCase())) {
						out.write(" " + cAttName + "=\"" + cAttribute.getValues().get(cAttValue.toUpperCase()) + "\"");
					} else {
						out.write(" " + cAttName + "=\"" + cAttValue + "\"");
						isOk = false;
					}
					break;
				case ENTITIES:
					cAttValue = cAttValue.toUpperCase();
					for (String cAttValuePart : cAttValue.split("\\s")) {
						if (!dtdDocument.getEntities().containsKey(cAttValuePart)) {
							writeWarning("La valeur de l'attribut [" + cAttValuePart + "] pour l'attribut [" + cAttName
									+ "] de l'element [" + qName + "] de type [" + cAttribute.getTypeEnum().toString()
									+ "] n'existe pas en tant que ENTITY !");
						}
					}
					out.write(" " + cAttName + "=\"" + cAttValue + "\"");
					break;
				case ENTITY:
					cAttValue = cAttValue.toUpperCase();
					if (!dtdDocument.getEntities().containsKey(cAttValue)) {
						out.write(" " + cAttName + "=\"" + cAttValue + "\"");
						writeWarning("La valeur de l'attribut [" + cAttValue + "] pour l'attribut [" + cAttName
								+ "] de l'element [" + qName + "] de type [" + cAttribute.getTypeEnum().toString()
								+ "] n'existe pas en tant que ENTITY !");
					}
					out.write(" " + cAttName + "=\"" + cAttValue + "\"");
					break;
				case ID:
					cAttValue = cAttValue.toUpperCase();
					out.write(" " + cAttName + "=\"" + cAttValue + "\"");
					if (!ids.contains(cAttValue)) {
						ids.add(cAttValue);
					} else {
						writeWarning("La valeur de l'attribut [" + cAttValue + "] pour l'attribut [" + cAttName
								+ "] de l'element [" + qName + "] de type [" + cAttribute.getTypeEnum().toString()
								+ "] est dupliquée !");
					}
					break;
				case IDREF:
					cAttValue = cAttValue.toUpperCase();
					out.write(" " + cAttName + "=\"" + cAttValue + "\"");
					idrefs.add(cAttValue);
					break;
				case IDREFS:
					cAttValue = cAttValue.toUpperCase();
					out.write(" " + cAttName + "=\"" + cAttValue + "\"");
					for (String cAttValuePart : cAttValue.split("\\s")) {
						idrefs.add(cAttValuePart);
					}
					break;
				case NAME:
					out.write(" " + cAttName + "=\"" + cAttValue + "\"");
					isOk = Parser.isValidName(cAttValue);
					break;
				case NAMES:
					out.write(" " + cAttName + "=\"" + cAttValue + "\"");
					isOk = Parser.isValidNames(cAttValue);
					break;
				case NMTOKEN:
					out.write(" " + cAttName + "=\"" + cAttValue + "\"");
					isOk = Parser.isValidNmtoken(cAttValue);
					break;
				case NMTOKENS:
					out.write(" " + cAttName + "=\"" + cAttValue + "\"");
					isOk = Parser.isValidNmtokens(cAttValue);
					break;
				case NOTATION:
					cAttValue = cAttValue.toUpperCase();
					if (!dtdDocument.getNotations().containsKey(cAttValue)) {
						writeWarning("La valeur de l'attribut [" + cAttValue + "] pour l'attribut [" + cAttName
								+ "] de l'element [" + qName + "] de type [" + cAttribute.getTypeEnum().toString()
								+ "] n'existe pas en tant que NOTATION !");
					} else if (!cAttribute.getValues().containsKey(cAttValue)) {
						LogManager.writeWarning("La valeur de l'attribut [" + cAttValue + "] pour l'attribut ["
								+ cAttName + "] de l'element [" + qName + "] de type ["
								+ cAttribute.getTypeEnum().toString()
								+ "] n'est pas une NOTATION déclarée dans la liste des valeurs possible !");
					}
					out.write(" " + cAttName + "=\"" + cAttValue + "\"");
					break;
				case NUMBER:
					out.write(" " + cAttName + "=\"" + cAttValue + "\"");
					isOk = Parser.isValidNumber(cAttValue);
					break;
				case NUMBERS:
					out.write(" " + cAttName + "=\"" + cAttValue + "\"");
					isOk = Parser.isValidNumbers(cAttValue);
					break;
				case NUTOKEN:
					out.write(" " + cAttName + "=\"" + cAttValue + "\"");
					isOk = Parser.isValidNutoken(cAttValue);
					break;
				case NUTOKENS:
					out.write(" " + cAttName + "=\"" + cAttValue + "\"");
					isOk = Parser.isValidNutokens(cAttValue);
					break;
				default:
					out.write(" " + cAttName + "=\"" + cAttValue + "\"");
					break;
				}

				if (!isOk) {
					writeWarning("La valeur de l'attribut [" + cAttValue + "] pour l'attribut [" + cAttName
							+ "] de l'element [" + qName + "] n'est pas un [" + cAttribute.getTypeEnum().toString()
							+ "] valide !");
				}

			} else {
				out.write(" " + cAttName + "=\"" + cAttValue + "\"");
				writeWarning("Attribut [" + cAttName + "] inconnu pour l'element [" + qName + "]");
			}

		}

	}

	public void endElement(String uri, String localName, String name) throws SAXException {
		try {

			String qName = name.toUpperCase();

			if (state.isActive()) {
				out.write("</" + qName + ">");
			} else {
				if (dtdDocument.getElements().containsKey(qName)) {
					Element lDTDElement = dtdDocument.getElements().get(qName);
					if (!lDTDElement.isEmpty()) {
						ElementObserver pDTDElementObserver = ancestorsObservers.getLast();

						boolean accepted = pDTDElementObserver.acceptNext("#EXIT");

						if (accepted) {
							ancestors.removeLast();
							ancestorsObservers.removeLast();

							out.write("</" + qName + ">");

						} else {
							throw new SAXException("Erreur : la fermeture de  l'élement [" + qName
									+ "] n'est pas autorisée à ce niveau ! ");
						}
					}
				} else {
					out.write("</" + qName + ">");
				}
			}
			if (state.isActive()) {
				state.decrementLength();
				if (state.getLength() == 0) {
					state.setActive(false);
				}
			}

		} catch (IOException e) {
			writeError("Erreur lors de l'écriture : " + e.getMessage());
		}
	}

	public void characters(char ch[], int start, int length) throws SAXException {
		try {

			String text = new String(ch, start, length);

			if (isDocumentElement) {
				return;
			}

			// System.out.println(text);

			if (state.isActive()) {
				out.write(encode(text));
			} else {

				boolean accepted = false;

				ElementObserver pDTDElementObserver = ancestorsObservers.getLast();

				if (text.equals("")) {
					accepted = true;
				} else if (pDTDElementObserver.acceptNext("#PCDATA")) {
					accepted = true;
				} else if (pDTDElementObserver.acceptNext("CDATA")) {
					accepted = true;
				} else if (pDTDElementObserver.acceptNext("RCDATA")) {
					accepted = true;
				} else if (text.replaceAll("\\s", "").equals("")) {
					accepted = true;
				}

				if (!accepted) {
					writeWarning("Text [" + text + "] inattendu après [" + pDTDElementObserver.getState().getName()
							+ "] dans [" + ancestors.getLast() + "]");
				}
				out.write(encode(text));
			}

		} catch (IOException e) {
			writeError("Erreur lors de l'écriture : " + e.getMessage());
		}
	}

	private CharsetEncoder encoder = Charset.forName("iso-8859-1").newEncoder();

	private String encode(String text) {

		if (encoder.canEncode(text)) {
			return replacePredifinedEntities(text);
		} else {
			StringBuilder sb = new StringBuilder();
			char[] textArray = text.toCharArray();

			for (char c : textArray) {
				if (encoder.canEncode(c)) {
					sb.append(replacePredifinedEntities(Character.toString(c)));
				} else {
					if (isDtdLoaded) {
						if (reverseSdataEntities.containsKey(Character.toString(c))) {
							sb.append("&" + reverseSdataEntities.get(Character.toString(c)) + ";");
						} else {
							// switch (SASgml.getConversionMode()) {
							// case Soft:
							// sb.append(c);
							// writeError("Impossible d'encoder [" + c
							// + "]");
							// break;
							// case Strict:
							// sb.append("?");
							// writeWarning("impossible d'encoder [" + c
							// + "] => remplacé par [?]");
							// default:
							// break;
							// }
							// sb.append("&#x" + Integer.toHexString((int) c)
							// + ";");
							sb.append("&#" + String.valueOf((int) c) + ";");
						}
					} else {
						// switch (SASgml.getConversionMode()) {
						// case Soft:
						// sb.append(c);
						// writeError("Impossible d'encoder [" + c
						// + "]");
						// break;
						// case Strict:
						// sb.append("?");
						// writeWarning("impossible d'encoder [" + c
						// + "] => remplacé par [?]");
						// default:
						// break;
						// }
						// sb.append("&#x" + Integer.toHexString((int) c) +
						// ";");
						sb.append("&#" + String.valueOf((int) c) + ";");
					}
				}
			}

			return sb.toString();
		}

	}

	private String replacePredifinedEntities(String text) {
		return text.replace("&", "&amp;").replace("<", "&lt;");
	}

	private String getMsg(String msg) {
		if (systemId != null && outSystemId != null) {
			msg = "Fichier [" + systemId + "] vers Fichier [" + outSystemId + "] : " + msg;
		} else if (systemId != null) {
			msg = "Fichier [" + systemId + "] : " + msg;
		} else if (outSystemId != null) {
			msg = "Fichier [" + outSystemId + "] : " + msg;
		}
		return msg;
	}

	@SuppressWarnings("unused")
	private void writeInfo(String msg) {
		LogManager.writeInfo(getMsg(msg));
	}

	private void writeWarning(String msg) {
		LogManager.writeWarning(getMsg(msg));
	}

	private void writeError(String msg) {
		LogManager.writeError(getMsg(msg));
	}

	public String getOutSystemId() {
		return outSystemId;
	}

	public void setOutSystemId(String outSystemId) {
		this.outSystemId = outSystemId;
	}

}