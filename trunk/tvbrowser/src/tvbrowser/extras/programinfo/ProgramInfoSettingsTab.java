package tvbrowser.extras.programinfo;

import java.awt.Component;
import java.awt.FlowLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;
import java.util.ArrayList;
import java.util.Enumeration;

import javax.swing.AbstractButton;
import javax.swing.BorderFactory;
import javax.swing.BoxLayout;
import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.SpinnerNumberModel;
import javax.swing.UIManager;
import javax.swing.event.ChangeEvent;
import javax.swing.event.ChangeListener;

import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.core.plugin.PluginManagerImpl;
import tvbrowser.ui.settings.util.ColorButton;
import tvbrowser.ui.settings.util.ColorLabel;
import util.program.ProgramTextCreator;
import util.ui.EnhancedPanelBuilder;
import util.ui.FontChooserPanel;
import util.ui.Localizer;
import util.ui.OrderChooser;
import util.ui.PluginsPictureSettingsPanel;
import util.ui.ScrollableJPanel;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.l2fprod.common.swing.plaf.LookAndFeelAddons;

import devplugin.Plugin;
import devplugin.PluginAccess;
import devplugin.ProgramReceiveTarget;
import devplugin.SettingsTab;

/**
 * The order settings for the ProgramInfo.
 *
 * @author René Mach
 *
 */
public class ProgramInfoSettingsTab implements SettingsTab {

  private OrderChooser mList;
  private Object[] mOldOrder;
  private boolean mOldSetupState;
  private PluginsPictureSettingsPanel mPictureSettings;

  private JCheckBox mZoomEnabled;
  private JSpinner mZoomValue;

  private JCheckBox mUserFont, mAntiAliasing;
  private FontChooserPanel mTitleFont, mBodyFont;

  private String mOldTitleFont, mOldBodyFont;
  private int mOldTitleFontSize, mOldBodyFontSize;
  private boolean mOldUserFontSelected, mOldAntiAliasingSelected;

  private boolean mOldShowFunctions;

  private String mOldLook;

  private JComboBox mLook;

  private String[] mLf = {
      "com.l2fprod.common.swing.plaf.aqua.AquaLookAndFeelAddons",
      "com.l2fprod.common.swing.plaf.metal.MetalLookAndFeelAddons",
      "com.l2fprod.common.swing.plaf.motif.MotifLookAndFeelAddons",
      "com.l2fprod.common.swing.plaf.windows.WindowsLookAndFeelAddons",
      "com.l2fprod.common.swing.plaf.windows.WindowsClassicLookAndFeelAddons"
  };

  private JCheckBox mShowFunctions, mShowTextSearchButton;

  private ButtonGroup mAvailableTargetGroup;
  private JCheckBox mPersonSearchCB;
  private JCheckBox mHighlight;
  private ColorLabel mHighlightColorLb;
  private ColorButton mHighlightButton;
  private int mOldTitleStyle;
  private int mOldBodyStyle;

