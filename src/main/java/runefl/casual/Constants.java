package runefl.casual;

public class Constants {

    // https://stackoverflow.com/questions/2712970/get-maven-artifact-version-at-runtime

    static boolean debugMode = false;

    public String getVersion() {
        return getClass().getPackage().getImplementationVersion();
    }
}
