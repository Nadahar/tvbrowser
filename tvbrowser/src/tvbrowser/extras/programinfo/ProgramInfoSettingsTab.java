package tvbrowser.extras.programinfo;

import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.BorderFactory;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.l2fprod.common.swing.plaf.LookAndFeelAddons;

import tvbrowser.core.icontheme.IconLoader;
import util.program.CompoundedProgramFieldType;
import util.program.ProgramTextCreator;
import util.ui.FontChooserPanel;
import util.ui.Localizer;
import util.ui.OrderChooser;
import util.ui.PluginsPictureSettingsPanel;

import devplugin.Plugin;
import devplugin.ProgramFieldType;
import devplugin.SettingsTab;

/**
 * The order settings for the ProgramInfo.
 * 
 * @author René Mach
 *
 */
public class ProgramInfoSettingsTab implements SettingsTab {
  
  private OrderChooser mList;
  private String mOldOrder, mOldSetupState;
  private PluginsPictureSettingsPanel mPictureSettings;
  
  private JCheckBox mZoomEnabled;
  private JSpinner mZoomValue;
  
  private JCheckBox mUserFont, mAntiAliasing;
  private FontChooserPanel mTitleFont, mBodyFont;
  
  private String mOldTitleFont, mOldBodyFont,
  mOldTitleFontSize, mOldBodyFontSize, mOldUserFontSelected,
  mOldAntiAliasingSelected;
  
  private String mOldLook;
  
  private JComboBox mLook;
  
  private static int mSelectedTab = 0;
  
  private String[] mLf = {
      "com.l2fprod.common.swing.plaf.aqua.AquaLookAndFeelAddons",
      "com.l2fprod.common.swing.plaf.metal.MetalLookAndFeelAddons",
      "com.l2fprod.common.swing.plaf.motif.MotifLookAndFeelAddons",
      "com.l2fprod.common.swing.plaf.windows.WindowsLookAndFeelAddons",
      "com.l2fprod.common.swing.plaf.windows.WindowsClassicLookAndFeelAddons"
  };
  
  private JCheckBox mShowFunctions, mShowTextSearchButton;
  
