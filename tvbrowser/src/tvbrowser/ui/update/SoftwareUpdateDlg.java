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
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.net.URL;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.WindowConstants;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.builder.ButtonBarBuilder;

import devplugin.Channel;
import devplugin.Version;

import tvbrowser.core.Settings;
import util.browserlauncher.Launch;
import util.exc.TvBrowserException;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;
import util.ui.customizableitems.SelectableItem;
import util.ui.customizableitems.SelectableItemList;
import util.ui.customizableitems.SelectableItemRenderer;
import util.ui.html.ExtendedHTMLEditorKit;

public class SoftwareUpdateDlg extends JDialog implements ActionListener, ListSelectionListener, WindowClosingIf {

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(SoftwareUpdateDlg.class);

  private JButton mCloseBtn, mDownloadBtn;

  private JEditorPane mDescriptionPane;
  
  private String mDownloadUrl;
  
  private JCheckBox mAutoUpdates;
  
  private JSplitPane mSplitPane;
  
  private SelectableItemList test;

  public SoftwareUpdateDlg(Dialog parent, String downloadUrl, boolean onlyUpdate, SoftwareUpdateItem[] itemArr) {
    super(parent, true);    
    createGui(downloadUrl,onlyUpdate, itemArr);
  }

  
  public SoftwareUpdateDlg(Frame parent, String downloadUrl, boolean onlyUpdate, SoftwareUpdateItem[] itemArr) {
    super(parent, true);
    createGui(downloadUrl,onlyUpdate, itemArr);
  }
  
  private void createGui(String downloadUrl, boolean onlyUpdate, SoftwareUpdateItem[] itemArr) {
    mDownloadUrl = downloadUrl;
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
    
    if(onlyUpdate) {
      mAutoUpdates = new JCheckBox(mLocalizer.msg("autoUpdates","Find plugin updates automatically"), Settings.propAutoUpdatePlugins.getBoolean());
      mAutoUpdates.addItemListener(new ItemListener() {
        public void itemStateChanged(ItemEvent e) {
          Settings.propAutoUpdatePlugins.setBoolean(e.getStateChange() == ItemEvent.SELECTED);
        }
      });
      
      builder.addFixed(mAutoUpdates);
    }
    
    builder.addGlue();
    builder.addFixed(mDownloadBtn);
    builder.addRelatedGap();
    builder.addFixed(mCloseBtn);

    JPanel northPn = new JPanel();
    northPn.add(new JLabel(onlyUpdate ?mLocalizer.msg("updateHeader","Updates for installed plugins were found.") : 
      mLocalizer.msg("header","Here you can download new plugins and updates for it.")));

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

    test = new SelectableItemList(new Object[0],itemArr);
    test.addListSelectionListener(this);
    
    mSplitPane = new JSplitPane(JSplitPane.VERTICAL_SPLIT, true, test, new JScrollPane(
        mDescriptionPane));
    mSplitPane.setDividerLocation(Settings.propPluginUpdateDialogDividerLocation.getInt());
    
    contentPane.add(northPn, BorderLayout.NORTH);
    contentPane.add(mSplitPane, BorderLayout.CENTER);
    contentPane.add(southPn, BorderLayout.SOUTH);

    Settings.layoutWindow("softwareUpdateDlg", this, new Dimension(600,700));

    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

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
      Version version = item.getVersion();

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
      Version installedVersion = item.getInstalledVersion(); 
      if (installedVersion != null) {
        content.append("<tr><th>").append(mLocalizer.msg("installed", "Installed version")).append("</th><td>").append(installedVersion)
          .append("</td></tr>");
      } 
      if (website != null) {
        content.append("<tr><th>").append(mLocalizer.msg("website", "Website")).append("</th><td><a href=\"").append(
            website).append("\">").append(website).append("</a></td></tr>");
      }
      content.append("</table></body></html>");

      mDescriptionPane.setText(content.toString());
      mDescriptionPane.setCaretPosition(0);
    }
  }

  public void actionPerformed(ActionEvent event) {
    if (event.getSource() == mCloseBtn) {
      close();
    } else if (event.getSource() == mDownloadBtn) {
      Object[] objects = test.getSelection();
      int successfullyDownloadedItems = 0;
      for (Object object : objects) {
        SoftwareUpdateItem item = (SoftwareUpdateItem) object;
        try {
          item.download(mDownloadUrl);
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

  public void valueChanged(ListSelectionEvent event) {
    try {
      if(event.getSource() instanceof JList) {
        JList list = (JList) event.getSource();
    
        Object[] items = list.getSelectedValues();
        if (items.length == 1) {
          updateDescription((SoftwareUpdateItem) ((SelectableItem) items[0]).getItem());
        } else {
          updateDescription(null);
        }
      }
      else {
        mDownloadBtn.setEnabled(test.getSelection().length > 0);
      }
    }catch(Exception e) {e.printStackTrace();}
  }

  public void close() {
    Settings.propPluginUpdateDialogDividerLocation.setInt(mSplitPane.getDividerLocation());
    setVisible(false);
    dispose();
  }
  
  private static class SoftwareUpdateItemListCellRenderer extends SelectableItemRenderer {
    private int mSelectionWidth;
    
    public Component getListCellRendererComponent(JList list, Object value,
    int index, boolean isSelected, boolean cellHasFocus) {
      JPanel p = new JPanel(new BorderLayout(2,0));
      p.setBorder(BorderFactory.createEmptyBorder(0,2,0,0));
      
      SelectableItem selectableItem = (SelectableItem) value;

      JCheckBox cb = new JCheckBox("",selectableItem.isSelected());
      mSelectionWidth = cb.getPreferredSize().width;
      
      cb.setOpaque(false);
      
      p.add(cb, BorderLayout.WEST);
      
      if(selectableItem.getItem() instanceof Channel) {
        JLabel l = new JLabel();
        
        if(Settings.propShowChannelNamesInChannellist.getBoolean()) {
          l.setText(selectableItem.getItem().toString());
        }
                
        l.setOpaque(false);
        
        if(Settings.propShowChannelIconsInChannellist.getBoolean()) {
          l.setIcon(UiUtilities.createChannelIcon(((Channel)selectableItem.getItem()).getIcon()));
        }
        
        p.add(l, BorderLayout.CENTER);
        
        if(isSelected)
          l.setForeground(list.getSelectionForeground());
        else
          l.setForeground(list.getForeground());
      }
      else
        cb.setText(selectableItem.getItem().toString());
      
      if (isSelected) {
        p.setOpaque(true);
        p.setBackground(list.getSelectionBackground());
        cb.setForeground(list.getSelectionForeground());
        
      } else {
        p.setOpaque(false);
        p.setForeground(list.getForeground());
        cb.setForeground(list.getForeground());
      }
      cb.setEnabled(list.isEnabled());

      return p;
    }
    
    /**
     * @return The selection width.
     */
    public int getSelectionWidth() {
      return mSelectionWidth;
    }
    
  }
}