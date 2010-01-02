package idontwant2see;

import java.lang.reflect.Method;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JCheckBox;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;

//import util.ui.DefaultProgramImportanceSelectionPanel;
import util.ui.Localizer;
import util.ui.ScrollableJPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.SettingsTab;

public class IDontWant2SeeSettingsTab implements SettingsTab {
  private JCheckBox mAutoSwitchToMyFilter;
  private JRadioButton mSimpleContextMenu;
  private JRadioButton mCascadedContextMenu;
  private ExclusionTablePanel mExclusionPanel;
  private Localizer mLocalizer = IDontWant2See.mLocalizer;
  //TODO After 3.0 release enable settings for program importance
  //private DefaultProgramImportanceSelectionPanel mProgramImportancePanel;
  private JPanel mProgramImportancePanel;
  private IDontWant2SeeSettings mSettings;
  
  public IDontWant2SeeSettingsTab(IDontWant2SeeSettings settings) {
    mSettings = settings;
  }

  public JPanel createSettingsPanel() {
    final CellConstraints cc = new CellConstraints();
    final PanelBuilder pb = new PanelBuilder(
        new FormLayout("5dlu,default,0dlu:grow,default,5dlu",
            "default,10dlu,default,5dlu,default,5dlu,default,5dlu,fill:default:grow,10dlu,fill:default:grow"),
            new ScrollableJPanel());

    final PanelBuilder pb2 = new PanelBuilder(new FormLayout(
        "default,2dlu,default", "default,1dlu,default,default"));

    mSimpleContextMenu = new JRadioButton(mLocalizer.msg(
        "settings.menu.simple", "Direct in the context menu:"), mSettings
        .isSimpleMenu());
    mSimpleContextMenu.setHorizontalTextPosition(JRadioButton.RIGHT);

    mCascadedContextMenu = new JRadioButton(mLocalizer.msg(
        "settings.menu.cascaded", "In a sub menu:"), !mSettings.isSimpleMenu());
    mCascadedContextMenu.setHorizontalTextPosition(JRadioButton.RIGHT);
    mCascadedContextMenu.setVerticalAlignment(JRadioButton.TOP);

    final ButtonGroup bg = new ButtonGroup();

    bg.add(mSimpleContextMenu);
    bg.add(mCascadedContextMenu);

    pb2.add(mSimpleContextMenu, cc.xy(1, 1));
    pb2.addLabel("-"
        + mLocalizer.msg("name", "I don't want to see!")
        + " ("
        + mLocalizer.msg("menu.completeCaseSensitive",
            "Instant exclusion with title") + ")", cc.xy(3, 1));

    pb2.add(mCascadedContextMenu, cc.xy(1, 3));
    pb2.addLabel("-"
        + mLocalizer.msg("menu.completeCaseSensitive",
            "Instant exclusion with title"), cc.xy(3, 3));
    pb2.addLabel(
        "-" + mLocalizer.msg("menu.userEntered", "User entered value"), cc.xy(
            3, 4));

    mAutoSwitchToMyFilter = new JCheckBox(mLocalizer.msg("settings.autoFilter",
        "Automatically activate filter on adding/removing"), mSettings
        .isSwitchToMyFilter());

    pb.add(mAutoSwitchToMyFilter, cc.xyw(2, 1, 3));
    pb.addSeparator(mLocalizer.msg("settings.contextMenu", "Context menu"), cc
        .xyw(1, 3, 5));
    pb.add(pb2.getPanel(), cc.xyw(2, 5, 3));
    pb.addSeparator(mLocalizer.msg("settings.search", "Search"), cc
        .xyw(1, 7, 5));
    pb.add(mExclusionPanel = new ExclusionTablePanel(mSettings), cc.xyw(2, 9, 3));

  //TODO After 3.0 release enable settings for program importance
    /*mProgramImportancePanel = DefaultProgramImportanceSelectionPanel.createPanel(mSettings.getProgramImportance(),true,false);*/
   try { 
    Class importanceSettings = Class.forName("util.ui.DefaultProgramImportanceSelectionPanel");
    Method createPanel = importanceSettings.getMethod("createPanel", new Class[] {byte.class, boolean.class, boolean.class});
    mProgramImportancePanel  = (JPanel)createPanel.invoke(importanceSettings, new Object[] {mSettings.getProgramImportance(),true,false});
    
    pb.add(mProgramImportancePanel, cc.xyw(2, 11, 4));
    
   }catch(Exception e) {
     e.printStackTrace();
     //ignore
   }
    final JPanel p = new JPanel(new FormLayout("0dlu,0dlu:grow",
        "5dlu,fill:default:grow"));
    
    JScrollPane scrollPane = new JScrollPane(pb.getPanel());
    scrollPane.setBorder(null);
    scrollPane.setViewportBorder(null);
    
    p.add(scrollPane, cc.xy(2, 2));

    return p;
  }

  public Icon getIcon() {
    return IDontWant2See.getInstance().createImageIcon("apps", "idontwant2see", 16);
  }

  public String getTitle() {
    return null;
  }

  public void saveSettings() {
    mSettings.setSimpleMenu(mSimpleContextMenu.isSelected());
    mSettings.setSwitchToMyFilter(mAutoSwitchToMyFilter.isSelected());
  //TODO After 3.0 release enable settings for program importance
    //mSettings.setProgramImportance(mProgramImportancePanel.getSelectedImportance());

    try {
      mSettings.setProgramImportance((Byte)mProgramImportancePanel.getClass().getMethod("getSelectedImportance", new Class[0]).invoke(mProgramImportancePanel, new Object[0]));
    } catch (Exception e) {
      // ignore
    }
    
    mExclusionPanel.saveSettings(mSettings);
  }
}
