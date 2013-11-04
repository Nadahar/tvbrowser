/*
 * TV-Pearl by Reinhard Lehrbaum
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
 */
package tvpearlplugin;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Window;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.Vector;

import javax.swing.ButtonGroup;
import javax.swing.Icon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JPasswordField;
import javax.swing.JRadioButton;
import javax.swing.JScrollPane;
import javax.swing.JSpinner;
import javax.swing.JTabbedPane;
import javax.swing.JTextArea;
import javax.swing.JTextField;
import javax.swing.SpinnerNumberModel;

import tvbrowser.ui.mainframe.MainFrame;
import util.ui.MarkPriorityComboBoxRenderer;
import util.ui.PluginChooserDlg;
import util.ui.PluginProgramConfigurationPanel;
import util.ui.ScrollableJPanel;
import util.ui.UiUtilities;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.FormSpecs;

import devplugin.ProgramReceiveIf;
import devplugin.ProgramReceiveTarget;
import devplugin.SettingsTab;

public final class TVPearlPluginSettingsTab implements SettingsTab
{
  static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(TVPearlPluginSettingsTab.class);

	private JCheckBox mUpdateAtStart;
	private JCheckBox mUpdateAfterUpdateFinished;
	private JCheckBox mUpdateManual;
	private JComboBox mViewOption;
	private JCheckBox mMarkPearl;
	private JComboBox mMarkPriority;
	private JCheckBox mShowInfoModal;
	private JCheckBox mEnableFilter;
	private JRadioButton mFilterShowOnly;
	private JRadioButton mFilterShowNot;
	private JTextArea mFilterComposer;
	private JLabel mPluginLabel;
	
	private JTextField mUserName;
	private JPasswordField mUserPassword;

	private ProgramReceiveTarget[] mClientPluginTargets;

  private TVPearlSettings mSettings;
  
  private JTabbedPane mTabbedPane;
  
  private static int mSelectedPane;
  
  private PluginProgramConfigurationPanel mFormatingPanel;
  
  private JSpinner mCommentCount;
  
  public TVPearlPluginSettingsTab(final TVPearlSettings settings) {
    mSettings = settings;
    mSelectedPane = 0;
  }

