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
	private static final String DEFAULT_URL = "http://hilfe.tvbrowser.org/viewtopic.php?t=1470";

	private static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(TVPearlPlugin.class);
	private static java.util.logging.Logger mLog = java.util.logging.Logger.getLogger(TVPearlPlugin.class.getName());

	private static TVPearlPlugin mInstance;
	private Properties mProperties;
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
		return (new TVPearlPluginSettingsTab());
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

	public void loadSettings(Properties prop)
	{
		if (prop == null)
		{
			mProperties = new Properties();
		}
		else
		{
			mProperties = prop;
		}
		checkProperties();
		setDialogBounds();
		mTVPearls.setUrl(getUrl());
	}

	public Properties storeSettings()
	{
		mProperties.setProperty("Url", mTVPearls.getUrl());

		if (mPearlDialogLocation != null)
		{
			mProperties.setProperty("Dialog.X", Integer.toString(mPearlDialogLocation.x));
			mProperties.setProperty("Dialog.Y", Integer.toString(mPearlDialogLocation.y));
		}

		if (mPearlDialogSize != null)
		{
			mProperties.setProperty("Dialog.Width", Integer.toString(mPearlDialogSize.width));
			mProperties.setProperty("Dialog.Height", Integer.toString(mPearlDialogSize.height));
		}

		if (mPearlInfoDialogLocation != null)
		{
			mProperties.setProperty("InfoDialog.X", Integer.toString(mPearlInfoDialogLocation.x));
			mProperties.setProperty("InfoDialog.Y", Integer.toString(mPearlInfoDialogLocation.y));
		}

		if (mPearlInfoDialogSize != null)
		{
			mProperties.setProperty("InfoDialog.Width", Integer.toString(mPearlInfoDialogSize.width));
			mProperties.setProperty("InfoDialog.Height", Integer.toString(mPearlInfoDialogSize.height));
		}

		return mProperties;
	}

	private String getUrl()
	{
		String url = mProperties.getProperty("Url");
		if (url == null || url.length() == 0)
		{
			url = DEFAULT_URL;
		}
		return url;
	}

	private void checkProperties()
	{
		String prop;

		prop = mProperties.getProperty("UpdateAtStart");
		if (prop == null)
		{
			mProperties.setProperty("UpdateAtStart", "0");
		}

		prop = mProperties.getProperty("UpdateAfterUpdateFinished");
		if (prop == null)
		{
			mProperties.setProperty("UpdateAfterUpdateFinished", "1");
		}

		prop = mProperties.getProperty("UpdateManual");
		if (prop == null)
		{
			mProperties.setProperty("UpdateManual", "1");
		}

		prop = mProperties.getProperty("ViewOption");
		if (prop == null)
		{
			mProperties.setProperty("ViewOption", "1");
		}

		prop = mProperties.getProperty("MarkPearl");
		if (prop == null)
		{
			mProperties.setProperty("MarkPearl", "1");
		}

		prop = mProperties.getProperty("MarkPriority");
		if (prop == null)
		{
			mProperties.setProperty("MarkPriority", Integer.toString(Program.MIN_MARK_PRIORITY));
		}
	}

	private void setDialogBounds()
	{
		try
		{
			mPearlDialogSize = new Dimension(Integer.parseInt(mProperties.getProperty("Dialog.Width")), Integer.parseInt(mProperties.getProperty("Dialog.Height")));
		}
		catch (Exception e)
		{}
		try
		{
			mPearlDialogLocation = new Point(Integer.parseInt(mProperties.getProperty("Dialog.X")), Integer.parseInt(mProperties.getProperty("Dialog.Y")));
		}
		catch (Exception e)
		{}

		try
		{
			mPearlInfoDialogSize = new Dimension(Integer.parseInt(mProperties.getProperty("InfoDialog.Width")), Integer.parseInt(mProperties.getProperty("InfoDialog.Height")));
		}
		catch (Exception e)
		{}
		try
		{
			mPearlInfoDialogLocation = new Point(Integer.parseInt(mProperties.getProperty("InfoDialog.X")), Integer.parseInt(mProperties.getProperty("InfoDialog.Y")));
		}
		catch (Exception e)
		{}
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

		if (getPropertyBoolean("UpdateAtStart"))
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
		if (mHasRightToDownload && getPropertyBoolean("UpdateAfterUpdateFinished"))
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

	Properties getSettings()
	{
		return mProperties;
	}

	boolean getPropertyBoolean(String property)
	{
		boolean result = false;
		try
		{
			result = Integer.parseInt(mProperties.getProperty(property)) == 1;
		}
		catch (Exception ex)
		{}
		return result;
	}

	void setProperty(String property, boolean value)
	{
		mProperties.setProperty(property, value ? "1" : "0");
	}

	Integer getPropertyInteger(String property)
	{
		Integer result = 0;
		try
		{
			result = Integer.parseInt(mProperties.getProperty(property));
		}
		catch (Exception ex)
		{}
		return result;
	}

	void setProperty(String property, Integer value)
	{
		mProperties.setProperty(property, value.toString());
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
			mInfoDialog.setModal(getPropertyBoolean("ShowInfoModal"));
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
		return getPropertyInteger("MarkPriority");
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
}
