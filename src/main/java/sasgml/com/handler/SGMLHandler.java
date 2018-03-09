package sasgml.com.handler;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.LinkedList;

import javax.xml.stream.XMLOutputFactory;
import javax.xml.stream.XMLStreamException;
import javax.xml.stream.XMLStreamWriter;
import javax.xml.transform.dom.DOMResult;

import org.w3c.dom.Document;

import sasgml.com.catalog.CatalogManager;
import sasgml.com.exception.SASgmlException;
import sasgml.com.log.LogManager;
import sasgml.com.model.AttList;
import sasgml.com.model.Attribute;
import sasgml.com.model.AttributeDefaultTypeEnum;
import sasgml.com.model.Attributes;
import sasgml.com.model.DocumentTypeDefinition;
import sasgml.com.model.Element;
import sasgml.com.model.ElementItem;
import sasgml.com.model.ElementObserver;
import sasgml.com.model.ElementTypeEnum;
import sasgml.com.model.Entity;
import sasgml.com.parsing.Parser;
import sasgml.com.parsing.Token;
import sasgml.com.util.FileUtil;

public class SGMLHandler implements ISGMLHandler {

	// Dtd Document From system or public
	private DocumentTypeDefinition idDocumentTypeDefinition;
	// Dtd Document From content subset
	private DTDHandler subDTDHandler;

	private String rootDtdName;
	private String dtdSystemId;
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
	private XMLStreamWriter out;

	// For Input
	private String systemId;

	public String getSystemId() {
		return systemId;
	}

	public void setSystemId(String systemId) {
		this.systemId = systemId;
	}

	private void initialize() {
		idDocumentTypeDefinition = new DocumentTypeDefinition();
		ids = new LinkedHashSet<String>();
		idrefs = new LinkedHashSet<String>();
		currentAttributes = new HashMap<String, String>();
		ancestors = new LinkedList<String>();
		ancestorsObservers = new LinkedList<ElementObserver>();
		rootDtdName = "NONE";
		isDtdLoaded = false;
		isDocumentElement = true;

		subDTDHandler = new DTDHandler();
	}

	public SGMLHandler(String pFilePath) {
		initialize();
		outSystemId = pFilePath;
		File lOutFile = FileUtil.createFile(pFilePath);
		try {
			out = XMLOutputFactory.newInstance().createXMLStreamWriter(
					new OutputStreamWriter(new FileOutputStream(lOutFile),
							"utf-8"));
		} catch (Exception e) {
			writeError("Erreur lors de la création du fichier [" + pFilePath
					+ "] : " + e.getMessage());
		}
	}

	public SGMLHandler(StringWriter pStringWriter) {
		initialize();
		try {
			out = XMLOutputFactory.newInstance().createXMLStreamWriter(
					pStringWriter);
		} catch (Exception e) {
			writeError("Erreur lors de la création du flux : " + e.getMessage());
		}
	}

	public SGMLHandler(Document lDocument) {
		initialize();
		outSystemId = lDocument.getBaseURI();
		try {
			DOMResult result = new DOMResult(lDocument);
			out = XMLOutputFactory.newInstance().createXMLStreamWriter(result);
		} catch (Exception e) {
			writeError("Erreur lors de la création du document : "
					+ e.getMessage());
		}
	}

	@Override
	public void endDocument() throws SASgmlException {
		try {
			while (ancestors.size() > 0) {
				ancestors.removeLast();
				ancestorsObservers.removeLast();
				out.writeEndElement();
			}
			out.close();
		} catch (XMLStreamException e) {
			writeError("Erreur lors de l'écriture : " + e.getMessage());
		}

		idrefs.removeAll(ids);

		if (idrefs.size() != 0) {
			LogManager
					.writeWarning("Certains IDREF sont inconnus dans le document  "
							+ idrefs.toString() + " !");
		}

	}

	@Override
	public void startDocument() throws SASgmlException {

		// WriteInfo("Lancement du parseur SGML");

		try {
			out.writeStartDocument("UTF-8", "1.0");
			out.flush();
		} catch (XMLStreamException e) {
			writeError("Erreur lors de l'écriture : " + e.getMessage());
		}
	}