	public JPanel createSettingsPanel()
	{
	  mTabbedPane = new JTabbedPane();
	  
	  final FormLayout layout = new FormLayout(
        "5dlu, pref, 3dlu, fill:pref:grow, 5dlu",
        "5dlu");

	  final PanelBuilder builder = new PanelBuilder(layout,
        new ScrollableJPanel());
		// builder.setDefaultDialogBorder();
		//builder.setBorder(null);

		final CellConstraints cc = new CellConstraints();

		mUpdateAtStart = new JCheckBox(mLocalizer.msg("updateAtStart",
        "Update after TV-Browser start"), mSettings.getUpdatePearlsAfterStart());
    mUpdateAfterUpdateFinished = new JCheckBox(mLocalizer.msg(
        "updateAfterUpdateFinished", "Update after program data download"),
        mSettings.getUpdatePearlsAfterDataUpdate());
    mUpdateManual = new JCheckBox(mLocalizer.msg("updateManual",
        "Update manual"), mSettings.getUpdatePearlsManually());
    mViewOption = new JComboBox(getViewOption());
    if (mSettings.getShowAllPearls()) {
      mViewOption.setSelectedIndex(0);
    } else if (mSettings.getShowSubscribedChannels()) {
      mViewOption.setSelectedIndex(1);
    } else if (mSettings.getShowFoundPearls()) {
      mViewOption.setSelectedIndex(2);
    }
    if (mViewOption.getSelectedIndex() < 0) {
      mViewOption.setSelectedIndex(0);
    }
    mMarkPearl = new JCheckBox(mLocalizer.msg("markPearl",
        "Mark pearls within the TV-Browser"), mSettings.getMarkPearls());
    mMarkPriority = new JComboBox(getPriorities());
    mMarkPriority.setRenderer(new MarkPriorityComboBoxRenderer());
    mMarkPriority.setSelectedIndex(mSettings.getMarkPriority() + 1);
    mShowInfoModal = new JCheckBox(
        mLocalizer.msg("modal", "modal Info dialog"), mSettings
            .getShowInfoModal());

		mClientPluginTargets = TVPearlPlugin.getInstance().getClientPluginsTargets();
		mPluginLabel = new JLabel();
		handlePluginSelection();

		final JButton choose = new JButton(mLocalizer.msg("selectPlugins",
        "Choose Plugins"));
		choose.addActionListener(new ActionListener()
		{
			public void actionPerformed(final ActionEvent e)
			{
				try
				{
				  final Window w = UiUtilities.getLastModalChildOf(MainFrame
              .getInstance());
				  
          PluginChooserDlg chooser = new PluginChooserDlg(w, mClientPluginTargets, null, TVPearlPlugin.getInstance(), null);
          
					chooser.setVisible(true);

					if (chooser.getReceiveTargets() != null)
					{
						mClientPluginTargets = chooser.getReceiveTargets();
					}

					handlePluginSelection();
				}
				catch (Exception ee)
				{
					ee.printStackTrace();
				}
			}
		});

		final JPanel pluginPanel = new JPanel(new FormLayout(
        "fill:pref:grow, 3dlu, pref", "default"));
		pluginPanel.add(mPluginLabel, cc.xy(1, 1));
		pluginPanel.add(choose, cc.xy(3, 1));

		mEnableFilter = new JCheckBox(mLocalizer.msg("enableFilter",
        "enable filter"), mSettings.getFilterEnabled());
    mFilterShowOnly = new JRadioButton(mLocalizer.msg("filterShowOnly",
        "show only"), mSettings.getFilterIncluding());
    mFilterShowNot = new JRadioButton(mLocalizer.msg("filterShowNot",
        "show not"), mSettings.getFilterExcluding());
    final ButtonGroup group = new ButtonGroup();
		group.add(mFilterShowOnly);
		group.add(mFilterShowNot);
		mFilterComposer = new JTextArea(3, 10);
		mFilterComposer.setText(getComposers());
		final JScrollPane scrollComposer = new JScrollPane(mFilterComposer);
		
		mEnableFilter.addActionListener(new ActionListener() {

      public void actionPerformed(final ActionEvent e) {
        updateFilterEnabled();
      }
    });
		updateFilterEnabled();

		builder.appendRow(FormSpecs.PREF_ROWSPEC);
		builder.setRow(2);
		builder.addLabel(mLocalizer.msg("view", "View"), cc.xy(2, builder.getRow()));
		builder.add(mViewOption, cc.xy(4, builder.getRow()));
		
		newLine(builder);
		builder.add(mShowInfoModal, cc.xyw(2, builder.getRow(), 3));

    addSeparator(builder,mLocalizer.msg("programTable", "Program table"));
		
    newLine(builder);
		builder.add(mMarkPearl, cc.xyw(2, builder.getRow(), 3));

    newLine(builder);
		builder.addLabel(mLocalizer.msg("markPriority", "Mark priority"), cc.xy(2, builder.getRow()));
		builder.add(mMarkPriority, cc.xy(4, builder.getRow()));

    addSeparator(builder,mLocalizer.msg("sendToPlugin", "Send reminded program to"));

    newLine(builder);
		builder.add(pluginPanel, cc.xyw(2, builder.getRow(), 3));

    addSeparator(builder,mLocalizer.msg("updateOption", "Update options"));

    newLine(builder);
		builder.add(mUpdateAtStart, cc.xyw(2, builder.getRow(), 3));

    newLine(builder);
		builder.add(mUpdateAfterUpdateFinished, cc.xyw(2, builder.getRow(), 3));

    newLine(builder);
		builder.add(mUpdateManual, cc.xyw(2, builder.getRow(), 3));

    addSeparator(builder,mLocalizer.msg("filter", "Filter"));

    newLine(builder);
		builder.add(mEnableFilter, cc.xyw(2, builder.getRow(), 3));

    newLine(builder);
		builder.add(mFilterShowOnly, cc.xyw(2, builder.getRow(), 3));
		
    newLine(builder);
		builder.add(mFilterShowNot, cc.xyw(2, builder.getRow(), 3));
    
    newLine(builder);
    builder.add(scrollComposer, cc.xyw(2, builder.getRow(), 3));
    
	builder.nextRow(2);

	final JScrollPane scrollPane = new JScrollPane(builder.getPanel());
	scrollPane.setBorder(Borders.DIALOG);
	scrollPane.setViewportBorder(null);
		
	mTabbedPane.addTab(mLocalizer.msg("tabPearlDisplay", "TV Pearl Display"), scrollPane);
		
	JPanel tabPanel = new JPanel(new BorderLayout());
	tabPanel.setBorder(Borders.createEmptyBorder("5dlu,5dlu,0dlu,5dlu"));
		
	tabPanel.add(mTabbedPane, BorderLayout.CENTER);
		
	PanelBuilder creationPanel = new PanelBuilder(new FormLayout("5dlu,default,3dlu,min:grow",
		    "5dlu,default,3dlu,default,default,10dlu,default,5dlu,fill:150dlu:grow,10dlu,default,5dlu,default"));
		
	mUserName = new JTextField(mSettings.getForumUserName());
	mUserPassword = new JPasswordField(mSettings.getForumPassword());
		
	mFormatingPanel = new PluginProgramConfigurationPanel(TVPearlPlugin.getInstance().getSelectedPluginProgramFormatings(), TVPearlPlugin.getInstance().getAvailableLocalPluginProgramFormatings(), null,false,false);
	
	mCommentCount = new JSpinner(new SpinnerNumberModel(mSettings.getCommentCount(),5,50,1));
	
	int y = 2;
		
	creationPanel.addLabel(mLocalizer.msg("forumUser","Message board user name:"), CC.xy(2, y));
	creationPanel.add(mUserName, CC.xy(4, y++));
    creationPanel.addLabel(mLocalizer.msg("forumPassword","Message board user password:"), CC.xy(2, ++y));
    creationPanel.add(mUserPassword, CC.xy(4, y++));
    creationPanel.add(UiUtilities.createHtmlHelpTextArea(mLocalizer.msg("forumPasswortWarning", "Not suggested to store it, because it is saved in plain text")), CC.xy(4, y++));
    creationPanel.addSeparator(mLocalizer.msg("formatings","Program formatings"), CC.xyw(1,++y,4));
    
    y++;
    
    creationPanel.add(mFormatingPanel, cc.xyw(2,++y,3));
    creationPanel.getPanel().setPreferredSize(new Dimension(200, 200));
    
    y += 2;
    
    JPanel countPanel = new JPanel(new FormLayout("default","default"));
    countPanel.add(mCommentCount, cc.xy(1, 1));
    
    creationPanel.addSeparator(mLocalizer.msg("commentSeparator","Comments"), cc.xyw(1, y++, 4));
    creationPanel.addLabel(mLocalizer.msg("saveComments", "Save comments:"), cc.xy(2, ++y));
    creationPanel.add(countPanel, cc.xy(4, y));
    
    final JScrollPane scrollPane2 = new JScrollPane(creationPanel.getPanel());
    scrollPane2.setBorder(Borders.DIALOG);
    scrollPane2.setViewportBorder(null);
    scrollPane2.setPreferredSize(new Dimension(200, 200));
    
	mTabbedPane.addTab(mLocalizer.msg("tabPearlCreation", "TV Pearl Creation"), scrollPane2);
	mTabbedPane.setSelectedIndex(mSelectedPane);
	
	return tabPanel;
  }

