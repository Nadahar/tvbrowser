/*
 * JarWinJavaExeLauncher
 * 
 * Copyright (C) 2007 René Mach
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
 */
package laucher;
 
//import java.io.BufferedReader;
import java.io.File;
import java.io.FileFilter;
/*import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;*/
import java.util.ArrayList;

/**
 * A class that launches a Java application with java.exe
 * without showing the dos console. Needed for Java gui applications
 * that want to react on a Windows shutdown with a shutdown hook.
 * 
 * @author René Mach
 */
public class JarWinJavaExeLauncher {
  public JarWinJavaExeLauncher(String[] args) {
    ArrayList<String> dValues = new ArrayList<String>();
    ArrayList<String> cmdValues = new ArrayList<String>();
    String startApp = null;
    
    for(int i = 0; i < args.length; i++) {
      if(args[i].toLowerCase().startsWith("-d") || args[i].toLowerCase().startsWith("-x")) {
        dValues.add(args[i]);
      }
      else if(args[i].toLowerCase().startsWith("/start")) {
        startApp = args[++i];
      }
      else if(args[i].toLowerCase().startsWith("/nosplash")) {
        dValues.add("-splash:");
      }
      else {
        cmdValues.add(args[i]);
      }
    }
    
    ArrayList<String> CMD = new ArrayList<String>();
    
    CMD.add(findAcceptableJavaVersion());
    
    //String CMD = "\"" + findAcceptableJavaVersion() + "\" ";
    
    for(String opt : dValues)
      CMD.add(opt);
      //CMD += opt + " ";
    
    //CMD += "-jar " + startApp;
    CMD.add("-jar");
    CMD.add(startApp);
    
    for(String cmd : cmdValues )
      CMD.add(cmd);
      //CMD += " " + cmd;    
    //System.out.println(CMD);
    try {
      /*Process p = */Runtime.getRuntime().exec(CMD.toArray(new String[CMD.size()]), null, new File(System.getProperty("user.dir")));
      
      //new StreamReaderThread(p.getInputStream()).start();
      //new StreamReaderThread(p.getErrorStream()).start();
      
    }catch(Exception e) {//e.printStackTrace();
      System.exit(1);
    }
    
    System.exit(0);
  }
  
  private String findAcceptableJavaVersion() {
    try {
      String progDir = System.getenv("ProgramFiles");
    
      if(progDir != null) {
        File[] jres = new File(progDir,"Java").listFiles(new FileFilter() {
          public boolean accept(File pathname) {
            return pathname.getName().toLowerCase().indexOf("jre") != -1;
          }
        });
      
        if(jres != null && jres.length > 0) {
          int index = 0;
        
          for(int i = 1; i < jres.length; i++) {
            if(new File(jres[i].getAbsolutePath() + "\\bin\\java.exe").isFile() && getJreFromName(jres[i].getName()) > getJreFromName(jres[index].getName()))
              index = i;
          }
          
          if(new File(jres[index].getAbsolutePath() + "\\bin\\java.exe").isFile())
            return jres[index].getAbsolutePath() + "\\bin\\java.exe";
        }
      }
    }catch(Throwable e) {e.printStackTrace();}
    
    return "java.exe";
  }
  
  private int getJreFromName(String name) throws Exception {
    String value = name.substring(name.indexOf(".")-1);
    
    String[] version = value.split("[.]");
    int parsedVersion = 0;
    
    if(version != null && version.length == 3) {      
      parsedVersion = Integer.parseInt(version[0]) * 10000;
      parsedVersion += Integer.parseInt(version[1]) * 1000;
      
      if(version[2].indexOf("_") == -1)
        parsedVersion += Integer.parseInt(version[2]) * 100;
      else {
        String[] subVersion = version[2].split("_");
        
        parsedVersion += Integer.parseInt(subVersion[0]) * 100;
        parsedVersion += Integer.parseInt(subVersion[1]);
      }
    }
    
    return parsedVersion;
  }
  
  public static void main(String[] args) {
    new JarWinJavaExeLauncher(args);
  }
  
  /*private class StreamReaderThread extends Thread {
    private InputStream mInput;
    
    public StreamReaderThread(InputStream in) {
      mInput = in;
    }
  
    public void run() {
      try {
        BufferedReader reader = new BufferedReader(new InputStreamReader(mInput));
        while (reader.readLine() != null);
      } catch (IOException e) {}
      System.exit(0);
    }
  }*/
}
