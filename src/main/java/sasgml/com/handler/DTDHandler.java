package sasgml.com.handler;

import runefl.casual.ConsoleOutput;
import sasgml.com.exception.SASgmlException;
import sasgml.com.log.LogManager;
import sasgml.com.model.AttList;
import sasgml.com.model.DocumentTypeDefinition;
import sasgml.com.model.Element;
import sasgml.com.model.Entity;
import sasgml.com.model.Notation;

public class DTDHandler implements IDTDHandler {

	private DocumentTypeDefinition dtdDocument;

	private String dtdName;
	private String dtdType;
	private String sysId;
	private String pubId;

	public DTDHandler() {
		super();
		setDtdDocument(new DocumentTypeDefinition());
	}

	@Override
	public void endDocumentTypeDefinition() throws SASgmlException {
		// TODO Auto-generated method stub

	}

	@Override
	public void startDocumentTypeDefinition() throws SASgmlException {
		// TODO Auto-generated method stub

	}

	@Override
	public void comment(String string) throws SASgmlException {
		// TODO Auto-generated method stub

	}

	@Override
	public void entity(Entity entity) throws SASgmlException {
		// System.out.println(entity.GetDescription());
		// TODO Auto-generated method stub
		// if (!entity.getName().contains("%")) {
		// System.out.println(entity.getName() + " : " +
		// entity.getTextContent());
		// }

		// System.out.println(entity.getName() + " : " +
		// entity.getTextContent());

		dtdDocument.addEntity(entity);

				// LogManager.WriteInfo("Detection de l'entit� ["
		// + entity.getTextContent() + "]");

	}

	@Override
	public void element(Element element) throws SASgmlException {
		// TODO Auto-generated method stub
		// System.out.println(element.GetDescription());
		// if (element.getName().contains("("))
		// {
		// for (String cName : element.getName().replace("(", "").replace(")",
		// "").split("[\\|,]"))
		// {
		// if(!cName.equals(""))
		// {
		// Element cDTDElement = new Element(cName, element.getValue(),
		// element.getTypeString());
		// elements.put(cName, cDTDElement);
		// }
		// }
		// }
		// else
		// {
		// elements.put(element.getName(), element);
		// }

		//LogManager.writeInfo("Detection de l'element [" + element.getTextContent() + "]");
		dtdDocument.addElement(element);

	}

	@Override
	public void attlist(AttList attlist) throws SASgmlException {
		// TODO Auto-generated method stub
		// LogManager.WriteInfo("Detection de l'attributeList ["
		// + attlist.getTextContent() + "]");

		dtdDocument.addAttList(attlist);
	}

	@Override
	public void includeEntity(String entityName) throws SASgmlException {
		dtdDocument.addInclude(entityName);
	}

	@Override
	public void PI(String name, String content) {
		// TODO Auto-generated method stub

	}

	@Override
	public void notation(Notation notation) {
		dtdDocument.addNotation(notation);
	}

	public DocumentTypeDefinition getDtdDocument() {
		return dtdDocument;
	}

	public void setDtdDocument(DocumentTypeDefinition dtdDocument) {
		this.dtdDocument = dtdDocument;
	}

	public void doctype(String dtdName, String dtdType, String sysId,
			String pubId) {
		this.dtdName = dtdName;
		this.dtdType = dtdType;
		this.sysId = sysId;
		this.pubId = pubId;
	}

	public String getDtdName() {
		return dtdName;
	}

	public String getDtdType() {
		return dtdType;
	}

	public String getSysId() {
		return sysId;
	}

	public String getPubId() {
		return pubId;
	}

	public void setDtdName(String dtdName) {
		this.dtdName = dtdName;
	}

	public void setDtdType(String dtdType) {
		this.dtdType = dtdType;
	}

	public void setSysId(String sysId) {
		this.sysId = sysId;
	}

	public void setPubId(String pubId) {
		this.pubId = pubId;
	}

}
