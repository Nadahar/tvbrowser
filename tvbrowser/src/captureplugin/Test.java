/*
 * Created on 12.08.2004
 */
package captureplugin;

import java.io.File;
import java.io.FileInputStream;
import java.io.ObjectInputStream;


/**
 * @author bodum
 */
public class Test {

    public static void main(String[] args) {

        CapturePlugin plug = new CapturePlugin();
        
        try {
           
            plug.readData(new ObjectInputStream(new FileInputStream(new File("/home/bodum/.tvbrowser/captureplugin.CapturePlugin.dat"))));

            plug.execute();
            
            System.exit(0);
        
        
        } catch (Exception e) {
            e.printStackTrace();
        }
        
    }
}
