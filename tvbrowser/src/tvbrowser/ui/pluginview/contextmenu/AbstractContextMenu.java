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



package tvbrowser.ui.pluginview.contextmenu;


import javax.swing.*;
import javax.swing.tree.TreePath;
import java.awt.event.ActionEvent;


/**
 * Created by: Martin Oberhauser (martin@tvbrowser.org)
 * Date: 03.01.2005
 * Time: 22:12:32
 */
public abstract class AbstractContextMenu implements ContextMenu {

  /** The localizer for this class. */
    private static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(AbstractContextMenu.class);

  private JTree mTree;

  protected AbstractContextMenu(JTree tree) {
    mTree = tree;
  }

  protected Action getCollapseExpandAction(final TreePath treePath) {

    final boolean mIsExpanded = mTree.isExpanded(treePath);

    Action action = new AbstractAction(){
      public void actionPerformed(ActionEvent e) {
        if (mIsExpanded) {
          mTree.collapsePath(treePath);
        }
        else {
          mTree.expandPath(treePath);
        }
      }
    };
    if (mIsExpanded) {
      action.putValue(Action.NAME, mLocalizer.msg("collapse","collapse"));
    }
    else {
      action.putValue(Action.NAME, mLocalizer.msg("expand","expand"));
    }

    return action;
  }
}
