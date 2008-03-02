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
import java.awt.Color;
import java.awt.Dialog;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Frame;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComponent;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.WindowConstants;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import devplugin.Version;

import tvbrowser.core.Settings;
import tvbrowser.core.icontheme.IconLoader;
import util.exc.TvBrowserException;
import util.ui.Localizer;
import util.ui.TextAreaIcon;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;
import util.ui.customizableitems.SelectableItemList;
import util.ui.customizableitems.SelectableItemRendererCenterComponentIf;

import util.ui.html.HTMLTextHelper;

public class SoftwareUpdateDlg extends JDialog implements ActionListener, ListSelectionListener, WindowClosingIf {

  /** The localizer for this class. */
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(SoftwareUpdateDlg.class);

  private JButton mCloseBtn, mDownloadBtn;
  
  private String mDownloadUrl;
  
  private JCheckBox mAutoUpdates;
  
  private SelectableItemList mSoftwareUpdateItemList;
  
  private int mLastIndex;

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

    southPn.add(builder.getPanel(), BorderLayout.SOUTH);

    mSoftwareUpdateItemList = new SelectableItemList(new Object[0],itemArr);
    mSoftwareUpdateItemList.addListSelectionListener(this);
    mSoftwareUpdateItemList.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    mSoftwareUpdateItemList.setListUI(new MyListUI());
    
    mSoftwareUpdateItemList.addCenterRendererComponent(SoftwareUpdateItem.class,new SelectableItemRendererCenterComponentIf() {
      private final ImageIcon NEW_VERSION_ICON = IconLoader.getInstance().getIconFromTheme("status", "software-update-available", 16);
      
      public JPanel createCenterPanel(JList list, Object value, int index, boolean isSelected, boolean isEnabled) {
        CellConstraints cc = new CellConstraints();
        FormLayout layout = new FormLayout("5dlu,default,5dlu,default:grow","2dlu,default,2dlu,fill:pref:grow,2dlu");
        PanelBuilder pb = new PanelBuilder(layout);
        pb.getPanel().setOpaque(false);
        
        SoftwareUpdateItem item = (SoftwareUpdateItem)value;
        
        JLabel label = pb.addLabel(HTMLTextHelper.convertHtmlToText(item.getName()) + " " + item.getVersion(), cc.xy(2,2));
        label.setFont(label.getFont().deriveFont(Font.BOLD, label.getFont().getSize2D()+2));
        
        JLabel label3 = new JLabel();
        
        Version installedVersion = item.getInstalledVersion();
        if ((installedVersion != null) && (installedVersion.compareTo(item.getVersion()) < 0)) { 
          label.setIcon(NEW_VERSION_ICON);
          
          label3.setText("(" + mLocalizer.msg("installed","Installed version: ") + installedVersion.toString()+")");
          label3.setFont(label3.getFont().deriveFont((float)label3.getFont().getSize2D()+2));
          
          pb.add(label3, cc.xy(4,2));
        }
        
        if (isSelected && isEnabled) {
          label.setForeground(list.getSelectionForeground());
          
          String author = item.getProperty("author");
          String website = item.getWebsite();

          if (author != null) {
            layout.appendRow(new RowSpec("2dlu"));
            layout.appendRow(new RowSpec("default"));
            layout.appendRow(new RowSpec("2dlu"));
            
            JLabel autor = pb.addLabel(mLocalizer.msg("author", "Author"), cc.xy(2,7));
            autor.setFont(autor.getFont().deriveFont(Font.BOLD));
            autor.setForeground(list.getSelectionForeground());
            
            pb.addLabel(HTMLTextHelper.convertHtmlToText(author), cc.xy(4,7)).setForeground(list.getSelectionForeground());
          }
          
          if (website != null) {
            if(author == null) {
              layout.appendRow(new RowSpec("2dlu"));
            }
            
            layout.appendRow(new RowSpec("default"));
            layout.appendRow(new RowSpec("2dlu"));
            
            JLabel webs = pb.addLabel(mLocalizer.msg("website", "Website"), cc.xy(2,author == null ? 7 : 9));
            webs.setFont(webs.getFont().deriveFont(Font.BOLD));
            webs.setForeground(list.getSelectionForeground());
            
            pb.addLabel(website, cc.xy(4,author == null ? 7 : 9)).setForeground(list.getSelectionForeground());
          }
          
          TextAreaIcon icon = new TextAreaIcon(HTMLTextHelper.convertHtmlToText(item.getDescription()), new JLabel().getFont(),list.getPreferredScrollableViewportSize().width - 15, 2);
          
          JLabel iconLabel = new JLabel("");
          iconLabel.setForeground(list.getSelectionForeground());
          iconLabel.setIcon(icon);
          
          pb.add(iconLabel, cc.xyw(2,4,3));
          
          
          label3.setForeground(list.getSelectionForeground());
        } else {
          if(!item.isStable()) {
            label.setForeground(new Color(200, 0, 0));
          }
          else {
            label.setForeground(list.getForeground());
          }

          JLabel label2 = pb.addLabel(HTMLTextHelper.convertHtmlToText(item.getDescription().length() > 100 ? item.getDescription().substring(0,100) + "..." : item.getDescription()), cc.xyw(2,4,3));
          
          label2.setForeground(list.getForeground());        
          label3.setForeground(Color.gray);
        }
        
        return pb.getPanel();
      }

      public void calculateSize(JList list, int index, JPanel contentPane) {
        ((MyListUI)list.getUI()).setCellHeight(index, contentPane.getPreferredSize().height);
      }
    });
        
