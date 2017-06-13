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
import java.awt.Dialog.ModalityType;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Properties;
import java.util.Vector;
import java.util.logging.Logger;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.SwingUtilities;

import compat.MenuCompat;
import compat.UiCompat;
import compat.VersionCompat;
import devplugin.ActionMenu;
import devplugin.ContextMenuAction;
import devplugin.Plugin;
import devplugin.PluginCenterPanel;
import devplugin.PluginCenterPanelWrapper;
import devplugin.PluginInfo;
import devplugin.PluginTreeNode;
import devplugin.PluginsFilterComponent;
import devplugin.PluginsProgramFilter;
import devplugin.Program;
import devplugin.ProgramRatingIf;
import devplugin.ProgramReceiveIf;
import devplugin.ProgramReceiveTarget;
import devplugin.SettingsTab;
import devplugin.Version;
import util.misc.StringPool;
import util.paramhandler.ParamParser;
import util.program.AbstractPluginProgramFormating;
import util.program.LocalPluginProgramFormating;
import util.ui.Localizer;
import util.ui.UiUtilities;

public final class TVPearlPlugin extends devplugin.Plugin implements Runnable
{

	private static final boolean PLUGIN_IS_STABLE = true;
  private static final Version PLUGIN_VERSION = new Version(0, 27, 0, PLUGIN_IS_STABLE);

  private static final String TARGET_PEARL_COPY = "pearlCopy";
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(TVPearlPlugin.class);
	private static final Logger mLog = java.util.logging.Logger.getLogger(TVPearlPlugin.class.getName());

	private static TVPearlPlugin mInstance;
	private Thread mThread;
	private TVPearl mTVPearls;
	private boolean mHasRightToDownload = false;
	private PearlDialog mDialog = null;
	private PearlDialog mDialogCreation = null;
	private PearlInfoDialog mInfoDialog = null;
	private Vector<String> mComposerFilter;
	private ProgramReceiveTarget[] mClientPluginTargets;
	private PluginTreeNode mRootNode = new PluginTreeNode(this, false);

  private TVPearlSettings mSettings;
  private boolean mUpdating = false;
  
  private PluginCenterPanelWrapper mDisplayWrapper;
  private JPanel mCenterPanelDisplayWrapper;
  private PearlDisplayPanel mMangePanel;
  private PearlCreationJPanel mPearlCreationPanel;
  
  private JPanel mCenterPanelCreationWrappper;

  private AbstractPluginProgramFormating[] mConfigs = null;
  private LocalPluginProgramFormating[] mLocalFormatings = null;
  
  private PearlCreationTableModel mCreationTableModel = null;
  
  private static final LocalPluginProgramFormating DEFAULT_FORMAT = new LocalPluginProgramFormating(
      mLocalizer.msg("name", "TV Pearl"),
      "{title}",
      "{start_day_of_week}, {start_day}. {start_month_name}, {leadingZero(start_hour,\"2\")}:{leadingZero(start_minute,\"2\")}, {channel_name}\n{title}\n\n{genre}",
      "UTF-8");
  
	public TVPearlPlugin()
	{
	  mCreationTableModel = new PearlCreationTableModel();
		mInstance = this;
		mClientPluginTargets = new ProgramReceiveTarget[0];
		setAvailableLocalPluginProgramFormatings(null);
		setSelectedPluginProgramFormatings(null);
	}

	public static TVPearlPlugin getInstance()
	{
		return mInstance;
	}

	public static final Logger getLogger()
	{
		return mLog;
	}

	public PluginInfo getInfo()
	{
	  final String name = mLocalizer.msg("name", "TV Pearl");
    final String desc = mLocalizer.msg("description",
        "Shows the TV Pearls from the TV-Browser forum.") + " This product includes software developed at The Apache Software Foundation (http://www.apache.org/).";
    final String author = "Reinhard Lehrbaum";

		return new PluginInfo(TVPearlPlugin.class, name, desc, author);
	}

	public static Version getVersion()
	{
		return PLUGIN_VERSION;
	}

	public SettingsTab getSettingsTab()
	{
		return (new TVPearlPluginSettingsTab(mSettings));
	}

