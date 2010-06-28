/*
 * Created on 12.08.2004
 *
 * TODO To change the template for this generated file go to
 * Window - Preferences - Java - Code Style - Code Templates
 */
package tvbrowser.core.plugin;

/**
 *
 * @author Til Schneider, www.murfman.de
 */
public abstract class PluginStateAdapter implements PluginStateListener {

  public void pluginActivated(PluginProxy plugin) {}
  
  public void pluginDeactivated(PluginProxy plugin) {}
  
  public void pluginLoaded(PluginProxy plugin) {}

  public void pluginUnloaded(PluginProxy plugin) {}
  
}
