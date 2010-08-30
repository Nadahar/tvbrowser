package util.ui;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.util.ArrayList;

import javax.swing.JButton;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import tvbrowser.ui.mainframe.MainFrame;
import tvbrowser.ui.settings.GlobalPluginProgramFormatingSettings;
import tvbrowser.ui.settings.SettingsDialog;
import util.program.AbstractPluginProgramFormating;
import util.program.LocalPluginProgramFormating;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Plugin;
import devplugin.SettingsItem;

/**
 * A class that provides a panel for configuration of the
 * supported program formatings of a plugin.
 * 
 * @author René Mach
 * @since 2.5.1
 */
public class PluginProgramConfigurationPanel extends JPanel implements ActionListener {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(PluginProgramConfigurationPanel.class);
  
  private OrderChooser mOrder;
  private JButton mAdd, mEdit, mDelete;
  
  private LocalPluginProgramFormating mDefaultLocalFormating;
  private boolean mShowTitleSetting, mShowEncodingSetting;
  
  private JEditorPane mHelpLabel;
  
  /**
   * Creates an instance of this settings panel.
   * 
   * @param selectedValues The selected formatting for showing in program context menu.
   * @param availableLocalFormatings The available formattings provided by the plugin itself.
   * @param defaultLocalFormating The default formatting used by the plugin.
   * @param showTitleSetting Show the title setting part of this dialog.
   * @param showEncodingSetting Show the encoding setting part of this dialog.
   */
  public PluginProgramConfigurationPanel(AbstractPluginProgramFormating[] selectedValues, LocalPluginProgramFormating[] availableLocalFormatings, LocalPluginProgramFormating defaultLocalFormating, boolean showTitleSetting, boolean showEncodingSetting) {
    CellConstraints cc = new CellConstraints();
    PanelBuilder pb = new PanelBuilder(new FormLayout("5dlu,default:grow,5dlu","pref,fill:default:grow,5dlu,pref,10dlu,pref"), this);
    
    mDefaultLocalFormating = (defaultLocalFormating == null) ? new LocalPluginProgramFormating("Plugin - Default","{title}","{channel_name} - {title}\n{leadingZero(start_day,\"2\")}.{leadingZero(start_month,\"2\")}.{start_year} {leadingZero(start_hour,\"2\")}:{leadingZero(start_minute,\"2\")}-{leadingZero(end_hour,\"2\")}:{leadingZero(end_minute,\"2\")}\n\n{splitAt(short_info,\"78\")}\n\n","UTF-8") : defaultLocalFormating;
    mShowTitleSetting = showTitleSetting;
    mShowEncodingSetting = showEncodingSetting;
    
    AbstractPluginProgramFormating[] availableGlobalFormatings = Plugin.getPluginManager().getAvailableGlobalPuginProgramFormatings();
    
    if(availableLocalFormatings == null) {
      availableLocalFormatings = new LocalPluginProgramFormating[1];
      availableLocalFormatings[0] = mDefaultLocalFormating;
    }
    
    ArrayList<AbstractPluginProgramFormating> formatingsList = new ArrayList<AbstractPluginProgramFormating>();
    
    for(AbstractPluginProgramFormating config : availableGlobalFormatings) {
      if(config != null) {
        formatingsList.add(config);
      }
    }
      
    for(LocalPluginProgramFormating config : availableLocalFormatings) {
      if(config != null) {
        formatingsList.add(config);
      }
    }
    
    AbstractPluginProgramFormating[] allArr = formatingsList.toArray(new AbstractPluginProgramFormating[formatingsList.size()]);
    
    FormLayout layout = new FormLayout("default,5dlu,default,5dlu,default","pref");
    layout.setColumnGroups(new int[][] {{1,3,5}});
    
    JPanel buttonPanel = new JPanel(layout);
    
    mAdd = new JButton(Localizer.getLocalization(Localizer.I18N_ADD));
    mAdd.setIcon(TVBrowserIcons.newIcon(TVBrowserIcons.SIZE_SMALL));
    mAdd.addActionListener(this);
    
    mEdit = new JButton(Localizer.getLocalization(Localizer.I18N_EDIT));
    mEdit.setIcon(TVBrowserIcons.edit(TVBrowserIcons.SIZE_SMALL));
    mEdit.setEnabled(false);
    mEdit.addActionListener(this);
    
    mDelete = new JButton(Localizer.getLocalization(Localizer.I18N_DELETE));
    mDelete.setIcon(TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL));
    mDelete.setEnabled(false);
    mDelete.addActionListener(this);
    
    buttonPanel.add(mAdd, cc.xy(1,1));
    buttonPanel.add(mEdit, cc.xy(3,1));
    buttonPanel.add(mDelete, cc.xy(5,1));
    
