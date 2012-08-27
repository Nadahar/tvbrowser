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

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.awt.event.AdjustmentEvent;
import java.awt.event.AdjustmentListener;
import java.awt.event.InputEvent;
import java.awt.event.KeyEvent;
import java.util.Timer;
import java.util.TimerTask;
import java.util.Vector;

import javax.swing.AbstractAction;
import javax.swing.JComboBox;
import javax.swing.JComponent;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JRootPane;
import javax.swing.JScrollBar;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;

import util.ui.TimeFormatter;
import util.ui.persona.Persona;
import util.ui.persona.PersonaListener;

import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;

import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.Program;
import devplugin.ProgramFilter;

public class TimelinePanel extends JPanel implements PersonaListener {
  static final util.ui.Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(TimelinePlugin.class);
  
  private ProgramScrollPanel mMainPane;
  private JComboBox mDateList;
  private JComboBox mTimeList;
  private JComboBox mFilterList;
  private transient Timer mTimer;
  private int[] mTimes;
  private boolean mLockNow = false;
  private boolean mStartWithNow = false;
  private boolean mIgnoreReset = false;
  private double mRelation;

  private JLabel mDateLabel;
  private JLabel mFilterLabel;
  
  public TimelinePanel(final boolean startWithNow, boolean addPanel) {
    mStartWithNow = startWithNow;

    createGUI(addPanel);

    int seconds = ((int) (System.currentTimeMillis() / 1000.0)) % 30;
    mTimer = new Timer();
    mTimer.scheduleAtFixedRate(new TimerTask() {
      public void run() {
        if (mLockNow) {
          try {
            mIgnoreReset = true;

            gotoNow();
            mMainPane.repaint();
          } finally {
            mIgnoreReset = false;
          }
        } else {
          mMainPane.update();
        }
      }
    }, (30 - seconds + 1) * 1000L, 30 * 1000); // update after 1 and 31 seconds each minute
    
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        gotoNow();
        if (mStartWithNow) {
          mLockNow = true;
          mTimeList.setSelectedIndex(1);
        }
        mMainPane.setDateLabel((Date)mDateList.getSelectedItem());
      }
    });
  }

  private void createGUI(boolean addPanel) {
    setOpaque(false);
    setLayout(new BorderLayout());
    
    if(addPanel) {
      add(getInfoPanel(), BorderLayout.NORTH);
    }
    else {
      getInfoPanel();
    }

    mMainPane = new ProgramScrollPanel();
    mMainPane.getHorizontalScrollBar().addAdjustmentListener(
        getHorizontalScrollBarListener());

    add(mMainPane, BorderLayout.CENTER);
  }

  private AdjustmentListener getHorizontalScrollBarListener() {
    return new AdjustmentListener() {
      public void adjustmentValueChanged(final AdjustmentEvent e) {
        resetGoto();
        final int value = e.getValue();

        if (value < TimelinePlugin.getInstance().getOffset() / 2) {
          final int index = mDateList.getSelectedIndex();
          if (index > 0) {
            mDateList.setSelectedIndex(index - 1);
            gotoTime(mMainPane.getShownHours() * 60);
          }
        } else if (value + mMainPane.getHorizontalScrollBar().getVisibleAmount()> mMainPane.getHorizontalScrollBar().getMaximum() - TimelinePlugin.getInstance().getOffset() / 2) {
          final int index = mDateList.getSelectedIndex();
          if (index < mDateList.getItemCount() - 1) {
            mDateList.setSelectedIndex(mDateList.getSelectedIndex() + 1);
            gotoTime(Plugin.getPluginManager().getTvBrowserSettings().getProgramTableStartOfDay());
          }
        }
      }
    };
  }

  private JPanel getInfoPanel() {
    final FormLayout layout = new FormLayout(
        "5dlu, default, 3dlu, default, 15dlu, default, 3dlu, default, 15dlu, default, 3dlu, default",
        "default, 5dlu");

    final PanelBuilder builder = new PanelBuilder(layout);
    builder.setOpaque(false);
    builder.setBorder(null);
    final CellConstraints cc = new CellConstraints();

    mDateList = new JComboBox(getDateList());
    mDateList.setSelectedIndex(1);
    mDateList.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        resetGoto();
        TimelinePlugin.getInstance().setChoosenDate(
            (Date) mDateList.getSelectedItem());
        mMainPane.updateProgram();
        mMainPane.setDateLabel((Date)mDateList.getSelectedItem());
      }
    });
    mTimeList = new JComboBox(getTimeList());
    mTimeList.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        final int index = mTimeList.getSelectedIndex();
        switch (index) {
        case 0:
          break;
        case 1:
          gotoNow();
          mLockNow = true;
          break;
        default:
          gotoTimeMiddle(mTimes[mTimeList.getSelectedIndex() - 2]);
          break;
        }
        mTimeList.setSelectedIndex(index);
      }
    });

    mFilterList = new JComboBox(Plugin.getPluginManager().getFilterManager()
        .getAvailableFilters());
    TimelinePlugin.getInstance().setFilter(
        Plugin.getPluginManager().getFilterManager().getCurrentFilter());
    mFilterList.setSelectedItem(TimelinePlugin.getInstance().getFilter());
    mFilterList.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        TimelinePlugin.getInstance().setFilter(
            (ProgramFilter) mFilterList.getSelectedItem());
        mMainPane.updateProgram();
      }
    });

    mDateLabel = builder.addLabel(mLocalizer.msg("date", "Date:"), cc.xy(2, 1));
    builder.add(mDateList, cc.xy(4, 1));
    builder.add(mTimeList, cc.xy(8, 1));
    mFilterLabel = builder.addLabel(mLocalizer.msg("filter", "Filter:"), cc.xy(10, 1));
    builder.add(mFilterList, cc.xy(12, 1));
    
    return builder.getPanel();
  }
  
  void setFilter(ProgramFilter filter) {
    //mMainPane.selectProgram(null);
    for(int i = 0; i < mFilterList.getItemCount(); i++) {
      if(mFilterList.getItemAt(i).equals(filter)) {
        mFilterList.setSelectedIndex(i);
        break;
      }
    }
    mMainPane.updateProgram();
  }
  
  public void addKeyboardAction(JRootPane rootPane) {
    // Debug Info
    rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
        KeyStroke.getKeyStroke(KeyEvent.VK_F12, 0), "debugInfo");
    rootPane.getActionMap().put("debugInfo", new AbstractAction() {
      private static final long serialVersionUID = 1L;
      
      private String formatDim(final Dimension d) {
        return "width=" + d.width + ", height=" + d.height;
      }
      
      public void actionPerformed(final ActionEvent e) {
        String info = "Viewport: "
            + formatDim(mMainPane.getViewport().getSize()) + "\n";
        info += "RowHeader: " + formatDim(mMainPane.getRowHeader().getSize())
            + "\n";
        info += "ColumnHeader: "
            + formatDim(mMainPane.getColumnHeader().getSize()) + "\n";
        info += "Offset: " + TimelinePlugin.getInstance().getOffset() + "\n";
        info += "HorizontalScrollBar: "
            + mMainPane.getHorizontalScrollBar().getMaximum() + "\n";
        info += "View: "
            + formatDim(mMainPane.getViewport().getView().getSize()) + "\n";

        JOptionPane.showMessageDialog(null, info);
      }
    });

    // goto Now
    rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
        KeyStroke.getKeyStroke(KeyEvent.VK_F9, 0), "gotoNow");
    rootPane.getActionMap().put("gotoNow", new AbstractAction() {
      private static final long serialVersionUID = 1L;

      public void actionPerformed(final ActionEvent e) {
        resetGoto();
        gotoNow();
      }
    });

    // goto Now (lock)
    rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
        KeyStroke.getKeyStroke(KeyEvent.VK_F9, InputEvent.CTRL_MASK),
        "gotoNowLock");
    rootPane.getActionMap().put("gotoNowLock", new AbstractAction() {
      private static final long serialVersionUID = 1L;

      public void actionPerformed(final ActionEvent e) {
        gotoNowLock();
      }
    });

    // goto Next Day
    rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
        KeyStroke.getKeyStroke(KeyEvent.VK_N, InputEvent.CTRL_MASK), "nextDay");
    rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
        KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_DOWN, InputEvent.CTRL_MASK),
        "nextDay");
    rootPane.getActionMap().put("nextDay", new AbstractAction() {
      private static final long serialVersionUID = 1L;

      public void actionPerformed(final ActionEvent e) {
        nextDay();
      }
    });

    // goto Previous Day
    rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
        KeyStroke.getKeyStroke(KeyEvent.VK_P, InputEvent.CTRL_MASK),
        "previousDay");
    rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
        KeyStroke.getKeyStroke(KeyEvent.VK_PAGE_UP, InputEvent.CTRL_MASK),
        "previousDay");
    rootPane.getActionMap().put("previousDay", new AbstractAction() {
      private static final long serialVersionUID = 1L;

      public void actionPerformed(final ActionEvent e) {
        previousDay();
      }
    });

    // goto 00:00
    rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
        KeyStroke.getKeyStroke(KeyEvent.VK_HOME, InputEvent.CTRL_MASK),
        "gotoBegin");
    rootPane.getActionMap().put("gotoBegin", new AbstractAction() {
      private static final long serialVersionUID = 1L;

      public void actionPerformed(final ActionEvent e) {
        gotoTime(0);
      }
    });

    // goto 23:59
    rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
        KeyStroke.getKeyStroke(KeyEvent.VK_END, InputEvent.CTRL_MASK),
        "gotoEnd");
    rootPane.getActionMap().put("gotoEnd", new AbstractAction() {
      private static final long serialVersionUID = 1L;

      public void actionPerformed(final ActionEvent e) {
        gotoTime(23 * 60 + 59);
      }
    });

    // goto Next Hour
    rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
        KeyStroke.getKeyStroke(KeyEvent.VK_RIGHT, InputEvent.CTRL_MASK),
        "nextHour");
    rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
        KeyStroke.getKeyStroke(KeyEvent.VK_KP_RIGHT, InputEvent.CTRL_MASK),
        "nextHour");
    rootPane.getActionMap().put("nextHour", new AbstractAction() {
      private static final long serialVersionUID = 1L;

      public void actionPerformed(final ActionEvent e) {
        nextHour();
      }
    });

    // goto Previous Hour
    rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
        KeyStroke.getKeyStroke(KeyEvent.VK_LEFT, InputEvent.CTRL_MASK),
        "previousHour");
    rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(
        KeyStroke.getKeyStroke(KeyEvent.VK_KP_LEFT, InputEvent.CTRL_MASK),
        "previousHour");
    rootPane.getActionMap().put("previousHour", new AbstractAction() {
      private static final long serialVersionUID = 1L;

      public void actionPerformed(final ActionEvent e) {
        previousHour();
      }
    });
  }
  
  void resetGoto() {
    if (!mIgnoreReset) {
      mTimeList.setSelectedIndex(0);
      mLockNow = false;
    }
  }
  
  private void previousDay() {
    final int index = mDateList.getSelectedIndex();
    if (index > 0) {
      mDateList.setSelectedIndex(index - 1);
    }
  }
  
  private void nextDay() {
    final int index = mDateList.getSelectedIndex();
    if (index < mDateList.getItemCount() - 1) {
      mDateList.setSelectedIndex(mDateList.getSelectedIndex() + 1);
    }
  }
  
  private void nextHour() {
    if (mMainPane.getHorizontalScrollBar().getValue()
        + mMainPane.getHorizontalScrollBar().getVisibleAmount() >= mMainPane
        .getHorizontalScrollBar().getMaximum()) {
      final int index = mDateList.getSelectedIndex();
      if (index < mDateList.getItemCount() - 1) {
        mDateList.setSelectedIndex(mDateList.getSelectedIndex() + 1);
        gotoTime(0);
      }
    } else {
      mMainPane.addTime(60);
    }
  }

  private void previousHour() {
    if (mMainPane.getHorizontalScrollBar().getValue() <= mMainPane
        .getHorizontalScrollBar().getMinimum()) {
      final int index = mDateList.getSelectedIndex();
      if (index > 0) {
        mDateList.setSelectedIndex(index - 1);
        gotoTime(24 * 60 - 1);
      }
    } else {
      mMainPane.addTime(-60);
    }
  }
  
  void gotoNowLock() {
    mTimeList.setSelectedIndex(1);
  }
  
  void scrollToTime(int minute) {
    if(TimelinePlugin.getNowMinute() == minute) {
      mTimeList.setSelectedIndex(1);
    }
    else {
      final TimeFormatter formatter = new TimeFormatter();
      
      final int h = minute / 60;
      final int m = minute % 60;
      
      String test = formatter.formatTime(h, m);
      
      for(int i = 0; i < mTimeList.getItemCount(); i++) {
        if(mTimeList.getItemAt(i).equals(test)) {
          mTimeList.setSelectedIndex(i);
          return;
        }
      }
      
      gotoTime(minute);
    }
  }
  
  void gotoTime(final int minute) {
    mMainPane.gotoTime(minute);
  }
  
  void gotoDate(Date date) {
    if(mDateList != null) {
      if(mDateList.getSelectedItem().equals(date)) {
        return;
      }
      
      for(int i = 0; i < mDateList.getItemCount(); i++) {
        if(mDateList.getItemAt(i).equals(date)) {
          mDateList.setSelectedIndex(i);
          break;
        }
      }
    }
    
    resetGoto();
    mMainPane.updateProgram();
  }

  private void gotoTimeMiddle(int minute) {
    int shownMinutes = (int) (mMainPane.getWidth() / TimelinePlugin.getInstance().getSizePerMinute());
    minute -= shownMinutes / 2;
    minute = (int) (Math.ceil(minute / 60.0) * 60);
    gotoTime(minute);
  }

  private static Vector<Date> getDateList() {
    final Vector<Date> list = new Vector<Date>();
    final Date today = Date.getCurrentDate();
    for (int i = -1; i < 28; i++) {
      list.add(today.addDays(i));
    }
    return list;
  }

  private Vector<String> getTimeList() {
    final Vector<String> list = new Vector<String>();
    list.add(mLocalizer.msg("goto", "Goto..."));
    list.add(mLocalizer.msg("now", "Now"));

    mTimes = Plugin.getPluginManager().getTvBrowserSettings()
        .getTimeButtonTimes();
    final TimeFormatter formatter = new TimeFormatter();

    for (int mTime : mTimes) {
      final int h = mTime / 60;
      final int m = mTime % 60;
      list.add(formatter.formatTime(h, m));
    }
    return list;
  }


  public void gotoNow() {
    final Date now = Date.getCurrentDate();
    if (!((Date) mDateList.getSelectedItem()).equals(now)) {
      for (int i = 0; i < mDateList.getItemCount(); i++) {
        final Date d = (Date) mDateList.getItemAt(i);
        if (d.equals(now)) {
          mDateList.setSelectedIndex(i);
          break;
        }
      }
    }
    gotoTimeMiddle(TimelinePlugin.getNowMinute());
  }

  public void resize() {
    final JScrollBar sb = mMainPane.getHorizontalScrollBar();
    mRelation = (double) sb.getValue() / (double) sb.getMaximum();
    mMainPane.resize();

    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        sb.setValue((int) (sb.getMaximum() * mRelation));
      }
    });
  }


  public void updatePersona() {
    if(Persona.getInstance().getHeaderImage() != null) {
      if(mDateLabel != null) {
        mDateLabel.setForeground(Persona.getInstance().getTextColor());
        mFilterLabel.setForeground(Persona.getInstance().getTextColor());
        mMainPane.setDateLabelColor(Persona.getInstance().getTextColor());
      }
    }
    else {
      if(mDateLabel != null) {
        mDateLabel.setForeground(UIManager.getColor("Label.foreground"));
        mFilterLabel.setForeground(mDateLabel.getForeground());
        mMainPane.setDateLabelColor(mDateLabel.getForeground());
      }
    }
  }
  
  void scrollToChannel(Channel channel) {
    mMainPane.scrollToChannel(channel);
  }
  
  void scrollToProgram(final Program prog) {
    if(prog != null) {
      if(!mDateList.getSelectedItem().equals(prog.getDate())) {
        gotoDate(prog.getDate());
      }
    }
    
    mMainPane.selectProgram(prog);
  }
}