	@Override
	public void startElement(String qName, Attributes attributes)
			throws SASgmlException {

		if (isDocumentElement) {
			if (!isDtdLoaded) {
				String dtdUri = CatalogManager.get("sgml", "DOCTYPE", qName);
				if (dtdUri == null || dtdUri.equals(dtdSystemId)) {
					throw new SASgmlException(
							"Impossible de charger la dtd du document ["
									+ qName + "]");
				}
				loadDtdFromFile(dtdUri);
				rootDtdName = qName;
				if (!isDtdLoaded) {
					throw new SASgmlException(
							"Impossible de charger la dtd du document ["
									+ qName + "]");
				}
			}

			// WriteInfo("Chargement de la DTD [" + rootDtdName +
			// "]");

		}

		if (!isDocumentElement) {

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
					ElementObserver lDTDElementObserver = ancestorsObservers
							.get(i);
					accepted = lDTDElementObserver.acceptAsInclusion(qName);
					if (accepted) {
						break;
					}
				}

				if (!accepted) {

					// FOR INCLUSION WHEN DOCTYPE != ROOT ELEMENT NAME : exemple
					// FBV and SUPERFBV

					Element rootElement = idDocumentTypeDefinition
							.getElements().get(rootDtdName);
					ElementObserver rootElementObserver = new ElementObserver(
							rootElement.getModel());
					accepted = rootElementObserver.acceptAsInclusion(qName);
				}
			}

			/*
			 * Cas des element en OO (OMIT TAG)
			 */
			if (!accepted) {
				for (ElementItem cDTDElementItem : pDTDElementObserver
						.getPlausibleNextName()) {
					if (idDocumentTypeDefinition.getElements().containsKey(
							cDTDElementItem.getName())) {
						String pQName = cDTDElementItem.getName();
						Element lDTDElement = idDocumentTypeDefinition
								.getElements().get(pQName);
						if (lDTDElement.getType() == ElementTypeEnum.NOSTARTENDTAG) {
							pDTDElementObserver.acceptNext(pQName);
							ancestors.add(pQName);
							pDTDElementObserver = new ElementObserver(
									lDTDElement.getModel());
							ancestorsObservers.add(pDTDElementObserver);

							try {
								out.writeStartElement(pQName);

								writeAndCheckAttributes(pQName,
										new Attributes());

							} catch (XMLStreamException e) {
								LogManager
										.writeError("Erreur lors de l'écriture : "
												+ e.getMessage());
							}

							startElement(qName, attributes);
							return;
						}
					}
				}

			}

			/*
			 * Cas ou le parent est un empty tag : tentative de fermer le parent
			 * qui est en réaité l'element precedent
			 */
			if (!accepted) {
				String pQName = ancestors.getLast();
				if (idDocumentTypeDefinition.getElements().containsKey(pQName)) {
					Element lDTDElement = idDocumentTypeDefinition
							.getElements().get(pQName);
					if (lDTDElement.getType() == ElementTypeEnum.EMPTYTAG) {
						try {
							out.writeEndElement();
						} catch (XMLStreamException e) {
							LogManager
									.writeError("Erreur lors de l'écriture : "
											+ e.getMessage());
						}
						ancestorsObservers.removeLast();
						ancestors.removeLast();
						accepted = true;

						// on devrait recommencer pour accepter ou pas le
						// qName

						startElement(qName, attributes);
						return;

					}
				}

			}

