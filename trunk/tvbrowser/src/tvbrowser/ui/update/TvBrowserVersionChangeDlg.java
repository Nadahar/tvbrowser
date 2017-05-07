/*
 * TV-Browser
 * Copyright (C) 2003-2010 TV-Browser-Team (dev@tvbrowser.org)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 3
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
 *     $Date$
 *     $Id$
 *   $Author$
 * $Revision$
 */
package tvbrowser.ui.update;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Version;
import tvbrowser.TVBrowser;
import tvbrowser.core.Settings;
import tvbrowser.ui.mainframe.SoftwareUpdater;
import util.browserlauncher.Launch;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

/**
 * A dialog that is shown on each version change
 * to ask the user for Plugin update.
 * 
 * @author René Mach
 */
public class TvBrowserVersionChangeDlg extends JDialog implements WindowClosingIf {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(TvBrowserVersionChangeDlg.class);
  private boolean mCloseTvBrowser;
  
  /**
   * Creates the TV-Browser version change dialog.
   * <p>
   * @param oldTvBrowserVersion The TV-Browser version to update from.
   * @param obligatoryUpdate TV-Browser version that triggers an update for smaller old versions
   */
  public TvBrowserVersionChangeDlg(Version oldTvBrowserVersion, Version obligatoryUpdate) {
    setLocationRelativeTo(null);
    setModalityType(ModalityType.DOCUMENT_MODAL);
    mCloseTvBrowser = true;
    init(oldTvBrowserVersion,obligatoryUpdate);
  }

  private void init(final Version oldTvBrowserVersion, Version obligatoryUpdate) {
    setTitle(mLocalizer.msg("title","TV-Browser was updated from {0} to {1}",oldTvBrowserVersion,TVBrowser.VERSION));
    
    UiUtilities.registerForClosing(this);
    
    CellConstraints cc = new CellConstraints();
    PanelBuilder pb = new PanelBuilder(
        new FormLayout("default:grow,default,default:grow",
            "default,fill:default:grow,default"),
        (JPanel)getContentPane());
    JLabel l = pb.addLabel(mLocalizer.msg("header","TV-Browser was updated from {0} to {1}!",oldTvBrowserVersion,TVBrowser.VERSION), cc.xy(2,1));
    l.setForeground(new Color(200,0,0));
    l.setFont(l.getFont().deriveFont(Font.BOLD,22));
    l.setBorder(Borders.createEmptyBorder("10dlu,3dlu,5dlu,3dlu"));
    
    JEditorPane pane = UiUtilities.createHtmlHelpTextArea(mLocalizer.msg("text","<div style=\"font-size:large;text-align:justify\"><p>TV-Browser is developed on a regular basis. Every version contains changes to improve TV-Browser, but sometimes it is necessary to change some functions that could lead to discontinued support for old Plugin versions.</p><br><div style=\"font-weight:bold;color:red\">We recommend to update all installed plugins now. (It can happen that a plugin update is obligatory.)</div><p>You will need an Internet connection.</b> If you currently don't have an internet connection we recommend to close TV-Browser now and using the previous version until a Plugin update is possible.</p><p>Do you want to update your Plugins now (this may take some time)?</p></div>"),
        new HyperlinkListener() {
          public void hyperlinkUpdate(HyperlinkEvent e) {
            if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
              Launch.openURL(e.getURL().toString());
            }
          }
        },UIManager.getColor("EditorPane.background"));
    
    pane.setPreferredSize(new Dimension(400,330));
    
    pane.setBackground(UIManager.getColor("EditorPane.background"));
    pane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,1,0,Color.darkGray),Borders.createEmptyBorder("0dlu,10dlu,0dlu,10dlu")));
    
    pb.add(pane, cc.xyw(1,2,3));
    
    JButton[] buttons = {new JButton(mLocalizer.msg("updatePlugins","Update Plugins now")),
        new JButton(mLocalizer.msg("closeTvBrowser","Close TV-Browser now")),
        new JButton(mLocalizer.msg("closeDialog","Close this dialog and don't update"))};
    
    buttons[0].addActionListener(e -> {
      try {
        SoftwareUpdateItem[] updateItems = PluginAutoUpdater.getUpdateItemsForVersionChange();
        
        if(updateItems.length > 0) {
          Settings.propPluginBetaWarning.setBoolean(false);
          SoftwareUpdateDlg updateDlg = new SoftwareUpdateDlg(null,SoftwareUpdater.ONLY_UPDATE_TYPE,updateItems,true,oldTvBrowserVersion);
          
          if(!updateDlg.isEmpty()) {
            updateDlg.setLocationRelativeTo(null);
            updateDlg.setVisible(true);
          }
          else {
            Settings.propPluginBetaWarning.setBoolean(true);
          }
        }
        
        mCloseTvBrowser = false;
      } catch (IOException e1) {
      }
      
      close();
    });
    
    buttons[1].addActionListener(e -> {
      close();
    });
    
    buttons[2].addActionListener(e -> {
      mCloseTvBrowser = false;
      close();
    });
    
    buttons[0].setFont(buttons[0].getFont().deriveFont(Font.BOLD,13));
    buttons[1].setFont(buttons[1].getFont().deriveFont(Font.BOLD,13));
    buttons[2].setFont(buttons[2].getFont().deriveFont(Font.BOLD,13));
    
    buttons[2].setEnabled(oldTvBrowserVersion.compareTo(obligatoryUpdate) >= 0);
    buttons[2].setToolTipText(mLocalizer.msg("obligatoryTooltip", "If this button is disabled the plugin update is obligatory."));
    
    getRootPane().setDefaultButton(buttons[0]);
    
    ButtonBarBuilder bb = new ButtonBarBuilder();
    bb.setOpaque(true);
    bb.addGlue();
    bb.addButton(buttons);
    bb.addGlue();
    bb.setBorder(Borders.createEmptyBorder("6dlu,6dlu,6dlu,6dlu"));
    
    pb.add(bb.getPanel(), cc.xyw(1,3,3));
    pb.getPanel().setOpaque(true);
    pb.getPanel().setBackground(UIManager.getColor("EditorPane.background"));
  }
  
  /**
   * Gets if TV-Browser is to be closed.
   * <p>
   * @return <code>True</code> if TV-Browser is to be closed.
   */
  public boolean getIsToCloseTvBrowser() {
    return mCloseTvBrowser;
  }
  
  public void close() {
    dispose();
  }
}
