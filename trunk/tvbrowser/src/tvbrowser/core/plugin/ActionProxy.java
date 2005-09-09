package tvbrowser.core.plugin;

import java.awt.event.ActionEvent;
import java.beans.PropertyChangeListener;

import javax.swing.Action;

public class ActionProxy implements Action {

  Action mAction;
  PluginProxy mPluginProxy;
  
  public ActionProxy(PluginProxy pluginProxy, Action action) {
    mPluginProxy = pluginProxy;
    mAction = action;
  }

  public Object getValue(String key) {
    return mAction.getValue(key);
  }

  public void putValue(String key, Object value) {
    mAction.putValue(key, value);
  }

  public void setEnabled(boolean b) {
    mAction.setEnabled(b);
  }

  public boolean isEnabled() {
    return mAction.isEnabled();
  }

  public void addPropertyChangeListener(PropertyChangeListener listener) {
    mAction.addPropertyChangeListener(listener);
  }

  public void removePropertyChangeListener(PropertyChangeListener listener) {
    mAction.removePropertyChangeListener(listener);
  }

  public void actionPerformed(ActionEvent e) {
    try {
      System.out.println("XXXXXXXXXXXX");
      mAction.actionPerformed(e);
    } catch (RuntimeException ex) {
      System.out.println("HANDLE ERROR");
      mPluginProxy.handlePluginException(ex);
      
    }
  }

}
