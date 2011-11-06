package jumptoprogramplugin;

import java.awt.event.ActionEvent;
import java.lang.reflect.Method;

import util.ui.Localizer;

import devplugin.ActionMenu;
import devplugin.ContextMenuAction;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.Version;

public class JumpToProgramPlugin extends Plugin {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(JumpToProgramPlugin.class);
  private static final Version mVersion = new Version(0,3,0,false);
  
  public static Version getVersion() {
    return mVersion;
  }
  
  public PluginInfo getInfo() {
    return new PluginInfo(JumpToProgramPlugin.class, mLocalizer.msg("name", "JumpToProgram"), mLocalizer.msg("desc", "Jumps to the selected program in the program table."), "Ren\u00e9 Mach", "GPL");
  }
  
  public ActionMenu getContextMenuActions(final Program program) {
    return new ActionMenu(new ContextMenuAction(mLocalizer.msg("menu", "Show program in program table")) {
      public void actionPerformed(ActionEvent e) {
        
        try {
          Class pluginManager = getPluginManager().getClass();
          Method selectProgram = pluginManager.getMethod("selectProgram", new Class<?>[] {Program.class});
          selectProgram.invoke(getPluginManager(), new Object[] {program});
        } catch (Exception e1) {
          getPluginManager().scrollToProgram(program);
        }
      }
    });
  }
  
  public String getPluginCategory() {
    return Plugin.OTHER_CATEGORY;
  }
}
