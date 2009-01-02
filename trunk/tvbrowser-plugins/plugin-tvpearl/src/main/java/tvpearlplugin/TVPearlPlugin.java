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

import java.awt.Dimension;
import java.awt.Point;
import java.awt.Window;
import java.awt.datatransfer.Clipboard;
import java.awt.datatransfer.StringSelection;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.ComponentEvent;
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

import util.paramhandler.ParamParser;
import util.program.LocalPluginProgramFormating;
import util.ui.Localizer;
import util.ui.UiUtilities;
import devplugin.ActionMenu;
import devplugin.ContextMenuAction;
import devplugin.PluginInfo;
import devplugin.PluginTreeNode;
import devplugin.Program;
import devplugin.ProgramReceiveIf;
import devplugin.ProgramReceiveTarget;
import devplugin.SettingsTab;
import devplugin.Version;

public class TVPearlPlugin extends devplugin.Plugin implements Runnable
{

	private static final String TARGET_PEARL_COPY = "pearlCopy";
  private static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(TVPearlPlugin.class);
	private static java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(TVPearlPlugin.class.getName());

	private static TVPearlPlugin mInstance;
	private Thread mThread;
	private TVPearl mTVPearls;
	private boolean mHasRightToDownload = false;
	private Point mPearlDialogLocation = null;
	private Dimension mPearlDialogSize = null;
	private Point mPearlInfoDialogLocation = null;
	private Dimension mPearlInfoDialogSize = null;
	private PearlDialog mDialog = null;
	private PearlInfoDialog mInfoDialog = null;
	private Vector<String> mComposerFilter;
	private ProgramReceiveTarget[] mClientPluginTargets;
	private PluginTreeNode mRootNode = new PluginTreeNode(this, false);

  private TVPearlSettings mSettings;

	public TVPearlPlugin()
	{
		mInstance = this;
		mClientPluginTargets = new ProgramReceiveTarget[0];
	}

	public static TVPearlPlugin getInstance()
	{
		return mInstance;
	}

	public static Logger getLogger()
	{
		return mLog;
	}

	public PluginInfo getInfo()
	{
		String name = mLocalizer.msg("name", "TV Pearl");
		String desc = mLocalizer.msg("description", "Shows the TV Pearls from the TV-Browser forum.");
		String author = "Reinhard Lehrbaum";

		return new PluginInfo(TVPearlPlugin.class, name, desc, author);
	}

	public static Version getVersion()
	{
		return new Version(0, 18, 0);
	}

	public SettingsTab getSettingsTab()
	{
		return (new TVPearlPluginSettingsTab(mSettings));
	}

	public void onActivation()
	{
		mTVPearls = new TVPearl();
		mComposerFilter = new Vector<String>();
	}

	public ActionMenu getButtonAction()
	{
		AbstractAction action = new AbstractAction()
		{
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent evt)
			{
				showDialog();
			}
		};

		action.putValue(Action.NAME, mLocalizer.msg("name", "TV Pearl"));
		action.putValue(Action.SMALL_ICON, createImageIcon("actions", "pearl", 16));
		action.putValue(BIG_ICON, createImageIcon("actions", "pearl", 22));

