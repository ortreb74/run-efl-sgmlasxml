package runefl.casual;

import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public abstract class CommandLine {

    Map<String,ArrayList<String>> map = new HashMap<String,ArrayList<String>>();

    public CommandLine(String[] args) {
        boolean implicitMode = true;

        String key = "";
        for (String word : args) {
            if (word.equals("-debug")) {
                Constants.debugMode = true;
            } else {
                if (word.startsWith("-")) {
                    implicitMode = false;
                }

                if (implicitMode) {
                    processImplicitMode(word);
                } else {
                    if (word.startsWith("-")) {
                        key = word.substring(1);
                    } else {
                        add(key, word);
                    }
                }
            }
        }

        if (Constants.debugMode) displayContent();

    }

    public void displayContent() {
        for (Map.Entry<String,ArrayList<String>> e : map.entrySet()) {
            ConsoleOutput.write (e.getKey() + "=" + StringUtils.join(e.getValue()," "));
        }
    }

    public List<String> getList(String key) {
        return map.get(key);
    }

    public String getString(String key) {
        return StringUtils.join(map.get(key),"");
    }

    abstract protected void processImplicitMode(String word);

    protected void add(String key, String value) {
        ArrayList<String> list;

        if (!map.containsKey(key)) {
            list = new ArrayList<String>();
            map.put(key,list);
        } else list = map.get(key);

        list.add(value);
    }

    protected void softSet(String key, String value) {
        // cette méthode a pour fonction de n'ajouter la valeur que s'il n'en existe pas une pour la clef

        ArrayList<String> list;

        if (!map.containsKey(key)) {
            list = new ArrayList<String>();
            map.put(key,list);
            list.add(value);
        }
    }

    protected void defaultSet(String key, String value) {
        // cette méthode a pour fonction de donner une valeur a une clef que si elle n'est pas encore définie

        ArrayList<String> list;
        list = new ArrayList<String>();
        map.put(key,list);
        list.add(value);
    }

    public boolean missingKey(String key) {
        return ! map.containsKey(key);
    }
}
