package compat;

import java.io.File;

import devplugin.Plugin;

public final class IOCompat {
  /**
   * Check given path if it is a relative path of the TV-Browser settings home directory.
   * <p>
   * @param path The path to check.
   * @return A relative path if given path is in subdirectory of TV-Browser settings or the given path if not. 
   */
  public static String checkForRelativePath(String path) {
    if(path != null && (path.contains("\\") || path.contains("/"))) {
      File audioPath = new File(path);
      
      String[] homeParts = Plugin.getPluginManager().getTvBrowserSettings().getTvBrowserUserHome().replace("\\", "/").split("/");
      String[] pathParts = audioPath.getParent().replace("\\", "/").split("/");
      
      int i = 0;
      
      while(homeParts.length > i && pathParts.length > i && homeParts[i].equals(pathParts[i])) {
        i++;
      }
      
      if(i > 0) {
        StringBuilder relativeValue = new StringBuilder();
        
        if(i < homeParts.length) {
          for(int j = i; j < homeParts.length; j++) {
            relativeValue.append("../");
          }
        }
        else {
          relativeValue.append("./");
        }
        
        for(int j = i; j < pathParts.length; j++) {
          relativeValue.append(pathParts[j]).append("/");
        }
        
        relativeValue.append(audioPath.getName());
        
        return relativeValue.toString();
      }
    }
    
    return path;
  }
  
  /**
   * Translates a given path that can be relative or absolute to an absolute path.
   * <p>
   * @param path The path to check and translate to an absolute path.
   * @return A full path.
   * @since 3.3.4
   */
  public static String translateRelativePath(String path) {
    if(path != null && (path.startsWith("..") || path.startsWith("."))) {
      if(path.startsWith("./")) {
        path = path.substring(2);
      }
      
      String[] pathParts = path.replace("\\", "/").split("/");
      String[] homeParts = Plugin.getPluginManager().getTvBrowserSettings().getTvBrowserUserHome().replace("\\", "/").split("/");
      
      int homePartsToUse = homeParts.length;
      int i = 0;
      
      while(i < pathParts.length && pathParts[i].equals("..")) {
        i++;
        homePartsToUse--;
      }
      
      if(homePartsToUse >= 0 && pathParts.length > i) {
        StringBuilder pathToUse = new StringBuilder();
        
        for(int j = 0; j < homePartsToUse; j++) {
          pathToUse.append(homeParts[j]).append("/");
        }
        for(int j = i; j < pathParts.length; j++) {
          pathToUse.append(pathParts[j]).append("/");
        }
        
        if(pathToUse.length() > 1 && pathToUse.toString().endsWith("/")) {
          pathToUse.deleteCharAt(pathToUse.length()-1);
          
          path = pathToUse.toString();
        }
      }
    }
    
    return path;
  }
}
