package tvbrowser.ui.pluginview.contextmenu;

import javax.swing.tree.TreePath;

import tvbrowser.ui.pluginview.PluginTree;
import devplugin.ActionMenu;
import devplugin.Plugin;
import devplugin.SettingsTab;

public class PluginBasedPluginContextMenu extends PluginContextMenu {
  private Plugin mPlugin;

  public PluginBasedPluginContextMenu(PluginTree tree, TreePath path, Plugin plugin, ActionMenu[] menus) {
    super(tree, path, menus);
    mPlugin = plugin;
  }

  @Override
  protected ActionMenu getButtonAction() {
    return mPlugin.getButtonAction();
  }

  @Override
  protected boolean hasSettingsTab() {
    SettingsTab settingsTab = mPlugin.getSettingsTab();
    return settingsTab != null;
  }

  @Override
  protected String getPluginId() {
    return mPlugin.getId();
  }

}
