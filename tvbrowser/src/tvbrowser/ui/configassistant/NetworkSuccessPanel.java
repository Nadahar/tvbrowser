/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
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
package tvbrowser.ui.configassistant;

import java.awt.BorderLayout;

import javax.swing.JPanel;

import tvbrowser.core.ChannelList;
import tvbrowser.core.tvdataservice.ChannelGroupManager;
import tvbrowser.ui.mainframe.MainFrame;
import util.ui.UiUtilities;
import util.ui.progress.Progress;
import util.ui.progress.ProgressWindow;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

class NetworkSuccessPanel extends AbstractCardPanel {
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(NetworkSuccessPanel.class);

  private JPanel mContent;
  
  public NetworkSuccessPanel(PrevNextButtons btns) {
    super(btns);
  }

  public JPanel getPanel() {
    mContent = new JPanel(new BorderLayout());

    mContent.add(new StatusPanel(StatusPanel.NETWORK), BorderLayout.NORTH);
    
    JPanel content = new JPanel(new FormLayout("fill:pref:grow, fill:300dlu:grow, fill:pref:grow", "fill:pref:grow, pref, fill:pref:grow"));
    content.setBorder(Borders.DLU4_BORDER);

    CellConstraints cc = new CellConstraints();
    
    content.add(UiUtilities.createHtmlHelpTextArea(mLocalizer.msg("success", "Success")), cc.xy(2,2));
    
    mContent.add(content, BorderLayout.CENTER);
    
    return mContent;
  }

  public boolean onNext() {
    final ProgressWindow win=new ProgressWindow(MainFrame.getInstance());

    win.run(new Progress(){
      public void run() {
        ChannelGroupManager.getInstance().checkForAvailableGroupsAndChannels(win);
      }
    });
    
    ChannelList.reload();
    ChannelList.initSubscribedChannels();
    return true;
  }
  
}
