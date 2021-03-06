package tvbrowser.extras.reminderplugin;

import java.awt.Color;
import java.awt.Graphics;

import javax.swing.JButton;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JSeparator;
import javax.swing.SwingConstants;
import javax.swing.Timer;

import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;

import devplugin.Date;
import devplugin.Program;
import tvbrowser.core.Settings;
import util.io.IOUtilities;
import util.settings.PluginPictureSettings;
import util.settings.ProgramPanelSettings;
import util.ui.Localizer;
import util.ui.ProgramPanel;
import util.ui.ScrollableJPanel;
import util.ui.UiUtilities;

public class PanelReminder extends ScrollableJPanel {
  private ReminderListItem mItem;
  
  /**
   * label showing the textual program state ("has just begun", "already finished")
   */

  private JLabel mHeader;
  private JLabel mCommentLabel;
  private ProgramPanel mProgramPanel;
  private JComboBox<RemindValue> mReminderCB;
  private JButton mCloseBt;
  private String mCloseBtText;

  private Timer mAutoCloseTimer;
  private int mRemainingSecs;
  
  private int mRemainingMinutes;
  private InterfaceClose<PanelReminder> mCloseInterface;
  private int mRunningMinutes;

  private int mOriginalMinutes;
  
  private final FormLayout mLayout;
  private final JPanel mContentPanel;
  
  /**
   * automatically close when current time is larger than this point in time
   */
  private long mAutoCloseAtMillis;
  
