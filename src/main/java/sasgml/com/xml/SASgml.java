package sasgml.com.xml;

import java.io.BufferedWriter;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStreamWriter;
import java.io.PrintStream;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;

import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.w3c.dom.Document;
import org.w3c.dom.ls.DOMImplementationLS;
import org.w3c.dom.ls.LSOutput;
import org.w3c.dom.ls.LSSerializer;
import org.xml.sax.SAXException;

import sasgml.com.catalog.CatalogManager;
import sasgml.com.entity.EntityManager;
import sasgml.com.exception.SASgmlException;
import sasgml.com.handler.SGMLHandler;
import sasgml.com.handler.XMLHandler;
import sasgml.com.log.LogManager;
import sasgml.com.model.DocumentTypeDefinition;
import sasgml.com.model.Entities;
import sasgml.com.parsing.SGMLParser;

// 3.00.02 : version livrée par le build
// 3.00.02_getDtd : publication de la dtd parsée : rootDtdName (en mode static !)
//                : la classe publie un attribut rootDtdName :

public class SASgml {

	public static final String Version = "3.00.02_getDtd";
	private static ConversionModeEnum ConversionMode = ConversionModeEnum.Strict;

	public static void setPrintStream(PrintStream out) {
		LogManager.setPrintStream(out);
	}

	// SIECHAINE-70
	public static String rootDtdName;

	public static void parseSgmlDtd(String pSourceFilePath,
			String pDestinationilePath) throws IOException, SASgmlException {
		DocumentTypeDefinition lDtdDocument = new DocumentTypeDefinition();
		lDtdDocument.load(new File(pSourceFilePath),true);
		Writer out = new BufferedWriter(new OutputStreamWriter(
				new FileOutputStream(pDestinationilePath), "utf-8"));
		try {
			out.write(lDtdDocument.getDescription());
		} finally {
			out.close();
		}
	}
	
	public static void convertSgmlToXmlDtd(String pSourceFilePath,
			String pDestinationilePath) throws IOException, SASgmlException {
		DocumentTypeDefinition lDtdDocument = new DocumentTypeDefinition();
		lDtdDocument.load(new File(pSourceFilePath),true);
		lDtdDocument.wrieTo(pDestinationilePath);
	}

	public static void convertSGMLtoXML(String pSourceFilePath,
			String pDestinationFilePath) throws SASgmlException, IOException {

		SGMLParser lSGMLParser = new SGMLParser();
		SGMLHandler lSGMLHandler = new SGMLHandler(pDestinationFilePath);
		lSGMLHandler.setSystemId(pSourceFilePath);
		lSGMLParser.setSGMLHandler(lSGMLHandler);
		lSGMLParser.parse(new File(pSourceFilePath));
	}

	public static Document getSGMLasXMLDocument(String pSourceFilePath)
			throws UnsupportedEncodingException, SAXException, IOException,
			SASgmlException, ParserConfigurationException {
		DocumentBuilderFactory factory = DocumentBuilderFactory.newInstance();

		factory.setNamespaceAware(true);
		DocumentBuilder builder = factory.newDocumentBuilder();

		String xml = getSGMLasXMLString(pSourceFilePath);
		return builder.parse(new ByteArrayInputStream(xml.getBytes("utf-8")));

	}

	public static void convertXMLStringToSGML(String pXmlString,
			String pDestinationFilePath) {

		SAXParserFactory factory = SAXParserFactory.newInstance();
		try {
			InputStream is = new ByteArrayInputStream(
					pXmlString.getBytes("utf-8"));
			SAXParser saxParser = factory.newSAXParser();
			XMLHandler handler = new XMLHandler(pDestinationFilePath);
			saxParser.setProperty(
					"http://xml.org/sax/properties/lexical-handler", handler);
			saxParser.parse(is, handler);
		} catch (Throwable err) {
			err.printStackTrace();
		}

	}

	public static void convertXMLtoSGML(String pSourceFilePath,
			String pDestinationFilePath) throws Exception {

		SAXParserFactory factory = SAXParserFactory.newInstance();
		InputStream is = new FileInputStream(pSourceFilePath);
		SAXParser saxParser = factory.newSAXParser();
		XMLHandler handler = new XMLHandler(pDestinationFilePath);
		handler.setSystemId(pSourceFilePath);
		saxParser.setProperty("http://xml.org/sax/properties/lexical-handler",
				handler);
		saxParser.parse(is, handler);
	}

	public static void convertXMLtoSGML(Document pSourceDocument,
			String pDestinationFilePath)
			throws TransformerFactoryConfigurationError, Exception {

		DOMImplementationLS domImplementationLS = (DOMImplementationLS) pSourceDocument
				.getImplementation().getFeature("LS", "3.0");
		LSOutput lsOutput = domImplementationLS.createLSOutput();
		ByteArrayOutputStream outputStream = new ByteArrayOutputStream();
		lsOutput.setByteStream(outputStream);
		LSSerializer lsSerializer = domImplementationLS.createLSSerializer();
		lsSerializer.write(pSourceDocument, lsOutput);
		outputStream.close();

		InputStream is = new ByteArrayInputStream(outputStream.toByteArray());
		SAXParserFactory factory = SAXParserFactory.newInstance();
		SAXParser saxParser = factory.newSAXParser();
		XMLHandler handler = new XMLHandler(pDestinationFilePath);
		handler.setSystemId(pSourceDocument.getBaseURI());
		saxParser.setProperty("http://xml.org/sax/properties/lexical-handler",
				handler);
		saxParser.parse(is, handler);
	}

	public static String getSGMLasXMLString(String pSourceFilePath)
			throws SASgmlException, IOException {
		SGMLParser lSGMLParser = new SGMLParser();
		StringWriter lStringWriter = new StringWriter();
		SGMLHandler lSGMLHandler = new SGMLHandler(lStringWriter);
		lSGMLHandler.setSystemId(pSourceFilePath);
		lSGMLParser.setSGMLHandler(lSGMLHandler);
		lSGMLParser.parse(new File(pSourceFilePath));

		rootDtdName = lSGMLHandler.getRootDtdName();

		lStringWriter.flush();
		lStringWriter.close();
		return lStringWriter.toString();
	}

	public static void load(String dtdPath, String catalogFilePath) {
		CatalogManager.setDtdPath(dtdPath);
		CatalogManager.load();
		if (new File(catalogFilePath).exists()) {
			CatalogManager.load(catalogFilePath);
		}
		Entities.load();
	}

	public static ConversionModeEnum getConversionMode() {
		return ConversionMode;
	}

	public static void setConversionMode(ConversionModeEnum conversionMode) {
		ConversionMode = conversionMode;
	}

	public static void loadEntityReplacement(String entityReplacement) {		
		EntityManager.load(entityReplacement);		
	}

}
