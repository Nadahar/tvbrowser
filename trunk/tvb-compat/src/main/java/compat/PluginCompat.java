package compat;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.lang.reflect.Method;

import javax.swing.JMenuItem;
import javax.swing.JPopupMenu;

import devplugin.ActionMenu;
import devplugin.Program;
import devplugin.Version;
import tvbrowser.TVBrowser;
import tvbrowser.core.contextmenu.ContextMenuManager;
import tvbrowser.extras.searchplugin.SearchPluginProxy;
import tvbrowser.ui.mainframe.MainFrame;
import util.ui.Localizer;
import util.ui.menu.MenuUtil;

public final class PluginCompat {
  private static Localizer LOCALIZER = Localizer.getLocalizerFor(PluginCompat.class);
  
  public static final String CATEGORY_ALL = "all";
  public static final String CATEGORY_REMOTE_CONTROL_SOFTWARE = "remote_soft";
  public static final String CATEGORY_REMOTE_CONTROL_HARDWARE = "remote_hard";
  public static final String CATEGORY_ADDITONAL_DATA_SERVICE_SOFTWARE = "datasources_soft";
  public static final String CATEGORY_ADDITONAL_DATA_SERVICE_HARDWARE = "datasources_hard";
  public static final String CATEGORY_RATINGS = "ratings";
  public static final String CATEGORY_OTHER = "misc";
  
  public static JPopupMenu createRemovedProgramContextMenu(Program program) {
    JPopupMenu result = null;
    
    if(TVBrowser.VERSION.compareTo(new Version(3,20,true)) >= 0) {
      try {
        Method m = ContextMenuManager.class.getDeclaredMethod("createRemovedProgramContextMenu", Program.class);
        result = (JPopupMenu)m.invoke(ContextMenuManager.getInstance(), program);
      } catch (Exception e) {
        // ignore 
      }
    }
    
    if(result == null) {
      result = createRemovedProgramContextMenuLegacy(program);
    }
    
    return result;
  }
  
  private static JPopupMenu createRemovedProgramContextMenuLegacy(final Program program) {
    JPopupMenu menu = new JPopupMenu();
    
    ActionMenu repetitionSearch = SearchPluginProxy.getInstance().getContextMenuActions(program);
    
    if(repetitionSearch != null) {
      menu.add(MenuUtil.createMenuItem(repetitionSearch));
    }
    
    JMenuItem item = new JMenuItem(LOCALIZER.msg("scrollToPlaceOfProgram","Scroll to last place of program in program table"));
    item.setFont(MenuUtil.CONTEXT_MENU_PLAINFONT);
    item.addActionListener(new ActionListener() {
        @Override
        public void actionPerformed(ActionEvent e) {
          MainFrame.getInstance().goTo(program.getDate());
          MainFrame.getInstance().showChannel(program.getChannel());

          try {
            Method m = MainFrame.class.getMethod("scrollToTime", int.class, boolean.class);
            m.invoke(MainFrame.getInstance(), program.getStartTime(), false);
          }catch(Exception e2) {
            MainFrame.getInstance().scrollToTime(program.getStartTime());  
          }
          
          try {
            Method m = MainFrame.class.getMethod("showProgramTableTabIfAvailable");
            m.invoke(MainFrame.getInstance());
          }catch(Exception e2) {
            // ignore  
          }      
        }
    });
    
    menu.add(item);
    
    return menu;
  }
}
