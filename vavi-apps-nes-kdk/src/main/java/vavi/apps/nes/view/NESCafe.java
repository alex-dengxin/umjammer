/*
 * http://www.nescafeweb.com/ 
 */

package vavi.apps.nes.view;

import vavi.apps.nes.NES;


/**
 * Main NESCafe Class.
 * 
 * @author David de Niese
 * @version 0.56f
 */
public final class NESCafe {
    /**
     * The Current Version
     */
    public static final double version = 0.56;

    /**
     * Startup Method.
     */
    public static void main(String[] args) {
        // Welcome Message
        System.out.println("Starting NESCafe v" + version);
        // Create Graphical User Interface and NES Machine
        if (!checkVMNumber()) {
            System.err.println("NESCafe cannot run on this computer. Please upgrade to the latest JVM");
            System.exit(1);
        }
        try {
            // Create the GUI and NES
            GUI gui = new GUI();
            NES nes = new NES();
            // Initialize the NES Machine and the Graphical User Interface
            nes.init(gui);
            gui.init(nes);
            // Load Default ROM from JAR
            nes.cartLoadDefault();
        } catch (Exception e) {
            System.err.println("NESCafe cannot run on this computer. Please upgrade to the latest JVM");
            System.exit(1);
        }
    }

    /**
     * The required JVM Version.
     */
    static final String JDK_REQUIRED_VERSION = "1.2";

    /**
     * True if you have the required Version of the JVM.
     */
    static final boolean checkVMNumber() {
        // Determine the VM
        String vmVersion = System.getProperty("java.vm.version");
        // Check for Correct Version
        return (vmVersion.compareTo(JDK_REQUIRED_VERSION) > 0);
    }
}
