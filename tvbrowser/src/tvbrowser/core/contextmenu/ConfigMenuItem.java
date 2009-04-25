package tvbrowser.core.contextmenu;

import util.ui.Localizer;
import devplugin.ActionMenu;
import devplugin.ContextMenuIf;
import devplugin.Program;

public class ConfigMenuItem implements ContextMenuIf {
  private static final Localizer mLocalizer = Localizer
      .getLocalizerFor(ConfigMenuItem.class);

  public static final String CONFIG = "######CONFIG######";
  
  private static ConfigMenuItem mInstance;
  
  public static ConfigMenuItem getInstance() {
    if (mInstance == null) {
      mInstance = new ConfigMenuItem();
    }
    return mInstance;
  }
  
  
  private ConfigMenuItem() { }
  
  public ActionMenu getContextMenuActions(Program program) {
    return null;
  }

  public String getId() {
    return CONFIG;
  }

  public String toString() {
    return mLocalizer.msg("configureMenu", "Configure this Menu");
  }
  
}
