


package tvbrowser.ui.filter;

public abstract class AbstractFilter implements devplugin.ProgramFilter {
  
 
  public abstract boolean accept(devplugin.Program prog);  
  public abstract String getName();  
  public String toString() {
      return getName();
  }

}