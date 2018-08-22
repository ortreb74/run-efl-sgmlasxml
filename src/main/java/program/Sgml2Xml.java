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

}
