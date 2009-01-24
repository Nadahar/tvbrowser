/*
 * Timeline by Reinhard Lehrbaum
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
package timelineplugin;

import java.awt.Font;
import java.awt.FontMetrics;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Calendar;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JLabel;
import javax.swing.SwingUtilities;

import timelineplugin.format.TextFormatter;
import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.ProgramFilter;
import devplugin.SettingsTab;
import devplugin.Version;

public class TimelinePlugin extends devplugin.Plugin
{
	protected static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(TimelinePlugin.class);

	private static TimelinePlugin mInstance;

	private static final Font DEFAULT_FONT = new Font("Dialog", Font.PLAIN, 12);

	private TimelineDialog mDialog;
	private Date mChoosenDate;
	private TextFormatter mFormatter;
	private ProgramFilter mFilter;

	private int mChannelWidth = -1;
	private int mOffset;

  private TimelineSettings mSettings;

  private String mTitleFormat;

	public TimelinePlugin()
	{
		mInstance = this;
	}

	public static TimelinePlugin getInstance()
	{
		return mInstance;
	}

	public PluginInfo getInfo()
	{
		String name = mLocalizer.msg("name", "Timeline");
		String desc = mLocalizer.msg("description", "Timeline view of the program data.");
		String author = "Reinhard Lehrbaum";

		return new PluginInfo(TimelinePlugin.class, name, desc, author);
	}

	public static Version getVersion()
	{
		return new Version(0, 6);
	}

	public SettingsTab getSettingsTab()
	{
		return (new TimelinePluginSettingsTab());
	}

	public void onDeactivation()
	{
		if (mDialog != null && mDialog.isVisible())
		{
			mDialog.dispose();
		}
	}

	public ActionMenu getButtonAction()
	{
		AbstractAction action = new AbstractAction()
		{
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent evt)
			{
				showTimeline();
			}
		};

		action.putValue(Action.NAME, mLocalizer.msg("name", "Timeline"));
		action.putValue(Action.SMALL_ICON, createImageIcon("actions", "timeline", 16));
		action.putValue(BIG_ICON, createImageIcon("actions", "timeline", 22));

		return new ActionMenu(action);
	}

	protected void showTimeline()
	{
		if (mDialog != null && mDialog.isVisible())
		{
			mDialog.dispose();
		}

		mFormatter = new TextFormatter();
		mFormatter.setFont(getFont());
		mFormatter.setInitialiseMaxLine(true);
		mFormatter.setFormat(mSettings.getTitleFormat());

		setChoosenDate(Date.getCurrentDate());

		setOffset(mSettings.getOffset() / 2);
		mDialog = new TimelineDialog(getParentFrame(), mSettings.startWithNow());
		mDialog.pack();

			try
			{
			  Rectangle rect = mSettings.getPosition();
        mDialog.setBounds(rect);
			}
			catch (Exception ee)
			{}
		mDialog.setVisible(true);
		savePosition();
	}

	public void handleTvBrowserStartFinished()
	{
		if (mSettings.showAtStartUp())
		{
			SwingUtilities.invokeLater(new Runnable()
			{
				public void run()
				{
					showTimeline();
				}
			});
		}
	}



	double getSizePerMinute()
	{
		return mSettings.getHourWidth() / 60.0;
	}

	int getChannelWidth()
	{
		if (mChannelWidth < 0)
		{
			JLabel l = new JLabel();
			FontMetrics fm = l.getFontMetrics(getFont());
			int neededWidth = 0;
			if (mSettings.showChannelName())
			{
				Channel[] mChannels = Plugin.getPluginManager().getSubscribedChannels();
				for (int i = 0; i < mChannels.length; i++)
				{
					int width = fm.stringWidth(mChannels[i].getName());
					if (neededWidth < width)
					{
						neededWidth = width;
					}
				}
				neededWidth += 10;
			}
			mChannelWidth = neededWidth + (mSettings.showChannelIcon() ? 42 : 0);
		}
		return mChannelWidth;
	}

	void setChannelWidth(int value)
	{
		mChannelWidth = value;
	}

	int getOffset()
	{
		return mOffset;
	}

	void setOffset(int offset)
	{
		mOffset = offset;
	}

	void resetChannelWidth()
	{
		mChannelWidth = -1;
	}

	Font getFont()
	{
		return DEFAULT_FONT;
	}

	Date getChoosenDate()
	{
		return mChoosenDate;
	}

	void setChoosenDate(Date d)
	{
		mChoosenDate = d;
	}

	int getNowMinute()
	{
		Calendar now = Calendar.getInstance();
		return now.get(Calendar.HOUR_OF_DAY) * 60 + now.get(Calendar.MINUTE);
	}

	int getNowPosition()
	{
		return getOffset() + (int) Math.round(getSizePerMinute() * getNowMinute());
	}

	void setFilter(ProgramFilter filter)
	{
		mFilter = filter;
	}

	ProgramFilter getFilter()
	{
		return mFilter;
	}

	TextFormatter getFormatter()
	{
		return mFormatter;
	}

	void resize()
	{
		mDialog.resize();
	}

	private void savePosition()
	{
	  mSettings.savePosition(mDialog.getX(), mDialog.getY(), mDialog.getWidth(),
        mDialog.getHeight());
	}

	public void loadSettings(Properties prop)
	{
	  mSettings = new TimelineSettings(prop);
    mSettings.setTitleFormat(mTitleFormat);
		mChannelWidth = mSettings.getChannelWidth();
	}

	public Properties storeSettings()
	{
		return mSettings.storeSettings();
	}

	public void readData(ObjectInputStream in) throws IOException, ClassNotFoundException
	{
		int version = in.readInt();

		if (version == 1)
		{
			mTitleFormat = (String) in.readObject();
		}
	}

	public void writeData(ObjectOutputStream out) throws IOException
	{
		out.writeInt(1); // version
    out.writeObject(mSettings.getTitleFormat());
	}

  public static TimelineSettings getSettings() {
    return getInstance().mSettings;
  }

}
