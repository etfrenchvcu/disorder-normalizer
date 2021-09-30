/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package tool;

import java.io.FileOutputStream;
import java.io.IOException;

/**
 *
 * @author
 */
public class Logger {
            
    private static FileOutputStream log;

    public static void setLogFile(FileOutputStream logFile) {
        log = logFile;
    }

    public static void writeLogFile(String string) throws IOException {
        log.write((string+"\n").getBytes());
    }      
    
}
