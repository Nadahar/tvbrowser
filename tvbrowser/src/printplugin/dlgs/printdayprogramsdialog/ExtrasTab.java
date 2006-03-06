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

package printplugin.dlgs.printdayprogramsdialog;

import java.awt.BorderLayout;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JCheckBox;
import javax.swing.JPanel;

import printplugin.dlgs.components.ProgramPreviewPanel;
import printplugin.settings.ProgramIconSettings;
import util.ui.TabLayout;

public class ExtrasTab extends JPanel {

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(ExtrasTab.class);


  private ProgramPreviewPanel mProgramPreviewPanel;
  private JCheckBox mShowPluginMarkingCb;

  public ExtrasTab(Frame dlgParent) {
    super();

    setLayout(new BorderLayout());

    JPanel content = new JPanel(new TabLayout(1));

    JPanel previewPn = new JPanel(new BorderLayout());

    previewPn.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("programItem","Program item")));
    mProgramPreviewPanel = new ProgramPreviewPanel(dlgParent);
    previewPn.add(mProgramPreviewPanel, BorderLayout.CENTER);

    mShowPluginMarkingCb = new JCheckBox(mLocalizer.msg("showPluginMarkings","Show plugin markings"));
    mShowPluginMarkingCb.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        mProgramPreviewPanel.setShowPluginMarking(mShowPluginMarkingCb.isSelected());
      }
    });

    content.add(previewPn);
    content.add(mShowPluginMarkingCb);

    add(content, BorderLayout.NORTH);
  }

  public void setProgramIconSettings(ProgramIconSettings programIconSettings) {
    mProgramPreviewPanel.setProgramIconSettings(programIconSettings);
    mShowPluginMarkingCb.setSelected(programIconSettings.getPaintPluginMarks());
  }

  public ProgramIconSettings getProgramIconSettings() {
    return mProgramPreviewPanel.getProgramIconSettings();
  }

}