  public PanelReminder(final ReminderListItem reminderListItem, final InterfaceClose<PanelReminder> closeInterface) {
    mItem = reminderListItem;
    mCloseInterface = closeInterface;
    mRemainingMinutes = 0;
    mCloseBtText = Localizer.getLocalization(Localizer.I18N_CLOSE);
    mOriginalMinutes = reminderListItem.getMinutes();
    
    final Program program = mItem.getProgram();

    mLayout = new FormLayout("5dlu,default:grow,20dlu,default,5dlu","5dlu,default,default,5dlu,default,5dlu,default");
    
    mContentPanel = new JPanel(mLayout) {
      @Override
      protected void paintComponent(Graphics g) {
        Color c = g.getColor();
        
        g.setColor(getBackground());
        
        g.fillRect(0, 0, getWidth(), getHeight());
        
        g.setColor(c);
        
        super.paintComponent(g);
      }
    };
    
    setLayout(new FormLayout("default:grow","default"));
    add(mContentPanel, CC.xy(1, 1));
    setOpaque(true);
    mContentPanel.setOpaque(false);
    
    new Thread("SHOW NEW REMINDER THREAD") {
      @Override
      public void run() {
        int opacity = 200;
        int count = 0;
        Color background = mContentPanel.getBackground();
        
        while(opacity >= 0) {
          mContentPanel.setBackground(new Color(238, 118, 0, Math.max(0, opacity)));
          
          if(count > 166) {
            opacity--;
          }
          else {
            count++;
          }
          
          try {
            sleep(30);
          } catch (InterruptedException e) {
            e.printStackTrace();
          }
        }
        
        mContentPanel.setBackground(background);
      };
    }.start();
    
    mHeader = new JLabel();
    mProgramPanel = new ProgramPanel(program,
        new ProgramPanelSettings(new PluginPictureSettings(
            PluginPictureSettings.ALL_PLUGINS_SETTINGS_TYPE), false,
            ProgramPanelSettings.X_AXIS, false, true, false));
    mProgramPanel.addPluginContextMenuMouseListener(ReminderPluginProxy
        .getInstance());
    mReminderCB = new JComboBox<>();
    
    final int progMinutesAfterMidnight = program.getStartTime();
    int minutesAfterMidnight = IOUtilities.getMinutesAfterMidnight() + 1440
        * (program.getDate().getNumberOfDaysSince(Date.getCurrentDate()));
    
    mRunningMinutes = 0;
    final Date today = Date.getCurrentDate();
    
    String msg = null;
    
    if(program.isOnAir()) {
      mRunningMinutes = Math.max(mRunningMinutes, (minutesAfterMidnight - progMinutesAfterMidnight));
    }
    else if(program.isExpired()) {
      mRunningMinutes = -1;
    }
    
    mRemainingSecs = ReminderConstants.getAutoCloseReminderTime(program);
    mAutoCloseAtMillis = System.currentTimeMillis() + 1000 * mRemainingSecs;
    
    int seconds = mRemainingSecs;
    if (ReminderPlugin.getInstance().getSettings().getProperty(ReminderPropertyDefaults.KEY_SHOW_TIME_COUNTER,"false").compareTo("true") == 0) {
      seconds = 10;
    }
    
    mCloseBt = new JButton(getCloseButtonText(seconds));
    mCloseBt.setToolTipText(ReminderPlugin.LOCALIZER.msg("closeHint", "<html><b>Ctrl+Esc:</b> Close first Reminder and use selected value<br><b>Shift+Esc:</b> Close first Reminder and reschedule at next possible time<html>"));
    mCloseBt.addActionListener(e -> {
      closeInterface.close(this);
    });
    
    if (today.compareTo(program.getDate()) >= 0
        && minutesAfterMidnight > progMinutesAfterMidnight) {
      msg = updateRunningTime();
    } else {
      msg = ReminderFrame.LOCALIZER.msg("soonStarts", "Soon starts");
      mRemainingMinutes = ReminderPlugin.getTimeToProgramStart(program);
    }
    
    if(msg != null) {
      mHeader.setText(msg);
    }

    updateCloseBtText();
    mAutoCloseTimer = new Timer(1000, e -> {
        handleTimerEvent();
      }
    );
    mAutoCloseTimer.start();
    
    final JPanel channelPanel = new JPanel(new FormLayout("50dlu:grow,5dlu,default","fill:0dlu:grow,default,default,default,fill:0dlu:grow"));
    channelPanel.add(mProgramPanel, CC.xywh(1, 1, 1, 5));
    channelPanel.setOpaque(false);
    
    if (program.getLength() > 0) {
      final JLabel endTime = new JLabel(ReminderFrame.LOCALIZER.msg("endTime",
          "until {0}", program.getEndTimeString()));
      channelPanel.add(endTime, CC.xy(3, 2));
    }
    
    String sortNumber = "";
    
    if(Settings.propShowSortNumberInProgramLists.getBoolean() && program.getChannel().getSortNumber().trim().length() > 0) {
      sortNumber = program.getChannel().getSortNumber() + ". ";
    }
    
    String channelName = sortNumber + program.getChannel().getName();
    JLabel channelLabel = new JLabel();
    channelLabel.setToolTipText(channelName);
    channelLabel.setIcon(UiUtilities.createChannelIcon(program.getChannel()
        .getIcon()));
    channelLabel.setHorizontalTextPosition(SwingConstants.RIGHT);
    channelPanel.add(channelLabel, CC.xy(3, 3));
    channelLabel = new JLabel(channelName);
    channelPanel.add(channelLabel, CC.xy(3, 4));
    
    mContentPanel.add(mHeader, CC.xyw(2, 2, 3));
    mContentPanel.add(channelPanel, CC.xyw(2, 3, 3));
    
    int y = addComment(5);
    
    mContentPanel.add(mReminderCB, CC.xy(2, y));
    mContentPanel.add(mCloseBt, CC.xy(4, y++));
    mContentPanel.add(new JSeparator(JSeparator.HORIZONTAL), CC.xyw(1, ++y, 5));
    
    int i=0;
    
    while (i < ReminderConstants.REMIND_AFTER_VALUE_ARR.length && mRunningMinutes-Math.abs(ReminderConstants.REMIND_AFTER_VALUE_ARR[i].getMinutes()) < 0) {
      if(mRunningMinutes >= 0 && Math.abs(ReminderConstants.REMIND_AFTER_VALUE_ARR[i].getMinutes()) < program.getLength()) {
        mReminderCB.addItem(ReminderConstants.REMIND_AFTER_VALUE_ARR[i]);
      }
      
      i++;
    }
    
    mReminderCB.addItem(ReminderConstants.DONT_REMIND_AGAIN_VALUE);
    mReminderCB.setSelectedItem(ReminderConstants.DONT_REMIND_AGAIN_VALUE);
    
    i = 0;
    
    while (i < ReminderConstants.REMIND_BEFORE_VALUE_ARR.length
        && ReminderConstants.REMIND_BEFORE_VALUE_ARR[i].getMinutes() < mRemainingMinutes) {
      mReminderCB.addItem(ReminderConstants.REMIND_BEFORE_VALUE_ARR[i++]);
    }
    // don't show reminder selection if it contains only the
    // entry "don't remind me"
    mReminderCB.setVisible(mReminderCB.getItemCount() > 1);
    
    updateRunningTime();
  }
  
