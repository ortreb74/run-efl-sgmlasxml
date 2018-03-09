package sasgml.com.evo279;

import java.io.File;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import sasgml.com.exception.SASgmlException;
import sasgml.com.model.DocumentTypeDefinition;
import sasgml.com.model.Entity;
import sasgml.com.xml.SASgml;

public class Run {

	public static void main(String[] args) throws IOException, SASgmlException {
		// -mode dtdparse -dtdpath i:\liens\e2\dtd\custom\entities -input i:\liens\e2\dtd\custom\entities\mem.dtd -output memall.dtd
		
		// et là il manque -dtdpath i:\liens\e2\dtd\custom\entities
		// SgmlManager.load("i:/liens/e2/dtd/custom/entities", "");
		SASgml.load("i:/liens/e2/dtd/custom/entities", "");
		
		// ce que ça faire
		DocumentTypeDefinition lDtdDocument = new DocumentTypeDefinition();
		lDtdDocument.load(new File("i:/liens/e2/dtd/custom/entities/mem.dtd"),true);
		
		Map<String,Integer> entities = new HashMap<String,Integer>();
		
		PrintWriter file3 = new PrintWriter("3.txt");
		
		// les valeurs ne sont pas bonnes et c'est un mystère		
		for (Entry<String, Entity> association : lDtdDocument.getEntities().entrySet()) {  
			
			Entity entity = association.getValue();
			
			if (entity.getMode().equals("SDATA")) {
				if (entity.getValue().length() != 1) {
					System.out.println(entity.getDescription());
				}
				// System.out.println(entity.getFileName() + "\t" + association.getKey() + "\t" + entity.getName() + "\t" + String.format("%04x",(int) entity.getValue().toCharArray()[0]));
				file3.println(entity.getFileName() + "\t" + association.getKey() + "\t" + entity.getName() + "\t" + String.format("%04x",(int) entity.getValue().toCharArray()[0]));
				entities.put(entity.getName(),Integer.valueOf((int) entity.getValue().toCharArray()[0]));
			}			
		}
		
		file3.close();
		
		PrintWriter file = new PrintWriter("a.txt");
		
		for (Entry<String,Integer> atom : sortByValue(entities).entrySet()) {
			file.println(atom.getKey() + "\t" + atom.getValue());
		}	
		
		file.close();
		
		file = new PrintWriter("b.txt");
		
		Integer prevValue = -1;
		List<String> possibleKeys = new ArrayList<String>();
		
		for (Entry<String,Integer> atom : sortByValue(entities).entrySet()) {
			if (atom.getKey().equals("thinsp")) {
				System.out.println(atom.getKey() + "\t" + atom.getValue());
			}
			if (prevValue.equals(atom.getValue())) {
				possibleKeys.add(atom.getKey()); 				
			} else {
				if (possibleKeys.size() > 1) {					
					file.print(prevValue);
					
					for (String key : possibleKeys) {
						file.print("\t" + key);
					}
					
					file.println();
				}
				
				possibleKeys.clear();
				possibleKeys.add(atom.getKey());
				prevValue = atom.getValue();				
			}
		}	
		
		file.close();
	}

    public static <K, V extends Comparable<? super V>> Map<K, V> 
    sortByValue( Map<K, V> map )
{
    List<Map.Entry<K, V>> list =
        new LinkedList<Map.Entry<K, V>>( map.entrySet() );
    Collections.sort( list, new Comparator<Map.Entry<K, V>>()
    {
        public int compare( Map.Entry<K, V> o1, Map.Entry<K, V> o2 )
        {
            return (o1.getValue()).compareTo( o2.getValue() );
        }
    } );

    Map<K, V> result = new LinkedHashMap<K, V>();
    for (Map.Entry<K, V> entry : list)
    {
        result.put( entry.getKey(), entry.getValue() );
    }
    return result;
}
	
}
