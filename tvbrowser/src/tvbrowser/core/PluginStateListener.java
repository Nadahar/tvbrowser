package tvbrowser.core;


public interface PluginStateListener {
  
  public void pluginActivated(devplugin.Plugin p);
  public void pluginDeactivated(devplugin.Plugin p); 
  public void pluginLoaded(devplugin.Plugin p);
  public void pluginUnloaded(devplugin.Plugin p);
  
}