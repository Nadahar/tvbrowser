/*
 * CapturePlugin by Andreas Hessel (Vidrec@gmx.de), Bodo Tasche
 * 
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date: 2006-03-06 17:29:38 +0100 (Mo, 06 Mär 2006) $
 *   $Author: troggan $
 * $Revision: 1944 $
 */
package captureplugin.drivers.elgatodriver;

import java.io.File;
import java.io.FileWriter;

import captureplugin.drivers.utils.StreamReaderThread;

/**
 * This class is the Interface to the AppleScript-System. It runs the Scripts
 * 
 * @author bodum
 */
public class AppleScriptRunner {
    /** Default TimeOut in Seconds */
    private int mTimeOut = 30;
    
    /**
     * Creates the Runner with a default Timeout of 30 secs
     */
    public AppleScriptRunner() {
    }

    /**
     * Create the Runner
     * @param timeout Timeout in Seconds for the execution of the Scripts
     */
    public AppleScriptRunner(int timeout) {
        mTimeOut = timeout;
    }

    /**
     * Executes the AppleScripts
     * 
     * @param script Script to execute
     * @return Output if exec was successfull, null if Error occured
     */
    public String executeScript(String script) {
        try {
            File file = File.createTempFile("osascript", "temp");
            
            FileWriter writer = new FileWriter(file);
            writer.write(script);
            writer.close();
            
            Process p = Runtime.getRuntime().exec("osascript " + file.getAbsolutePath());
          
            String output = "";
            int time = 0;
            
            StreamReaderThread out = new StreamReaderThread(p.getInputStream(),true, "UTF-8");
            StreamReaderThread error = new StreamReaderThread(p.getErrorStream(),false, "UTF-8");
            out.start();
            error.start();
            
            // wait until the process has exited, max MaxTimouts
            
            if (mTimeOut > 0 ){
                while (time < mTimeOut * 1000) {
                    Thread.sleep(100);
                    time += 100;
                    try {
                        p.exitValue();
                        break;
                    } catch (IllegalThreadStateException e) {
                    }
                }
            } else {
                while (true) {
                    Thread.sleep(100);
                    try {
                        p.exitValue();
                        break;
                    } catch (IllegalThreadStateException e) {
                    }
                }
            }
            
            while (time < mTimeOut * 1000) {
                Thread.sleep(100);
                time += 100;
                try {
                    p.exitValue();
                    break;
                } catch (IllegalThreadStateException e) {
                }
            }

            // get the process output
            
            if(!out.isAlive())
              output = out.getOutput();

            if (p.exitValue() >= 0)
                return output;
            
        } catch (Exception e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        
        return null;
    }

}