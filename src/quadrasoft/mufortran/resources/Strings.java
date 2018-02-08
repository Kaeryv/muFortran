package quadrasoft.mufortran.resources;

import java.util.HashMap;
import java.util.Map;

public class Strings {
    private static Map<String, String> _strings = new HashMap<>();

    public static void load() {
        _strings.put("version", "v1.4c");
        /*
		 * FORMS
		 */
        _strings.put("Forms:FirstLaunchWarning", "This is the first time you launch µFortran, thanks for using it. ;)"
                + "\nThe next frame will ask your for a default folder (workspace) where we can store projects. "
                + "\nYou can for example create a \"fortran\" folder in your documents ...");
        _strings.put("Forms:GotoDL", "go to download page ?");
        _strings.put("Forms:NoSelection","Nothing selected.");
        _strings.put("Forms:SelectedChars", "Characters selected : ");
		/*
		 * Terminal
		 */
        _strings.put("Console:UserPrefsCreation", "Creating config file.");
        _strings.put("Console:VersionCheckDlFail", "Error: Could not reach remote .version");
        _strings.put("Console:NewVersion", "A new version of µFortran is available");

        _strings.put("Console:OsInfo", "Running on " + System.getProperty("os.name") + " v "
                + System.getProperty("os.version") + " " + System.getProperty("os.arch"));
        _strings.put("Console:JavaInfo", "Detected JRE from " + System.getProperty("java.vendor") + " v"
                + System.getProperty("java.version") + " " + System.getProperty("sun.arch.data.model") + " bits");
        _strings.put("Console:UnableToGetVersion", "Error: Unable to check for newer version.");
		/*
		 * Urls
		 */
        _strings.put("app:name", "µFortran");
        _strings.put("url:version", "https://raw.githubusercontent.com/Kaeryv/muFortran/master/VERSION");
        _strings.put("app:projectextension", ".µprj");
    }

    public static String s(String key) {
        return _strings.get(key);
    }
}
