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
package newsplugin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import util.browserlauncher.Launch;
import util.io.IOUtilities;
import util.ui.Localizer;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;
import util.ui.html.ExtendedHTMLDocument;
import util.ui.html.ExtendedHTMLEditorKit;

/**
 * Shows the news.
 *
 * @author Til Schneider, www.murfman.de
 */
public class NewsDialog implements WindowClosingIf {

  /** The localizer used by this class. */
  private static final util.ui.Localizer mLocalizer
    = util.ui.Localizer.getLocalizerFor(NewsDialog.class);

  /** The icon to use for marking new news. */
  private static Icon mNewIcon;

  /** The actual dialog. */
  private JDialog mDialog;

  /** Show only the new news? */
  private JCheckBox mOnlyNewChB;

  /** The scroll pane. */
  private JScrollPane mScrollPane;

  /** The news pane that shows the news */
  private JEditorPane mNewsPane;

  /** The close button */
  private JButton mCloseBn;

  /** The news to show */
  private ArrayList<News> mNewsList;

  /** The number of news that should be marked as new */
  private int mNewNewsCount;


  /**
   * Creates a new instance of NewsDialog.
   *
   * @param parent A component in the parent window tree.
   * @param newsList The news to show.
   * @param newNewsCount The number of news that should be marked as new.
   */
  public NewsDialog(Window parent, ArrayList<News> newsList, int newNewsCount) {
    mDialog = UiUtilities.createDialog(parent, false);
    mNewsList = newsList;
    mNewNewsCount = newNewsCount;

    mDialog.setTitle(mLocalizer.msg("title", "News"));
    UiUtilities.registerForClosing(this);

    JPanel main = new JPanel(new BorderLayout());
    main.setBorder(UiUtilities.DIALOG_BORDER);
    main.setPreferredSize(new Dimension(500, 350));
    mDialog.setContentPane(main);

    if (mNewNewsCount > 0) {
      String msg = mLocalizer.msg("onlyNew", "Show only new news");
      mOnlyNewChB = new JCheckBox(msg, true);
      mOnlyNewChB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          updateNewsPane();
        }
      });
      main.add(mOnlyNewChB, BorderLayout.NORTH);
    }

    mNewsPane = new JEditorPane();
    mNewsPane.setEditorKit(new ExtendedHTMLEditorKit());
    mNewsPane.setEditable(false);
    mNewsPane.addHyperlinkListener(new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent evt) {
        if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          URL url = evt.getURL();
          if (url != null) {
            Launch.openURL(url.toString());
          }
        }
      }
    });
    mScrollPane = new JScrollPane(mNewsPane);
    main.add(mScrollPane, BorderLayout.CENTER);

    // buttons
    JPanel buttonPn = new JPanel(new FlowLayout(FlowLayout.TRAILING));
    main.add(buttonPn, BorderLayout.SOUTH);

    mCloseBn = new JButton(Localizer.getLocalization(Localizer.I18N_CLOSE));
    mCloseBn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        mDialog.dispose();
      }
    });
    buttonPn.add(mCloseBn);
    mDialog.getRootPane().setDefaultButton(mCloseBn);
    mCloseBn.requestFocusInWindow();

    NewsPlugin.getInstance().layoutWindow("newsDlg",mDialog);

    updateNewsPane();
  }


  /**
   * Updates the news pane.
   */
  private void updateNewsPane() {
    ExtendedHTMLDocument doc = (ExtendedHTMLDocument) mNewsPane.getDocument();
    mNewsPane.setText(createHtmlText(doc));

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        mScrollPane.getVerticalScrollBar().setValue(0);
      }
    });
  }


  /**
   * Centers the dialog to its parent and shows it.
   */
  public void show() {
    mDialog.setVisible(true);
  }


  /**
   * Gets the html text.
   *
   * @param doc The document to create the text for.
   * @return The html text.
   */
  private String createHtmlText(ExtendedHTMLDocument doc) {
    DateFormat dateFormat = DateFormat.getDateInstance();

    StringBuilder buf = new StringBuilder("<html><head>"
      + "<style type=\"text/css\" media=\"screen\"><!--"
      + "body { font-family: Dialog; }"
      + "td.time { font-size: small; font-style: italic; }"
      + "td.title { font-weight: bold; }"
      + "td.author { text-align: right; font-style: italic; }"
      + "td.spacer { border-bottom: 1px solid black; }"
      + "--></style>" +
      "</head><body>");
    if (mNewsList.size() == 0) {
      buf.append("<p align=\"center\">");
      buf.append(mLocalizer.msg("no.news", "There are no news..."));
      buf.append("</p>");
    } else {
      if (mNewIcon == null) {
        mNewIcon = TVBrowserIcons.newIcon(TVBrowserIcons.SIZE_LARGE);
      }

      // Show the news - backwards (newest first)
      int newsCount = mNewsList.size();
      for (int i = 0; i < newsCount; i++) {


        if ((mOnlyNewChB != null) && mOnlyNewChB.isSelected()
          && (i >= mNewNewsCount))
        {
          // Show only the new ones
          break;
        }

        if (i != 0) {
          buf.append("<hr>");
        }

        News news = mNewsList.get(i);

        buf.append("<table width=\"100%\">");
        buf.append("<tr>");
        if (i < mNewNewsCount) {
          buf.append("<td rowspan=\"4\" width=\"30\" valign=\"top\">");
          JLabel iconLabel = new JLabel(mNewIcon);
          iconLabel.setToolTipText(mLocalizer.msg("newNews", "This news is new"));
          buf.append(doc.createCompTag(iconLabel));
          buf.append("</td>");
        }
        buf.append("<td class=\"time\">" + dateFormat.format(news.getTime()) + ":</td></tr>");

        buf.append("<tr><td class=\"title\">" + news.getTitle() + "</td></tr>");

        String text = news.getText();
        text = IOUtilities.replace(text, "&lt;", "<");
        text = IOUtilities.replace(text, "&gt;", ">");
        text = IOUtilities.replace(text, "/>", ">"); // JEditorPane knows no XHTML
        buf.append("<tr><td class=\"text\">" + text + "</td></tr>");

        buf.append("<tr><td class=\"author\">" + news.getAuthor() + "</td></tr>");
        buf.append("</table>");
      }
    }
    buf.append("</body></html>");

    return buf.toString();
  }


  public void close() {
    mDialog.dispose();
  }

  /**
   * @return If this dialog is visible.
   */
  public boolean isVisible() {
    return mDialog != null && mDialog.isVisible();
  }


  public JRootPane getRootPane() {
    return mDialog.getRootPane();
  }

}
