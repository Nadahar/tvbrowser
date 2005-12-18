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
package programinfo;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JPopupMenu;
import javax.swing.JScrollPane;
import javax.swing.SwingConstants;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import util.ui.BrowserLauncher;
import util.ui.findasyoutype.TextComponentFindAction;
import util.ui.html.ExtendedHTMLDocument;
import util.ui.html.ExtendedHTMLEditorKit;
import devplugin.Plugin;
import devplugin.Program;

/**
 * TV-Browser
 *
 * @author Martin Oberhauser
 */
public class ProgramInfoDialog extends JDialog implements SwingConstants {

  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(ProgramInfoDialog.class);

  private JEditorPane mInfoEP;

  public ProgramInfoDialog(Frame parent, String styleSheet, final Program program)
  {
    super(parent, true);

    setTitle(mLocalizer.msg("title", "Program information"));

    JPanel main = new JPanel(new BorderLayout());
    main.setPreferredSize(new Dimension(500, 350));
    main.setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));
    setContentPane(main);

    mInfoEP = new JEditorPane();
    mInfoEP.setEditorKit(new ExtendedHTMLEditorKit());

    ExtendedHTMLDocument doc = (ExtendedHTMLDocument) mInfoEP.getDocument();
    ProgramTextCreator creator = new ProgramTextCreator();

    String text = creator.createInfoText(program, doc, styleSheet);
    mInfoEP.setText(text);
    mInfoEP.setEditable(false);
    mInfoEP.addHyperlinkListener(new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent evt) {
        if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          URL url = evt.getURL();
          if (url != null) {
            BrowserLauncher.openURL(url.toString());
          }
        }
      }
    });

    mInfoEP.addMouseListener(new MouseAdapter(){
      public void mousePressed(MouseEvent evt) {
        if (evt.isPopupTrigger()) {
          showPopup(evt, program);
        }
      }

      public void mouseReleased(MouseEvent evt) {
        if (evt.isPopupTrigger()) {
          showPopup(evt, program);
        }
      }

      public void mouseClicked(MouseEvent e) {
        handleMouseClicked(e, program);
      }
    });

    //final FindAsYouType findasyoutype = new FindAsYouType(mInfoEP);
    final TextComponentFindAction findasyoutype = new TextComponentFindAction(mInfoEP, true);

    final JScrollPane scrollPane = new JScrollPane(mInfoEP);
    main.add(scrollPane, BorderLayout.CENTER);

    // buttons
    JPanel buttonPn = new JPanel(new BorderLayout());

    buttonPn.setBorder(BorderFactory.createEmptyBorder(5,0,0,0));

    main.add(buttonPn, BorderLayout.SOUTH);

    JButton findBtn = new JButton(findasyoutype);

    findBtn.setIcon(ProgramInfo.getInstance().createImageIcon("actions", "system-search", 16));
        
    findBtn.setText("");
    findBtn.setToolTipText(mLocalizer.msg("search", "Search Text"));

    buttonPn.add(findBtn, BorderLayout.WEST);

    JButton closeBtn = new JButton(mLocalizer.msg("close", "Close"));
    closeBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        dispose();
      }
    });

    buttonPn.add(closeBtn, BorderLayout.EAST);

    getRootPane().setDefaultButton(closeBtn);

    // Scroll to the beginning
    Runnable runnable = new Runnable() {
      public void run() {
        scrollPane.getVerticalScrollBar().setValue(0);
      }
    };
    SwingUtilities.invokeLater(runnable);
  }

  /**
   * Shows the Popup
   * @param evt MouseEvent for Popup-Location
   * @param program Program to use for Popup
   */
  private void showPopup(MouseEvent evt, Program program) {
    if (program != null) {
      JPopupMenu menu = Plugin.getPluginManager().createPluginContextMenu(program, ProgramInfo.getInstance());
      menu.show(mInfoEP, evt.getX() - 15, evt.getY() - 15);
    }
  }

  private void handleMouseClicked(MouseEvent evt, Program program) {
    if (SwingUtilities.isLeftMouseButton(evt) && (evt.getClickCount() == 2)) {
      Plugin.getPluginManager().handleProgramDoubleClick(program, ProgramInfo.getInstance());
    }
    if (SwingUtilities.isMiddleMouseButton(evt) && (evt.getClickCount() == 1)) {
      Plugin.getPluginManager().handleProgramMiddleClick(program, ProgramInfo.getInstance());
    }
  }
}