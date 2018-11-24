package com.graphserver.app;

import com.graphserver.swing.GraphApp;

import javax.swing.*;

public class App {
    /**
     * Make sure that you MonetDB is up and then run the App.main();
     * @param args no params needed
     */
    public static void main(String... args) {

        SwingUtilities.invokeLater(new Runnable() {

            public void run() {
                GraphApp ex = new GraphApp();
                ex.setVisible(true);
            }
        });
    }
}