  private int addComment(int y) {
    String comment = mItem.getComment();
    
    if (comment != null && comment.length() > 0) {
      if(mCommentLabel == null) {
        mLayout.insertRow(y-1, RowSpec.decode("default"));
        mLayout.insertRow(y-1, RowSpec.decode("2dlu"));
        
        mCommentLabel = new JLabel(comment);
        
        mContentPanel.add(mCommentLabel, CC.xyw(2, y, 3));
        
        y += 2;
      }
      else {
        mCommentLabel.setText(comment);
      }
    }
    else if(mCommentLabel != null) {
      mContentPanel.remove(mCommentLabel);
      mLayout.removeRow(5);
      mLayout.removeRow(4);
      mCommentLabel = null;
    }
    
    return y;
  }
  
  private int mLastUpdateMinute;
  
  /**
   * Although this is called once a second, we don't want to count each second
   * individually. If the whole system is under heavy load, or hibernated the
   * gaps between two calls may increase dramatically. The interval of 1000 ms
   * only ensures, that this is not polled more often than once a second.
   */
  private void handleTimerEvent() {
    mRemainingSecs = Math.max(0, (int)(mAutoCloseAtMillis - System.currentTimeMillis()) / 1000);

    if (mRemainingSecs <= 0) {
      if(!ReminderPlugin.getInstance().getSettings().getProperty(ReminderPropertyDefaults.KEY_AUTO_CLOSE_BEHAVIOUR,ReminderPropertyDefaults.VALUE_AUTO_CLOSE_BEHAVIOUR_ON_END).equals(ReminderPropertyDefaults.VALUE_AUTO_CLOSE_BEHAVIOUR_ON_NEVER)) {
        mCloseInterface.close(this);
      }
      else {
        stopTimer();
        updateRunningTime();
      }
    } else {
      updateCloseBtText();
      updateRunningTime();
    }
    
    if(mReminderCB.getItemCount() > 1 && mLastUpdateMinute != IOUtilities.getMinutesAfterMidnight()) {
      mLastUpdateMinute = IOUtilities.getMinutesAfterMidnight();
      
      final int diff = mLastUpdateMinute  - (mItem.getProgram().getStartTime() + 1440
          * (mItem.getProgram().getDate().getNumberOfDaysSince(Date.getCurrentDate())));
      
      if(!mItem.getProgram().isOnAir() && !mItem.getProgram().isExpired()) {
        mRemainingMinutes = ReminderPlugin.getTimeToProgramStart(mItem.getProgram());
      }
      else {
        mRemainingMinutes = 0;
      }
      
      final int selectedIndex = mReminderCB.getSelectedIndex();
      RemindValue selectedValue = (RemindValue)mReminderCB.getSelectedItem();
      
      for(int i = mReminderCB.getItemCount()-1; i >= 0; i--) {
        final RemindValue test = mReminderCB.getItemAt(i);
        
        if(test.getMinutes() != ReminderConstants.DONT_REMIND_AGAIN && 
            ((test.getMinutes() >= 0 && mRemainingMinutes <= test.getMinutes()) ||
            (diff >= Math.abs(test.getMinutes())))) {
          if(selectedValue != null && test.equals(selectedValue)) {
            selectedValue = null;
          }
          mReminderCB.removeItemAt(i);
        }
      }
      
      if(selectedValue == null) {
        mReminderCB.setSelectedIndex(Math.min(selectedIndex, mReminderCB.getItemCount()-1));
      }
      
      if(mReminderCB.getItemCount() == 1) {
        mReminderCB.setVisible(false);
      }
    }
  }
  
  private void updateCloseBtText() {
    if(mRemainingSecs <= 10 || ReminderPlugin.getInstance().getSettings().getProperty(ReminderPropertyDefaults.KEY_SHOW_TIME_COUNTER,"false").compareTo("true") == 0) {
      mCloseBt.setText(getCloseButtonText(mRemainingSecs));
    }
  }
  