    contentPane.add(northPn, BorderLayout.NORTH);
    contentPane.add(mSoftwareUpdateItemList, BorderLayout.CENTER);
    contentPane.add(southPn, BorderLayout.SOUTH);

    Settings.layoutWindow("softwareUpdateDlg", this, new Dimension(800,600));
    
    setDefaultCloseOperation(WindowConstants.DO_NOTHING_ON_CLOSE);

    addWindowListener(new WindowAdapter() {
      public void windowClosing(WindowEvent e) {
        close();
      }
    });
    
    UiUtilities.registerForClosing(this);
  }

  public void actionPerformed(ActionEvent event) {
    if (event.getSource() == mCloseBtn) {
      close();
    } else if (event.getSource() == mDownloadBtn) {
      Object[] objects = mSoftwareUpdateItemList.getSelection();
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
    if (!(event.getSource() instanceof JList)){
      mDownloadBtn.setEnabled(mSoftwareUpdateItemList.getSelection().length > 0);
      
      if(!event.getValueIsAdjusting()) {
        JList list = ((JList)event.getSource());
        if(mLastIndex != -1 && list.getSelectedIndex() != mLastIndex) {
          ((MyListUI)list.getUI()).setCellHeight(mLastIndex,list.getCellRenderer().getListCellRendererComponent(list, list.getModel().getElementAt(mLastIndex),
              mLastIndex, false, false).getPreferredSize().height);
        }
        
        mLastIndex = list.getSelectedIndex();
      }
    }
    
    mSoftwareUpdateItemList.calculateSize();
  }

  public void close() {
    setVisible(false);
    dispose();
  }
  
  private static class MyListUI extends javax.swing.plaf.basic.BasicListUI {
    protected synchronized void setCellHeight(int row, int height) {
      cellHeights[row] = height;      
    }
    
    public Dimension getPreferredSize(JComponent c) {
      int width = super.getPreferredSize(c).width;
      int height = 0;
      
      Insets i = c.getInsets();
      
      height += i.top + i.bottom;
      
      for(int cellHeight : cellHeights) {
        height += cellHeight;
      }
      
      return new Dimension(width,height);
    }
  }
}