  private void addSeparator(final PanelBuilder builder, final String label) {
    builder.appendRow(FormSpecs.PARAGRAPH_GAP_ROWSPEC);
    builder.appendRow(FormSpecs.PREF_ROWSPEC);
		builder.nextRow(2);
    builder.addSeparator(label);
  }

  private PanelBuilder newLine(final PanelBuilder builder) {
    builder.appendRow(FormSpecs.LINE_GAP_ROWSPEC);
    builder.appendRow(FormSpecs.PREF_ROWSPEC);
	builder.nextRow(2);
	
	return builder;
  }

	public Icon getIcon()
	{
		return null;
	}

	public String getTitle()
	{
		return null;
	}
	
	

	public void saveSettings()
	{
	  mSelectedPane = mTabbedPane.getSelectedIndex();
		mSettings.setUpdatePearlsAfterStart(mUpdateAtStart.isSelected());
    mSettings.setUpdatePearlsAfterDataUpdate(mUpdateAfterUpdateFinished
        .isSelected());
    mSettings.setUpdatePearlsManually(mUpdateManual.isSelected());
    final int index = mViewOption.getSelectedIndex();
    if (index == 0) {
      mSettings.setShowAllPearls();
    } else if (index == 1) {
      mSettings.setShowSubscribedChannels();
    } else if (index == 2) {
      mSettings.setShowFoundPearls();
    }
    mSettings.setMarkPearls(mMarkPearl.isSelected());
    mSettings.setMarkPriority(mMarkPriority.getSelectedIndex() - 1);
    mSettings.setShowInfoModal(mShowInfoModal.isSelected());
    mSettings.setFilterEnabled(mEnableFilter.isSelected());
    if (mFilterShowOnly.isSelected()) {
      mSettings.setFilterIncluding();
    }
    if (mFilterShowNot.isSelected()) {
      mSettings.setFilterExcluding();
    }
		
    mSettings.setForumUserName(mUserName.getText());
    mSettings.setForumUserPassword(new String(mUserPassword.getPassword()));
    
		TVPearlPlugin.getInstance().setClientPluginsTargets(mClientPluginTargets);
		
		final Vector<String> composers = new Vector<String>();
		for (String item : mFilterComposer.getText().split("\n"))
		{
			item = item.trim();
			if (item.length() > 0)
			{
				composers.add(item);
			}
		}
		TVPearlPlugin.getInstance().setComposers(composers);

		TVPearlPlugin.getInstance().setAvailableLocalPluginProgramFormatings(mFormatingPanel.getAvailableLocalPluginProgramFormatings());
		TVPearlPlugin.getInstance().setSelectedPluginProgramFormatings(mFormatingPanel.getSelectedPluginProgramFormatings());
		
		TVPearlPlugin.getInstance().updateChanges();
		
		mSettings.setCommentCount((Integer)mCommentCount.getValue());
	}

