package tvbrowser.ui.pluginview.contextmenu;

import javax.swing.tree.TreePath;

import tvbrowser.core.plugin.PluginProxy;
import tvbrowser.ui.pluginview.PluginTree;
import devplugin.ActionMenu;

public class ProxyBasedPluginContextMenu extends PluginContextMenu {

  public ProxyBasedPluginContextMenu(PluginTree tree, TreePath path, PluginProxy proxy, ActionMenu[] menus) {
    super(tree, path, menus);
    this.mProxy = proxy;
  }

  private PluginProxy mProxy;

  @Override
  protected ActionMenu getButtonAction() {
    return mProxy.getButtonAction();
  }

  @Override
  protected String getPluginId() {
    return mProxy.getId();
  }

  @Override
  protected boolean hasSettingsTab() {
    return mProxy.getSettingsTab() != null;
  }

}
