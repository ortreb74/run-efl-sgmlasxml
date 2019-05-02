package program;

import runefl.casual.CommandLine;

public class CommandLineSgml2Xml extends CommandLine {

    public CommandLineSgml2Xml(String[] args) {
        super(args);

        defaultSet("dtdPath", "c:/dtd");
        defaultSet("catalogFilePath", "c:/catalog/catalog-els.xml");
    }

    @Override
    protected void processImplicitMode(String word) {
        if (word.endsWith(".sgm")) {
            defaultSet("input",word);
            defaultSet("output",word.replaceFirst(".sgm$",".xml"));
        }

    }
}