		return new ActionMenu(action);
	}

	protected void showDialog()
	{
		if (mDialog == null)
		{
			Window w = UiUtilities.getBestDialogParent(getParentFrame());

			if (w instanceof JDialog)
			{
				mDialog = new PearlDialog((JDialog) w);
			}
			else
			{
				mDialog = new PearlDialog((JFrame) w);
			}

			mDialog.addWindowListener(new WindowAdapter()
			{
				public void windowClosed(WindowEvent e)
				{
					closeInfoDialog();
				}

				public void windowClosing(WindowEvent e)
				{
					closeInfoDialog();
				}
			});

			mDialog.pack();
			mDialog.setModal(false);
			mDialog.addComponentListener(new java.awt.event.ComponentAdapter()
			{

				public void componentResized(ComponentEvent e)
				{
					mPearlDialogSize = e.getComponent().getSize();
				}

				public void componentMoved(ComponentEvent e)
				{
					e.getComponent().getLocation(mPearlDialogLocation);
				}
			});

			if ((mPearlDialogLocation != null) && (mPearlDialogSize != null))
			{
				mDialog.setLocation(mPearlDialogLocation);
				mDialog.setSize(mPearlDialogSize);
				mDialog.setVisible(true);
			}
			else
			{
				mDialog.setSize(500, 350);
				UiUtilities.centerAndShow(mDialog);
				mPearlDialogLocation = mDialog.getLocation();
				mPearlDialogSize = mDialog.getSize();
			}
		}
		else
		{
			mDialog.setVisible(true);
			mDialog.updateProgramList();
			mDialog.requestFocus();
		}
	}

	public ActionMenu getContextMenuActions(final Program program)
	{
		TVPProgram p = mTVPearls.getPearl(program);

		if (p != null || program.getID() == null)
		{
			ContextMenuAction menu = new ContextMenuAction();
			menu.setText(mLocalizer.msg("comment", "TV Pearl comment"));
			menu.putValue(Action.ACTION_COMMAND_KEY, menu.getValue(Action.NAME));
			menu.setSmallIcon(getSmallIcon());
			menu.setActionListener(new ActionListener()
			{
				public void actionPerformed(ActionEvent event)
				{
					showPearlInfo(mTVPearls.getPearl(program));
				}
			});

			return new ActionMenu(menu);
		}
		else
		{
			return null;
		}
	}

	public Icon[] getMarkIconsForProgram(Program p)
	{
		return new Icon[] { getSmallIcon() };
	}

	public PluginTreeNode getRootNode()
	{
		return mRootNode;
	}

	public boolean canUseProgramTree()
	{
		return true;
	}

	public void loadSettings(Properties prop) {
    // settings are handled in another class, so do not store a reference to the
    // properties parameter!
    if (prop == null) {
      mSettings = new TVPearlSettings(new Properties());
    } else {
      mSettings = new TVPearlSettings(prop);
    }
    setDialogBounds();
    mTVPearls.setUrl(mSettings.getUrl());
  }

	public Properties storeSettings()
	{
		mSettings.setUrl(mTVPearls.getUrl());

		mSettings.storeDialog("Dialog", mPearlDialogLocation, mPearlDialogSize);
    mSettings.storeDialog("InfoDialog", mPearlInfoDialogLocation,
        mPearlInfoDialogSize);

		return mSettings.storeSettings();
	}

	private void setDialogBounds()
	{
	  mPearlDialogSize = mSettings.getDialogSize("Dialog");
    mPearlDialogLocation = mSettings.getDialogDimension("Dialog");
    mPearlInfoDialogSize = mSettings.getDialogSize("InfoDialog");
    mPearlInfoDialogLocation = mSettings.getDialogDimension("InfoDialog");
	}

	public void readData(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		mTVPearls.readData(in);
	}

	public void writeData(ObjectOutputStream out) throws IOException
	{
		mTVPearls.writeData(out);
	}

	public void handleTvBrowserStartFinished()
	{
		mHasRightToDownload = true;

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

  void update()
	{
		mThread = new Thread(this, "TV Pearl Update");
		mThread.setPriority(Thread.MIN_PRIORITY);
		mThread.start();
	}

	void updateChanges()
	{
		mTVPearls.updateTVB();
	}

	ImageIcon getSmallIcon()
	{
		return createImageIcon("actions", "pearl", 16);
	}

	ImageIcon getProgramFoundIcon()
	{
		return createImageIcon("actions", "program_found", 16);
	}

	ImageIcon getProgramUnknownIcon()
	{
		return createImageIcon("actions", "program_unknown", 16);
	}

	String getDayName(Calendar cal, boolean showToday)
	{
		SimpleDateFormat df = new SimpleDateFormat("E");
		String day = df.format(cal.getTime());
		Calendar c = Calendar.getInstance();
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

	void setComposers(Vector<String> composers)
	{
		mComposerFilter = composers;
	}

	void showPearlInfo(TVPProgram p)
	{
		if (p != null)
		{
			if (mInfoDialog != null)
			{
				mInfoDialog.dispose();
			}

			Window w = UiUtilities.getLastModalChildOf(getParentFrame());

			if (w instanceof JDialog)
			{
				mInfoDialog = new PearlInfoDialog((JDialog) w, p);
			}
			else
			{
				mInfoDialog = new PearlInfoDialog((JFrame) w, p);
			}

			mInfoDialog.pack();
			mInfoDialog.setModal(mSettings.getShowInfoModal());
			mInfoDialog.addComponentListener(new java.awt.event.ComponentAdapter()
			{

				public void componentResized(ComponentEvent e)
				{
					mPearlInfoDialogSize = e.getComponent().getSize();
				}

				public void componentMoved(ComponentEvent e)
				{
					e.getComponent().getLocation(mPearlInfoDialogLocation);
				}
			});

			if ((mPearlInfoDialogLocation != null) && (mPearlInfoDialogSize != null))
			{
				mInfoDialog.setLocation(mPearlInfoDialogLocation);
				mInfoDialog.setSize(mPearlInfoDialogSize);
				mInfoDialog.setVisible(true);
			}
			else
			{
				mInfoDialog.setSize(450, 400);
				UiUtilities.centerAndShow(mInfoDialog);
				mPearlInfoDialogLocation = mInfoDialog.getLocation();
				mPearlInfoDialogSize = mInfoDialog.getSize();
			}
		}
	}

	private void closeInfoDialog()
	{
		try
		{
			mInfoDialog.dispose();
		}
		catch (Exception e)
		{}
	}

	public void run()
	{
		mLog.info("Start TV Pearl update.");
		mTVPearls.update();
		mLog.info("TV Pearl update finished.");
		mLog.info(mTVPearls.getInfo());
		updateChanges();
		if (mDialog != null)
		{
			mDialog.updateProgramList();
		}
	}

	public void updateDialog()
	{
		mDialog.update();
	}

	public int getMarkPriorityForProgram(Program p)
	{
		return mSettings.getMarkPriority();
	}

	public boolean hasPluginTarget()
	{
		return mClientPluginTargets.length > 0;
	}

	public ProgramReceiveTarget[] getClientPluginsTargets()
	{
		ArrayList<ProgramReceiveTarget> list = new ArrayList<ProgramReceiveTarget>();
		for (ProgramReceiveTarget target : mClientPluginTargets)
		{
			ProgramReceiveIf plugin = target.getReceifeIfForIdOfTarget();
			if (plugin != null && plugin.canReceiveProgramsWithTarget())
			{
				list.add(target);
			}
		}
		return list.toArray(new ProgramReceiveTarget[list.size()]);
	}

	public void setClientPluginsTargets(ProgramReceiveTarget[] targets)
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
  public boolean canReceiveProgramsWithTarget() {
    return true;
  }

  @Override
  public ProgramReceiveTarget[] getProgramReceiveTargets() {
    return new ProgramReceiveTarget[] { new ProgramReceiveTarget(this,
        mLocalizer.msg("programTarget", "Copy as TV pearl to clipboard"),
        TARGET_PEARL_COPY) };
  }

  @Override
  public boolean receivePrograms(Program[] programArr,
      ProgramReceiveTarget receiveTarget) {
    if (receiveTarget.getTargetId().equals(TARGET_PEARL_COPY)) {
      LocalPluginProgramFormating format = new LocalPluginProgramFormating(
          mLocalizer.msg("name", "TV Pearl"),
          "{title}",
          "{start_day_of_week}, {start_day}. {start_month_name}, {leadingZero(start_hour,\"2\")}:{leadingZero(start_minute,\"2\")}, {channel_name}\n{title}\n\n{genre}",
          "UTF-8");
      ParamParser parser = new ParamParser();
      StringBuffer buffer = new StringBuffer();
      for (Program program : programArr) {
        String programText = parser.analyse(format.getContentValue(), program);
        if (programText != null) {
          buffer.append(programText);
          buffer.append("\n\n");
        }
      }
      String text = buffer.toString();
      if (text.length() > 0) {
        Clipboard clip = java.awt.Toolkit.getDefaultToolkit()
            .getSystemClipboard();
        clip.setContents(new StringSelection(text), null);
      }
      return true;
    }
    return false;
  }

}
