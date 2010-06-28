/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (martin@tvbrowser.org)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */

package devplugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;


/**
 * Action class for context menu entries (can be used in {@link ActionMenu} class).
 */
public class ContextMenuAction extends AbstractAction {

    private ActionListener mListener;

    public ContextMenuAction(String title, Icon icon) {
      putValue(Action.NAME, title);
      putValue(Action.SMALL_ICON, icon);
    }

    public ContextMenuAction(String title) {
      putValue(Action.NAME, title);
    }

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


