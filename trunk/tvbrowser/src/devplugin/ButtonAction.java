package devplugin;

import javax.swing.*;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

/**
 * Created by: Martin Oberhauser (martin@tvbrowser.org)
 * Date: 01.01.2005
 * Time: 11:54:02
 */
public class ButtonAction extends AbstractAction {

  private ActionListener mListener;

  public ButtonAction() {

  }

  public void setText(String text) {
    putValue(Action.NAME, text);
  }

  public void setSmallIcon(Icon icon) {
    putValue(Action.SMALL_ICON, icon);
  }

  public void setBigIcon(Icon icon) {
    putValue(Plugin.BIG_ICON, icon);
  }

  public void setShortDescription(String description) {
    putValue(Action.SHORT_DESCRIPTION, description);
  }

   public void setActionListener(ActionListener listener) {
    mListener = listener;
  }

  public void actionPerformed(ActionEvent event) {
    if (mListener != null) {
      mListener.actionPerformed(event);
    }
  }
}