	public void onActivation()
	{
		mTVPearls = new TVPearl();
		mComposerFilter = new Vector<String>();
		
    mCenterPanelDisplayWrapper = UiCompat.createPersonaBackgroundPanel();
    mCenterPanelCreationWrappper = UiCompat.createPersonaBackgroundPanel();
    
    mDisplayWrapper = new PluginCenterPanelWrapper() {
      @Override
      public PluginCenterPanel[] getCenterPanels() {
        final PearlCreationPanel p = new PearlCreationPanel();
        
        if(VersionCompat.isAtLeastTvBrowser4()) {
          p.setIcon(getNewPearlIcon());
        }
        
        return new PluginCenterPanel[] {new PearlCenterPanel(),p};
      }
    };
	}
	
  private void addCenterPanel() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {try {
        mMangePanel = new PearlDisplayPanel(null);
        mPearlCreationPanel = new PearlCreationJPanel(mCreationTableModel,mSettings);
        //Persona.getInstance().registerPersonaListener(mMangePanel);
        
        mCenterPanelDisplayWrapper.add(mMangePanel,BorderLayout.CENTER);
        mCenterPanelDisplayWrapper.updateUI();
        
        mCenterPanelCreationWrappper.add(mPearlCreationPanel, BorderLayout.CENTER);
        mCenterPanelCreationWrappper.updateUI();
      }catch(Throwable t) {t.printStackTrace();}
      }
    });    
  }

	public ActionMenu getButtonAction()
	{
	  final AbstractAction action = new AbstractAction()
		{
			private static final long serialVersionUID = 1L;

			public void actionPerformed(final ActionEvent evt)
			{
				showDialog(PearlDialog.TYPE_PEARL_DISPLAY);
			}
		};

		action.putValue(Action.NAME, mLocalizer.msg("name", "TV Pearl"));
		action.putValue(Action.SMALL_ICON, createImageIcon("actions", "pearl", 16));
		action.putValue(BIG_ICON, createImageIcon("actions", "pearl", 22));

		ActionMenu result = new ActionMenu(action);
	  
		if(!VersionCompat.isCenterPanelSupported()) {
		  result.getAction().putValue(Action.NAME, TVPearlPluginSettingsTab.mLocalizer.msg("tabPearlDisplay", "TV Pearl Display"));
		  
		  final ContextMenuAction create = new ContextMenuAction(TVPearlPluginSettingsTab.mLocalizer.msg("tabPearlCreation", "TV Pearl Creation"), getNewPearlIcon()) {
		    @Override
		    public void actionPerformed(ActionEvent event) {
		      showDialog(PearlDialog.TYPE_PEARL_CREATION);
		    }
		  };
		  
		  result = MenuCompat.createActionMenu(MenuCompat.ID_ACTION_NONE, mLocalizer.msg("name", "TV Pearl"), createImageIcon("actions", "pearl", 16), new ActionMenu[] {result, new ActionMenu(create)});
		}
		
		return result;
	}

	void showDialog(int type)
	{
	  PearlDialog d = type == PearlDialog.TYPE_PEARL_DISPLAY ? mDialog : mDialogCreation;
	  
		if (d == null)
		{
		  final Window w = UiUtilities.getBestDialogParent(getParentFrame());
			
			d = new PearlDialog(w, type);
			
			d.addWindowListener(new WindowAdapter()
			{
				public void windowClosed(final WindowEvent e)
				{
					closeInfoDialog();
				}

				public void windowClosing(final WindowEvent e)
				{
					closeInfoDialog();
				}
			});

			d.pack();

      layoutWindow(type == PearlDialog.TYPE_PEARL_DISPLAY ? "pearlDialog" : "pearlDialogCreation", d, new Dimension(500, 350));

			if(type == PearlDialog.TYPE_PEARL_DISPLAY) {
			  mDialog = d;
			}
			else {
			  mDialogCreation = d;
			}
			
			d.setVisible(true);
			d.requestFocus();
		}
		else {
			d.setVisible(true);
			d.updateProgramList();
			d.requestFocus();
		}
	}
	
	public ActionMenu getContextMenuActions(final Program program)
	{
	  final TVPProgram p = getPearl(program);

		if (p != null)
		{
		  final ContextMenuAction menu = new ContextMenuAction();
			menu.setText(mLocalizer.msg("comment", "TV Pearl comment"));
			menu.putValue(Action.ACTION_COMMAND_KEY, menu.getValue(Action.NAME));
			menu.setSmallIcon(getSmallIcon());
			menu.setActionListener(new ActionListener()
			{
				public void actionPerformed(final ActionEvent event)
				{
					showPearlInfo(getPearl(program));
				}
			});
						
			Action[] test = getCreatePearlMenu(program,menu);
			
			if(test != null && !p.getAuthor().equals(mSettings.getForumUserName())) {
			  return new ActionMenu(getInfo().getName(), test);
			}
			else {
			  return new ActionMenu(menu);
			}
		}
		else {
		  Action[] test = getCreatePearlMenu(program,null);
		  
		  if(test == null) {
		    return null;
		  }
		  else if(test.length == 1) {
		    return new ActionMenu(test[0]);
		  }
		  else {
		    return new ActionMenu(mLocalizer.msg("context.multiple", "Create new TV pearl"), getSmallIcon(), test);
		  }
		}
	}
	
	private Action[] getCreatePearlMenu(final Program program, Action show) {
	  if(program != null && ((!program.isExpired() && !program.isOnAir()) || program.getID() == null) && !mCreationTableModel.contains(program)) {
      final AbstractPluginProgramFormating[] selected = getSelectedPluginProgramFormatings();
      
      if(selected.length == 1) {
        final ContextMenuAction menu = new ContextMenuAction(mLocalizer.msg("context.single", "Create new TV pearl as '{0}'", selected[0].getName()), getNewPearlIcon());
        menu.setActionListener(new ActionListener() {
          @Override
          public void actionPerformed(ActionEvent e) {
            mCreationTableModel.addRowSorted(new TVPearlCreation(program, selected[0]));
          }
        });
        
        if(show == null) {
          return new Action[] {menu};
        }
        else {
          return new Action[] {show,menu};
        }
      }
      else {
        int offset = show == null ? 0 : 1;
        
        Action[] subMenu = new Action[selected.length + offset];
        
        if(show != null) {
          subMenu[0] = show;
        }
        
        for(int i = 0; i < selected.length; i++) {
          final AbstractPluginProgramFormating formating = selected[i];
          
          AbstractAction action = new AbstractAction(formating.getName()) {
            @Override
            public void actionPerformed(ActionEvent e) {
              mCreationTableModel.addRowSorted(new TVPearlCreation(program, formating));
            }
          };
          
          subMenu[i+offset] = action;
        }
        
        return subMenu;
      }
    }
	  
	  return null;
	}

	public Icon[] getMarkIconsForProgram(final Program p)
	{
	  if(p.isExpired() || mTVPearls.getPearl(p) != null) {
	    if(mCreationTableModel.contains(p)) {
	      return new Icon[] { getSmallIcon(),getNewPearlIcon() };
	    }
	    else {
	      return new Icon[] { getSmallIcon() };
	    }
	  }
	  else {
	    return new Icon[] { getNewPearlIcon() };
	  }
	}

	public PluginTreeNode getRootNode()
	{
		return mRootNode;
	}

	public boolean canUseProgramTree()
	{
		return true;
	}

	public void loadSettings(final Properties prop) {
    // settings are handled in another class, so do not store a reference to the
    // properties parameter!
    mSettings = new TVPearlSettings(prop);
    mTVPearls.setUrl(mSettings.getUrl());
  }

	public Properties storeSettings()
	{
		mSettings.setUrl(mTVPearls.getUrl());
		return mSettings.storeSettings();
	}
	
	public void readData(final ObjectInputStream in) throws IOException,
      ClassNotFoundException
	{
	  int version = in.readInt();
	  
		mTVPearls.readData(in, version);
		
		if(version >= 4) {
		  int count = in.readInt();

      ArrayList<AbstractPluginProgramFormating> list = new ArrayList<AbstractPluginProgramFormating>(count);

      for(int i = 0; i < count; i++) {
        AbstractPluginProgramFormating value = AbstractPluginProgramFormating.readData(in);

        if(value != null) {
          list.add(value);
        }
      }

      mConfigs = list.toArray(new AbstractPluginProgramFormating[Math.min(count,list.size())]);

      count = in.readInt();
      ArrayList<LocalPluginProgramFormating> listLocal = new ArrayList<LocalPluginProgramFormating>(count);

      for(int i = 0; i < count; i++) {
        LocalPluginProgramFormating value = (LocalPluginProgramFormating) AbstractPluginProgramFormating.readData(in);

        if(value != null) {
          LocalPluginProgramFormating loadedInstance = getInstanceOfFormatingFromSelected(value);
          if (loadedInstance != null) {
            listLocal.add(loadedInstance);
          }
          else {
            listLocal.add(value);
          }
        }
      }
      mLocalFormatings = listLocal.toArray(new LocalPluginProgramFormating[Math.min(count,list.size())]);
		}
		
		if(mConfigs.length == 0) {
		  mConfigs = Plugin.getPluginManager().getAvailableGlobalPuginProgramFormatings();
		}
		
		if(version >= 5) {
		  mCreationTableModel = PearlCreationTableModel.readData(in, version);
		}
	}
	
  public boolean canReceiveProgramsWithTarget() {
    return true;
  }

  public ProgramReceiveTarget[] getProgramReceiveTargets() {
    ArrayList<ProgramReceiveTarget> list = new ArrayList<ProgramReceiveTarget>();
    
    for(AbstractPluginProgramFormating selected : getSelectedPluginProgramFormatings()) {
      if(selected != null && selected.isValid()) {
        list.add(new ProgramReceiveTarget(this, selected.getName(), selected.getId()));
      }
    }
    
    list.add(new ProgramReceiveTarget(this,
        mLocalizer.msg("programTarget", "Copy as TV pearl to clipboard"),
        TARGET_PEARL_COPY));
    
    return list.toArray(new ProgramReceiveTarget[list.size()]);
  }
	
  private LocalPluginProgramFormating getInstanceOfFormatingFromSelected(LocalPluginProgramFormating value) {
    for(AbstractPluginProgramFormating config : mConfigs) {
      if(config.equals(value)) {
        return (LocalPluginProgramFormating)config;
      }
    }

    return null;
  }
  
  protected LocalPluginProgramFormating[] getAvailableLocalPluginProgramFormatings() {
    return mLocalFormatings;
  }

  protected void setAvailableLocalPluginProgramFormatings(LocalPluginProgramFormating[] value) {
    if(value == null || value.length < 1) {
      mLocalFormatings = new LocalPluginProgramFormating[1];
      mLocalFormatings[0] = DEFAULT_FORMAT;
    } else {
      mLocalFormatings = value;
    }
  }

  protected AbstractPluginProgramFormating[] getSelectedPluginProgramFormatings() {
    return mConfigs;
  }

  protected void setSelectedPluginProgramFormatings(AbstractPluginProgramFormating[] value) {
    if(value == null || value.length < 1) {
      mConfigs = new LocalPluginProgramFormating[1];
      mConfigs[0] = DEFAULT_FORMAT;
    } else {
      mConfigs = value;
    }
    
    if(mPearlCreationPanel != null) {
      mPearlCreationPanel.updateFormatingEditor(value);
    }
  }

	public void writeData(final ObjectOutputStream out) throws IOException
	{
	  out.writeInt(5);
		mTVPearls.writeData(out);
		
		if(mConfigs != null) {
      ArrayList<AbstractPluginProgramFormating> list = new ArrayList<AbstractPluginProgramFormating>();

      for(AbstractPluginProgramFormating config : mConfigs) {
        if(config != null) {
          list.add(config);
        }
      }
      
      out.writeInt(list.size());

      for(AbstractPluginProgramFormating config : list) {
        config.writeData(out);
      }
    } else {
      out.writeInt(0);
    }

    if(mLocalFormatings != null) {
      ArrayList<AbstractPluginProgramFormating> list = new ArrayList<AbstractPluginProgramFormating>();

      for(AbstractPluginProgramFormating config : mLocalFormatings) {
        if(config != null) {
          list.add(config);
        }
      }

      out.writeInt(list.size());

      for(AbstractPluginProgramFormating config : list) {
        config.writeData(out);
      }
    }
    
    mCreationTableModel.writeData(out);
	}

	public void handleTvBrowserStartFinished()
	{
		mHasRightToDownload = true;
		
		SwingUtilities.invokeLater(new Runnable() {
      @Override
      public void run() {
        addCenterPanel();
      }
    });

		if (mSettings.getUpdatePearlsAfterStart())
		{
			update();
			mHasRightToDownload = false;
		}
		else
		{
			updateChanges();
		}
	}

  public void handleTvDataUpdateFinished()
	{
		if (mHasRightToDownload && mSettings.getUpdatePearlsAfterDataUpdate())
		{
			update();
		}
		mTVPearls.recheckProgramID();
		updateChanges();
	}

  synchronized private void update()
	{
    if (mUpdating) {
      return;
    }
    mUpdating = true;
		mThread = new Thread(this, "TV Pearl Update");
		mThread.setPriority(Thread.MIN_PRIORITY);
		mThread.start();
	}

	void updateChanges()
	{
		mTVPearls.updateTVB();
		
		if(mMangePanel != null) {
		  mMangePanel.updateProgramList();
		}
	}

	ImageIcon getSmallIcon()
	{
		return createImageIcon("actions", "pearl", 16);
	}
	
	ImageIcon getNewPearlIcon() {
	  return createImageIcon("actions", "pearl-new", 16);
	}

	ImageIcon getProgramIcon(boolean knownProgram)
	{
	  if (knownProgram) {
	    return createImageIcon("actions", "program_found", 16);
	  }
	  else {
	    return createImageIcon("actions", "program_unknown", 16);
	  }
	}

	static String getDayName(final Calendar cal, final boolean showToday)
	{
	  final SimpleDateFormat df = new SimpleDateFormat("E");
		String day = df.format(cal.getTime());
		final Calendar c = Calendar.getInstance();
		if (showToday)
		{
			if (cal.get(Calendar.YEAR) == c.get(Calendar.YEAR) && cal.get(Calendar.MONTH) == c.get(Calendar.MONTH))
			{
				if (cal.get(Calendar.DAY_OF_MONTH) == c.get(Calendar.DAY_OF_MONTH))
				{
					day = Localizer.getLocalization(Localizer.I18N_TODAY);
				}
				else if (cal.get(Calendar.DAY_OF_MONTH) == c.get(Calendar.DAY_OF_MONTH) + 1)
				{
					day = Localizer.getLocalization(Localizer.I18N_TOMORROW);
				}
				else if (cal.get(Calendar.DAY_OF_MONTH) == c.get(Calendar.DAY_OF_MONTH) - 1)
				{
					day = Localizer.getLocalization(Localizer.I18N_YESTERDAY);
				}
			}
		}
		return day;
	}

	TVPProgram[] getProgramList()
	{
		return mTVPearls.getPearlList();
	}

	Vector<String> getComposers()
	{
		return mComposerFilter;
	}

	void setComposers(final Vector<String> composers)
	{
		mComposerFilter = composers;
	}

	void showPearlInfo(final TVPProgram p)
	{
		if (p != null)
		{
			if (mInfoDialog != null)
			{
				mInfoDialog.dispose();
			}

			final Window w = UiUtilities.getLastModalChildOf(getParentFrame());

			if (w instanceof JDialog)
			{
				mInfoDialog = new PearlInfoDialog((JDialog) w, p);
			}
			else
			{
				mInfoDialog = new PearlInfoDialog((JFrame) w, p);
			}

			mInfoDialog.pack();
			mInfoDialog.setModalityType(mSettings.getShowInfoModal() ? VersionCompat.getSuggestedModalityType() : ModalityType.MODELESS);
			
			layoutWindow("infoDialog", mInfoDialog);
			mInfoDialog.setVisible(true);
		}
	}

	private void closeInfoDialog()
	{
    if (mInfoDialog == null) {
      return;
    }
		try
		{
		  mInfoDialog.dispose();
		}
		catch (Exception e)
		{}
	}

	synchronized public void run()
	{
		try {
      mLog.info("Start TV Pearl update.");
      mTVPearls.update();
      mLog.info("TV Pearl update finished.");
      mTVPearls.logInfo();
      updateChanges();
      if (mDialog != null)
      {
      	mDialog.updateProgramList();
      }
    } finally {
      mUpdating = false;
    }
	}

	public void updateDialog()
	{
	  if(mDialog != null) {
	    mDialog.update();
	  }
	  if(mMangePanel != null) {
	    mMangePanel.update();
	  }
	}

	@Override
	public int getMarkPriorityForProgram(final Program p)
	{
	  if(mTVPearls.getPearl(p) != null) {
	    return mSettings.getMarkPriority();
	  }
	  
	  return Program.NO_MARK_PRIORITY;
	}

	public boolean hasPluginTarget()
	{
		return mClientPluginTargets.length > 0;
	}

	public ProgramReceiveTarget[] getClientPluginsTargets()
	{
	  final ArrayList<ProgramReceiveTarget> list = new ArrayList<ProgramReceiveTarget>();
		for (ProgramReceiveTarget target : mClientPluginTargets)
		{
		  final ProgramReceiveIf plugin = target.getReceifeIfForIdOfTarget();
			if (plugin != null && plugin.canReceiveProgramsWithTarget())
			{
				list.add(target);
			}
		}
		return list.toArray(new ProgramReceiveTarget[list.size()]);
	}

	void setClientPluginsTargets(final ProgramReceiveTarget[] targets)
	{
		if (targets != null)
		{
			mClientPluginTargets = targets;
		}
		else
		{
			mClientPluginTargets = new ProgramReceiveTarget[0];
		}
	}

  public static TVPearlSettings getSettings() {
    return getInstance().mSettings;
  }
  
  @Override
  public boolean receivePrograms(final Program[] programArr,
      final ProgramReceiveTarget receiveTarget) {
    if(programArr != null) {
      if (receiveTarget.isReceiveTargetWithIdOfProgramReceiveIf(this, TARGET_PEARL_COPY)) {
        final ParamParser parser = new ParamParser();
        final StringBuilder buffer = new StringBuilder();
        for (Program program : programArr) {
          final TVPProgram test = mTVPearls.getPearl(program);
          
          if(test == null && !program.isExpired() && !program.isOnAir()) {
            final String programText = parser.analyse(DEFAULT_FORMAT.getContentValue(),
                program);
            if (programText != null) {
              buffer.append(programText.trim());
              buffer.append("\n\n");
            }
          }
        }
        final String text = buffer.toString();
        if (text.length() > 0) {
          final Clipboard clip = java.awt.Toolkit.getDefaultToolkit()
              .getSystemClipboard();
          clip.setContents(new StringSelection(text), null);
        }
        return true;
      }
      
      for(AbstractPluginProgramFormating formating : mConfigs) {
        if(receiveTarget.isReceiveTargetWithIdOfProgramReceiveIf(this, formating.getId())) {
          for(Program program : programArr) {
            final TVPProgram test = mTVPearls.getPearl(program);
            
            if((test == null || !test.getAuthor().equals(mSettings.getForumUserName())) && !program.isExpired() && !program.isOnAir()) {
              mCreationTableModel.addRowSorted(new TVPearlCreation(program, formating));
            }
          }
          
          return true;
        }
      }
    }
    
    return false;
  }

  @Override
  public PluginsProgramFilter[] getAvailableFilter() {
    return new PluginsProgramFilter[] { new PluginsProgramFilter(this){

      @Override
      public String getSubName() {
        return "";
      }

      public boolean accept(Program program) {
        return hasPearl(program);
      }
    }};
  }

  @Override
  @SuppressWarnings("unchecked")
  public Class<? extends PluginsFilterComponent>[] getAvailableFilterComponentClasses() {
  	return (Class<? extends PluginsFilterComponent>[]) new Class[] {PearlFilterComponent.class};
  }

  @Override
  public ProgramRatingIf[] getRatingInterfaces() {
    return new ProgramRatingIf[] { new ProgramRatingIf(){

      public String getName() {
        return mLocalizer.msg("name", "TV Pearl");
      }

      public Icon getIcon() {
        return createImageIcon("actions", "pearl", 16);
      }

      public int getRatingForProgram(Program program) {
        if (getPearl(program) != null) {
          return 100;
        }
        return -1;
      }

      public Icon getIconForProgram(Program program) {
        if (getPearl(program) != null) {
          return createImageIcon("actions", "pearl", 16);
        }
        return null;
      }

      public boolean hasDetailsDialog() {
        return true;
      }

      public void showDetailsFor(final Program program) {
        TVPProgram pearl = getPearl(program);
        if (pearl != null) {
        	showPearlInfo(pearl);
        }
      }
    }};
  }

	boolean hasPearl(final Program program) {
		return getPearl(program) != null;
	}

	/**
	 * get pearl for program<br>
	 * synchronized because this method is exposed via rating interface
	 * @param program
	 * @return
	 */
	synchronized TVPProgram getPearl(final Program program) {
		return mTVPearls.getPearl(program);
	}

	public static String poolString(String input) {
		return StringPool.getString(input);
	}
	
	public Frame getSuperFrame() {
	  return getParentFrame();
	}
	
  public PluginCenterPanelWrapper getPluginCenterPanelWrapper() {
    return mDisplayWrapper;
  }
	
  private class PearlCenterPanel extends PluginCenterPanel {
    @Override
    public String getName() {
      return TVPearlPluginSettingsTab.mLocalizer.msg("tabPearlDisplay", "TV Pearl Display");
    }

    @Override
    public JPanel getPanel() {
      return mCenterPanelDisplayWrapper;
    }
  }
  
  private class PearlCreationPanel extends PluginCenterPanel {
    @Override
    public String getName() {
      return TVPearlPluginSettingsTab.mLocalizer.msg("tabPearlCreation", "TV Pearl Creation");
    }

    @Override
    public JPanel getPanel() {
      return mCenterPanelCreationWrappper;
    }
  }
  
  public PearlCreationTableModel getCreationTableModel() {
    return mCreationTableModel;
  }
}
