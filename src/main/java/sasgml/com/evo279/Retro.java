package sasgml.com.evo279;

import sasgml.com.xml.SASgml;

public class Retro {

	public static void main(String[] args) throws Exception {
		SASgml.load("i:/liens/e2/dtd/custom/entities", "");
		
		String path1 = "mint-entities.xml";
		String path2 = "mint-entities-retro.sgm";
		SASgml.convertXMLtoSGML(path1, path2);
	}

}