  public JPanel createSettingsPanel() {
    final ProgramInfoSettings settings = ProgramInfo.getInstance()
        .getSettings();
    mOldAntiAliasingSelected = settings.getAntialiasing();
    mOldUserFontSelected = settings.getUserFont();
    mOldTitleFontSize = settings.getTitleFontSize();
    mOldBodyFontSize = settings.getBodyFontSize();
    mOldTitleFont = settings.getTitleFontName();
    mOldBodyFont = settings.getBodyFontName();
    mOldTitleStyle = settings.getTitleFontStyle();
    mOldBodyStyle = settings.getBodyFontStyle();

    mAntiAliasing = new JCheckBox(ProgramInfo.mLocalizer
        .msg("antialiasing", "Antialiasing"));
    mAntiAliasing.setSelected(mOldAntiAliasingSelected);

    mUserFont = new JCheckBox(ProgramInfo.mLocalizer.msg("userfont", "Use user fonts"));
    mUserFont.setSelected(mOldUserFontSelected);

    mTitleFont = new FontChooserPanel(null, new Font(mOldTitleFont, mOldTitleStyle, mOldTitleFontSize), true);
    mTitleFont.setBorder(BorderFactory.createEmptyBorder());
//    mTitleFont.setMaximumSize(mTitleFont.getPreferredSize());
    mTitleFont.setAlignmentX(Component.LEFT_ALIGNMENT);

    mBodyFont = new FontChooserPanel(null, new Font(mOldBodyFont, mOldBodyStyle, mOldBodyFontSize), true);
    mBodyFont.setBorder(BorderFactory.createEmptyBorder());
//    mBodyFont.setMaximumSize(mBodyFont.getPreferredSize());
    mBodyFont.setAlignmentX(Component.LEFT_ALIGNMENT);

    mTitleFont.setEnabled(mUserFont.isSelected());
    mBodyFont.setEnabled(mUserFont.isSelected());

    mOldLook = settings.getLook();

    String[] lf = { "Aqua", "Metal", "Motif", "Windows XP", "Windows Classic" };

    mLook = new JComboBox(lf);

    String look = mOldLook.length() > 0 ? mOldLook : LookAndFeelAddons.getBestMatchAddonClassName();

    for(int i = 0; i < mLf.length; i++) {
      if(look.toLowerCase().indexOf(mLf[i].toLowerCase()) != -1) {
        mLook.setSelectedIndex(i);
        break;
      }
    }

    mOldShowFunctions = settings.getShowFunctions();

    mShowFunctions = new JCheckBox(ProgramInfo.mLocalizer.msg("showFunctions",
        "Show Functions"), settings.getShowFunctions());
    mShowTextSearchButton = new JCheckBox(ProgramInfo.mLocalizer.msg(
        "showTextSearchButton", "Show \"Search in program\""), ProgramInfo
        .getInstance().getSettings().getShowSearchButton());

    mShowTextSearchButton.setEnabled(mShowFunctions.isSelected());

    mShowFunctions.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        mShowTextSearchButton.setEnabled(mShowFunctions.isSelected());
      }
    });

    mHighlight = new JCheckBox(ProgramInfoDialog.mLocalizer.msg("highlight", "Highlight favorite matches"), settings.getHighlightFavorite());
    mHighlight.addActionListener(new ActionListener() {

      @Override
      public void actionPerformed(ActionEvent e) {
        mHighlightColorLb.setEnabled(mHighlight.isSelected());
        mHighlightButton.setEnabled(mHighlight.isSelected());
      }
    });

    CellConstraints cc = new CellConstraints();
    EnhancedPanelBuilder formatPanel = new EnhancedPanelBuilder(new FormLayout("5dlu,10dlu,pref,pref,5dlu,default:grow,pref,5dlu"));
    formatPanel.setDefaultDialogBorder();
    formatPanel.addParagraph(ProgramInfo.mLocalizer.msg("font","Font settings"));
    formatPanel.addRow();
    formatPanel.add(mAntiAliasing, cc.xyw(2,formatPanel.getRowCount(), formatPanel.getColumnCount() - 2));
    formatPanel.addRow();
    formatPanel.add(mUserFont, cc.xyw(2,formatPanel.getRowCount(),formatPanel.getColumnCount() - 2));
    formatPanel.addRow();
    final JLabel titleLabel = new JLabel(ProgramInfo.mLocalizer.msg("title", "Title font"));
    formatPanel.add(titleLabel, cc.xy(3, formatPanel.getRowCount()));
    formatPanel.add(mTitleFont, cc.xyw(6,formatPanel.getRowCount(),2));
    formatPanel.addRow();
    final JLabel bodyLabel = new JLabel(ProgramInfo.mLocalizer.msg("body", "Description font"));
    formatPanel.add(bodyLabel, cc.xy(3,formatPanel.getRowCount()));
    formatPanel.add(mBodyFont, cc.xyw(6,formatPanel.getRowCount(),2));

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

    formatPanel.addParagraph(ProgramInfo.mLocalizer.msg("design","Design"));
    formatPanel.addRow();
    formatPanel.add(mLook, cc.xyw(2,formatPanel.getRowCount(),2));

    formatPanel.addParagraph(ProgramInfoDialog.mLocalizer.msg("functions","Functions"));
    formatPanel.addRow();
    formatPanel.add(mShowFunctions, cc.xyw(2,formatPanel.getRowCount(),formatPanel.getColumnCount() - 2));
    formatPanel.addRow();
    formatPanel.add(mShowTextSearchButton, cc.xyw(3,formatPanel.getRowCount(),formatPanel.getColumnCount() - 3));

    formatPanel.addParagraph(ProgramInfo.mLocalizer.msg("favorites","Favorites"));
    formatPanel.addRow();
    formatPanel.add(mHighlight, cc.xyw(2,formatPanel.getRowCount(),5));
    JPanel panel = new JPanel(new FlowLayout());
    mHighlightColorLb = new ColorLabel(settings.getHighlightColor());
    panel.add(mHighlightColorLb);
    mHighlightColorLb.setStandardColor(settings.getHighlightColor());
    mHighlightButton = new ColorButton(mHighlightColorLb);
    panel.add(mHighlightButton);
    mHighlight.getActionListeners()[0].actionPerformed(null);
    formatPanel.add(panel, cc.xy(7,formatPanel.getRowCount()));

    mOldOrder = settings.getFieldOrder();
    mOldSetupState = ProgramInfo.getInstance().getSettings().getSetupwasdone();

    mList = new OrderChooser(mOldOrder, ProgramTextCreator.getDefaultOrder(),
        true);

    JButton previewBtn = new JButton(ProgramInfo.mLocalizer.msg("preview", "Preview"));
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

    EnhancedPanelBuilder orderPanel = new EnhancedPanelBuilder("default:grow");
    orderPanel.setDefaultDialogBorder();

    orderPanel.addRow("fill:default:grow");
    orderPanel.add(mList, cc.xy(1, orderPanel.getRowCount()));


    EnhancedPanelBuilder picturePanel = new EnhancedPanelBuilder("default:grow");
    picturePanel.setDefaultDialogBorder();

    picturePanel.addRow("default");
    picturePanel.add(mPictureSettings = new PluginsPictureSettingsPanel(ProgramInfo.getInstance().getPictureSettings(),false), cc.xy(1, picturePanel.getRowCount()));

    PanelBuilder pb = new PanelBuilder(new FormLayout("default,2dlu,default,5dlu,default","default"));

    pb.add(mZoomEnabled = new JCheckBox(ProgramInfo.mLocalizer.msg(
        "scaleImage", "Scale picture:"), ProgramInfo.getInstance()
        .getSettings().getZoomEnabled()), cc.xy(1, 1));
    pb.add(mZoomValue = new JSpinner(new SpinnerNumberModel(ProgramInfo
        .getInstance().getSettings().getZoomValue(), 50, 300, 1)), cc.xy(3, 1));
    final JLabel label = pb.addLabel("%",cc.xy(5,1));

    mZoomEnabled.addItemListener(new ItemListener() {
      public void itemStateChanged(ItemEvent e) {
        mZoomValue.setEnabled(mZoomEnabled.isSelected());
        label.setEnabled(mZoomEnabled.isSelected());
      }
    });

    mZoomValue.setEnabled(mZoomEnabled.isSelected());
    label.setEnabled(mZoomEnabled.isSelected());

    picturePanel.addRow("5dlu");
    picturePanel.addRow("default");
    picturePanel.add(pb.getPanel(), cc.xy(1, picturePanel.getRowCount()));



    PluginAccess webPlugin = PluginManagerImpl.getInstance().getActivatedPluginForId("java.webplugin.WebPlugin");

    mAvailableTargetGroup = new ButtonGroup();

    final ArrayList<InternalRadioButton<?>> availableDefaultTargets = new ArrayList<InternalRadioButton<?>>();

    availableDefaultTargets.add(new InternalRadioButton<String>(ProgramInfoDialog.mLocalizer.msg("searchTvBrowser","Search in TV-Browser")));
    mAvailableTargetGroup.add(availableDefaultTargets.get(0));
    availableDefaultTargets.add(new InternalRadioButton<String>(ProgramInfoDialog.mLocalizer.msg("searchWikipedia","Search in Wikipedia")));
    mAvailableTargetGroup.add(availableDefaultTargets.get(1));

    final String currentValue = settings.getActorSearch();

    int selectedIndex = -1;

    if(webPlugin != null && webPlugin.canReceiveProgramsWithTarget()) {
      ProgramReceiveTarget[] targets = webPlugin.getProgramReceiveTargets();

      if(targets != null) {
        for(ProgramReceiveTarget target : targets) {
          availableDefaultTargets.add(new InternalRadioButton<ProgramReceiveTarget>(target));
          mAvailableTargetGroup.add(availableDefaultTargets.get(availableDefaultTargets.size()-1));

          if(currentValue.equals(target.getReceiveIfId() + "#_#_#" + target.getTargetId())) {
            selectedIndex = availableDefaultTargets.size()-1;
          }
        }
      }
    }

    if(selectedIndex == -1) {
      if(currentValue.equals("internalSearch")) {
        selectedIndex = 0;
      }
      else {
        selectedIndex = 1;
      }
    }

    availableDefaultTargets.get(selectedIndex).setSelected(true);

    ScrollableJPanel buttonPanel = new ScrollableJPanel();
    buttonPanel.setLayout(new BoxLayout(buttonPanel,BoxLayout.Y_AXIS));
    buttonPanel.setOpaque(false);

    for(InternalRadioButton<?> button : availableDefaultTargets) {
      buttonPanel.add(button);
    }

    final JScrollPane scrollPane = new JScrollPane(buttonPanel);
    scrollPane.setBackground(UIManager.getDefaults().getColor("List.background"));
    scrollPane.getViewport().setBackground(UIManager.getDefaults().getColor("List.background"));
    PanelBuilder actorPanel = new PanelBuilder(
        new FormLayout(
        "default:grow", "pref,3dlu,default,1dlu,fill:default:grow"));
    actorPanel.setDefaultDialogBorder();

    mPersonSearchCB = new JCheckBox(ProgramInfo.mLocalizer.msg("enableSearch",
        "Show person names as links to person search"));
    actorPanel.add(mPersonSearchCB, cc.xy(1, 1));
    final JLabel searchLabel = new JLabel(ProgramInfo.mLocalizer.msg(
        "defaultActorSearchMethod", "Default search method:"));
    actorPanel.add(searchLabel, cc.xy(1, 3));
    actorPanel.add(scrollPane, cc.xy(1, 5));

    mPersonSearchCB.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        scrollPane.setEnabled(mPersonSearchCB.isSelected());
        searchLabel.setEnabled(mPersonSearchCB.isSelected());
        for (InternalRadioButton<?> button : availableDefaultTargets) {
          button.setEnabled(mPersonSearchCB.isSelected());
        }
      }
    });
    mPersonSearchCB.setSelected(settings.getEnableSearch());
    mPersonSearchCB.getActionListeners()[0].actionPerformed(null);

    final JTabbedPane tabbedPane = new JTabbedPane();
    tabbedPane.add(ProgramInfo.mLocalizer.msg("look","Look"), formatPanel.getPanel());
    tabbedPane.add(ProgramInfo.mLocalizer.msg("fields","Fields"), orderPanel.getPanel());
    tabbedPane.add(Localizer.getLocalization(Localizer.I18N_PICTURES), picturePanel.getPanel());
    tabbedPane.add(ProgramInfo.mLocalizer.msg("actorSearch","Actor search"), actorPanel.getPanel());
    tabbedPane.setSelectedIndex(0);

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
    mZoomValue.setValue(100);

    String look = LookAndFeelAddons.getBestMatchAddonClassName();

    for(int i = 0; i < mLf.length; i++) {
      if(look.toLowerCase().indexOf(mLf[i].toLowerCase()) != -1) {
        mLook.setSelectedIndex(i);
        break;
      }
    }
  }

  public void saveSettings() {
    try {
      final ProgramInfoSettings settings = ProgramInfo.getInstance().getSettings();
      settings.setZoomEnabled(mZoomEnabled.isSelected());
      settings.setZoomValue((Integer) mZoomValue.getValue());

      settings.setFieldOrder(mList.getOrder());
      settings.setSetupwasdone(true);
      settings.setPictureSettings(mPictureSettings.getSettings().getType());

      ProgramInfo.getInstance().setOrder();

      settings.setAntialiasing(mAntiAliasing.isSelected());
      settings.setUserFont(mUserFont.isSelected());

      Font f = mTitleFont.getChosenFont();
      settings.setTitleFontName(f.getFamily());
      settings.setTitleFontSize(f.getSize());
      settings.setTitleFontStyle(f.getStyle());

      f = mBodyFont.getChosenFont();
      settings.setBodyFontName(f.getFamily());
      settings.setBodyFontSize(f.getSize());
      settings.setBodyFontStyle(f.getStyle());

      settings.setLook(mLf[mLook.getSelectedIndex()]);
      ProgramInfo.getInstance().setLook();

      if (mShowFunctions != null) {
        settings.setShowFunctions(mShowFunctions.isSelected());
        if (mShowFunctions.isSelected() != mOldShowFunctions) {
          ProgramInfoDialog.recreateInstance();
        }
      }
      if (mShowTextSearchButton != null) {
        settings.setShowSearchButton(mShowTextSearchButton.isSelected());
      }
      settings.setHighlightFavorite(mHighlight.isSelected());
      settings.setHighlightColor(mHighlightColorLb.getColor());

      Enumeration<AbstractButton> actorSearchDefault = mAvailableTargetGroup.getElements();

      while (actorSearchDefault.hasMoreElements()) {
        AbstractButton button = actorSearchDefault.nextElement();

        if (button.isSelected()) {
          settings.setActorSearch(((InternalRadioButton<?>) button).getValue());
          break;
        }
      }
      settings.setEnableSearch(mPersonSearchCB.isSelected());
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  private void restoreSettings() {
    final ProgramInfoSettings settings = ProgramInfo.getInstance()
        .getSettings();
    settings.setSetupwasdone(mOldSetupState);
    settings.setFieldOrder(mOldOrder);
    ProgramInfo.getInstance().setOrder();

    settings.setAntialiasing(mOldAntiAliasingSelected);
    settings.setUserFont(mOldUserFontSelected);
    settings.setTitleFontName(mOldTitleFont);
    settings.setTitleFontSize(mOldTitleFontSize);
    settings.setBodyFontName(mOldBodyFont);
    settings.setBodyFontSize(mOldBodyFontSize);
    settings.setTitleFontStyle(mOldTitleStyle);
    settings.setBodyFontStyle(mOldBodyStyle);

    settings.setLook(mOldLook);
    ProgramInfo.getInstance().setLook();

    settings.setShowFunctions(mOldShowFunctions);

    ProgramInfoDialog.recreateInstance();
  }

  public Icon getIcon() {
    return IconLoader.getInstance().getIconFromTheme("actions", "edit-find", 16);
  }

  public String getTitle() {
    return ProgramInfo.getInstance().toString();
  }

  private static class InternalRadioButton<T> extends JRadioButton {
    private T mValue;

    protected InternalRadioButton(T value) {
      super(value.toString());

      mValue = value;
      setOpaque(false);
    }

    protected String getValue() {
      if(mValue instanceof String) {
        if(mValue.equals(ProgramInfoDialog.mLocalizer.msg("searchTvBrowser","Search in TV-Browser"))) {
          return "internalSearch";
        }
        else {
          return "internalWikipedia";
        }
      }
      else if (mValue instanceof ProgramReceiveTarget) {
        ProgramReceiveTarget target = (ProgramReceiveTarget)mValue;
        return target.getReceiveIfId() + "#_#_#" + target.getTargetId();
      }

      return "internalWikipedia";
    }
  }
}
