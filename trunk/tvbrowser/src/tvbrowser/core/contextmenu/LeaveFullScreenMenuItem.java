package tvbrowser.core.contextmenu;

import util.ui.Localizer;
import devplugin.ActionMenu;
import devplugin.ContextMenuIf;
import devplugin.Program;

public class LeaveFullScreenMenuItem implements ContextMenuIf {
  private static final Localizer mLocalizer = Localizer
      .getLocalizerFor(LeaveFullScreenMenuItem.class);

  protected static final String LEAVEFULLSCREEN = "######LEAVEFULLSCREEN######";
  
  private static LeaveFullScreenMenuItem mInstance;
  
  public static LeaveFullScreenMenuItem getInstance() {
    if (mInstance == null) {
      mInstance = new LeaveFullScreenMenuItem();
    }
    return mInstance;
  }
  
  
  private LeaveFullScreenMenuItem() { }
  
  public ActionMenu getContextMenuActions(Program program) {
    return null;
  }

  public String getId() {
    return LEAVEFULLSCREEN;
  }

  public String toString() {
    return mLocalizer.msg("leaveFullScreen", "Leave fullscreen mode");
  }
  
}
