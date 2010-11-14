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

package tvbrowser.ui.aboutbox;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.FlowLayout;
import java.awt.Frame;
import java.awt.Image;
import java.awt.Toolkit;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.WindowConstants;
import javax.swing.text.BadLocationException;
import javax.swing.text.Document;

import tvbrowser.TVBrowser;
import util.ui.ImageUtilities;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;
import util.ui.html.ExtendedHTMLDocument;
import util.ui.html.ExtendedHTMLEditorKit;

/**
 *
 * @author Martin Oberhauser (darras@users.sourceforge.net)
 *
 */
public class AboutBox extends JDialog implements WindowClosingIf{

  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(AboutBox.class);

  public AboutBox(Frame parent) {
    super(parent,true);

    UiUtilities.registerForClosing(this);

    setTitle(mLocalizer.msg("about", "About {0}", TVBrowser.MAINWINDOW_TITLE));

    JPanel contentPane=(JPanel)getContentPane();

    contentPane.setLayout(new BorderLayout());


    JPanel right = new JPanel();
    right.setLayout(new BorderLayout());

    final JEditorPane infoEP = new JEditorPane();
    infoEP.setEditorKit(new ExtendedHTMLEditorKit());
    ExtendedHTMLDocument doc = (ExtendedHTMLDocument) infoEP.getDocument();
    String text = createAboutText(doc);
    infoEP.setText(text);
    infoEP.setEditable(false);

    right.add(infoEP,BorderLayout.CENTER);


    JPanel btnPanel=new JPanel(new FlowLayout(FlowLayout.RIGHT));
    btnPanel.setBorder(BorderFactory.createEmptyBorder(0, 0, 10, 10));

    JButton copyClipboard = new JButton(mLocalizer.msg("copyClipboard", "Copy to Clipboard"));
    copyClipboard.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent e) {
            Document infoDoc = infoEP.getDocument();

            try {
                StringSelection sel = new StringSelection(infoDoc.getText(0, infoDoc.getLength()));
                Toolkit.getDefaultToolkit().getSystemClipboard().setContents(sel, null);
            } catch (BadLocationException e1) {
                e1.printStackTrace();
            }
        }
      });
    btnPanel.add(copyClipboard);

    JButton closeBtn = new JButton(Localizer.getLocalization(Localizer.I18N_CLOSE));

    closeBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        close();
      }
    });

    setDefaultCloseOperation(WindowConstants.DISPOSE_ON_CLOSE);

    getRootPane().setDefaultButton(closeBtn);
    btnPanel.add(closeBtn);
    right.add(btnPanel,BorderLayout.SOUTH);

    contentPane.add(right, BorderLayout.CENTER);

    Image image = ImageUtilities.createImage("imgs/tvabout.png");
    if (image != null) {
      ImageUtilities.waitForImageData(image, null);
      JLabel gfx = new JLabel(new ImageIcon(image));

      JPanel gfxPanel = new JPanel(new BorderLayout());
      gfxPanel.setBackground(new Color(80,170,233));
      gfxPanel.setOpaque(true);
      gfxPanel.add(gfx, BorderLayout.SOUTH);

      contentPane.add(gfxPanel, BorderLayout.WEST);
    }
  }

  private StringBuilder createInfoEntry(StringBuilder buf, String key,
      String value) {

    buf.append("<tr><td width=\"35%\" valign=\"top\">");
    buf.append("<div id=\"key\">");

    buf.append(key);
    buf.append("</div>");
    return buf.append("</td><td>").append(value).append("</td></tr>");
  }

  private void createSpacer(StringBuilder buf) {
      buf.append("<tr><td id=\"small\"></td></tr>");

  }

  private void createJavaVersionEntry(StringBuilder buf) {

      buf.append("<tr><td colspan=\"2\">");

      buf.append("<div id=\"small\">");

      buf.append(System.getProperty("java.vm.name")).append("<br>");
    buf.append(System.getProperty("java.vendor")).append("<br>");
      buf.append(System.getProperty("java.home"));

      buf.append("</div>");

      buf.append("</td></tr>");
  }

  private String createAboutText(ExtendedHTMLDocument doc) {

    StringBuilder buf = new StringBuilder();
    buf.append("<html>" +
               "  <head>" +
               "<style type=\"text/css\" media=\"screen\">" +
               "<!--" +
               "body {font-family:Dialog;}" +
               "" +
               "#title { font-size:18px; font-family:Dialog; text-align:center; font-weight:bold; margin-top:5px}" +
               "#key { font-size:12px; font-family:Dialog; font-weight:bold; }" +
               "" +
               "#small { font-size:9px; font-family:Dialog; margin-bottom: 5px}" +
               "-->" +
               "  </head>" +
               "  <body>" +
               "    <div id=\"title\">"+mLocalizer.msg("version", "Version")+": " + TVBrowser.VERSION.toString() +"</div>" +
               "<p>" +
               "    <table width=\"100%\" border=\"0\">");


    createInfoEntry(buf, mLocalizer.msg("platform", "Platform") + ":",
       System.getProperty("os.name") + " " + System.getProperty("os.version"));

    createInfoEntry(buf, mLocalizer.msg("system", "System") + ":",
       System.getProperty("os.arch"));

    createSpacer(buf);

    createInfoEntry(buf, mLocalizer.msg("javaVersion", "Java Version") + ":",
       System.getProperty("java.version"));

    createJavaVersionEntry(buf);

    createSpacer(buf);

    createInfoEntry(buf, mLocalizer.msg("location", "Location") + ":",
    System.getProperty("user.country") + "," + System.getProperty("user.language"));

    java.util.TimeZone timezone = java.util.TimeZone.getDefault();
    int tzOffset = timezone.getRawOffset() / 1000 / 60 / 60;
    String tzOffsetAsString = mLocalizer.msg("hours",
        "({0,number,+#;#} hours)", tzOffset);

    createInfoEntry(buf, mLocalizer.msg("timezone", "Timezone") + ":",
       timezone.getDisplayName() + " " + tzOffsetAsString);


    buf.append("</table></p>");
    buf.append("<p>");
    buf.append("<div id=\"small\">");

    buf.append(mLocalizer.msg("copyrightText",
          "Copyright (c) {0} by {1}, under the GNU General Public License", "04/2003", "Martin Oberhauser, Til Schneider, Bodo Tasche, Ren\u00e9 Mach, Michael Keppler"));

    buf.append("</div>");

    buf.append("<div id=\"small\">");
    buf.append("This product includes software developed " +
                       "by L2FProd.com (http://www.L2FProd.com/), \n" +
            "The Apache Software Foundation (http://www.apache.org/) and\n" +
            "BEQ Technologies Inc. (http://www.bayequities.com/tech/Products/jreg_key.shtml)");

    buf.append("</div>");
    buf.append("</p>");
    buf.append("  </body>" +
               "</html>");
    return buf.toString();
  }

  public void close() {
    dispose();
  }
}
