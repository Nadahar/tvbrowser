/*
 * Golem.de guckt - Plugin for TV-Browser
 * Copyright (C) 2010 Bodo Tasche
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
 * SVN information:
 *     $Date: 2010-02-20 13:09:24 +0100 (Sa, 20. Feb 2010) $
 *   $Author: bananeweizen $
 * $Revision: 6530 $
 */
package golemplugin;

import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.Collection;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import util.ui.Localizer;
import util.ui.ProgramList;
import util.ui.SendToPluginDialog;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.builder.ButtonBarBuilder2;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.Sizes;

import devplugin.Program;

public class GolemDialog extends JDialog implements WindowClosingIf {

  static final Localizer mLocalizer = Localizer.getLocalizerFor(GolemDialog.class);

  public GolemDialog(Window w) {
    super(w);
    setModal(true);

    createGui();

    setSize(Sizes.dialogUnitXAsPixel(200, this),
        Sizes.dialogUnitYAsPixel(200, this));

  }

  private void createGui() {
    setTitle(mLocalizer.msg("title", "Golem.de watches"));

    JPanel panel = (JPanel)getContentPane();

    panel.setBorder(Borders.DLU4_BORDER);

    FormLayout layout = new FormLayout("fill:pref:grow");

    panel.setLayout(layout);

    CellConstraints cc = new CellConstraints();

    final Collection<Program> plist = GolemPlugin.getInstance().getSettings().getProgramList();

    ProgramList list = new ProgramList(plist.toArray(new Program[plist.size()]));
    list.addMouseListeners(GolemPlugin.getInstance());

    int line = 1;
    layout.appendRow(RowSpec.decode("fill:min:grow"));

    panel.add(new JScrollPane(list), cc.xy(1, line));

    final ButtonBarBuilder2 builderButton = ButtonBarBuilder2.createLeftToRightBuilder();

    JButton sendBtn = new JButton(TVBrowserIcons.copy(TVBrowserIcons.SIZE_SMALL));
    sendBtn.setToolTipText(mLocalizer.msg("send", "Send to other Plugins"));
    sendBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        if (plist.size() > 0) {
          SendToPluginDialog sendDialog = new SendToPluginDialog(GolemPlugin.getInstance(), (Window)GolemDialog.this, plist.toArray(new Program[plist.size()]));
          sendDialog.setVisible(true);
        }
      }
    });

    JButton ok = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    ok.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        close();
      }
    });
    getRootPane().setDefaultButton(ok);

    builderButton.addFixed(sendBtn);
    builderButton.addGlue();
    builderButton.addFixed(ok);

    line+=2;
    layout.appendRow(RowSpec.decode("3dlu"));
    layout.appendRow(RowSpec.decode("pref"));
    panel.add(builderButton.getPanel(), cc.xy(1, line));

    UiUtilities.registerForClosing(this);
  }

  public void close() {
    setVisible(false);
  }
}
