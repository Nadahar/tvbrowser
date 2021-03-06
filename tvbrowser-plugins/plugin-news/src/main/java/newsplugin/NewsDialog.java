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
 *     $Date: 2011-10-23 14:14:52 +0200 (So, 23 Okt 2011) $
 *   $Author: ds10 $
 * $Revision: 7242 $
 */
package newsplugin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.FlowLayout;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;
import java.text.DateFormat;
import java.util.ArrayList;

import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;

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
  
  /** Show only news of the given type */
  private JComboBox mNewsTypeSelection;

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

  private boolean mShowOnlyNew;
  
  private boolean mHasNews;
  
  private JCheckBox mSafeSettings;
  
  /**
   * Creates a new instance of NewsDialog.
   *
   * @param parent A component in the parent window tree.
   * @param newsList The news to show.
   * @param newNewsCount The number of news that should be marked as new.
   */
  public NewsDialog(Window parent, ArrayList<News> newsList, int newNewsCount, boolean showOnlyNew, int newsTypeIndex) {
    mHasNews = false;
    mShowOnlyNew = showOnlyNew;
    mDialog = UiUtilities.createDialog(parent, false);
    mDialog.setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);
    mDialog.addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        close();
      }
    });
    mNewsList = newsList;
    mNewNewsCount = newNewsCount;

    mDialog.setTitle(mLocalizer.msg("title", "News"));
    UiUtilities.registerForClosing(this);

    JPanel main = new JPanel(new BorderLayout());
    main.setBorder(UiUtilities.DIALOG_BORDER);
    main.setPreferredSize(new Dimension(500, 350));
    mDialog.setContentPane(main);
    
    mNewsTypeSelection = new JComboBox();
    
    mNewsTypeSelection.addItem(mLocalizer.msg("type.all", "All"));
    mNewsTypeSelection.addItem(mLocalizer.msg("type.tvbrowser", "TV-Browser"));
    mNewsTypeSelection.addItem(mLocalizer.msg("type.desktop", "Only for TV-Browser for desktop"));
    mNewsTypeSelection.addItem(mLocalizer.msg("type.android", "Only for TV-Browser for Android"));
    mNewsTypeSelection.addItem(mLocalizer.msg("type.website", "Website"));
    
    mNewsTypeSelection.setSelectedIndex(newsTypeIndex);
    
    mNewsTypeSelection.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        updateNewsPane();
      }
    });
    
    mSafeSettings = new JCheckBox(mLocalizer.msg("safeSettings", "Save selection"), false);
    
    JPanel settings = new JPanel(new FormLayout("default,3dlu,default:grow,default","default,3dlu,default,3dlu,default,2dlu"));
    
    settings.add(new JLabel(mLocalizer.msg("type", "News type:")), CC.xy(1, 1));
    settings.add(mNewsTypeSelection, CC.xyw(3, 1, 2));
    settings.add(mSafeSettings, CC.xy(4, 5));

    if (mNewNewsCount > 0) {
      String msg = mLocalizer.msg("onlyNew", "Show only new news");
      mOnlyNewChB = new JCheckBox(msg, showOnlyNew);
      mOnlyNewChB.addActionListener(new ActionListener() {
        public void actionPerformed(ActionEvent evt) {
          mShowOnlyNew = mOnlyNewChB.isSelected();
          updateNewsPane();
        }
      });
      
      settings.add(mOnlyNewChB, CC.xyw(1, 3, 3));
    }
    
    main.add(settings, BorderLayout.NORTH);

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
    mNewsPane.setBackground(UIManager.getColor("List.background"));
    
    mScrollPane = new JScrollPane(mNewsPane);
    main.add(mScrollPane, BorderLayout.CENTER);

    // buttons
    JPanel buttonPn = new JPanel(new FlowLayout(FlowLayout.TRAILING));
    main.add(buttonPn, BorderLayout.SOUTH);

    mCloseBn = new JButton(Localizer.getLocalization(Localizer.I18N_CLOSE));
    mCloseBn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent evt) {
        close();
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
      + "body { font-family: Dialog; color: "+ getCssRgbColorEntry(UIManager.getColor("List.foreground")) + "; }"
      + "td.time { font-size: small; font-style: italic; }"
      + "td.title { font-weight: bold; }"
      + "td.author { text-align: right; font-style: italic; }"
      + "td.spacer { border-bottom: 1px solid black; }"
      + "--></style>" +
      "</head><body style=\"background:"+getCssRgbColorEntry(UIManager.getColor("List.background"))+"\">");
    if (mNewsList.size() == 0) {
      buf.append("<p align=\"center\">");
      buf.append(mLocalizer.msg("no.news", "There are no news..."));
      buf.append("</p>");
    } else {
      if (mNewIcon == null) {
        mNewIcon = TVBrowserIcons.newIcon(TVBrowserIcons.SIZE_LARGE);
      }

      StringBuilder newsText = new StringBuilder();
      
      // Show the news - backwards (newest first)
      int newsCount = mNewsList.size();
      for (int i = 0; i < newsCount; i++) {
        if ((mOnlyNewChB != null) && mOnlyNewChB.isSelected()
          && (i >= mNewNewsCount))
        {
          // Show only the new ones
          break;
        }

        News news = mNewsList.get(i);
        
        String acceptedNewsType = News.TYPE_ALL;
        
        switch(mNewsTypeSelection.getSelectedIndex()) {
          case 1: acceptedNewsType = News.TYPE_TV_BROWSER;break;
          case 2: acceptedNewsType = News.TYPE_TV_DESKTOP;break;
          case 3: acceptedNewsType = News.TYPE_TV_ANDROID;break;
          case 4: acceptedNewsType = News.TYPE_TV_WEBSITE;break;
        }
        
        if(news.isAcceptableType(acceptedNewsType)) {
          if (i != 0) {
            newsText.append("<hr>");
          }
  
          newsText.append("<table width=\"100%\">");
          newsText.append("<tr>");
          if (i < mNewNewsCount) {
            newsText.append("<td rowspan=\"4\" width=\"30\" valign=\"top\">");
            JLabel iconLabel = new JLabel(mNewIcon);
            iconLabel.setToolTipText(mLocalizer.msg("newNews", "This news is new"));
            newsText.append(doc.createCompTag(iconLabel));
            newsText.append("</td>");
          }
          newsText.append("<td class=\"time\">" + dateFormat.format(news.getTime()) + ":</td></tr>");
  
          newsText.append("<tr><td class=\"title\">" + news.getTitle() + "</td></tr>");
  
          String text = news.getText();
          text = IOUtilities.replace(text, "&lt;", "<");
          text = IOUtilities.replace(text, "&gt;", ">");
          text = IOUtilities.replace(text, "/>", ">"); // JEditorPane knows no XHTML
          newsText.append("<tr><td class=\"text\">" + text + "</td></tr>");
  
          newsText.append("<tr><td class=\"author\">" + news.getAuthor() + "</td></tr>");
          newsText.append("</table>");
        }
      }
      
      buf.append(newsText);
      
      mHasNews = newsText.toString().trim().length() > 0;
    }
    
    buf.append("</body></html>");
    
    return buf.toString();
  }

  public boolean hasNews() {
    return mHasNews;
  }

  public void close() {
    NewsPlugin.getInstance().saveMeInternal(mSafeSettings.isSelected(), mShowOnlyNew, mNewsTypeSelection.getSelectedIndex());
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

  private static String getCssRgbColorEntry(Color c) {
    StringBuilder builder = new StringBuilder("rgb(");
    
    builder.append(c.getRed());
    builder.append(",");
    builder.append(c.getGreen());
    builder.append(",");
    builder.append(c.getBlue());
    builder.append(")");
    
    return builder.toString();
  }
}