    mOrder = new OrderChooser(selectedValues == null ? allArr : selectedValues, allArr);
    mOrder.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    mOrder.addListSelectionListener(new ListSelectionListener() {
      public void valueChanged(ListSelectionEvent e) {
        if(!e.getValueIsAdjusting()) {
          mEdit.setEnabled(mOrder.getSelectedValue() instanceof LocalPluginProgramFormating);
          mDelete.setEnabled(mOrder.getSelectedValue() instanceof LocalPluginProgramFormating);
        }
      }
    });
    
    mOrder.addMouseListener(new MouseAdapter() {
      public void mouseClicked(MouseEvent e) {
        if(SwingUtilities.isLeftMouseButton(e) && e.getClickCount() >= 2){
          if(mOrder.getSelectedValue() instanceof LocalPluginProgramFormating) {
            LocalPluginProgramFormatingSettingsDialog.createInstance(UiUtilities.getLastModalChildOf(MainFrame.getInstance()), (LocalPluginProgramFormating)mOrder.getSelectedValue(), mDefaultLocalFormating, mShowTitleSetting, mShowEncodingSetting);
            mOrder.refreshList();
          }
        }
      }
    });
    
    mHelpLabel = UiUtilities.createHtmlHelpTextArea(mLocalizer.msg("help","This list contains formattings that are provided by the plugin itself and formating available for all plugins. You can configure formatings, that are provided by the plugin itself, direct here. The formatings that are available for all plugins can be configured in <a href=\"#link\">{0}</a>.",GlobalPluginProgramFormatingSettings.mLocalizer.msg("title","Plugin program formating")), new HyperlinkListener() {
      public void hyperlinkUpdate(HyperlinkEvent e) {
        if (e.getEventType() == HyperlinkEvent.EventType.ACTIVATED) {
          SettingsDialog.getInstance().showSettingsTab(SettingsItem.PLUGINPROGRAMFORMAT);
        }
      }
    });
    
    pb.addLabel(mLocalizer.msg("title","Formatings that should be shown for selection in the context menu:"),cc.xy(2,1));
    pb.add(mOrder, cc.xy(2,2));
    pb.add(buttonPanel, cc.xy(2,4));
    pb.add(mHelpLabel, cc.xy(2,6));
  }
  
  /**
   * Gets the selected program formatings for showing in the context menu.
   * 
   * @return The selected program formatings.
   */
  public AbstractPluginProgramFormating[] getSelectedPluginProgramFormatings() {
    Object[] o = mOrder.getOrder();
    
    AbstractPluginProgramFormating[] configs = new AbstractPluginProgramFormating[o.length];
    
    for(int i = 0; i < o.length; i++) {
      configs[i] = (AbstractPluginProgramFormating)o[i];
    }
    
    return configs;
  }

  /**
   * Gets the available program formatings provided by the plugin itself.
   * 
   * @return The available program formatings provided by the plugin itself.
   */
  public LocalPluginProgramFormating[] getAvailableLocalPluginProgramFormatings() {
    Object[] order = mOrder.getOrder();
    
    ArrayList<LocalPluginProgramFormating> list = new ArrayList<LocalPluginProgramFormating>();
    
    for(Object value : order) {
      if(value instanceof LocalPluginProgramFormating) {
        list.add((LocalPluginProgramFormating)value);
      }
    }
    
    return list.toArray(new LocalPluginProgramFormating[list.size()]);
  }

  public void actionPerformed(ActionEvent e) {
    if(e.getSource() == mAdd) {
      LocalPluginProgramFormating newFormatting = new LocalPluginProgramFormating(mLocalizer.msg("newName", "New formatting"), mDefaultLocalFormating.getTitleValue(), mDefaultLocalFormating.getContentValue(), mDefaultLocalFormating.getEncodingValue());
      mOrder.addElement(newFormatting);
      mOrder.setSelectedIndex(mOrder.getItemCount() - 1);
      LocalPluginProgramFormatingSettingsDialog.createInstance(UiUtilities.getLastModalChildOf(MainFrame.getInstance()), (LocalPluginProgramFormating)mOrder.getSelectedValue(), mDefaultLocalFormating, mShowTitleSetting, mShowEncodingSetting);
      mOrder.refreshList();
    }
    else if(e.getSource() == mDelete) {
      int index = mOrder.getSelectedIndex();
      mOrder.removeElementAt(index);
      mOrder.setSelectedIndex(index);
    } else if(e.getSource() == mEdit) {
      LocalPluginProgramFormatingSettingsDialog.createInstance(UiUtilities.getLastModalChildOf(MainFrame.getInstance()), (LocalPluginProgramFormating)mOrder.getSelectedValue(), mDefaultLocalFormating, mShowTitleSetting, mShowEncodingSetting);
      mOrder.refreshList();
    }
  }
}
