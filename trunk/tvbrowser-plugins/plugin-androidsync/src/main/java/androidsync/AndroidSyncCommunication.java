package androidsync;

import devplugin.Plugin;
import devplugin.PluginCommunication;

public class AndroidSyncCommunication extends PluginCommunication {
  private AndroidSync mPlugin;
  
  AndroidSyncCommunication(AndroidSync plugin) {
    mPlugin = plugin;
  }
  
  @Override
  public int getVersion() {
    return 1;
  }

  public String[] getStoredChannels() {
    String[] result = null;
    
    if(Plugin.getPluginManager().getActivatedPluginForId(mPlugin.getId()) != null) {
      result = mPlugin.getStoredChannels();
    }
    
    return result;
  }
}