			if (!accepted) {
				String message = "Erreur";
				if (systemId != null) {
					message += " dans le fichier [" + systemId + "]";
				}
				throw new SASgmlException(message + " : Element [" + qName
						+ "] inattendu après ["
						+ pDTDElementObserver.getState().getName() + "] dans ["
						+ ancestors.getLast() + "]");
			}
		}

		ElementObserver lDTDElementObserver = null;

		if (idDocumentTypeDefinition.getElements().containsKey(qName)) {
			Element lDTDElement = idDocumentTypeDefinition.getElements().get(
					qName);

			lDTDElementObserver = new ElementObserver(lDTDElement.getModel());

			try {
				out.writeStartElement(qName);

				if (isDocumentElement) {
					out.writeAttribute("xmlns:xsi",
							"http://www.w3.org/2001/XMLSchema-instance");
					out.writeAttribute("xsi:noNamespaceSchemaLocation",
							rootDtdName);
				}

				if (idDocumentTypeDefinition.getAttLists().containsKey(qName)) {
					writeAndCheckAttributes(qName, attributes);
				} else if (attributes.size() > 0) {
					for (String cAttName : attributes.keySet()) {
						String cAttValue = idDocumentTypeDefinition
								.getEntities().decodeString(
										attributes.get(cAttName).getValue());
						out.writeAttribute(cAttName, cAttValue);
					}
					LogManager
							.writeWarning("Impossible de trouver la définition de la liste des attributs pour l'element ["
									+ qName + "]");
				}
			} catch (XMLStreamException e) {
				writeError("Erreur lors de l'écriture : " + e.getMessage());
			}
			if (lDTDElement.isEmpty()) {
				try {
					out.writeEndElement();
				} catch (XMLStreamException e) {
					writeError("Erreur lors de l'écriture : " + e.getMessage());
				}
			} else {
				ancestors.add(qName);
				ancestorsObservers.add(lDTDElementObserver);
			}
		} else {
			try {
				out.writeStartElement(qName);
				for (String cAttName : attributes.keySet()) {
					String cAttValue = idDocumentTypeDefinition.getEntities()
							.decodeString(attributes.get(cAttName).getValue());
					out.writeAttribute(cAttName, cAttValue);
				}
			} catch (XMLStreamException e) {
				writeError("Erreur lors de l'écriture : " + e.getMessage());
			}

			ancestors.add(qName);
			ancestorsObservers.add(lDTDElementObserver);

			writeWarning("Element [" + qName + "] inconnu !");
		}

		if (isDocumentElement) {
			isDocumentElement = false;
		}

		try {
			out.flush();
		} catch (XMLStreamException e) {
			// TODO Auto-generated catch block
			writeError("Erreur lors de l'écriture : " + e.getMessage());
		}

	}

	private void writeAndCheckAttributes(String qName, Attributes attributes)
			throws XMLStreamException {
		AttList lDTDATTList = idDocumentTypeDefinition.getAttLists().get(qName);

		// check for default value in attlist

		for (String cAttName : lDTDATTList.getAttributes().keySet()) {
			if (!attributes.containsKey(cAttName)) {
				Attribute cAttribute = lDTDATTList.getAttributes()
						.get(cAttName);
				switch (cAttribute.getDefaultTypeEnum()) {
				case CONREF:
					break;
				case CURRENT:
					if (currentAttributes.containsKey(qName + "%" + cAttName)) {
						attributes.put(cAttName, new Attribute(cAttName,
								currentAttributes.get(qName + "%" + cAttName)));

					} else {
						LogManager
								.writeWarning("L'attribut ["
										+ cAttribute.getName()
										+ "] de l'element ["
										+ qName
										+ "] est en mode [CURRENT] mais il n'y a pas d'enregistrement courant !");
					}
					break;
				case FIXED:
					attributes.put(
							cAttName,
							new Attribute(cAttName, cAttribute
									.getDefaultValue()));
					break;
				case IMPLIED:
					break;
				case NONE:
					attributes.put(
							cAttName,
							new Attribute(cAttName, cAttribute
									.getDefaultValue()));
					break;
				case REQUIRED:
					writeWarning("L'attribut [" + cAttribute.getName()
							+ "] de l'element [" + qName + "] est manquant !");
					break;
				default:
					break;

				}
			}
		}

		// check for real value in sgml
		for (String cAttName : attributes.keySet()) {
			String cInitialAttValue = attributes.get(cAttName).getValue();
			String cAttValue = idDocumentTypeDefinition.getEntities()
					.decodeString(cInitialAttValue);
			if (lDTDATTList.getAttributes().containsKey(cAttName)) {
				Attribute cAttribute = lDTDATTList.getAttributes()
						.get(cAttName);

				boolean isOk = true;
				// Parser cParser;

				// Test du mode CONREF : if att specified, the element qName
				// must be empty
				if (cAttribute.getDefaultTypeEnum().equals(
						AttributeDefaultTypeEnum.CONREF)) {
					Element lDTDElement = idDocumentTypeDefinition
							.getElements().get(qName);
					lDTDElement.setEmpty(true);
				}

				// Ajout du mode CURRENT
				if (cAttribute.getDefaultTypeEnum().equals(
						AttributeDefaultTypeEnum.CURRENT)) {
					currentAttributes.put(qName + "%" + cAttName, cAttValue);
				}

				switch (cAttribute.getTypeEnum()) {
				case CDATA:
					out.writeAttribute(cAttName, cAttValue);
					break;
				case CHOICE_LIST:
					if (cAttribute.getValues().containsKey(cAttValue)) {
						out.writeAttribute(cAttName, cAttValue);
					} else if (cAttribute.getValues().containsKey(
							cAttValue.toUpperCase())) {
						out.writeAttribute(cAttName, cAttribute.getValues()
								.get(cAttValue.toUpperCase()));
					} else {
						out.writeAttribute(cAttName, cAttValue);
						isOk = false;
					}
					break;
				case ENTITIES:
					cAttValue = cAttValue.toUpperCase();
					StringBuilder lStringBuilder = new StringBuilder();
					for (String cAttValuePart : cAttValue.split("\\s")) {
						if (!idDocumentTypeDefinition.getEntities()
								.containsKey(cAttValuePart)) {
							writeWarning("La valeur de l'attribut ["
									+ cAttValuePart + "] pour l'attribut ["
									+ cAttName + "] de l'element [" + qName
									+ "] de type ["
									+ cAttribute.getTypeEnum().toString()
									+ "] n'existe pas en tant que ENTITY !");
						} else {
							Entity lDtdEntity = idDocumentTypeDefinition
									.getEntities().get(cAttValuePart);
							lStringBuilder.append(lDtdEntity.toDtdString()
									+ " ");
						}
					}
					// Ecriture de l'euivalent en json
					// out.writeAttribute(cAttName, lStringBuilder.toString()
					// .trim());
					out.writeAttribute(cAttName, cAttValue);
					break;
				case ENTITY:
					cAttValue = cAttValue.toUpperCase();
					if (!idDocumentTypeDefinition.getEntities().containsKey(
							cAttValue)) {
						out.writeAttribute(cAttName, cAttValue);
						writeWarning("La valeur de l'attribut [" + cAttValue
								+ "] pour l'attribut [" + cAttName
								+ "] de l'element [" + qName + "] de type ["
								+ cAttribute.getTypeEnum().toString()
								+ "] n'existe pas en tant que ENTITY !");
					} else {
						// Entity lDtdEntity =
						// idDocumentTypeDefinition.getEntities().get(cAttValue);
						// Ecriture de l'euivalent en json
						// out.writeAttribute(cAttName,
						// lDtdEntity.toDtdString());
						out.writeAttribute(cAttName, cAttValue);
					}
					break;
				case ID:
					cAttValue = cAttValue.toUpperCase();
					out.writeAttribute(cAttName, cAttValue);
					if (!ids.contains(cAttValue)) {
						ids.add(cAttValue);
					} else {
						writeWarning("La valeur de l'attribut [" + cAttValue
								+ "] pour l'attribut [" + cAttName
								+ "] de l'element [" + qName + "] de type ["
								+ cAttribute.getTypeEnum().toString()
								+ "] est dupliquée !");
					}
					break;
				case IDREF:
					cAttValue = cAttValue.toUpperCase();
					out.writeAttribute(cAttName, cAttValue);
					idrefs.add(cAttValue);
					break;
				case IDREFS:
					cAttValue = cAttValue.toUpperCase();
					out.writeAttribute(cAttName, cAttValue);
					for (String cAttValuePart : cAttValue.split("\\s")) {
						idrefs.add(cAttValuePart);
					}
					break;
				case NAME:
					out.writeAttribute(cAttName, cAttValue);
					isOk = Parser.isValidName(cAttValue);
					break;
				case NAMES:
					out.writeAttribute(cAttName, cAttValue);
					isOk = Parser.isValidNames(cAttValue);
					break;
				case NMTOKEN:
					out.writeAttribute(cAttName, cAttValue);
					isOk = Parser.isValidNmtoken(cAttValue);
					break;
				case NMTOKENS:
					out.writeAttribute(cAttName, cAttValue);
					isOk = Parser.isValidNmtokens(cAttValue);
					break;
				case NOTATION:
					cAttValue = cAttValue.toUpperCase();
					if (!idDocumentTypeDefinition.getNotations().containsKey(
							cAttValue)) {
						out.writeAttribute(cAttName, cAttValue);
						writeWarning("La valeur de l'attribut [" + cAttValue
								+ "] pour l'attribut [" + cAttName
								+ "] de l'element [" + qName + "] de type ["
								+ cAttribute.getTypeEnum().toString()
								+ "] n'existe pas en tant que NOTATION !");
					} else if (!cAttribute.getValues().containsKey(cAttValue)) {
						out.writeAttribute(cAttName, cAttValue);
						LogManager
								.writeWarning("La valeur de l'attribut ["
										+ cAttValue
										+ "] pour l'attribut ["
										+ cAttName
										+ "] de l'element ["
										+ qName
										+ "] de type ["
										+ cAttribute.getTypeEnum().toString()
										+ "] n'est pas une NOTATION déclarée dans la liste des valeurs possible !");
					} else {
						// Ecriture de l'euivalent en json
						// Notation lDtdNotation =
						// idDocumentTypeDefinition.getNotations().get(
						// cAttValue);
						// out.writeAttribute(cAttName,
						// lDtdNotation.toDtdString());
						out.writeAttribute(cAttName, cAttValue);
					}
					break;
				case NUMBER:
					out.writeAttribute(cAttName, cAttValue);
					isOk = Parser.isValidNumber(cAttValue);
					break;
				case NUMBERS:
					out.writeAttribute(cAttName, cAttValue);
					isOk = Parser.isValidNumbers(cAttValue);
					break;
				case NUTOKEN:
					out.writeAttribute(cAttName, cAttValue);
					isOk = Parser.isValidNutoken(cAttValue);
					break;
				case NUTOKENS:
					out.writeAttribute(cAttName, cAttValue);
					isOk = Parser.isValidNutokens(cAttValue);
					break;
				default:
					out.writeAttribute(cAttName, cAttValue);
					break;
				}

				if (!isOk) {
					writeWarning("La valeur de l'attribut [" + cAttValue
							+ "] pour l'attribut [" + cAttName
							+ "] de l'element [" + qName + "] n'est pas un ["
							+ cAttribute.getTypeEnum().toString()
							+ "] valide !");
				}

			} else {
				out.writeAttribute(cAttName, cAttValue);
				writeWarning("Attribut [" + cAttName
						+ "] inconnu pour l'element [" + qName + "]");
			}

		}

	}

	@Override
	public void endElement(String qName) throws SASgmlException {

		if (idDocumentTypeDefinition.getElements().containsKey(qName)) {
			Element lDTDElement = idDocumentTypeDefinition.getElements().get(
					qName);
			if (lDTDElement.isEmpty()) {
				throw new SASgmlException(
						"Element ["
								+ qName
								+ "] est EMPTY : la fermeture de cet élement est interdite !");
			}
		}

		while (!ancestors.getLast().equals(qName)) {
			String lastQName = ancestors.getLast();
			boolean accepted = false;
			if (idDocumentTypeDefinition.getElements().containsKey(lastQName)) {
				Element lDTDElement = idDocumentTypeDefinition.getElements()
						.get(lastQName);
				ElementObserver pDTDElementObserver = ancestorsObservers
						.getLast();

				if (lDTDElement.getType() == ElementTypeEnum.EMPTYTAG) {
					try {
						out.writeEndElement();
					} catch (XMLStreamException e) {
						writeError("Erreur lors de l'écriture : "
								+ e.getMessage());
					}
					ancestors.removeLast();
					ancestorsObservers.removeLast();
					accepted = pDTDElementObserver.acceptNext("#EXIT");
				} else if (lDTDElement.getType() == ElementTypeEnum.NOSTARTENDTAG) {
					ancestors.removeLast();
					ancestorsObservers.removeLast();
					accepted = pDTDElementObserver.acceptNext("#EXIT");
				}

			}
			if (!accepted) {
				String message = "Erreur";
				if (systemId != null) {
					message += " dans le fichier [" + systemId + "]";
				}
				throw new SASgmlException(
						message
								+ " : Element ["
								+ lastQName
								+ "] n'a pas pu être fermé avant la fermeture de l'element ["
								+ qName + "]");
			}
		}

		ElementObserver pDTDElementObserver = ancestorsObservers.getLast();

		boolean accepted = pDTDElementObserver.acceptNext("#EXIT");

		if (accepted) {
			ancestors.removeLast();
			ancestorsObservers.removeLast();

			try {
				out.writeEndElement();
			} catch (XMLStreamException e) {
				writeError("Erreur lors de l'écriture : " + e.getMessage());
			}

		} else {
			throw new SASgmlException("Erreur : la fermeture de  l'élement ["
					+ qName + "] n'est pas autorisée à ce niveau ! ");
		}

		try {
			out.flush();
		} catch (XMLStreamException e) {
			writeError("Erreur lors de l'écriture : " + e.getMessage());
		}

		// LogManager
		// .WriteInfo("Detection de la fin de l'element [" + qName + "]");

	}

	@Override
	public void startElement(ArrayList<String> grpList, String qName,
			Attributes attributes) throws SASgmlException {
		startElement(qName, attributes);
	}

	@Override
	public void endElement(ArrayList<String> grpList, String qName)
			throws SASgmlException {
		endElement(qName);
	}

	@Override
	public void text(String text) throws SASgmlException {
		try {

			if (ancestors.isEmpty()) {
				return;
			}

			// WriteInfo("Detection du texte [" + text + "]");

			// System.out.println(text);

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
			} else if (text.matches("\\s*")) {
				accepted = true;
			}

			if (!accepted) {
				writeWarning("Text [" + text + "] inattendu après ["
						+ pDTDElementObserver.getState().getName() + "] dans ["
						+ ancestors.getLast() + "]");
			}

			out.writeCharacters(idDocumentTypeDefinition.getEntities()
					.decodeString(text));

			out.flush();

		} catch (XMLStreamException e) {
			writeError("Erreur lors de l'écriture : " + e.getMessage());
		}
	}

	@Override
	public void comment(String text) throws SASgmlException {
		try {
			out.writeComment(text);
		} catch (XMLStreamException e) {
			writeError("Erreur lors de l'écriture : " + e.getMessage());
		}
	}

	public void loadDtdFromFile(String pFileName) {

		File lFile = new File(CatalogManager.getDtdPath() + "/" + pFileName);

		if (lFile.exists()) {
			try {
				idDocumentTypeDefinition.load(lFile, true);
				isDtdLoaded = true;
			} catch (Exception e) {
				writeError("Erreur lors du chargement de la DTD : "
						+ e.getMessage());
			}
		} else {
			writeWarning("Impossible de trouver le fichier ["
					+ lFile.getAbsolutePath() + "]");
			isDtdLoaded = false;
		}
	}

	@Override
	public void doctype(String name, String type, String sysid, String pubid)
			throws SASgmlException {

		// WriteWarning("Detection du DOCTYPE [" + name + "]");

		String dtdSystemId = null;

		if (type.equals(Token.SYSTEM)) {
			if (!sysid.equals("")) {
				dtdSystemId = CatalogManager.get("SYSTEM", sysid);
				if (dtdSystemId == null) {
					dtdSystemId = sysid;
				}
			}
		} else if (type.equals(Token.PUBLIC)) {
			if (!sysid.equals("")) {
				dtdSystemId = CatalogManager.get("SYSTEM", sysid);
				if (dtdSystemId == null) {
					dtdSystemId = sysid;
				}
			} else if (!pubid.equals("")) {
				dtdSystemId = CatalogManager.get("PUBLIC", pubid);
			}
		}

		try {
			idDocumentTypeDefinition = subDTDHandler.getDtdDocument();
			isDtdLoaded = idDocumentTypeDefinition.loadAsSubset(
					CatalogManager.getDtdPath(), dtdSystemId);
		} catch (IOException e) {
			writeWarning("Le chargement de la DTD avec SYSTEM ID ["
					+ dtdSystemId + "] a échoué : " + e.getMessage());
		}
		this.dtdSystemId = dtdSystemId;
		rootDtdName = name;

		// WriteWarning("Chargement du DOCTYPE [" + name + "]");

	}

	@Override
	public void markedSection(String decl, String data) throws SASgmlException {
		if (decl.replaceAll("\\s", "").equals(Token.CDATA)) {
			try {
				out.writeCData(data);
			} catch (XMLStreamException e) {
				writeError("Erreur lors de l'écriture : " + e.getMessage());
			}
		}
	}

	@Override
	public void PI(String name, String content) {
		// WriteInfo("Detection du PI [" + content + "]");
		try {
			out.writeProcessingInstruction("SASgml-xml", content);
		} catch (XMLStreamException e) {
			writeError("Erreur lors de l'écriture : " + e.getMessage());
		}

	}

	public DocumentTypeDefinition getDtdDocument() {
		return idDocumentTypeDefinition;
	}

	public void setDtdDocument(DocumentTypeDefinition dtdDocument) {
		this.idDocumentTypeDefinition = dtdDocument;
	}

	public DTDHandler getSubDTDHandler() {
		return subDTDHandler;
	}

	public void setSubDTDHandler(DTDHandler subDTDHandler) {
		this.subDTDHandler = subDTDHandler;
	}

	private String getMsg(String msg) {
		if (systemId != null && outSystemId != null) {
			msg = "Fichier [" + systemId + "] vers Fichier [" + outSystemId
					+ "] : " + msg;
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

	public String getRootDtdName() {
		return rootDtdName;
	}
}
