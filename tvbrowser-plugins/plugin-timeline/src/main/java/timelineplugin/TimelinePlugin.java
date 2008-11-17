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

import java.awt.*;
import java.awt.event.*;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.*;
import javax.swing.*;

import devplugin.*;
import devplugin.Date;
import timelineplugin.format.*;

public class TimelinePlugin extends devplugin.Plugin
{
	protected static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(TimelinePlugin.class);

	private static TimelinePlugin mInstance;

	private static final Font DEFAULT_FONT = new Font("Dialog", Font.PLAIN, 12);
	private static String DEFAULT_FORMAT = "{title}";

	private TimelineDialog mDialog;
	private Properties mProperties;
	private Date mChoosenDate;
	private String mTitleFormat;
	private TextFormatter mFormatter;
	private ProgramFilter mFilter;

	private int mChannelWidth = -1;
	private int mOffset;

	public TimelinePlugin()
	{
		mInstance = this;
		mTitleFormat = DEFAULT_FORMAT;
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
		return new Version(0, 5, 1, false);
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
		mFormatter.setFormat(getFormat());

		setChoosenDate(Date.getCurrentDate());

		try
		{
			setOffset(Integer.parseInt(mProperties.getProperty("width", "620")) / 2);
		}
		catch (Exception e)
		{
			setOffset(620 / 2);
		}
		mDialog = new TimelineDialog(getParentFrame(), startWithNow());
		mDialog.pack();

		if (!mProperties.isEmpty())
		{
			try
			{
				int x = Integer.parseInt(mProperties.getProperty("xpos", "0"));
				int y = Integer.parseInt(mProperties.getProperty("ypos", "0"));
				int width = Integer.parseInt(mProperties.getProperty("width", "620"));
				int height = Integer.parseInt(mProperties.getProperty("height", "390"));

				mDialog.setBounds(x, y, width, height);
			}
			catch (Exception ee)
			{}
		}
		else
		{
			mDialog.setSize(620, 390);
			mDialog.setLocationRelativeTo(getParentFrame());
		}

		mDialog.setVisible(true);
		savePosition();
	}

	public void handleTvBrowserStartFinished()
	{
		if (showAtStartUp())
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

	int getFocusDelta()
	{
		int result = 50;
		try
		{
			result = Integer.parseInt(mProperties.getProperty("FocusDelta", "50"));
		}
		catch (Exception ee)
		{}
		return Math.abs(result);
	}

	int getHourWidth()
	{
		int result = 120;
		try
		{
			result = Integer.parseInt(mProperties.getProperty("HourWidth", "120"));
		}
		catch (Exception ee)
		{}
		return Math.abs(result);
	}

	double getSizePerMinute()
	{
		return getHourWidth() / 60.0;
	}

	int getChannelHeight()
	{
		int result = 20;
		try
		{
			result = Integer.parseInt(mProperties.getProperty("ChannelHeight", "20"));
		}
		catch (Exception ee)
		{}
		return Math.abs(result);
	}

	int getChannelWidth()
	{
		if (mChannelWidth < 0)
		{
			JLabel l = new JLabel();
			FontMetrics fm = l.getFontMetrics(getFont());
			int neededWidth = 0;
			if (showChannelName())
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
			mChannelWidth = neededWidth + (showChannelIcon() ? 42 : 0);
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

	boolean showBar()
	{
		return (getProgressView() & 2) == 2;
	}

	boolean showProgress()
	{
		return (getProgressView() & 1) == 1;
	}

	private int getProgressView()
	{
		int result = 1;
		try
		{
			result = Integer.parseInt(mProperties.getProperty("ProgressView", "1"));
		}
		catch (Exception ee)
		{}
		return result;
	}

	boolean showAtStartUp()
	{
		Boolean result = false;
		try
		{
			result = Integer.parseInt(mProperties.getProperty("ShowAtStartup")) == 1;
		}
		catch (Exception ex)
		{}
		return result;
	}

	boolean startWithNow()
	{
		Boolean result = false;
		try
		{
			result = Integer.parseInt(mProperties.getProperty("StartWithNow")) == 1;
		}
		catch (Exception ex)
		{}
		return result;
	}

	boolean resizeWithMouse()
	{
		Boolean result = false;
		try
		{
			result = Integer.parseInt(mProperties.getProperty("ResizeWithMouse")) == 1;
		}
		catch (Exception ex)
		{}
		return result;
	}

	boolean showChannelName()
	{
		Boolean result = true;
		try
		{
			result = Integer.parseInt(mProperties.getProperty("ShowChannelName")) == 1;
		}
		catch (Exception ex)
		{}
		return result;
	}

	boolean showChannelIcon()
	{
		Boolean result = true;
		try
		{
			result = Integer.parseInt(mProperties.getProperty("ShowChannelIcon")) == 1;
		}
		catch (Exception ex)
		{}
		return result;
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

	String getFormat()
	{
		return mTitleFormat;
	}

	String getDefaultFormat()
	{
		return DEFAULT_FORMAT;
	}

	void setFormat(String format)
	{
		mTitleFormat = format;
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

	void setProperty(String property, String value)
	{
		mProperties.setProperty(property, value);
	}

	void setProperty(String property, Boolean value)
	{
		mProperties.setProperty(property, value ? "1" : "0");
	}

	void resize()
	{
		mDialog.resize();
	}

	private void savePosition()
	{
		mProperties.setProperty("xpos", String.valueOf(mDialog.getX()));
		mProperties.setProperty("ypos", String.valueOf(mDialog.getY()));
		mProperties.setProperty("width", String.valueOf(mDialog.getWidth()));
		mProperties.setProperty("height", String.valueOf(mDialog.getHeight()));
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

		try
		{
			mChannelWidth = Integer.parseInt(mProperties.getProperty("ChannelWidth", "-1"));
		}
		catch (Exception ee)
		{
			mChannelWidth = -1;
		}
	}

	public Properties storeSettings()
	{
		mProperties.setProperty("HourWidth", String.valueOf(getHourWidth()));
		mProperties.setProperty("ChannelHeight", String.valueOf(getChannelHeight()));
		mProperties.setProperty("ChannelWidth", String.valueOf(getChannelWidth()));
		mProperties.setProperty("ProgressView", String.valueOf(getProgressView()));
		return mProperties;
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
		out.writeObject(mTitleFormat);
	}
}