	private Vector<String> getViewOption()
	{
	  final Vector<String> result = new Vector<String>();

		result.add(mLocalizer.msg("viewAll", "View all pearls"));
		result.add(mLocalizer.msg("viewChannel",
        "View pearls from subscribed channels"));
		result.add(mLocalizer.msg("viewProgram", "View only pearls that where found within the local program data"));

		return result;
	}

	private Vector<String> getPriorities()
	{
	  final Vector<String> result = new Vector<String>();

		result.add(mLocalizer.msg("noPriority", "None"));
		result.add(mLocalizer.msg("min", "Minimum"));
		result.add(mLocalizer.msg("lowerMedium", "Lower Medium"));
		result.add(mLocalizer.msg("medium", "Medium"));
		result.add(mLocalizer.msg("higherMedium", "Higher Medium"));
		result.add(mLocalizer.msg("max", "Maximum"));

		return result;
	}

	private String getComposers()
	{
		StringBuilder result = new StringBuilder(100);
		for (String composer : TVPearlPlugin.getInstance().getComposers())
		{
		  result.append(composer).append('\n');
		}
		return result.toString().trim();
	}

	private void handlePluginSelection()
	{
	  final ArrayList<ProgramReceiveIf> plugins = new ArrayList<ProgramReceiveIf>();

		if (mClientPluginTargets != null)
		{
			for (ProgramReceiveTarget target : mClientPluginTargets)
			{
				if (!plugins.contains(target.getReceifeIfForIdOfTarget()))
				{
					plugins.add(target.getReceifeIfForIdOfTarget());
				}
			}

			final ProgramReceiveIf[] mClientPlugins = plugins
          .toArray(new ProgramReceiveIf[plugins.size()]);

			if (mClientPlugins.length > 0)
			{
				mPluginLabel.setText(mClientPlugins[0].toString());
				mPluginLabel.setEnabled(true);
			}
			else
			{
				mPluginLabel.setText(mLocalizer.msg("noPlugins", "No Plugins choosen"));
				mPluginLabel.setEnabled(false);
			}

			for (int i = 1; i < (mClientPlugins.length > 4 ? 3 : mClientPlugins.length); i++)
			{
				mPluginLabel.setText(mPluginLabel.getText() + ", " + mClientPlugins[i]);
			}

			if (mClientPlugins.length > 4)
			{
				mPluginLabel.setText(mPluginLabel.getText() + " (" + (mClientPlugins.length - 3) + " " + mLocalizer.msg("otherPlugins", "others...") + ")");
			}
		}
	}

  /**
   * enable or disable the author filter controls, depending on the checkbox
   * value
   */
  private void updateFilterEnabled() {
    mFilterShowOnly.setEnabled(mEnableFilter.isSelected());
    mFilterShowNot.setEnabled(mEnableFilter.isSelected());
    mFilterComposer.setEnabled(mEnableFilter.isSelected());
  }
}
