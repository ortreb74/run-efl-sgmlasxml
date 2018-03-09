package sasgml.com.handler;

import sasgml.com.exception.SASgmlException;
import sasgml.com.model.AttList;
import sasgml.com.model.Element;
import sasgml.com.model.Entity;
import sasgml.com.model.Notation;

public interface IDTDHandler {

	public void endDocumentTypeDefinition() throws SASgmlException;

	public void startDocumentTypeDefinition() throws SASgmlException;

	public void comment(String string) throws SASgmlException;

	public void entity(Entity entity) throws SASgmlException;

	public void element(Element element)
			throws SASgmlException;

	public void attlist(AttList attlist) throws SASgmlException;

	public void includeEntity(String entityName) throws SASgmlException;

	public void PI(String name, String content);

	public void notation(Notation dtdNotation) ;
}
