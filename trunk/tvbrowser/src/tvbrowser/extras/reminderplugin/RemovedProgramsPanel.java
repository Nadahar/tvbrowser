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

package tvbrowser.extras.reminderplugin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ItemEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;

import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;

import devplugin.AfterDataUpdateInfoPanel;
import devplugin.Program;
import tvbrowser.core.plugin.PluginManagerImpl;
import util.settings.ProgramPanelSettings;
import util.ui.ProgramList;

public class RemovedProgramsPanel extends AfterDataUpdateInfoPanel {

  private static final util.ui.Localizer mLocalizer
     = util.ui.Localizer.getLocalizerFor(RemovedProgramsPanel. class );
  private JCheckBox mDisable;
  private ProgramList mProgramList;

  public RemovedProgramsPanel(Program[] programs) {
    init(programs);
  }

  private void init(Program[] programs) {
    setLayout(new BorderLayout(6,6));

    JLabel lb = new JLabel(mLocalizer.msg("header","<html>Die folgenden Sendungen, an die sie erinnert werden wollten, sind in der aktualisierten Programmvorschau nicht mehr enthalten:</html>"));
    mDisable = new JCheckBox(mLocalizer.msg("dontShowAnymore","Don't show anymore"));
    mDisable.addItemListener(e -> {
      ReminderPlugin.getInstance().getSettings().setProperty("showRemovedDialog", String.valueOf(e.getStateChange() == ItemEvent.DESELECTED));
    });
    
    add(lb, BorderLayout.NORTH);
    
    mProgramList = new ProgramList(programs, ProgramPanelSettings.getShowOnlyDateAndTitleSettings());
    mProgramList.addMouseListener(new MouseAdapter() {
      @Override
      public void mouseReleased(MouseEvent e) {
        if(e.isPopupTrigger()) {
          showPopup(e);
        }
      }
      
      @Override
      public void mousePressed(MouseEvent e) {
        if(e.isPopupTrigger()) {
          showPopup(e);
        }
      }
    });
    
    add(new JScrollPane(mProgramList), BorderLayout.CENTER);
    add(mDisable, BorderLayout.SOUTH);

    setPreferredSize(new Dimension(300,200));
  }
  
  /**
   * Shows the Popup
   * 
   * @param e Mouse-Event
   */
  private void showPopup(MouseEvent e) {
    int row = mProgramList.locationToIndex(e.getPoint());

    mProgramList.setSelectedIndex(row);

    Program p = (Program) mProgramList.getSelectedValue();

    JPopupMenu menu = PluginManagerImpl.getInstance().createRemovedProgramContextMenu(p);//PluginManagerImpl.getInstance().createPluginContextMenu(p, ReminderPluginProxy.getInstance());
    menu.show(mProgramList, e.getX(), e.getY());
  }

  @Override
  public void closed() {}
}
