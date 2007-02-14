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

package tvbrowser.ui.update;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dialog;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.DefaultListCellRenderer;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.builder.ButtonBarBuilder;

import devplugin.PluginAccess;

import tvbrowser.core.Settings;
import tvbrowser.core.plugin.PluginManagerImpl;
import util.browserlauncher.Launch;
import util.exc.TvBrowserException;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;
import util.ui.html.ExtendedHTMLEditorKit;

public class SoftwareUpdateDlg extends JDialog implements ActionListener, ListSelectionListener, WindowClosingIf {

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(SoftwareUpdateDlg.class);

  private JButton mCloseBtn, mDownloadBtn;

  private JList mList;

  private JEditorPane mDescriptionPane;

  public SoftwareUpdateDlg(Dialog parent) {
    super(parent, true);
    createGui();
  }

  
  public SoftwareUpdateDlg(Frame parent) {
    super(parent, true);
    createGui();
  }
  
  private void createGui() {
    setTitle(mLocalizer.msg("title", "Download plugins"));

    JPanel contentPane = (JPanel) getContentPane();
    contentPane.setLayout(new BorderLayout(0, 10));
    contentPane.setBorder(BorderFactory.createEmptyBorder(10, 10, 11, 11));
    mCloseBtn = new JButton(Localizer.getLocalization(Localizer.I18N_CLOSE));
    mCloseBtn.addActionListener(this);

    mDownloadBtn = new JButton(mLocalizer.msg("download", "Download selected items"));
    mDownloadBtn.addActionListener(this);
    mDownloadBtn.setEnabled(false);

    ButtonBarBuilder builder = new ButtonBarBuilder();
    
    builder.addGlue();
    builder.addFixed(mDownloadBtn);
    builder.addRelatedGap();
    builder.addFixed(mCloseBtn);

    mList = new JList();
    mList.setCellRenderer(new SoftwareUpdateItemRenderer());
    mList.addListSelectionListener(this);

    JPanel northPn = new JPanel();
    northPn.add(new JLabel(mLocalizer.msg("header",
        "Hier k&ouml;nnen neue Plugins heruntergeladen und installiert werden.")));

    JPanel southPn = new JPanel(new BorderLayout());

    mDescriptionPane = new JEditorPane();

    mDescriptionPane.setEditorKit(new ExtendedHTMLEditorKit());
    mDescriptionPane.setEditable(false);

    mDescriptionPane.addHyperlinkListener(new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent evt) {
        if (evt.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          URL url = evt.getURL();
          if (url != null) {
            Launch.openURL(url.toString());
          }
        }
      }
    });

    southPn.add(builder.getPanel(), BorderLayout.SOUTH);

    JSplitPane splitPn = new JSplitPane(JSplitPane.HORIZONTAL_SPLIT, true, new JScrollPane(mList), new JScrollPane(
        mDescriptionPane));
    splitPn.setDividerLocation(150);

    contentPane.add(northPn, BorderLayout.NORTH);
    contentPane.add(splitPn, BorderLayout.CENTER);
    contentPane.add(southPn, BorderLayout.SOUTH);

    if (Settings.propUpdateDialogWidth.getInt() == -1 || Settings.propUpdateDialogHeight.getInt() == -1)
      this.setSize(450, 400);
    else
      this.setSize(Settings.propUpdateDialogWidth.getInt(), Settings.propUpdateDialogHeight.getInt());

    setDefaultCloseOperation(JDialog.DO_NOTHING_ON_CLOSE);

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        close();
      }
    });
    
    UiUtilities.registerForClosing(this);
  }

  private void updateDescription(SoftwareUpdateItem item) {
    if (item == null) {
      mDescriptionPane.setText("");
    } else {
      StringBuffer content = new StringBuffer();
      String author = item.getProperty("author");
      String website = item.getWebsite();
      devplugin.Version version = item.getVersion();

      content.append(
          "<html><head>" + "<style type=\"text/css\" media=\"screen\"><!--" + "body { font-family: Dialog; }"
              + "h1 { font-size: medium; }" + "td { text-align: left; font-style: bold; font-size: small;}"
              + "th { text-align: right; font-style: plain; font-size: small; }" + "--></style>" + "</head>").append(
          "<body><h1>").append(item.getName()).append("</h1>").append("<p>").append(item.getDescription()).append(
          "</p><br><table>");

      if (author != null) {
        content.append("<tr><th>").append(mLocalizer.msg("author", "Author")).append("</th><td>").append(author)
            .append("</td></tr>");
      }
      if (version != null) {
        content.append("<tr><th>").append(mLocalizer.msg("version", "Available version")).append("</th><td>").append(version)
            .append("</td></tr>");
      }
      for (PluginAccess plugin : PluginManagerImpl.getInstance().getActivatedPlugins()) {
		if (plugin.getInfo().getName().equalsIgnoreCase(item.getName())) {
	        content.append("<tr><th>").append(mLocalizer.msg("installed", "Installed version")).append("</th><td>").append(plugin.getInfo().getVersion())
            .append("</td></tr>");
	        break;
		}
	  } 
      if (website != null) {
        content.append("<tr><th>").append(mLocalizer.msg("website", "Website")).append("</th><td><a href=\"").append(
            website).append("\">").append(website).append("</a></td></tr>");
      }
      content.append("</table></body></html>");

      mDescriptionPane.setText(content.toString());
    }
  }

  public void actionPerformed(ActionEvent event) {
    if (event.getSource() == mCloseBtn) {
      close();
    } else if (event.getSource() == mDownloadBtn) {
      Object[] o = mList.getSelectedValues();
      int successfullyDownloadedItems = 0;
      for (int i = 0; i < o.length; i++) {
        SoftwareUpdateItem item = (SoftwareUpdateItem) o[i];
        try {
          item.download();
          successfullyDownloadedItems++;
        } catch (TvBrowserException e) {
          util.exc.ErrorHandler.handle(e);
        }
      }
      if (successfullyDownloadedItems > 0) {
        JOptionPane.showMessageDialog(null, mLocalizer.msg("restartprogram", "please restart tvbrowser before..."));
        close();
      }
    }
  }

  public void setSoftwareUpdateItems(SoftwareUpdateItem[] items) {
    mList.setListData(items);
  }

  public void valueChanged(ListSelectionEvent event) {
    JList list = (JList) event.getSource();

    Object[] items = list.getSelectedValues();
    if (items.length == 1) {
      updateDescription((SoftwareUpdateItem) items[0]);
    } else {
      updateDescription(null);
    }

    if (items.length == 0) {
      mDownloadBtn.setEnabled(false);
    } else {
      mDownloadBtn.setEnabled(true);
    }

  }

  class SoftwareUpdateItemRenderer extends DefaultListCellRenderer {

    public Component getListCellRendererComponent(JList list, Object value, int index, boolean isSelected,
        boolean cellHasFocus) {

      JLabel label = (JLabel) super.getListCellRendererComponent(list, value, index, isSelected, cellHasFocus);

      if (value instanceof SoftwareUpdateItem) {
        SoftwareUpdateItem item = (SoftwareUpdateItem) value;
        label.setText(item.getName());
      }
      return label;

    }

  }

  public void close() {
    Settings.propUpdateDialogWidth.setInt(getWidth());
    Settings.propUpdateDialogHeight.setInt(getHeight());

    setVisible(false);
  }

}