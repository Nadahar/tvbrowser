package tvbrowser.core.contextmenu;

import devplugin.ActionMenu;
import devplugin.ContextMenuIf;
import devplugin.Program;

public class SeparatorMenuItem implements ContextMenuIf {

  public static final String SEPARATOR = "######SEPARATOR######";
  
  public ActionMenu getContextMenuActions(Program program) {
    return null;
  }

  public String getId() {
    return SEPARATOR;
  }

  public String toString() {
    return "--------------------";
  }
  
}