  public JPanel createSettingsPanel() {
    mOldAntiAliasingSelected = ProgramInfo.getInstance().getSettings().getProperty("antialiasing", "false");
    mOldUserFontSelected = ProgramInfo.getInstance().getSettings().getProperty("userfont", "false");
    mOldTitleFontSize = ProgramInfo.getInstance().getSettings().getProperty("title", "18");
    mOldBodyFontSize = ProgramInfo.getInstance().getSettings().getProperty("small", "11");
    mOldTitleFont = ProgramInfo.getInstance().getSettings().getProperty("titlefont", "Verdana");
    mOldBodyFont = ProgramInfo.getInstance().getSettings().getProperty("bodyfont", "Verdana");  
    
    mAntiAliasing = new JCheckBox(ProgramInfo.mLocalizer
        .msg("antialiasing", "Antialiasing"));
    mAntiAliasing.setSelected(mOldAntiAliasingSelected.compareToIgnoreCase("true") == 0);

    mUserFont = new JCheckBox(ProgramInfo.mLocalizer.msg("userfont", "Use user fonts"));
    mUserFont.setSelected(mOldUserFontSelected.compareToIgnoreCase("true") == 0);

    int size = Integer.parseInt(mOldTitleFontSize);

    mTitleFont = new FontChooserPanel(null,
        new Font(mOldTitleFont, Font.PLAIN, size), false);
    mTitleFont.setMaximumSize(mTitleFont.getPreferredSize());
    mTitleFont.setAlignmentX(FontChooserPanel.LEFT_ALIGNMENT);
    mTitleFont.setBorder(BorderFactory.createEmptyBorder(5, 20, 0, 0));

    size = Integer.parseInt(mOldBodyFontSize);
    
    mBodyFont = new FontChooserPanel(null, new Font(mOldBodyFont,
            Font.PLAIN, size), false);
    mBodyFont.setMaximumSize(mBodyFont.getPreferredSize());
    mBodyFont.setAlignmentX(FontChooserPanel.LEFT_ALIGNMENT);
    mBodyFont.setBorder(BorderFactory.createEmptyBorder(0, 20, 0, 0));

    mTitleFont.setEnabled(mUserFont.isSelected());
    mBodyFont.setEnabled(mUserFont.isSelected());
    
    mOldLook = ProgramInfo.getInstance().getProperty("look", "");
    
    String[] lf = {"Aqua", "Metal", "Motif", "Windows XP",
    "Windows Classic"};
    
    mLook = new JComboBox(lf);
    
    String look = mOldLook.length() > 0 ? mOldLook : LookAndFeelAddons.getBestMatchAddonClassName();
    
    for(int i = 0; i < mLf.length; i++)
      if(look.toLowerCase().indexOf(mLf[i].toLowerCase()) != -1) {
        mLook.setSelectedIndex(i);
        break;
      }
    
    mShowFunctions = new JCheckBox(ProgramInfo.mLocalizer.msg("showFunctions","Show Functions"),ProgramInfo.getInstance().isShowFunctions());
    mShowTextSearchButton  = new JCheckBox(ProgramInfo.mLocalizer.msg("showTextSearchButton","Show \"Search in program\""),ProgramInfo.getInstance().isShowTextSearchButton());
    
    mShowTextSearchButton.setEnabled(mShowFunctions.isSelected());
    
    mShowFunctions.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        mShowTextSearchButton.setEnabled(mShowFunctions.isSelected());
      }
    });
    
    CellConstraints cc = new CellConstraints();
    PanelBuilder formatPanelBuilder = new PanelBuilder(new FormLayout("5dlu,10dlu,pref,pref,default:grow,5dlu",
        "pref,5dlu,pref,pref,pref,pref,10dlu,pref,5dlu,pref" +
        ",10dlu,pref,5dlu,pref,pref"));
    formatPanelBuilder.setDefaultDialogBorder();
    
    formatPanelBuilder.addSeparator(ProgramInfo.mLocalizer.msg("font","Font settings"), cc.xyw(1,1,6));
    formatPanelBuilder.add(mAntiAliasing, cc.xyw(2,3,4));
    formatPanelBuilder.add(mUserFont, cc.xyw(2,4,4));
    final JLabel titleLabel = formatPanelBuilder.addLabel(ProgramInfo.mLocalizer.msg("title", "Title font"), cc.xy(3,5));
    formatPanelBuilder.add(mTitleFont, cc.xy(4,5));
    final JLabel bodyLabel = formatPanelBuilder.addLabel(ProgramInfo.mLocalizer.msg("body", "Description font"), cc.xy(3,6));
    formatPanelBuilder.add(mBodyFont, cc.xy(4,6));
    
    mUserFont.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        mTitleFont.setEnabled(mUserFont.isSelected());
        mBodyFont.setEnabled(mUserFont.isSelected());
        titleLabel.setEnabled(mUserFont.isSelected());
        bodyLabel.setEnabled(mUserFont.isSelected());
      }
    });
    
    mTitleFont.setEnabled(mUserFont.isSelected());
    mBodyFont.setEnabled(mUserFont.isSelected());
    titleLabel.setEnabled(mUserFont.isSelected());
    bodyLabel.setEnabled(mUserFont.isSelected());

    formatPanelBuilder.addSeparator(ProgramInfo.mLocalizer.msg("design","Design"), cc.xyw(1,8,6));
    formatPanelBuilder.add(mLook, cc.xyw(2,10,2));
    
    formatPanelBuilder.addSeparator(ProgramInfoDialog.mLocalizer.msg("functions","Functions"), cc.xyw(1,12,6));
    formatPanelBuilder.add(mShowFunctions, cc.xyw(2,14,5));
    formatPanelBuilder.add(mShowTextSearchButton, cc.xyw(3,15,4));
    
    mOldOrder = ProgramInfo.getInstance().getProperty("order", "");
    mOldSetupState = ProgramInfo.getInstance().getProperty("setupwasdone","false");        
    
    Object[] order;
    
    if (mOldOrder.indexOf(";") == -1) {
      if(mOldSetupState.compareTo("false") == 0) {
        order = ProgramTextCreator.getDefaultOrder();
      } else {
        order = new Object[0];
      }
      
      mList = new OrderChooser(order,ProgramTextCreator.getDefaultOrder(),true);
    }
    else {
      String[] id = mOldOrder.trim().split(";");
      order = new Object[id.length];
      for (int i = 0; i < order.length; i++) {
        int parsedId = Integer.parseInt(id[i]);
        
        if(parsedId == ProgramFieldType.UNKOWN_FORMAT) {
          order[i] = ProgramTextCreator.getDurationTypeString();
        }
        else if(parsedId >= 0) {
          order[i] = ProgramFieldType.getTypeForId(parsedId);
        }
        else {
          order[i] = CompoundedProgramFieldType.getCompoundedProgramFieldTypeForId(parsedId);
        }
      }
      mList = new OrderChooser(order,ProgramTextCreator.getDefaultOrder(),true);
    }
    
    JButton previewBtn = new JButton(ProgramInfo.mLocalizer.msg("preview", "Prewview"));
    previewBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        saveSettings();
        ProgramInfo.getInstance().showProgramInformation(
            Plugin.getPluginManager().getExampleProgram(), false);
        restoreSettings();
      }
    });

    JButton defaultBtn = new JButton(ProgramInfo.mLocalizer.msg("default", "Default"));
    defaultBtn.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        resetSettings();
      }
    });
        
    PanelBuilder builder = new PanelBuilder(new FormLayout("5dlu,default:grow,5dlu",
        "default,5dlu,default,5dlu,default,10dlu,default,5dlu,fill:default:grow"));
    builder.setDefaultDialogBorder();

    builder.addSeparator(Localizer.getLocalization(Localizer.I18N_PICTURES), cc.xyw(1,1,3));
    builder.add(mPictureSettings = new PluginsPictureSettingsPanel(ProgramInfo.getInstance().getPictureSettings(),false), cc.xy(2,3));
    
    PanelBuilder pb = new PanelBuilder(new FormLayout("default,2dlu,default,5dlu,default","default"));
    
    pb.add(mZoomEnabled = new JCheckBox(ProgramInfo.mLocalizer.msg("scaleImage","Scale picture:"), ProgramInfo.getInstance().getProperty("zoom","false").compareTo("true") == 0), cc.xy(1,1));
    pb.add(mZoomValue = new JSpinner(new SpinnerNumberModel(Integer.parseInt(ProgramInfo.getInstance().getProperty("zoomValue","100")),50,300,1)), cc.xy(3,1));
    final JLabel label = pb.addLabel("%",cc.xy(5,1));

    mZoomEnabled.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        mZoomValue.setEnabled(mZoomEnabled.isSelected());
        label.setEnabled(mZoomEnabled.isSelected());
      }
    });
    
    mZoomValue.setEnabled(mZoomEnabled.isSelected());
    label.setEnabled(mZoomEnabled.isSelected());
    
    builder.add(pb.getPanel(), cc.xy(2,5));
    
    builder.addSeparator(ProgramInfo.mLocalizer.msg("order","Info choosing/ordering"), cc.xyw(1,7,3));
    builder.add(mList, cc.xy(2,9));
    
    final JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.add(ProgramInfo.mLocalizer.msg("pictureOrder","Pictures/order"), builder.getPanel());
    tabbedPane.add(ProgramInfo.mLocalizer.msg("formating","Formating"), formatPanelBuilder.getPanel());
    tabbedPane.setSelectedIndex(mSelectedTab);
    
    tabbedPane.addChangeListener(new ChangeListener() {
      public void stateChanged(ChangeEvent e) {
        mSelectedTab = tabbedPane.getSelectedIndex();
      }
    });
    
    FormLayout layout = new FormLayout("default,default:grow,default","pref");
    layout.setColumnGroups(new int[][] {{1,3}});
    JPanel buttonPn = new JPanel(layout);
    buttonPn.add(previewBtn, cc.xy(3,1));
    buttonPn.add(defaultBtn, cc.xy(1,1));    
    
    JPanel base = new JPanel(new FormLayout("default:grow","fill:default:grow,10dlu,default"));
    base.setBorder(Borders.DIALOG_BORDER);
    base.add(tabbedPane, cc.xy(1,1));
    base.add(buttonPn, cc.xy(1,3));
    
    return base;
  }

  private void resetSettings() {
    mList.setOrder(ProgramTextCreator.getDefaultOrder(),ProgramTextCreator.getDefaultOrder());
    mAntiAliasing.setSelected(false);
    mUserFont.setSelected(false);
    
    mZoomEnabled.setSelected(false);
    mZoomValue.setValue(new Integer(100));
    
    String look = LookAndFeelAddons.getBestMatchAddonClassName();
    
    for(int i = 0; i < mLf.length; i++) {
      if(look.toLowerCase().indexOf(mLf[i].toLowerCase()) != -1) {
        mLook.setSelectedIndex(i);
        break;
      }
    }
  }

  public void saveSettings() {try {
    Object[] objects = mList.getOrder();

    String temp = "";

    for (Object object : objects) {
      if (object instanceof String) {
        temp += ProgramFieldType.UNKOWN_FORMAT + ";";
      } else if (object instanceof CompoundedProgramFieldType) {
        temp += ((CompoundedProgramFieldType) object).getId() + ";";
      }
      else {
        temp += ((ProgramFieldType) object).getTypeId() + ";";
      }
    }
    
    ProgramInfo.getInstance().getSettings().setProperty("zoom", String.valueOf(mZoomEnabled.isSelected()));
    ProgramInfo.getInstance().getSettings().setProperty("zoomValue", String.valueOf(mZoomValue.getValue()));
    
    ProgramInfo.getInstance().getSettings().setProperty("order", temp);
    ProgramInfo.getInstance().getSettings().setProperty("setupwasdone", "true");
    ProgramInfo.getInstance().getSettings().setProperty("pictureSettings", String.valueOf(mPictureSettings.getSettings().getType()));
    ProgramInfo.getInstance().setOrder();
    
    ProgramInfo.getInstance().getSettings().setProperty("antialiasing", String.valueOf(mAntiAliasing
        .isSelected()));
    ProgramInfo.getInstance().getSettings().setProperty("userfont", String.valueOf(mUserFont.isSelected()));

    Font f = mTitleFont.getChosenFont();
    ProgramInfo.getInstance().getSettings().setProperty("titlefont", f.getFamily());
    ProgramInfo.getInstance().getSettings().setProperty("title", String.valueOf(f.getSize()));

    f = mBodyFont.getChosenFont();
    ProgramInfo.getInstance().getSettings().setProperty("bodyfont", f.getFamily());
    ProgramInfo.getInstance().getSettings().setProperty("small", String.valueOf(f.getSize()));
    
    ProgramInfo.getInstance().getSettings().setProperty("look", mLf[mLook.getSelectedIndex()]);
    ProgramInfo.getInstance().setLook();
    
    if(mShowFunctions != null) {
      ProgramInfo.getInstance().setShowFunctions(mShowFunctions.isSelected());
    }
    if(mShowTextSearchButton != null) {
      ProgramInfo.getInstance().setShowTextSearchButton(mShowTextSearchButton.isSelected());
    }
  }catch(Exception e) {e.printStackTrace();}
  }

  private void restoreSettings() {
    ProgramInfo.getInstance().getSettings().setProperty("setupwasdone", mOldSetupState);
    ProgramInfo.getInstance().getSettings().setProperty("order", mOldOrder);
    ProgramInfo.getInstance().setOrder();
    
    ProgramInfo.getInstance().getSettings().setProperty("antialiasing", mOldAntiAliasingSelected);
    ProgramInfo.getInstance().getSettings().setProperty("userfont", mOldUserFontSelected);
    ProgramInfo.getInstance().getSettings().setProperty("titlefont", mOldTitleFont);
    ProgramInfo.getInstance().getSettings().setProperty("title", mOldTitleFontSize);
    ProgramInfo.getInstance().getSettings().setProperty("bodyfont", mOldBodyFont);
    ProgramInfo.getInstance().getSettings().setProperty("small", mOldBodyFontSize);
    
    ProgramInfo.getInstance().getSettings().setProperty("look", mOldLook);
    ProgramInfo.getInstance().setLook();
  }

  public Icon getIcon() {
    return IconLoader.getInstance().getIconFromTheme("actions", "edit-find", 16);
  }

  public String getTitle() {
    return ProgramInfo.getInstance().toString();
  }

}