  private String getCloseButtonText(int seconds) {
    final StringBuilder builder = new StringBuilder(mCloseBtText);
    
    if(!ReminderPlugin.getInstance().getSettings().getProperty(ReminderPropertyDefaults.KEY_AUTO_CLOSE_BEHAVIOUR,ReminderPropertyDefaults.VALUE_AUTO_CLOSE_BEHAVIOUR_ON_END).equals(ReminderPropertyDefaults.VALUE_AUTO_CLOSE_BEHAVIOUR_ON_NEVER) && (mRemainingSecs <= 10 || ReminderPlugin.getInstance().getSettings().getProperty("showTimeCounter","false").compareTo("true") == 0)) {
      builder.append(" (");
      if (seconds <= 60) {
        builder.append(seconds);
      }
      else {
        if (seconds >= 3600) {
          final int hours = seconds / 3600;
          builder.append(hours).append(":");
          seconds = seconds - 3600 * hours;
        }
        final int minutes = seconds / 60;
        if (minutes < 10 && minutes > -10) {
          builder.append("0");
        }
        builder.append(minutes).append(":");
        seconds = seconds - 60 * minutes;
        if (seconds < 10 && seconds > -10) {
          builder.append("0");
        }
        builder.append(seconds);
      }
      builder.append(")");
    }
    
    return builder.toString();
  }
  
  private String updateRunningTime() {
    String msg = null;

    // find first still running program, hide all other programs
    final ReminderListItem reminder = mItem;
    
    final Program program = reminder.getProgram();
    
    if (program.isOnAir()) {
      final int progMinutesAfterMidnight = program.getHours() * 60
          + program.getMinutes();
      int minutesRunning = IOUtilities.getMinutesAfterMidnight() - progMinutesAfterMidnight;
      if (minutesRunning < 0) {
        minutesRunning += 24 * 60;
      }
      if (minutesRunning == 0) {
        msg = ReminderFrame.LOCALIZER.msg("alreadyRunning", "Just started");
      }
      else if (minutesRunning == 1) {
        msg = ReminderFrame.LOCALIZER.msg("alreadyRunningMinute", "Already running {0} minute", minutesRunning);
      }
      else {
        msg = ReminderFrame.LOCALIZER.msg("alreadyRunningMinutes", "Already running {0} minutes", minutesRunning);
      }
    } else if (program.isExpired()) {
      msg = ReminderFrame.LOCALIZER.msg("ended", "Program elapsed");
    }
    else {
      msg = ReminderFrame.LOCALIZER.msg("soonStarts", "Soon starts");
    }

    if (mHeader != null) {
      mHeader.setText(msg);
    }
    return msg;
  }
  
  public static interface InterfaceClose<T> {
    public void close(T item);
  }
  
  public ReminderListItem getItem() {
    return mItem;
  }
  
  public int getNextReminderTime(boolean ignoreNoReminderIfSelected) {
    int minutes = ((RemindValue)mReminderCB.getSelectedItem()).getMinutes();
    
    if(ignoreNoReminderIfSelected && minutes == ReminderConstants.DONT_REMIND_AGAIN) {
      for(int i = mReminderCB.getItemCount()-1; i >= 0; i--) {
        if(mReminderCB.getItemAt(i).getMinutes() != ReminderConstants.DONT_REMIND_AGAIN) {
          minutes = mReminderCB.getItemAt(i).getMinutes();
          break;
        }
      }
    }
    
    return minutes;
  }
  
  public void stopTimer() {
    if(mAutoCloseTimer != null && mAutoCloseTimer.isRunning()) {
      mAutoCloseTimer.stop();
    }
  }
  
  public boolean update() {
    boolean result = false;
    
    if(mOriginalMinutes != mItem.getMinutes()) {
      for(int i = 0; i < mReminderCB.getItemCount(); i++) {
        final RemindValue value = mReminderCB.getItemAt(i);
        
        if(value.getMinutes() == mItem.getMinutes()) {
          mReminderCB.setSelectedItem(value);
          break;
        }
      }
      
      result = true;
    }
    else {
      addComment(5);
      
      repaint();
      revalidate();
    }
    
    return result;
  }
  
  public boolean containsItem(ReminderListItem item) {
    return mItem.equals(item);
  }
}
