package tvbrowser.core.plugin;

import javax.swing.Action;

import devplugin.ActionMenu;

public class ActionMenuProxy extends ActionMenu {
 
  private PluginProxy mPluginProxy;
  
  public ActionMenuProxy(PluginProxy pluginProxy, ActionMenu actionMenu) {
    super(actionMenu);
    mPluginProxy = pluginProxy;
  }
  
  public Action getAction() {
    return new ActionProxy(mPluginProxy, super.getAction());
  }
  
  public ActionMenu[] getSubItems() {
    ActionMenu[] items =super.getSubItems(); 
    
    ActionMenu[] result = new ActionMenuProxy[items.length];
    
    for (int i=0;i<items.length;i++) {
      result[i] = new ActionMenuProxy(mPluginProxy, items[i]);
    }
    
    return result;
  }
}