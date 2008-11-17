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
import java.util.*;
import java.util.Timer;
import javax.swing.*;

import util.ui.*;
import com.jgoodies.forms.builder.*;
import com.jgoodies.forms.layout.*;
import devplugin.*;
import devplugin.Date;

public class TimelineDialog extends JDialog implements WindowClosingIf
{
	private static final long serialVersionUID = 1L;

	protected static final util.ui.Localizer mLocalizer = util.ui.Localizer.getLocalizerFor(TimelinePlugin.class);

	private ProgramScrollPanel mMainPane;
	private JComboBox mDateList;
	private JComboBox mTimeList;
	private JComboBox mFilterList;
	private Timer mTimer;
	private int[] mTimes;
	private boolean mLockNow = false;
	private boolean mStartWithNow = false;
	private boolean mIgnoreReset = false;
	private double mRelation;

	public TimelineDialog(Frame frame, boolean startWithNow)
	{
		super(frame, true);
		setTitle(mLocalizer.msg("name", "Timeline"));
		mStartWithNow = startWithNow;

		createGUI();

		mTimer = new Timer();
		mTimer.scheduleAtFixedRate(new TimerTask()
		{
			public void run()
			{
				if (mLockNow)
				{
					try
					{
						mIgnoreReset = true;

						gotoNow();
						mMainPane.repaint();
					}
					finally
					{
						mIgnoreReset = false;
					}
				}
				else
				{
					mMainPane.update();
				}
			}
		}, 2000, 2000);

		UiUtilities.registerForClosing(this);

		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				gotoNow();
				if (mStartWithNow)
				{
					mLockNow = true;
					mTimeList.setSelectedIndex(1);
				}
			}
		});
	}

	private void createGUI()
	{
		JPanel content = (JPanel) this.getContentPane();
		content.setLayout(new BorderLayout());
		content.setBorder(UiUtilities.DIALOG_BORDER);

		content.add(getInfoPanel(), BorderLayout.NORTH);

		mMainPane = new ProgramScrollPanel();
		mMainPane.getHorizontalScrollBar().addAdjustmentListener(getHorizontalScrollBarListener());

		content.add(mMainPane, BorderLayout.CENTER);
		content.add(getFooterPanel(), BorderLayout.SOUTH);

		addKeyboardAction();
	}

	private AdjustmentListener getHorizontalScrollBarListener()
	{
		return new AdjustmentListener()
		{
			public void adjustmentValueChanged(AdjustmentEvent e)
			{
				resetGoto();
				int dayLimit = getDayLimit();
				int value = e.getValue();

				if (value < dayLimit)
				{
					int index = mDateList.getSelectedIndex();
					if (index > 0)
					{
						mDateList.setSelectedIndex(index - 1);
						gotoTime(24 * 60, value - dayLimit);
					}
				}
				else if (value + mMainPane.getHorizontalScrollBar().getVisibleAmount() >= mMainPane.getHorizontalScrollBar().getMaximum() - dayLimit)
				{
					int index = mDateList.getSelectedIndex();
					if (index < mDateList.getItemCount() - 1)
					{
						mDateList.setSelectedIndex(mDateList.getSelectedIndex() + 1);
						gotoTime(0, (value + mMainPane.getHorizontalScrollBar().getVisibleAmount()) - (mMainPane.getHorizontalScrollBar().getMaximum() - dayLimit));
					}
				}
			}
		};
	}

	private JPanel getInfoPanel()
	{
		FormLayout layout = new FormLayout("pref, 3dlu, pref, 15dlu, pref, 3dlu, pref, 15dlu, pref, 3dlu, pref", "pref, 5dlu");

		PanelBuilder builder = new PanelBuilder(layout);
		builder.setBorder(null);
		CellConstraints cc = new CellConstraints();

		mDateList = new JComboBox(getDateList());
		mDateList.setSelectedIndex(1);
		mDateList.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				resetGoto();
				TimelinePlugin.getInstance().setChoosenDate((Date) mDateList.getSelectedItem());
				mMainPane.updateProgram();
			}
		});
		mTimeList = new JComboBox(getTimeList());
		mTimeList.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				int index = mTimeList.getSelectedIndex();
				switch (index)
				{
					case 0:
						break;
					case 1:
						gotoNow();
						mLockNow = true;
						break;
					default:
						gotoTime(mTimes[mTimeList.getSelectedIndex() - 2]);
						break;
				}
				mTimeList.setSelectedIndex(index);
			}
		});

		mFilterList = new JComboBox(getFilterList());
		TimelinePlugin.getInstance().setFilter(Plugin.getPluginManager().getFilterManager().getCurrentFilter());
		mFilterList.setSelectedItem(TimelinePlugin.getInstance().getFilter());
		mFilterList.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				TimelinePlugin.getInstance().setFilter((ProgramFilter)mFilterList.getSelectedItem());
				mMainPane.updateProgram();
			}
		});

		builder.addLabel(mLocalizer.msg("date", "Date"), cc.xy(1, 1));
		builder.add(mDateList, cc.xy(3, 1));
		builder.add(mTimeList, cc.xy(7, 1));
		builder.addLabel(mLocalizer.msg("filter", "Filter"), cc.xy(9, 1));
		builder.add(mFilterList, cc.xy(11, 1));

		return builder.getPanel();
	}

	private JPanel getFooterPanel()
	{
		JPanel fp = new JPanel(new BorderLayout());
		fp.setBorder(BorderFactory.createEmptyBorder(5, 0, 5, 0));

		JButton settingsBtn = new JButton(TimelinePlugin.getInstance().createImageIcon("categories", "preferences-desktop", 16));
		settingsBtn.setToolTipText(mLocalizer.msg("settings", "Open settings"));
		settingsBtn.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent e)
			{
				close();
				Plugin.getPluginManager().showSettings(TimelinePlugin.getInstance());
			}
		});
		fp.add(settingsBtn, BorderLayout.WEST);

		JButton closeBtn = new JButton(mLocalizer.msg("close", "Close"));
		closeBtn.addActionListener(new ActionListener()
		{
			public void actionPerformed(ActionEvent evt)
			{
				dispose();
			}
		});
		fp.add(closeBtn, BorderLayout.EAST);
		getRootPane().setDefaultButton(closeBtn);

		return fp;
	}

	public void addKeyboardAction()
	{
		JRootPane rootPane = this.getRootPane();

		// Debug Info
		rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0), "debugInfo");
		rootPane.getActionMap().put("debugInfo", new AbstractAction()
		{
			private static final long serialVersionUID = 1L;

			private String formatDim(Dimension d)
			{
				return "width=" + d.width + ", height=" + d.height;
			}

			@SuppressWarnings("deprecation")
			public void actionPerformed(ActionEvent e)
			{
				String info = "Viewport: " + formatDim(mMainPane.getViewport().getSize()) + "\n";
				info += "RowHeader: " + formatDim(mMainPane.getRowHeader().getSize()) + "\n";
				info += "ColumnHeader: " + formatDim(mMainPane.getColumnHeader().getSize()) + "\n";
				info += "Offset: " + TimelinePlugin.getInstance().getOffset() + "\n";
				info += "HorizontalScrollBar: " + mMainPane.getHorizontalScrollBar().getMaximum() + "\n";
				info += "View: " + formatDim(mMainPane.getViewport().getView().getSize()) + "\n";

				JOptionPane.showMessageDialog(null, info);
			}
		});

		// goto Now
		rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0), "gotoNow");
		rootPane.getActionMap().put("gotoNow", new AbstractAction()
		{
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e)
			{
				resetGoto();
				gotoNow();
			}
		});

		// goto Now (lock)
		rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_F9, InputEvent.CTRL_MASK), "gotoNowLock");
		rootPane.getActionMap().put("gotoNowLock", new AbstractAction()
		{
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e)
			{
				mTimeList.setSelectedIndex(1);
			}
		});

		// goto Next Day
		rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK), "nextDay");
		rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, InputEvent.CTRL_MASK), "nextDay");
		rootPane.getActionMap().put("nextDay", new AbstractAction()
		{
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e)
			{
				int index = mDateList.getSelectedIndex();
				if (index < mDateList.getItemCount() - 1)
				{
					mDateList.setSelectedIndex(mDateList.getSelectedIndex() + 1);
				}
			}
		});

		// goto Previous Day
		rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK), "previousDay");
		rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, InputEvent.CTRL_MASK), "previousDay");
		rootPane.getActionMap().put("previousDay", new AbstractAction()
		{
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e)
			{
				int index = mDateList.getSelectedIndex();
				if (index > 0)
				{
					mDateList.setSelectedIndex(index - 1);
				}
			}
		});

		// goto 00:00
		rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_HOME, InputEvent.CTRL_MASK), "gotoBegin");
		rootPane.getActionMap().put("gotoBegin", new AbstractAction()
		{
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e)
			{
				gotoTime(0);
			}
		});

		// goto 23:59
		rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_END, InputEvent.CTRL_MASK), "gotoEnd");
		rootPane.getActionMap().put("gotoEnd", new AbstractAction()
		{
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e)
			{
				gotoTime(23 * 60 + 59);
			}
		});

		// goto Next Hour
		rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_MASK), "nextHour");
		rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_RIGHT, InputEvent.CTRL_MASK), "nextHour");
		rootPane.getActionMap().put("nextHour", new AbstractAction()
		{
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e)
			{
				if (mMainPane.getHorizontalScrollBar().getValue() + mMainPane.getHorizontalScrollBar().getVisibleAmount() >= mMainPane.getHorizontalScrollBar().getMaximum())
				{
					int index = mDateList.getSelectedIndex();
					if (index < mDateList.getItemCount() - 1)
					{
						mDateList.setSelectedIndex(mDateList.getSelectedIndex() + 1);
						gotoTime(0);
					}
				}
				else
				{
					mMainPane.addTime(60);
				}
			}
		});

		// goto Previous Hour
		rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_MASK), "previousHour");
		rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(KeyStroke.getKeyStroke(KeyEvent.VK_KP_LEFT, InputEvent.CTRL_MASK), "previousHour");
		rootPane.getActionMap().put("previousHour", new AbstractAction()
		{
			private static final long serialVersionUID = 1L;

			public void actionPerformed(ActionEvent e)
			{
				if (mMainPane.getHorizontalScrollBar().getValue() <= mMainPane.getHorizontalScrollBar().getMinimum())
				{
					int index = mDateList.getSelectedIndex();
					if (index > 0)
					{
						mDateList.setSelectedIndex(index - 1);
						gotoTime(24 * 60 - 1);
					}
				}
				else
				{
					mMainPane.addTime(-60);
				}
			}
		});

		this.setRootPane(rootPane);
	}

	private void resetGoto()
	{
		if (!mIgnoreReset)
		{
			mTimeList.setSelectedIndex(0);
			mLockNow = false;
		}
	}

	private void gotoTime(int minute)
	{
		mMainPane.gotoTime(minute);
	}

	private void gotoTime(int minute, int delta)
	{
		mMainPane.gotoTime(minute, delta);
	}

	private Vector<Date> getDateList()
	{
		Vector<Date> list = new Vector<Date>();
		Date today = Date.getCurrentDate();
		for (int i = -1; i < 28; i++)
		{
			list.add(today.addDays(i));
		}
		return list;
	}

	private Vector<String> getTimeList()
	{
		Vector<String> list = new Vector<String>();
		list.add(mLocalizer.msg("goto", "Goto..."));
		list.add(mLocalizer.msg("now", "Now"));

		mTimes = Plugin.getPluginManager().getTvBrowserSettings().getTimeButtonTimes();
		TimeFormatter formatter = new TimeFormatter();

		for (int i = 0; i < mTimes.length; i++)
		{
			int h = mTimes[i] / 60;
			int m = mTimes[i] % 60;
			list.add(formatter.formatTime(h, m));
		}
		return list;
	}

	private Vector<ProgramFilter> getFilterList()
	{
		Vector<ProgramFilter> list = new Vector<ProgramFilter>();

		for (ProgramFilter filter : Plugin.getPluginManager().getFilterManager().getAvailableFilters())
		{
			list.add(filter);
		}
		return list;
	}

	public void close()
	{
		dispose();
	}

	public void gotoNow()
	{
		Date now = Date.getCurrentDate();
		if (!((Date) mDateList.getSelectedItem()).equals(now))
		{
			for (int i = 0; i < mDateList.getItemCount(); i++)
			{
				Date d = (Date) mDateList.getItemAt(i);
				if (d.equals(now))
				{
					mDateList.setSelectedIndex(i);
					break;
				}
			}
		}
		gotoTime(TimelinePlugin.getInstance().getNowMinute());
	}

	private int getDayLimit()
	{
		return TimelinePlugin.getInstance().getOffset() - mMainPane.getHorizontalScrollBar().getSize().width / 2;
	}

	public void resize()
	{
		JScrollBar sb = mMainPane.getHorizontalScrollBar();
		mRelation = (double) sb.getValue() / (double) sb.getMaximum();
		mMainPane.resize();

		SwingUtilities.invokeLater(new Runnable()
		{
			public void run()
			{
				JScrollBar sb = mMainPane.getHorizontalScrollBar();
				sb.setValue((int) (sb.getMaximum() * mRelation));
			}
		});
	}
}
