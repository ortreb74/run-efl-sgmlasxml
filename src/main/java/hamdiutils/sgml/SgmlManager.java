package hamdiutils.sgml;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.io.UnsupportedEncodingException;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.transform.TransformerFactoryConfigurationError;

import org.w3c.dom.Document;
import org.xml.sax.SAXException;

import sasgml.com.exception.SASgmlException;
import sasgml.com.log.LogManager;
import sasgml.com.xml.ConversionModeEnum;
import sasgml.com.xml.SASgml;

public class SgmlManager {

	// public static void main(String[] args) throws IOException {
	//
	// LogManager.printProgramAndArgs("SASgml", args);
	//
	// String procMode = "sgml2xml";
	// String input = "";
	// String output = "";
	// String dtdpath = "";
	// String catalogFilePath = "";
	//
	// String[] iFilePathList = null;
	// String[] oFilePathList = null;
	//
	// File[] iFileList = null;
	// File oFile = null;
	//
	// if (args.length >= 3) {
	//
	// } else {
	// LogManager.WriteError("Le nombre d'argument est incorrect !");
	// System.exit(0);
	// }
	//
	// if (args[0].equals("-sgml2xml")) {
	// procMode = "sgml2xml";
	// } else if (args[0].equals("-xml2sgml")) {
	// procMode = "xml2sgml";
	// } else if (args[0].equals("-dtdmerge")) {
	// procMode = "dtdmerge";
	// } else {
	// LogManager.WriteError("Le mode de conversion n'est reconnu !");
	// System.exit(0);
	// }
	//
	// Integer outputIndexStart = 0;
	//
	// if (args[1].equals("-input")) {
	// for (int i = 2; i < args.length; i++) {
	// if (args[i].equals("-output")) {
	// outputIndexStart = i;
	// break;
	// } else {
	// input += " " + args[i];
	// }
	// }
	// } else {
	// LogManager.WriteError("La donn�e � convertir n'est pas reconnue !");
	// System.exit(0);
	// }
	//
	// if (outputIndexStart == 0) {
	// LogManager
	// .WriteError("La donn�e de d�stination n'est pas reconnue !");
	// System.exit(0);
	// } else {
	// for (int i = outputIndexStart + 1; i < args.length; i++) {
	// if (args[i].equals("-dtdpath")) {
	// if (i + 1 < args.length) {
	// dtdpath = args[i + 1];
	// }
	// outputIndexStart = i + 1;
	// break;
	// } else {
	// output += " " + args[i];
	// }
	// }
	// }
	//
	// if (args.length > outputIndexStart + 2) {
	// if (args[outputIndexStart+1].equals("-catalog")) {
	// catalogFilePath = args[outputIndexStart + 2];
	// }
	// }
	//
	// input = input.trim();
	// output = output.trim();
	//
	// if (input.equals("")) {
	// LogManager.WriteError("La donn�e � convertir n'est pas reconnue !");
	// System.exit(0);
	// } else {
	// iFilePathList = input.split("\\s", -1);
	// }
	//
	// if (output.equals("")) {
	// LogManager
	// .WriteError("La donn�e de d�stination n'est pas reconnue !");
	// System.exit(0);
	// } else {
	// oFilePathList = output.split("\\s", -1);
	// }
	//
	// if (iFilePathList.length == 1) {
	// File iFile = new File(iFilePathList[0]);
	// if (iFile.exists()) {
	// if (iFile.isDirectory()) {
	// iFileList = iFile
	// .listFiles(new ModeFileNameFilter(procMode));
	// } else {
	// iFileList = new File[1];
	// iFileList[0] = iFile;
	// }
	// } else {
	// LogManager.WriteError("Le fichier ou r�pertoire sp�cifi� ["
	// + iFilePathList[0] + "] est introuvable !");
	// System.exit(0);
	// }
	// } else {
	// iFileList = new File[iFilePathList.length];
	// for (Integer i = 0; i < iFilePathList.length; i++) {
	// iFileList[i] = new File(iFilePathList[i]);
	// }
	// }
	//
	// if (oFilePathList.length == 1) {
	// oFile = new File(oFilePathList[0]);
	// } else {
	// LogManager
	// .WriteError("La donn�e de d�stination n'est pas reconnue !");
	// System.exit(0);
	// }
	//
	// boolean isFileOutput = oFile.getAbsolutePath().toLowerCase()
	// .endsWith(".xml")
	// | oFile.getAbsolutePath().toLowerCase().endsWith(".sgm");
	//
	// if (!isFileOutput) {
	// if (!oFile.exists()) {
	// if (!oFile.mkdir()) {
	// LogManager
	// .WriteError("Impossible de cr�er le r�pertoire sp�cifi� ["
	// + oFile.getAbsolutePath() + "] !");
	// System.exit(0);
	// }
	// }
	// }
	//
	// SASgml.load(dtdpath, catalogFilePath);
	//
	// // try {
	// // SASgml.parseSgmlDtd("I:/liens/e2/DTD/custom/entities/tcb.dtd",
	// // "I:/tcb.dtd");
	// // } catch (SASgmlException e1) {
	// // // TODO Auto-generated catch block
	// // e1.printStackTrace();
	// // }
	//
	// // try {
	// // parseSgmlDtd("I:/liens/e2/DTD/custom/entities/tcb.dtd","I:/tcb.dtd");
	// // } catch (SASgmlException e) {
	// // // TODO Auto-generated catch block
	// // e.printStackTrace();
	// // }
	//
	// // LogManager
	// //
	// .WriteInfo("Fin du chargement des fichiers de catalog et des DTD XML");
	//
	// if (procMode.equals("sgml2xml")) {
	// if (iFileList.length == 1) {
	// if (isFileOutput) {
	// convertSGMLtoXMLWithMessage(iFileList[0].getAbsolutePath(),
	// oFile.getAbsolutePath());
	// } else {
	// convertSGMLtoXMLWithMessage(
	// iFileList[0].getAbsolutePath(),
	// oFile.getAbsolutePath()
	// + "/"
	// + iFileList[0].getName().replaceAll(
	// "[.][^.]+$", ".xml"));
	// }
	// } else {
	// if (isFileOutput) {
	// LogManager
	// .WriteError("Le param�tre de sorti doit r�pr�senter un chemin de r�pertoire valide ["
	// + oFile.getAbsolutePath() + "] !");
	// System.exit(0);
	// } else {
	// final File oFinalFile = oFile;
	// Parallel.blockingFor(Arrays.asList(iFileList),
	// new Parallel.Operation<File>() {
	// public void perform(File lFile) {
	// convertSGMLtoXMLWithMessage(
	// lFile.getAbsolutePath(),
	// oFinalFile.getAbsolutePath()
	// + "/"
	// + lFile.getName()
	// .replaceAll(
	// "[.][^.]+$",
	// ".xml"));
	// }
	// });
	// // for (File cFile : iFileList) {
	// // convertSGMLtoXMLWithMessage(
	// // cFile.getAbsolutePath(),
	// // oFile.getAbsolutePath()
	// // + "/"
	// // + cFile.getName().replaceAll(
	// // "[.][^.]+$", ".xml"));
	// // }
	// }
	// }
	// } else if (procMode.equals("xml2sgml")) {
	// if (iFileList.length == 1) {
	// if (isFileOutput) {
	// convertXMLtoSGMLWithMessage(iFileList[0].getAbsolutePath(),
	// oFile.getAbsolutePath());
	// } else {
	// convertXMLtoSGMLWithMessage(
	// iFileList[0].getAbsolutePath(),
	// oFile.getAbsolutePath()
	// + "/"
	// + iFileList[0].getName().replaceAll(
	// "[.][^.]+$", ".sgm"));
	// }
	// } else {
	// if (isFileOutput) {
	// LogManager
	// .WriteError("Le param�tre de sorti doit r�pr�senter un chemin de r�pertoire valide ["
	// + oFile.getAbsolutePath() + "] !");
	// System.exit(0);
	// } else {
	// final File oFinalFile = oFile;
	// Parallel.blockingFor(Arrays.asList(iFileList),
	// new Parallel.Operation<File>() {
	// public void perform(File lFile) {
	// convertXMLtoSGMLWithMessage(
	// lFile.getAbsolutePath(),
	// oFinalFile.getAbsolutePath()
	// + "/"
	// + lFile.getName()
	// .replaceAll(
	// "[.][^.]+$",
	// ".sgm"));
	// }
	// });
	// // for (File cFile : iFileList) {
	// // convertXMLtoSGML(
	// // cFile.getAbsolutePath(),
	// // oFile.getAbsolutePath()
	// // + "/"
	// // + cFile.getName().replaceAll(
	// // "[.][^.]+$", ".sgm"));
	// // }
	// }
	// }
	// } else {
	//
	// final File oFinalFile = oFile;
	//
	// ArrayList<String> dtdList = new ArrayList<String>();
	// for (int i = 10; i < 99; i++) {
	// dtdList.add(String.valueOf(i) + "Isoamsr.ent");
	// }
	//
	// Parallel.blockingFor(dtdList, new Parallel.Operation<String>() {
	// public void perform(String pFileName) {
	// try {
	// SASgml.parseSgmlDtd(CatalogManager.getDtdPath() + "/"
	// + pFileName.substring(2),
	// oFinalFile.getAbsolutePath() + "/" + pFileName);
	// } catch (IOException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// } catch (SASgmlException e) {
	// // TODO Auto-generated catch block
	// e.printStackTrace();
	// }
	// }
	// });
	// }
	// }

	public static String rootDtdName;

	public static void convertXMLtoSGMLWithMessage(String path1, String path2) {
		LogManager.writeInfo("Convertion du fichier " + path1);
		try {
			SASgml.convertXMLtoSGML(path1, path2);
			LogManager.writeInfo("Fichier " + path2 + " export�");
		} catch (Exception e) {
			new File(path2).delete();
			LogManager.writeError("Erreur lors de l'export du fichier " + path2
					+ " : [" + e.getMessage() + "]");
			e.printStackTrace();
		}
	}

	public static void convertSGMLtoXMLWithMessage(String path1, String path2) {
		LogManager.writeInfo("Convertion du fichier " + path1);
		try {
			SASgml.convertSGMLtoXML(path1, path2);
			LogManager.writeInfo("Fichier " + path2 + " export�");
		} catch (SASgmlException e) {
			new File(path2).delete();
			LogManager.writeError("Erreur lors de l'export du fichier " + path2
					+ " : [" + e.getMessage() + "]");
			e.printMessage();
		} catch (IOException e) {
			new File(path2).delete();
			LogManager.writeError("Erreur lors de l'export du fichier " + path2
					+ " : [" + e.getMessage() + "]");
			e.printStackTrace();
		}

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
