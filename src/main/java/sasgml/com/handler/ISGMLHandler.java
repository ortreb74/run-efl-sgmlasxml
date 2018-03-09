package sasgml.com.handler;

import java.util.ArrayList;

import sasgml.com.exception.SASgmlException;
import sasgml.com.model.Attributes;

public interface ISGMLHandler {

	public void endDocument() throws SASgmlException;

	public void startDocument() throws SASgmlException;

	public void startElement(String qName,
			Attributes attributes) throws SASgmlException;

	public void endElement(String qName)
			throws SASgmlException;

	public void startElement(ArrayList<String> grpList,String qName,
			Attributes attributes) throws SASgmlException;

	public void endElement(ArrayList<String> grpList,String qName)
			throws SASgmlException;

	public void text(String text) throws SASgmlException;

	public void comment(String string) throws SASgmlException;

	public void doctype(String name,String type,String sysid,String pubid) throws SASgmlException;
	
	public void markedSection(String decl, String data) throws SASgmlException;

	public void PI(String name, String content);

}
