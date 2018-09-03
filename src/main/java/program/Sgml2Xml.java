package program;

import runefl.casual.ConsoleOutput;
import sasgml.com.xml.ConversionModeEnum;
import sasgml.com.xml.SASgml;

public class Sgml2Xml {

    public static void main(String[] args) {
        try {
            CommandLineSgml2Xml commandLine = new CommandLineSgml2Xml(args);

            SASgml.setConversionMode(ConversionModeEnum.Soft);
            SASgml.load(commandLine.getString("dtdPath"), commandLine.getString("catalogFilePath"));

            if (commandLine.missingKey("input")) {
                commandLine.displayContent();
                ConsoleOutput.stopOnError("Missing parameter : input");
            }

            if (commandLine.missingKey("output")) {
                commandLine.displayContent();
                ConsoleOutput.stopOnError("Missing parameter : output");
            }

            ConsoleOutput.write("Conversion du fichier " + commandLine.getString("input"));

            SASgml.convertSGMLtoXML(commandLine.getString("input"), commandLine.getString("output"));
            ConsoleOutput.write("Fichier " + commandLine.getString("output") + " exporté");
        } catch (Exception e) {
            e.printStackTrace();
            ConsoleOutput.stopOnError("Erreur " + e.getMessage());
        }
    }

}
