package devplugin;

import javax.swing.*;
import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;

/**
 * Created by: Martin Oberhauser (martin@tvbrowser.org)
 * Date: 01.01.2005
 * Time: 11:45:28
 */

public class ContextMenuAction extends AbstractAction {

    private ActionListener mListener;

    public ContextMenuAction() {

    }

    public void setText(String text) {
      putValue(Action.NAME, text);
    }

    public void setSmallIcon(Icon icon) {
      putValue(Action.SMALL_ICON, icon);
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


