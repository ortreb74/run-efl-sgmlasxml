package sasgml.com.model;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.Arrays;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import runefl.casual.ConsoleOutput;
import sasgml.com.catalog.CatalogManager;
import sasgml.com.exception.SASgmlException;
import sasgml.com.handler.DTDHandler;
import sasgml.com.log.LogManager;
import sasgml.com.parsing.CaseInsensitiveFileSearcher;
import sasgml.com.parsing.DTDParser;

public class DocumentTypeDefinition {
	private Entities entities;
	private Elements elements;
	private AttLists attLists;
	private Notations notations;
	private HashSet<String> includes;

	public DocumentTypeDefinition(Entities entities, Elements elements,
			AttLists attlists, Notations notations, HashSet<String> includes) {
		super();
		setEntities(entities);
		setElements(elements);
		setAttLists(attlists);
		setNotations(notations);
		setIncludes(includes);
	}

	public DocumentTypeDefinition() {
		setEntities(new Entities());
		setElements(new Elements());
		setAttLists(new AttLists());
		setNotations(new Notations());
		setIncludes(new HashSet<String>());
	}

	public void merge(DocumentTypeDefinition pDtdDocument) {
		getEntities().putAll(pDtdDocument.getEntities());
		getElements().addAll(pDtdDocument.getElements());
		getAttLists().addAll(pDtdDocument.getAttLists());
		getNotations().addAll(pDtdDocument.getNotations());
	}

	public void arrangeAll() throws SASgmlException, IOException {
		entities.arrange();
		elements.arrange(entities);
		attLists.arrange(entities);
		notations.arrange(entities);
	}

	public void performAll() {
		elements.perform();
		notations.perform(entities);
		attLists.perform(entities, notations);
	}

	public Entities getEntities() {
		return entities;
	}

	public Elements getElements() {
		return elements;
	}

	public AttLists getAttLists() {
		return attLists;
	}

	public Notations getNotations() {
		return notations;
	}

	public void setEntities(Entities entities) {
		this.entities = entities;
	}

	public void setElements(Elements elements) {
		this.elements = elements;
	}

	public void setAttLists(AttLists attlists) {
		this.attLists = attlists;
	}

	public void setNotations(Notations notations) {
		this.notations = notations;
	}

	public HashSet<String> getIncludes() {
		return includes;
	}

	public void setIncludes(HashSet<String> includes) {
		this.includes = includes;
	}

	public void addEntity(Entity entity) {
		if (!entities.containsKey(entity.getName())) {
			entities.put(entity.getName(), entity);
			// entities.put(entity.getName().toUpperCase(), entity);
		}
	}

	public void addElement(Element element) {
		elements.add(element);
	}

	public void addAttList(AttList attlist) {
		attLists.add(attlist);
	}

	public void addInclude(String entityName) {
		includes.add(entityName);
	}

	public void addNotation(Notation notation) {
		notations.add(notation);
	}

	public String getDescription() {
		StringBuilder lStringBuilder = new StringBuilder();
		for (String cName : elements.keySet()) {
			lStringBuilder.append(elements.get(cName).getDescription() + "\n");
			if (attLists.containsKey(cName)) {
				lStringBuilder.append(attLists.get(cName).getDescription()
						+ "\n");
			}
		}
		
		for (String cName : entities.keySet()) {
			lStringBuilder.append(entities.get(cName).getDescription() + "\n");
		}

		return lStringBuilder.toString();
	}

	public String getXmlDescription() {
		StringBuilder lStringBuilder = new StringBuilder();

		Set<String> memory = new HashSet<String>();
		for (String cName : elements.keySet()) {
			Element cElement = elements.get(cName);
			List<String> nameList = Arrays.asList(cElement.getName().split(
					"[|]"));
			if (!memory.containsAll(nameList)) {
				memory.addAll(nameList);
				lStringBuilder.append(cElement.getXmlDescription() + "\n");
				if (attLists.containsKey(cName)) {
					lStringBuilder.append(attLists.get(cName)
							.getXmlDescription() + "\n");
				}
			}
		}

		return lStringBuilder.toString();
	}

	public boolean loadAsSubset(String dirPath, String systemId)
			throws IOException, SASgmlException {
		entities.arrange();

		ConsoleOutput.writeWarning("DocumentTypeDefinition.loadAsSubset dirPath [" + dirPath + "]" + " systemId [" + systemId + "]");
		ConsoleOutput.writeWarning("DocumentTypeDefinition.loadAsSubset taille de includes [" + includes.size() + "]");

		// INCLUDE

		for (String cEntityName : includes) {
			Entity cEntity = null;
			if (entities.containsKey(cEntityName)) {
				cEntity = entities.get(cEntityName);
			} else if (entities.containsKey(cEntityName.toUpperCase())) {
				cEntity = entities.get(cEntityName.toUpperCase());
			} else {
				LogManager
						.writeWarning("Impossible de trouver l'entité d'inclusion ["
								+ cEntityName
								+ "] dans le subset du document SGML.");
			}

			if (cEntity != null) {

				if (cEntity.getModeEnum().equals(ModeEnum.SYSTEM)) {
					CaseInsensitiveFileSearcher iCaseInsensitiveFile = new CaseInsensitiveFileSearcher(
							dirPath + "/" + cEntity.getSysId());
					if (iCaseInsensitiveFile.find()) {
						File includeFile = iCaseInsensitiveFile.getFile();
						DocumentTypeDefinition iDtdDocument = new DocumentTypeDefinition();
						iDtdDocument.load(includeFile);
						merge(iDtdDocument);
					} else {
						LogManager
								.writeWarning("Impossible de trouver le fichier ["
										+ dirPath
										+ "/"
										+ cEntity.getSysId()
										+ "].");
					}

				}
			}
		}

		// INCLUDE SYSTEM ID FROM DOCTYPE DECL

		if (systemId != null) {
			CaseInsensitiveFileSearcher iSystemCaseInsensitiveFile = new CaseInsensitiveFileSearcher(
					dirPath + "/" + systemId);
			if (iSystemCaseInsensitiveFile.find()) {
				File iSystemIncludeFile = iSystemCaseInsensitiveFile.getFile();
				DocumentTypeDefinition iDtdDocument = new DocumentTypeDefinition();
				iDtdDocument.load(iSystemIncludeFile);
				merge(iDtdDocument);
			} else {
				LogManager.writeWarning("Impossible de trouver le fichier ["
						+ dirPath + "/" + systemId + "].");
				return false;
			}
		} else {
			ConsoleOutput.writeError("Impossible de charger complètement la DTD : il y a une dépendance non résolue");
			return false;
		}

		arrangeAll();
		performAll();

		return true;
	}

