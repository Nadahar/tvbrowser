/*
 * Copyright Michael Keppler
 *
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package feedsplugin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import util.browserlauncher.Launch;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;
import util.ui.html.ExtendedHTMLDocument;
import util.ui.html.ExtendedHTMLEditorKit;
import util.ui.html.HorizontalLine;

import com.sun.syndication.feed.synd.SyndEntry;
import com.sun.syndication.feed.synd.SyndEntryImpl;

import devplugin.Program;

public final class FeedsDialog extends JDialog implements WindowClosingIf {
  static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(FeedsDialog.class);

  private static final long serialVersionUID = 1L;

  private JScrollPane mScrollPane;
  private JEditorPane mInfoPane;
  private JButton mCloseBn;

  protected String mTooltip;

  private ArrayList<SyndEntry> mEntries;

  public FeedsDialog(final Frame parent, final Program program) {
    super(parent, true);
    createGUI(FeedsPlugin.getInstance().getMatchingEntries(program));
  }

  public FeedsDialog(final Frame parentFrame, final SyndEntryImpl entry) {
    super(parentFrame, true);
    ArrayList<SyndEntry> entries = new ArrayList<SyndEntry>(1);
    entries.add(entry);
    createGUI(entries);
  }

  private void createGUI(ArrayList<SyndEntry> entries) {
    mEntries = entries;
    setTitle(mLocalizer.msg("title", "Newsfeed"));
    UiUtilities.registerForClosing(this);

    final JPanel main = new JPanel(new BorderLayout());
    main.setBorder(UiUtilities.DIALOG_BORDER);
    main.setPreferredSize(new Dimension(500, 350));
    setContentPane(main);

    mInfoPane = new JEditorPane();
    mInfoPane.setEditorKit(new ExtendedHTMLEditorKit());
    mInfoPane.setEditable(false);
    mInfoPane.addHyperlinkListener(new HyperlinkListener() {
      public void hyperlinkUpdate(final HyperlinkEvent evt) {
        if (evt.getEventType() == HyperlinkEvent.EventType.ENTERED) {
          mTooltip = mInfoPane.getToolTipText();
          mInfoPane.setToolTipText(getLinkTooltip(evt));
        }
        if (evt.getEventType() == HyperlinkEvent.EventType.EXITED) {
          mInfoPane.setToolTipText(mTooltip);
        }
        if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          final URL url = evt.getURL();
          if (url != null) {
            Launch.openURL(url.toString());
          }
        }
      }
    });
    mScrollPane = new JScrollPane(mInfoPane);
    main.add(mScrollPane, BorderLayout.CENTER);

    final JPanel buttonPn = new JPanel(new FlowLayout(FlowLayout.TRAILING));
    main.add(buttonPn, BorderLayout.SOUTH);

    mCloseBn = new JButton(Localizer.getLocalization(Localizer.I18N_CLOSE));
    mCloseBn.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent evt) {
        dispose();
      }
    });
    buttonPn.add(mCloseBn);
    getRootPane().setDefaultButton(mCloseBn);

    pack();

    final ExtendedHTMLDocument doc = (ExtendedHTMLDocument) mInfoPane.getDocument();
    mInfoPane.setText(createHtmlText(doc, entries));
    mScrollPane.getVerticalScrollBar().setValue(0);

    toFront();
  }

  private String getLinkTooltip(HyperlinkEvent evt) {
    return evt.getURL().toExternalForm();
  }

  public void close() {
    dispose();
  }

  private String createHtmlText(final ExtendedHTMLDocument doc, final ArrayList<SyndEntry> entries) {
    final Font bFont = new Font("Verdana", Font.PLAIN, 11);
    final StringBuilder buffer = new StringBuilder(1024);
    buffer.append("<html><body>");
    buffer.append("<table width=\"100%\" style=\"font-family:");
    buffer.append(bFont.getFamily());
    buffer.append(";\">");

    for (int i = entries.size() - 1; i >= 0; i--) {
      String link = entries.get(i).getLink();
      if (link != null && !link.isEmpty()) {
        for (int j = 0; j < i; j++) {
          if (entries.get(j).getLink().equals(link)) {
            entries.remove(i);
            break;
          }
        }
      }
    }
    for (int i = 0; i < entries.size(); i++) {
      SyndEntry entry = entries.get(i);
      if (i > 0) {
        addSeparator(doc, buffer);
      }
      buffer.append("<tr><td width=\"100%\"><b><a href=\"").append(entry.getLink()).append("\">").append(entry.getTitle())
          .append("</a></b></td></tr");
      buffer.append("<tr><td>").append(entry.getDescription().getValue()).append("</td></tr>");
    }
    buffer.append("</table></body></html>");

    return buffer.toString();
  }

  private void addSeparator(final ExtendedHTMLDocument doc, final StringBuilder buffer) {
    buffer.append("<tr><td>");
    buffer.append("<div style=\"font-size:0;\">").append(doc.createCompTag(new HorizontalLine()))
        .append("</div></td></tr>");
  }
}
