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
package tvbrowser.ui.settings.looksSettings;

import java.awt.FlowLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JPanel;
import javax.swing.JTextField;

import tvbrowser.core.Settings;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.DefaultComponentFactory;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;

/**
 * Settings for the Skin LnF
 * 
 * @author bodum
 */
public class SkinLNFSettings extends JDialog implements WindowClosingIf {
  /** Translation */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(SkinLNFSettings.class);
  private JTextField mThemePack;
 
  /**
   * Create the Dialog
   * @param parent Parent
   */
  public SkinLNFSettings(JDialog parent) {
    super(parent, true);
    setTitle(mLocalizer.msg("title", "Skin Look and Feel Settings"));
    createGui();
  }

  /**
   * Create the GUI
   */
  private void createGui() {
    JPanel content = (JPanel) getContentPane();
    
    content.setLayout(new FormLayout("5dlu, fill:150dlu:grow, 3dlu, pref, 3dlu", "pref, 5dlu, pref, 5dlu, pref, 5dlu, pref, fill:3dlu:grow, pref"));
    content.setBorder(Borders.DLU4_BORDER);
    
    CellConstraints cc = new CellConstraints();
    
    content.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("help", "Help")), cc.xyw(1,1,5));
    
    content.add(UiUtilities.createHtmlHelpTextArea(mLocalizer.msg("skinLFInfo", "Skin Info")), cc.xyw(2,3,3));

    String temp = Settings.propSkinLFThemepack.getString().replace('/',File.separatorChar);
    
    mThemePack = new JTextField(temp.startsWith(".") ? System.getProperty("user.dir") + temp.substring(1) : temp);
    JButton chooseBtn = new JButton(mLocalizer.msg("choose", "Choose"));

    content.add(DefaultComponentFactory.getInstance().createSeparator(mLocalizer.msg("chooseThemepack", "Choose Themepack")), cc.xyw(1,5,5));

    content.add(mThemePack, cc.xy(2,7));
    content.add(chooseBtn, cc.xy(4,7));
    
    chooseBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        JFileChooser fileChooser=new JFileChooser(new File("themepacks"));
        fileChooser.setFileFilter(new util.ui.ExtensionFileFilter(".zip","SkinLF themepacks (*.zip)"));
        fileChooser.showOpenDialog(SkinLNFSettings.this);
        File f=fileChooser.getSelectedFile();
        if (f!=null) {
          mThemePack.setText(f.getAbsolutePath());
        }
      }
    });
    
    JButton ok = new JButton(mLocalizer.msg("ok", "OK"));
    ok.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        okPressed();
      }
    });
    
    JButton cancel = new JButton(mLocalizer.msg("cancel", "Cancel"));
    cancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cancelPressed();
      }
    });

    ButtonBarBuilder bar = new ButtonBarBuilder();
    bar.addGriddedButtons(new JButton[] {ok, cancel});

    JPanel panel = new JPanel(new FlowLayout(FlowLayout.RIGHT, 0, 0));
    panel.add(bar.getPanel());
    content.add(panel, cc.xyw(1,9,5));
   
    UiUtilities.registerForClosing(this);
    
    setSize(Sizes.dialogUnitXAsPixel(300, this), Sizes.dialogUnitYAsPixel(150, this));
  }

  /**
   * Cancel was pressed
   */
  protected void cancelPressed() {
    setVisible(false);
  }

  /**
   * OK was pressed
   */
  protected void okPressed() {
    Settings.propSkinLFThemepack.setString("."+mThemePack.getText().substring(System.getProperty("user.dir").length()).replace(File.separatorChar,'/'));
    setVisible(false);
  }

  /**
   * Close the Dialog
   */
  public void close() {
    cancelPressed();
  }
}
