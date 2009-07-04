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
 *     $Date$
 *   $Author$
 * $Revision$
 */

package tvbrowser.ui.mainframe;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import util.browserlauncher.Launch;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.builder.ButtonBarBuilder2;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

/**
 * A class with a dialog that informs the user
 * about the plugin functions of TV-Browser.
 * 
 * @author René Mach
 * @since 2.7
 */
public class PluginInformationDialog extends JDialog implements WindowClosingIf {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(PluginInformationDialog.class); 
  
  /**
   * Creates an instance of this class.
   * <p>
   * @param parent The parent dialog for this dialog.
   */
  public PluginInformationDialog(Window parent) {
    super(parent);
    setModal(true);
    init();
  }

  private void init() {
    setTitle(mLocalizer.msg("title","TV-Browser is able to do much more"));
    
    UiUtilities.registerForClosing(this);
    
    CellConstraints cc = new CellConstraints();
    PanelBuilder pb = new PanelBuilder(
        new FormLayout("default:grow,default,default:grow",
            "default,fill:default:grow,default"),
        (JPanel)getContentPane());
    
    JLabel l = pb.addLabel(mLocalizer.msg("header","Important informations about TV-Browser functionality!"), cc.xy(2,1));
    l.setForeground(new Color(200,0,0));
    l.setFont(l.getFont().deriveFont(Font.BOLD,20));
    l.setBorder(Borders.createEmptyBorder("10dlu,0dlu,5dlu,0dlu"));
    
    JEditorPane pane = UiUtilities.createHtmlHelpTextArea(mLocalizer.msg("text","<div style=\"font-size:medium;text-align:justify\"><p>TV-Browser can be extended with additional functions (Plugins), for instance to control hardware, other data sources, to load ratings from ImdB and much more.</p><p>You also can find more Plugins <a href=\"http://www.tvbrowser.org/downloads-mainmenu-5/plugins-mainmenu-24.html\">on our website</a>, that are currently not available to download from TV-Browser, but are mostly already usable.</p><p>Do you want to see the list with the Plugins available through download from TV-Browser?<br>(You also can always open that list over the Plugins menu.)</p></div>"),
        new HyperlinkListener() {
          public void hyperlinkUpdate(HyperlinkEvent e) {
            if(e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
              Launch.openURL(e.getURL().toString());
            }
          }
        },Color.white);
    
    pane.setPreferredSize(new Dimension(400,300));
    
    pane.setBackground(Color.white);
    pane.setBorder(BorderFactory.createCompoundBorder(BorderFactory.createMatteBorder(0,0,1,0,Color.darkGray),Borders.createEmptyBorder("0dlu,10dlu,0dlu,10dlu")));
    
    pb.add(pane, cc.xyw(1,2,3));
    
    JButton[] buttons = {new JButton(mLocalizer.msg("showList","Show the list with the Plugins now")),
        new JButton(mLocalizer.msg("closeDialog","Close this dialog"))};
    
    buttons[0].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        MainFrame.getInstance().showUpdatePluginsDlg(true);
        close();
      }
    });
    
    buttons[1].addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        close();
      }
    });
    
    buttons[0].setFont(buttons[0].getFont().deriveFont(Font.BOLD,(float)13));
    buttons[1].setFont(buttons[1].getFont().deriveFont(Font.BOLD,(float)13));
    
    getRootPane().setDefaultButton(buttons[0]);
    
    ButtonBarBuilder2 bb = new ButtonBarBuilder2();
    bb.addGlue();
    bb.addButton(buttons);
    bb.addGlue();
    bb.setBorder(Borders.createEmptyBorder("6dlu,6dlu,6dlu,6dlu"));
    
    pb.add(bb.getPanel(), cc.xyw(1,3,3));
    pb.getPanel().setBackground(Color.white);
  }
  
  public void close() {
    dispose();
  }

}
