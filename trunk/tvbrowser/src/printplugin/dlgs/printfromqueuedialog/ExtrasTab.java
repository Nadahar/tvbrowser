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

package printplugin.dlgs.printfromqueuedialog;

import printplugin.dlgs.components.ProgramPreviewPanel;
import printplugin.settings.ProgramIconSettings;

import javax.swing.*;
import java.awt.*;

import util.ui.TabLayout;

public class ExtrasTab extends JPanel {

  /** The localizer for this class. */
   private static final util.ui.Localizer mLocalizer
       = util.ui.Localizer.getLocalizerFor(ExtrasTab.class);


  private ProgramPreviewPanel mProgramPreviewPanel;

  public ExtrasTab(final Frame dlgParent) {
    super();

    setLayout(new BorderLayout());
    JPanel content = new JPanel(new TabLayout(1));
    JPanel previewPn = new JPanel(new BorderLayout());
    previewPn.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg("programItem","Program item")));
    mProgramPreviewPanel = new ProgramPreviewPanel(dlgParent);
    previewPn.add(mProgramPreviewPanel, BorderLayout.CENTER);

    content.add(previewPn);

    add(content, BorderLayout.NORTH);
  }

  public void setProgramIconSettings(ProgramIconSettings programIconSettings) {
    mProgramPreviewPanel.setProgramIconSettings(programIconSettings);
  }

  public ProgramIconSettings getProgramIconSettings() {
    return mProgramPreviewPanel.getProgramIconSettings();
  }

  public void setDateFont(Font f) {
    mProgramPreviewPanel.setDateFont(f);
  }

  public Font getDateFont() {
    return mProgramPreviewPanel.getDateFont();
  }

}