	public void load(File pFile) throws IOException, SASgmlException {
		load(pFile, false);
	}

	public void load(File pFile, boolean isFirst) throws IOException,
			SASgmlException {
		if (pFile.exists()) {
			DTDParser parser = new DTDParser();
			parser.setDTDHandler(new DTDHandler());


			parser.parse(pFile);

			String lDirPath = pFile.getParentFile().getAbsolutePath();

			DocumentTypeDefinition cDtdDocument = parser.getDTDHandler().getDtdDocument();
			ConsoleOutput.writeWarning("DocumentTypeDefinition.load nombre d'inclusions lus dans la DTD [" + pFile.getAbsolutePath() + "] [" + cDtdDocument.getIncludes().size() + "]");

			cDtdDocument.getEntities().arrange();

			// INCLUDE

			for (String cEntityName : cDtdDocument.getIncludes()) {
				Entity cEntity = null;
				if (cDtdDocument.getEntities().containsKey(cEntityName)) {
					cEntity = cDtdDocument.getEntities().get(cEntityName);
				} else if (cDtdDocument.getEntities().containsKey(
						cEntityName.toUpperCase())) {
					cEntity = cDtdDocument.getEntities().get(
							cEntityName.toUpperCase());
				} else {
					LogManager
							.writeWarning("Impossible de trouver l'entit� d'inclusion ["
									+ cEntityName
									+ "] dans le fichier ["
									+ pFile.getAbsolutePath() + "]");
				}

				if (cEntity != null) {
					if (cEntity.getModeEnum().equals(ModeEnum.SYSTEM)) {
						CaseInsensitiveFileSearcher iCaseInsensitiveFile = new CaseInsensitiveFileSearcher(
								lDirPath + "/" + cEntity.getSysId());
						if (iCaseInsensitiveFile.find()) {
							File includeFile = iCaseInsensitiveFile.getFile();
							DocumentTypeDefinition iDtdDocument = new DocumentTypeDefinition();
							iDtdDocument.load(includeFile);
							cDtdDocument.merge(iDtdDocument);
						} else {
							LogManager
									.writeWarning("Impossible de trouver le fichier ["
											+ lDirPath
											+ "/"
											+ cEntity.getSysId() + "].");
						}
					}

					if (cEntity.getModeEnum().equals(ModeEnum.PUBLIC)) {
						File includeFile = new File(CatalogManager.getDtdPath() + CatalogManager.get("PUBLIC",cEntity.getSysId()));
						DocumentTypeDefinition iDtdDocument = new DocumentTypeDefinition();
						iDtdDocument.load(includeFile);
						cDtdDocument.merge(iDtdDocument);
					}
				}
			}

			if (isFirst) {
				cDtdDocument.arrangeAll();
				cDtdDocument.performAll();
			}

			setEntities(cDtdDocument.getEntities());
			setElements(cDtdDocument.getElements());
			setAttLists(cDtdDocument.getAttLists());
			setNotations(cDtdDocument.getNotations());
			setIncludes(cDtdDocument.getIncludes());

		} else {
			LogManager.writeWarning("Impossible de trouver le fichier ["
					+ pFile.getAbsolutePath() + "]");
		}
	}

	public void wrieTo(String pDestinationilePath) throws IOException {

		Set<String> memory = new HashSet<String>();
		for (String cName : elements.keySet()) {
			Element cElement = elements.get(cName);
			String fileName = cElement.getFileName();
			List<String> nameList = Arrays.asList(cElement.getName().split(
					"[|]"));
			if (!memory.containsAll(nameList)) {
				memory.addAll(nameList);
				Writer out = new BufferedWriter(new OutputStreamWriter(
						new FileOutputStream(pDestinationilePath + "/"
								+ fileName,true), "ISO-8859-1"));

								StringBuilder lStringBuilder = new StringBuilder();
				lStringBuilder.append(cElement.getXmlDescription() + "\n");
				if (attLists.containsKey(cName)) {
					lStringBuilder.append(attLists.get(cName)
							.getXmlDescription() + "\n");
				}
				
				try {
					out.write(lStringBuilder.toString());
				} finally {
					out.close();
				}
			}
		}
	}

}
