/*
 * TV-Browser
 * Copyright (C) 2014 TV-Browser team (dev@tvbrowser.org)
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
package tvbrowser.core.icontheme;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.Rectangle;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.FocusAdapter;
import java.awt.event.FocusEvent;
import java.awt.event.FocusListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.File;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.ArrayList;
import java.util.Locale;

import javax.swing.BoxLayout;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import tvbrowser.core.Settings;
import util.ui.Localizer;
import util.ui.ScrollableJPanel;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;

/**
 * Dialog for download of theme icons.
 * 
 * @author René Mach
 */
public class ThemeDownloadDlg extends JDialog implements WindowClosingIf {
  public static final int THEME_ICON_TYPE = 0;
  public static final int INFO_ICON_TYPE = 1;
  
  static final Localizer LOCALIZER = Localizer.getLocalizerFor(ThemeDownloadDlg.class);
  
  private ThemePanel mMarked;
  
  private ArrayList<ThemeDownloadItem> mSelectedItems;
  private ArrayList<ThemeDownloadItem> mSuccessDownloads;
  
  private JButton mDownload;
  private File mDownloadDirectory;
    
  public ThemeDownloadDlg(Window parent, int type) {
    super(parent, ModalityType.APPLICATION_MODAL);
    
    mDownloadDirectory = null;
    mSuccessDownloads = new ArrayList<ThemeDownloadItem>();
    
    ArrayList<String> currentNameList = new ArrayList<String>();
    String mSpecURL = null;
        
    switch(type) {
      case THEME_ICON_TYPE: {
        setTitle(LOCALIZER.msg("title.iconTheme", "TV-Browser icon themes download"));
        mSpecURL = IconLoader.DOWNLOAD_SPEC_URL;
        mDownloadDirectory = IconLoader.USER_ICON_DIR;
        
        IconTheme[] available = IconLoader.getInstance().getAvailableThemes();
        
        for(IconTheme theme : available) {
          currentNameList.add(theme.getName());
        }
      } break;
      case INFO_ICON_TYPE: {
        setTitle(LOCALIZER.msg("title.infoIconTheme", "TV-Browser info icon themes download"));
        mSpecURL = InfoThemeLoader.DOWNLOAD_SPEC_URL;
        mDownloadDirectory = InfoThemeLoader.USER_ICON_DIR;
        
        InfoIconTheme[] available = InfoThemeLoader.getInstance().getAvailableInfoIconThemes();
        
        for(InfoIconTheme theme : available) {
          currentNameList.add(theme.toString());
        }
      } break;
    }
    
    MouseListener mouseListener = createMouseListener();
    FocusListener focusListener = createFocusListener();
    
    mSelectedItems = new ArrayList<ThemeDownloadItem>();
    
    final JPanel mainPanel = new ScrollableJPanel() {
      @Override
      public int getScrollableBlockIncrement(Rectangle visibleRect, int orientation, int direction) {
        return Math.max(1,(int)visibleRect.getHeight()/4);
      }
      
      @Override
      public int getScrollableUnitIncrement(Rectangle visibleRect, int orientation, int direction) {
        return Math.max(1,(int)visibleRect.getHeight()/8);
      }
    };
    mainPanel.setLayout(new BoxLayout(mainPanel, BoxLayout.Y_AXIS));
    mainPanel.setBackground(UIManager.getColor("List.background"));
    
    mDownload = new JButton(LOCALIZER.msg("download", "Download selected themes"));
    mDownload.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        for(ThemeDownloadItem item : mSelectedItems) {
          if(!item.download(mDownloadDirectory)) {
            // TODO info message
          }
          else {
            mSuccessDownloads.add(item);
          }
        }
        
        close();
      }
    });
    
    JButton close = new JButton(Localizer.getLocalization(Localizer.I18N_CLOSE));
    close.addActionListener(new ActionListener() {
      @Override
      public void actionPerformed(ActionEvent e) {
        close();
      }
    });
    
    JPanel buttons = new JPanel(new FormLayout("default:grow,default,2dlu,default","5dlu,default"));
    
    buttons.add(mDownload, CC.xy(2, 2));
    buttons.add(close, CC.xy(4, 2));
    
    mDownload.setEnabled(false);
    close.grabFocus();
    getRootPane().setDefaultButton(close);
    
    try {
      ThemeDownloadItem[] downloadItems = ThemeDownloader.downloadThemes(new URL(mSpecURL), mDownloadDirectory);
      
      if(downloadItems != null) {
        for(ThemeDownloadItem item : downloadItems) {
          if(!currentNameList.contains(item.getName(Locale.getDefault()))) {
            mainPanel.add(new ThemePanel(item,mouseListener,focusListener));
          }
        }
      }
    } catch (MalformedURLException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    
    final JScrollPane scroll = new JScrollPane(mainPanel);
    scroll.getViewport().setBackground(UIManager.getColor("List.background"));
    
    setLayout(new BorderLayout());
    ((JPanel)getContentPane()).setBorder(Borders.DIALOG);
    
    add(scroll, BorderLayout.CENTER);
    add(buttons, BorderLayout.SOUTH);
    
    Settings.layoutWindow("themeDownloadDlg", this, new Dimension(500,500));
    
    SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        scroll.getVerticalScrollBar().setValue(0);        
      }
    });
    
    UiUtilities.registerForClosing(this);
    
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowOpened(WindowEvent e) {
        if(mainPanel.getComponentCount() == 0) {
          JOptionPane.showMessageDialog(ThemeDownloadDlg.this, LOCALIZER.msg("noIcons", "No new icons were found."), Localizer.getLocalization(Localizer.I18N_INFO), JOptionPane.INFORMATION_MESSAGE);
          close();
        }
      }
    });
  }
  
  private MouseListener createMouseListener() {
    MouseListener mouseListener = new MouseAdapter() {
      @Override
      public void mouseClicked(MouseEvent e) {
        Component c = e.getComponent();
        
        if(!(c instanceof ThemePanel)) {
          while(c.getParent() != null && c.getParent() instanceof ThemePanel) {
            c = c.getParent();
          }
        }
        
        if(c instanceof ThemePanel) {
          if(mMarked != null && !mMarked.equals(c)) {
            mMarked.unmark();
          }
          
          mMarked = (ThemePanel)c;
          mMarked.mark();
        }
        else {
          mMarked.unmark();
        }
      }
    };
    
    return mouseListener;
  }
  
  private FocusListener createFocusListener() {
    FocusListener focusListener = new FocusAdapter() {
      @Override
      public void focusGained(FocusEvent e) {
        Component c = e.getComponent();
        
        if(!(c instanceof ThemePanel)) {
          while(c.getParent() != null && c.getParent() instanceof ThemePanel) {
            c = c.getParent();
          }
        }
        
        if(c instanceof ThemePanel) {
          if(mMarked != null && !mMarked.equals(c)) {
            mMarked.unmark();
          }
          
          mMarked = (ThemePanel)c;
          mMarked.mark();
        }
        else {
          mMarked.unmark();
        }
      }
    };
    
    return focusListener;
  }
  
  public ThemeDownloadItem[] getSuccessItems() {
    return mSuccessDownloads.toArray(new ThemeDownloadItem[mSuccessDownloads.size()]);
  }

  @Override
  public void close() {
    mSelectedItems.clear();
    dispose();
  }
  
  public boolean downloadSuccess() {
    return !mSuccessDownloads.isEmpty();
  }
  
  private final class ThemePanel extends JPanel {
    private ThemeDownloadItem mItem;
    private JCheckBox mSelected;
    private JEditorPane mDescription;
    private JEditorPane mAuthor;
    private JLabel mName;
    
    public ThemePanel(ThemeDownloadItem item, MouseListener mouseListener, FocusListener focusListener) {
      mItem = item;
      mSelected = new JCheckBox();
      mSelected.setOpaque(false);
      mSelected.addItemListener(new ItemListener() {
        @Override
        public void itemStateChanged(ItemEvent e) {
          if(e.getStateChange() == ItemEvent.SELECTED) {
            mSelectedItems.add(mItem);
          }
          else { 
            mSelectedItems.remove(mItem);
          }
          
          mDownload.setEnabled(!mSelectedItems.isEmpty());
        }
      });
      mSelected.addFocusListener(focusListener);
      
      setBackground(UIManager.getColor("List.background"));
      
      Color foreground = UIManager.getColor("List.foreground");
      
      setLayout(new FormLayout("10dlu,2dlu,28dlu,5dlu,50dlu:grow","fill:default:grow,default,default,default,fill:default:grow"));
      setBorder(Borders.DLU2);
      
      mName = new JLabel(item.getName(Locale.getDefault()));
      mName.setFont(mName.getFont().deriveFont(Font.BOLD));
      mName.setFont(mName.getFont().deriveFont(mName.getFont().getSize2D() + 2.0f));
      mName.setOpaque(false);
      mName.setForeground(foreground);
      
      ImageIcon icon = mItem.getPreviewImage();
      
      String description = item.getDescription(Locale.getDefault());
      
      if(icon != null) {
        icon = (ImageIcon)UiUtilities.scaleIcon(icon, Sizes.dialogUnitXAsPixel(28, mName));
        
        JLabel iconLabel = new JLabel(icon);
        iconLabel.setOpaque(false);
        
        add(iconLabel, CC.xywh(3,1,1,5));
      }

      add(mSelected, CC.xywh(1,1,1,5));
      add(mName, CC.xy(5, 2));
      
      if(description != null) {
        mDescription = UiUtilities.createHtmlHelpTextArea(description, foreground, getBackground());
        mDescription.setOpaque(false);
        mDescription.addMouseListener(mouseListener);
        
        add(mDescription, CC.xy(5, 3));
      }
      
      mAuthor = UiUtilities.createHtmlHelpTextArea(LOCALIZER.msg("providedBy", "Provided by: {0}", mItem.getAuthor()) + item.getAuthor(), foreground, getBackground());
      mAuthor.setOpaque(false);
      mAuthor.addMouseListener(mouseListener);
      mAuthor.setForeground(foreground);
      
      add(mAuthor, CC.xy(5, 4));
      addMouseListener(mouseListener);
    }
    
    public void mark() {
      setBackground(UIManager.getColor("List.selectionBackground"));
      mName.setForeground(UIManager.getColor("List.selectionForeground"));
      
      if(mDescription != null) {
        mDescription.setForeground(mName.getForeground());
        UiUtilities.updateHtmlHelpTextArea(mDescription, mItem.getDescription(Locale.getDefault()), mName.getForeground(), getBackground());
      }
      
      mAuthor.setForeground(mName.getForeground());
      UiUtilities.updateHtmlHelpTextArea(mAuthor, LOCALIZER.msg("providedBy", "Provided by: {0}", mItem.getAuthor()), mName.getForeground(), getBackground());
    }
    
    public void unmark() {
      setBackground(UIManager.getColor("List.background"));
      mName.setForeground(UIManager.getColor("List.foreground"));
      
      if(mDescription != null) {
        mDescription.setForeground(mName.getForeground());
        UiUtilities.updateHtmlHelpTextArea(mDescription, mItem.getDescription(Locale.getDefault()), mName.getForeground(), getBackground());
      }
      
      mAuthor.setForeground(mName.getForeground());
      UiUtilities.updateHtmlHelpTextArea(mAuthor, LOCALIZER.msg("providedBy", "Provided by: {0}", mItem.getAuthor()), mName.getForeground(), getBackground());
    }
  }
}
