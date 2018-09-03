package hamdiutils.sgml;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import runefl.casual.ConsoleOutput;
import sasgml.com.exception.SASgmlException;
import sasgml.com.log.LogManager;
import sasgml.com.xml.ConversionModeEnum;
import sasgml.com.xml.SASgml;

public class SgmlManager {

	public static String rootDtdName;

	public static void convertSGMLtoXMLWithMessage(String path1, String path2) throws IOException, SASgmlException {
		ConsoleOutput.write("Conversion du fichier " + path1);

		SASgml.convertSGMLtoXML(path1, path2);
		ConsoleOutput.write("Fichier " + path2 + " exporté");
	}

	
	public static void setConversionMode(ConversionModeEnum conversionMode) {
		SASgml.setConversionMode(conversionMode);
	}
	
	
	public static void setLogPrintStream(PrintStream out) {
		LogManager.setPrintStream(out);
	}

	public static void parseSgmlDtd(String pSourceFilePath,
			String pDestinationilePath) throws IOException, SASgmlException {
		SASgml.parseSgmlDtd(pSourceFilePath, pDestinationilePath);
	}

	public static void convertSGMLtoXML(String pSourceFilePath,
			String pDestinationFilePath) throws SASgmlException, IOException {
		SASgml.convertSGMLtoXML(pSourceFilePath, pDestinationFilePath);
	}

	public static Document getSGMLasXMLDocument(String pSourceFilePath)
			throws UnsupportedEncodingException, SAXException, IOException,
			SASgmlException, ParserConfigurationException {
		Document result = SASgml.getSGMLasXMLDocument(pSourceFilePath);

		rootDtdName = SASgml.rootDtdName;

		return result;
	}

	public static void convertXMLStringToSGML(String pXmlString,
			String pDestinationFilePath) {
		SASgml.convertXMLStringToSGML(pXmlString, pDestinationFilePath);
	}

	public static void convertXMLtoSGML(String pSourceFilePath,
			String pDestinationFilePath) throws Exception {
		SASgml.convertXMLtoSGML(pSourceFilePath, pDestinationFilePath);
	}

	public static void convertXMLtoSGML(Document pSourceDocument,
			String pDestinationFilePath)
			throws TransformerFactoryConfigurationError, Exception {
		SASgml.convertXMLtoSGML(pSourceDocument, pDestinationFilePath);
	}

	public static String getSGMLasXMLString(String pSourceFilePath)
			throws SASgmlException, IOException {
		return SASgml.getSGMLasXMLString(pSourceFilePath);
	}

	public static void load(String dtdPath) {
		SASgml.load(dtdPath, "");
	}

	public static void load(String dtdPath, String catalogFilePath) {
		SASgml.load(dtdPath, catalogFilePath);
	}

	public static void loadEntityReplacement(String entityReplacement) {
		SASgml.loadEntityReplacement(entityReplacement);
	}

}
