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
 */
package tvbrowser.ui.configassistant;

import java.awt.BorderLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JSeparator;

import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

/**
 * A class for the picture settings of the TvBrowserDataService.
 * Is been loaded after an update of TV-Browser if old version
 * didn't support the picture showing.
 * 
 * @author Ren� Mach
 * @since 2.2.2
 */
public class TvBrowserPictureSettingsUpdateDialog extends JDialog implements WindowClosingIf, ActionListener {
  private static final long serialVersionUID = 1L;
  
  private PictureConfigPanel mConfigPanel;
  private JButton mOkButton, mCancelButton;
  
  private TvBrowserPictureSettingsUpdateDialog(JDialog parent) {
    super(parent,PictureConfigPanel.mLocalizer.msg("pictureSettings","Picture settings"),true);
    createGui();
  }

  private TvBrowserPictureSettingsUpdateDialog(JFrame parent) {
    super(parent,PictureConfigPanel.mLocalizer.msg("pictureSettings","Picture settings"),true);
    createGui();
  }
  
  /**
   * Create an show this dialog.
   * 
   * @param parent The parent window.
   */
  public static void createAndShow(Window parent) {
    Window p = UiUtilities.getLastModalChildOf(parent);
    
    if(p instanceof JDialog)
      new TvBrowserPictureSettingsUpdateDialog((JDialog)p);
    else
      new TvBrowserPictureSettingsUpdateDialog((JFrame)p);
  }

  private void createGui() {
    UiUtilities.registerForClosing(this);
    
    mConfigPanel = new PictureConfigPanel(true);

    CellConstraints cc = new CellConstraints();
    FormLayout layout = new FormLayout("pref:grow,pref,3dlu,pref,5dlu","pref,5dlu,pref,5dlu");
    layout.setColumnGroups(new int[][] {{2,4}});
    
    JPanel buttonPanel = new JPanel(layout);
    
    mOkButton = new JButton(PictureConfigPanel.mLocalizer.msg("ok","OK"));
    mOkButton.addActionListener(this);
    mCancelButton = new JButton(PictureConfigPanel.mLocalizer.msg("cancel","Cancel"));
    mCancelButton.addActionListener(this);
    
    buttonPanel.add(new JSeparator(), cc.xyw(1,1,5));
    buttonPanel.add(mOkButton, cc.xy(2,3));
    buttonPanel.add(mCancelButton, cc.xy(4,3));
    
    JPanel content = new JPanel(new BorderLayout(0,5));
    content.add(mConfigPanel, BorderLayout.CENTER);
    content.add(buttonPanel, BorderLayout.SOUTH);
    
    setContentPane(content);
    
    setSize(700,450);
    
    UiUtilities.centerAndShow(this);
  }

  public void close() {
    dispose();
  }

  public void actionPerformed(ActionEvent e) {
    if(e.getSource() == mOkButton) {
      mConfigPanel.saveSettings();
      close();
    }
    else if(e.getSource() == mCancelButton)
      close();
  }
}
