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

package tvbrowser.ui.settings.channel;

import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import org.apache.commons.lang.StringUtils;

import tvbrowser.core.tvdataservice.ChannelGroupManager;
import tvbrowser.core.tvdataservice.TvDataServiceProxy;
import util.browserlauncher.Launch;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;
import util.ui.html.ExtendedHTMLDocument;
import util.ui.html.ExtendedHTMLEditorKit;
import util.ui.html.HTMLTextHelper;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Channel;
import devplugin.ChannelGroup;

/**
 * Shows the ChannelGroup Information Dialog
 */
public class ChannelGroupInfoDialog extends JDialog implements WindowClosingIf{
  /** Translation */
  private static final util.ui.Localizer mLocalizer
  = util.ui.Localizer.getLocalizerFor(ChannelGroupInfoDialog.class);

  /** Infos about the channel group */
  private ChannelGroup mChannelGroup;



  public ChannelGroupInfoDialog(JDialog dialog,  ChannelGroup group) {
    super(dialog, true);
    setTitle(group.getName());
    mChannelGroup = group;

    initGui();
  }

  /**
   * Create the GUI
   */
  private void initGui() {
    UiUtilities.registerForClosing(this);
    JPanel panel = (JPanel) getContentPane();
    panel.setBorder(Borders.DLU4_BORDER);
    panel.setLayout(new FormLayout("fill:default:grow, default", "fill:default:grow, 3dlu, default"));

    CellConstraints cc = new CellConstraints();

    final JEditorPane infoPanel = new JEditorPane();

    infoPanel.setEditorKit(new ExtendedHTMLEditorKit());

    ExtendedHTMLDocument doc = (ExtendedHTMLDocument) infoPanel.getDocument();

    infoPanel.setEditable(false);
    infoPanel.setText(generateHtml(doc));

    infoPanel.addHyperlinkListener(new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent evt) {
        if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          URL url = evt.getURL();
          if (url != null) {
              Launch.openURL(url.toString());
          }
        }
      }
    });

    final JScrollPane scrollPane = new JScrollPane(infoPanel);
    panel.add(scrollPane, cc.xyw(1,1,2));

    JButton ok = new JButton(Localizer.getLocalization(Localizer.I18N_OK));

    ok.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        setVisible(false);
      }
    });

    SwingUtilities.invokeLater(new Runnable(){
      public void run() {
        infoPanel.scrollRectToVisible(new Rectangle(0,0));
      }
    });

    panel.add(ok, cc.xy(2,3));

    setSize(500, 350);
  }

  private String getNotNull(String s) {
    if (StringUtils.isBlank(s)) {
      return "<unknown>";
    }
    return s;
  }

  /**
   * Generate the HML for the EditorPane
   * @param doc Document to use
   * @return Html-Text
   */
  private String generateHtml(ExtendedHTMLDocument doc) {
    StringBuilder html = new StringBuilder(1024);

    html.append("<html><style type=\"text/css\" media=\"screen\">"
                + "<!--" +
                    "body {font-size:12px;font-family:Dialog;}" +
                    "h1 {font-size:12px;font-family:Dialog;font-weight:bold;}" +
                "-->" +
                "</style><body>");

    html.append("<table><tr><td valign=\"top\"><b>").append(mChannelGroup.getName()).append("</b></td></tr></table>");


    html.append("<h1>").append(mLocalizer.msg("provider", "Provider")).append("</h1>");
    html.append(HTMLTextHelper.convertTextToHtml(getNotNull(mChannelGroup.getProviderName()), true));

    html.append("<h1>").append(mLocalizer.msg("description", "Description")).append("</h1>");
    html.append(HTMLTextHelper.convertTextToHtml(getNotNull(mChannelGroup.getDescription()), true));

    html.append("<h1>").append(Localizer.getLocalization(Localizer.I18N_CHANNELS)).append("</h1>");
    TvDataServiceProxy proxy = ChannelGroupManager.getInstance().getTvDataService(mChannelGroup);
    if (proxy != null) {
      Channel[] ch = proxy.getAvailableChannels(mChannelGroup);

      if (ch != null) {
        StringBuilder buf = new StringBuilder();

        for (int i=0; i<ch.length-1; i++) {
          buf.append(ch[i].getName()).append(", ");
        }
        if (ch.length>0) {
          buf.append(ch[ch.length-1].getName());
        }
        html.append(HTMLTextHelper.convertTextToHtml(buf.toString(), false));
      }
    }


    html.append("</body></html>");

    return html.toString();
  }

  public void close() {
    setVisible(false);
  }

}
