package jumptoprogramplugin;

import java.awt.event.ActionEvent;
import java.lang.reflect.Method;

import util.ui.Localizer;

import devplugin.ActionMenu;
import devplugin.ContextMenuAction;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.PluginManager;
import devplugin.Program;
import devplugin.ProgramReceiveTarget;
import devplugin.Version;

public class JumpToProgramPlugin extends Plugin {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(JumpToProgramPlugin.class);
  private static final Version mVersion = new Version(0,5,0,true);
  private static final String RECEIVE_ID = "JUMP_TO_PROGRAM_###_JUMP_###";
  private final ProgramReceiveTarget[] mReceiveTargets = new ProgramReceiveTarget[] {new ProgramReceiveTarget(this, mLocalizer.msg("name", "JumpToProgram"), RECEIVE_ID)};
  
  public static Version getVersion() {
    return mVersion;
  }
  
  public PluginInfo getInfo() {
    return new PluginInfo(JumpToProgramPlugin.class, mLocalizer.msg("name", "JumpToProgram"), mLocalizer.msg("desc", "Jumps to the selected program in the program table."), "Ren\u00e9 Mach", "GPL");
  }
  
  public ActionMenu getContextMenuActions(final Program program) {
    return new ActionMenu(new ContextMenuAction(mLocalizer.msg("menu", "Show program in program table")) {
      public void actionPerformed(ActionEvent e) {
        select(program);
      }
    });
  }
  
  private boolean select(final Program program) {
    try {
      Class<? extends PluginManager> pluginManager = getPluginManager().getClass();
      Method selectProgram = pluginManager.getMethod("selectProgram", new Class<?>[] {Program.class});
      selectProgram.invoke(getPluginManager(), new Object[] {program});
    } catch (Exception e1) {
      getPluginManager().scrollToProgram(program);
    }
    
    return true;
  }
  
  public String getPluginCategory() {
    return Plugin.OTHER_CATEGORY;
  }
  
  public boolean canReceiveProgramsWithTarget() {
    return true;
  }
  
  public boolean receivePrograms(Program[] programArr, ProgramReceiveTarget receiveTarget) {
    if(programArr != null && programArr.length > 0 && receiveTarget.isReceiveTargetWithIdOfProgramReceiveIf(this, RECEIVE_ID)) {
      return select(programArr[0]);
    }
    
    return false;
  }
  
  public ProgramReceiveTarget[] getProgramReceiveTargets() {
    return mReceiveTargets;
  }
}
