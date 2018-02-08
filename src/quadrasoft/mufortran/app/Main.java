package quadrasoft.mufortran.app;

import quadrasoft.mufortran.app.forms.LoadManager;
import quadrasoft.mufortran.app.forms.WelcomePanel;
import quadrasoft.mufortran.fortran.FileTypesManager;
import quadrasoft.mufortran.general.Log;
import quadrasoft.mufortran.general.Session;

import javax.swing.*;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;

public class
Main {
    /*
     * Entry point. We create a Loadmanager wich handles all application setup. Then
     * we call the WelcomePanel wich greets the user. All of this happens while the
     * main app is being setup.
     */
    private static LoadManager loading;
    private static WelcomePanel splashScreen;
    private static MainFrame app;

    public static void main(String[] args) {
        SwingUtilities.invokeLater(() -> {
            iniFrames();
            loading.setLocationRelativeTo(null);
            loading.loadConfig();
            LoadManager.loadTheme();

            app = new MainFrame();
            app.toFront();
            app.setVisible(true);

            /*
             * Do we need to open a file just now ?
             */
            if (args.length > 0) {
                for (String argument : args) {
                    if (FileTypesManager.isFortranSource(argument) || FileTypesManager.isProjectFile(argument)) {
                        app.open(argument);
                        splashScreen.close();
                        splashScreen = null;
                    } else {
                        Log.send("Error: Unsupported file specified : \"" + argument + "\"");
                    }
                }

            } else {
                // If no file is specified, we show the typical splash-screen
                app.toFront();
                splashScreen.loadRecents();
                splashScreen.setVisible(true);
                splashScreen.setLocationRelativeTo(null);

            }
            // Loading is now complete, we close the loading screen
            loading.close();
            loading = null;
        });
    }

    private static void iniFrames(){
        // The loading window with loading bar
        loading = new LoadManager();
        // The splash-screen, ready to pop when load is complete
        splashScreen = new WelcomePanel();
        // We need to detect when the splash-screen is toggle off to process version check and update main frame
        splashScreen.addWindowListener(new WindowAdapter() {

            @Override
            public void windowClosing(WindowEvent e) {
                app.updateTrees();
                splashScreen.close();
                splashScreen = null;
                if (Session.autoCheck()) {
                    try {
                        LoadManager.checkVersion();
                    } catch (IOException e1) {
                        Log.send("Error: Could not get version from update server");
                        e1.printStackTrace();
                    }
                }

            }
        });
    }
}
