package program;

import sasgml.com.xml.ConversionModeEnum;
import sasgml.com.xml.SASgml;
import hamdiutils.sgml.SgmlManager;

public class Sgml2Xml {

    public static void main(String[] args) {

        SASgml.setConversionMode(ConversionModeEnum.Soft);

        SASgml.load("c:/dtd", "c:/catalog/el.xml");

        SgmlManager.convertSGMLtoXMLWithMessage(args[0], "a.xml");

    }


    /*
    public static void convertXMLtoSGML(String pSourceFilePath,
                                        String pDestinationFilePath) throws Exception {
        SASgml.convertXMLtoSGML(pSourceFilePath, pDestinationFilePath);
    }

    public static void convertSGMLtoXMLWithMessage(String path1, String path2) {
        System.writeInfo("Conversion du fichier " + path1);
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
    */

}
