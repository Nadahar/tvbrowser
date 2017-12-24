package tvbrowser.extras.reminderplugin;

import java.awt.Container;
import java.awt.Dimension;
import java.awt.GraphicsEnvironment;
import java.awt.Insets;
import java.awt.Rectangle;
import java.awt.Toolkit;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentEvent;
import java.awt.event.ComponentListener;
import java.awt.event.KeyEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;
import java.util.concurrent.atomic.AtomicBoolean;

import javax.swing.AbstractAction;
import javax.swing.BoxLayout;
import javax.swing.JButton;
import javax.swing.JComponent;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.KeyStroke;
import javax.swing.SwingUtilities;

import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.factories.CC;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;

import devplugin.Program;
import tvbrowser.TVBrowser;
import tvbrowser.core.Settings;
import tvbrowser.core.icontheme.IconLoader;
import tvbrowser.extras.reminderplugin.PanelReminder.InterfaceClose;
import util.ui.Localizer;
import util.ui.ScrollableJPanel;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

public class FrameReminders extends JFrame implements InterfaceClose<PanelReminder>, WindowClosingIf {
  private static final String ID_WINDOW = "reminderFrameReminders";
  
  private static FrameReminders INSTANCE;
  
  private ReminderList mGlobalReminderList;
  private final ScrollableJPanel mListReminders;
  private final JScrollPane mScrollPane;
  
  private final JButton mReschedule;
  private final JButton mDelete;

  private Thread mThreadUpdateHeight;

  private AtomicBoolean mAutoResize = null;
  
  private FrameReminders() {
    mListReminders = new ScrollableJPanel();
    mListReminders.setLayout(new BoxLayout(mListReminders, BoxLayout.Y_AXIS));
    mScrollPane = new JScrollPane(mListReminders);
    
    final JButton close = new JButton(Localizer.getLocalization(Localizer.I18N_CLOSE));
    close.addActionListener(e -> {
      close();
    });
    
    mReschedule = new JButton(IconLoader.getInstance().getIconFromTheme("actions", "appointment-new", 16));
    mReschedule.setEnabled(false);
    mReschedule.setToolTipText(ReminderPlugin.LOCALIZER.msg("reschedule", "Close current Reminders and reschedule possible Reminders. (Ctrl+Enter)"));
    mReschedule.addActionListener(e -> {
      for(int i = mListReminders.getComponentCount()-1; i >= 0; i--) {
        close((PanelReminder)mListReminders.getComponent(i), false, true);
      }
      
      if(ReminderPlugin.getInstance().getSettings().getProperty(ReminderPropertyDefaults.KEY_AUTO_CLOSE_FRAME_REMINDERS_IF_EMTPY, "true").equals("true")) {
        close();
      }
    });
    
    mDelete = new JButton(TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL));
    mDelete.setEnabled(false);
    mDelete.setToolTipText(Localizer.getLocalization(Localizer.I18N_DELETE)+ReminderPlugin.LOCALIZER.msg("delete", " (Ctrl+Delete)"));
    mDelete.addActionListener(e -> {
      for(int i = mListReminders.getComponentCount()-1; i >= 0; i--) {
        close((PanelReminder)mListReminders.getComponent(i), false, false);
      }
      
      if(ReminderPlugin.getInstance().getSettings().getProperty(ReminderPropertyDefaults.KEY_AUTO_CLOSE_FRAME_REMINDERS_IF_EMTPY, "true").equals("true")) {
        close();
      }
    });
    
    final JPanel content = new JPanel(new FormLayout("default,100dlu:grow,default,5dlu,default","fill:50dlu:grow,5dlu,default"));
    content.setBorder(Borders.DIALOG);
    content.add(mScrollPane, CC.xyw(1, 1, 5));
    content.add(mDelete, CC.xy(1, 3));
    content.add(mReschedule, CC.xy(3, 3));
    content.add(close, CC.xy(5, 3));
    
    setContentPane(content);
    getRootPane().setDefaultButton(close);
    
    addComponentListener(new ComponentListener() {
      private Thread mSavePosWait;
      private AtomicBoolean mWaitSavePos = new AtomicBoolean(false);
      
      private Thread mSaveSizeWait;
      private AtomicBoolean mWaitSaveSize = new AtomicBoolean(false);
      
      @Override
      public void componentShown(ComponentEvent e) {
        updateHeight();
        savePos(e);
      }
      
      @Override
      public void componentResized(ComponentEvent e) {
        saveSize(e);
      }
      
      @Override
      public void componentMoved(ComponentEvent e) {
        savePos(e);
      }
      
      @Override
      public void componentHidden(ComponentEvent e) {
        savePos(e);
      }
      
      private void savePos(ComponentEvent e) {
        mWaitSavePos.set(true);
        
        if(mSavePosWait == null || !mSavePosWait.isAlive()) {
          mSavePosWait = new Thread("SAVE WINDOW POSITION WAITING THREAD") {
            @Override
            public void run() {
              while(mWaitSavePos.getAndSet(false)) {
                try {
                  sleep(100);
                } catch (InterruptedException e) {
                  // ignore
                }
              }
              
              if((getExtendedState() & JFrame.MAXIMIZED_BOTH) != JFrame.MAXIMIZED_BOTH) {
                ReminderPlugin.getInstance().getSettings().setProperty(ReminderPropertyDefaults.KEY_FRAME_REMINDERS_XPOS, String.valueOf(e.getComponent().getX()));
                ReminderPlugin.getInstance().saveSettings();
              }
            }
          };
          mSavePosWait.start();
        }                  
      }
      
      private void saveSize(ComponentEvent e) {
        mWaitSaveSize.set(true);
        
        if(mSaveSizeWait == null || !mSaveSizeWait.isAlive()) {
          mSaveSizeWait = new Thread("SAVE WINDOW SIZE WAITING THREAD") {
            @Override
            public void run() {
              while(mWaitSaveSize.getAndSet(false)) {
                try {
                  sleep(100);
                } catch (InterruptedException e) {
                  // ignore
                }
              }
              
              if((getExtendedState() & JFrame.MAXIMIZED_BOTH) != JFrame.MAXIMIZED_BOTH) {
                ReminderPlugin.getInstance().getSettings().setProperty(ReminderPropertyDefaults.KEY_FRAME_REMINDERS_WIDTH, String.valueOf(e.getComponent().getWidth()));
                ReminderPlugin.getInstance().saveSettings();
              }
            }
          };
          mSaveSizeWait.start();
        }
      }
    });
    
    addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        close();
      }
    });
        
    setTitle(ReminderFrame.LOCALIZER.msg("title2", "Current Reminders"));
    setIconImages(TVBrowser.ICONS_WINDOW);
    setDefaultCloseOperation(JFrame.DO_NOTHING_ON_CLOSE);
    UiUtilities.registerForClosing(this);
    
    final KeyStroke stroke = ReminderPlugin.getKeyStrokeFrameReminders();
    
    StringBuilder key = new StringBuilder(); 
    key.append(String.valueOf(stroke.getKeyCode()));
    key.append("_");
    key.append(String.valueOf(stroke.getModifiers()));
    
    rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(stroke, key.toString());
    rootPane.getActionMap().put(key.toString(), new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        close();
      }
    });
    
    final KeyStroke closeReminderInOrder = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, KeyEvent.ALT_DOWN_MASK | KeyEvent.CTRL_DOWN_MASK);
    
    rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(closeReminderInOrder, "close_reminder_in_order");
    rootPane.getActionMap().put("close_reminder_in_order", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if(mListReminders.getComponentCount() > 0) {
          close((PanelReminder)mListReminders.getComponent(0));
        }
      }
    });
    
    final KeyStroke closeReminderInOrderAndReschedule = KeyStroke.getKeyStroke(KeyEvent.VK_ESCAPE, KeyEvent.SHIFT_DOWN_MASK);
    
    rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(closeReminderInOrderAndReschedule, "close_reminder_in_order_and_reschedule");
    rootPane.getActionMap().put("close_reminder_in_order_and_reschedule", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        if(mListReminders.getComponentCount() > 0) {
          close((PanelReminder)mListReminders.getComponent(0),true,true);
        }
      }
    });
    
    final KeyStroke closeAndReschedule = KeyStroke.getKeyStroke(KeyEvent.VK_ENTER, KeyEvent.CTRL_DOWN_MASK);
    
    rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(closeAndReschedule, "close_reminder_and_reschedule");
    rootPane.getActionMap().put("close_reminder_and_reschedule", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        mReschedule.doClick();
      }
    });
    
    final KeyStroke deleteAndClose = KeyStroke.getKeyStroke(KeyEvent.VK_DELETE, KeyEvent.CTRL_DOWN_MASK);
    
    rootPane.getInputMap(JComponent.WHEN_IN_FOCUSED_WINDOW).put(deleteAndClose, "delete_and_close_reminder");
    rootPane.getActionMap().put("delete_and_close_reminder", new AbstractAction() {
      @Override
      public void actionPerformed(ActionEvent e) {
        mDelete.doClick();
      }
    });
    
    Settings.layoutWindow(ID_WINDOW, this, new Dimension(Sizes.dialogUnitXAsPixel(400, this), Sizes.dialogUnitYAsPixel(300, this)));
    
    updateWindowSettings();
  }
  
  public void updateWindowSettings() {
    final boolean autoResize = ReminderPlugin.getInstance().getSettings().getProperty(ReminderPropertyDefaults.KEY_AUTO_RESIZE_ENABLED, ReminderPropertyDefaults.getPropertyDefaults().getDefaultValueForKey(ReminderPropertyDefaults.KEY_AUTO_RESIZE_ENABLED)).equals("false");
    
    if(mAutoResize == null || mAutoResize.get() != autoResize) {
      if(mAutoResize == null) {
        mAutoResize = new AtomicBoolean(autoResize);
      }
      else {
        mAutoResize.set(autoResize);
      }
      
      if(autoResize) {
        Settings.updateWindowSettings(ID_WINDOW, new Dimension(Sizes.dialogUnitXAsPixel(400, this), Sizes.dialogUnitYAsPixel(300, this)), false);
      }
      else {
        int width = Integer.parseInt(ReminderPlugin.getInstance().getSettings().getProperty(ReminderPropertyDefaults.KEY_FRAME_REMINDERS_WIDTH, String.valueOf(getWidth())));
        
        Settings.updateWindowSettings(ID_WINDOW, new Dimension(Sizes.dialogUnitXAsPixel(400, this), Sizes.dialogUnitYAsPixel(300, this)), true);
        pack();
        setSize(width, getHeight());
        
        setLocation(Integer.parseInt(ReminderPlugin.getInstance().getSettings().getProperty(ReminderPropertyDefaults.KEY_FRAME_REMINDERS_XPOS,"0")), Toolkit.getDefaultToolkit().getScreenSize().height-10);
        
        if(isVisible()) {
          updateHeight();
        }
      }
    }
  }
  
  private void updateButtons() {
    mReschedule.setEnabled(mListReminders.getComponentCount() > 0);
    mDelete.setEnabled(mListReminders.getComponentCount() > 0);
    
    if(isVisible()) {
      updateHeight();
    }
  }
  
  public static synchronized FrameReminders getInstance() {
    return getInstance(true);
  }
  
  public static synchronized FrameReminders getInstance(boolean create) {
    if(INSTANCE == null && create) {
      INSTANCE = new FrameReminders();
    }
    
    return INSTANCE;
  }
  
  public void addReminders(final ReminderList list, final ArrayList<ReminderListItem> items) {
    
    try {
      mGlobalReminderList = list;
      
      for(int i = items.size()-1; i >= 0; i--) {
        final ReminderListItem item = items.get(i);
        
        mGlobalReminderList.blockProgram(item.getProgram());
        
        mListReminders.add(new PanelReminder(item, this), 0);
      }
      
      if(!isVisible()) {
        setVisible(true);
      }
      
      SwingUtilities.invokeLater(() -> {
    	  if((getExtendedState() & JFrame.ICONIFIED) == JFrame.ICONIFIED) {
    	    setExtendedState(JFrame.NORMAL);
    	  }
    	
    	  mScrollPane.getVerticalScrollBar().setValue(0);
    	  mListReminders.repaint();
    	  mListReminders.revalidate();
      });
      
      if(ReminderPlugin.getInstance().getSettings().getProperty(ReminderPropertyDefaults.KEY_FRAME_REMINDERS_TO_FRONT_WHEN_REMINDER_ADDED,"false").equals("true")) {
        SwingUtilities.invokeLater(() -> {
          setExtendedState(getExtendedState());
          setAlwaysOnTop(true);
          toFront();
          requestFocus();
          setAlwaysOnTop(false);
        });
      }
    }catch(Throwable t) {
      t.printStackTrace();
    }
    
    updateButtons();
  }

  @Override
  public void close(final PanelReminder item) {
    close(item, ReminderPlugin.getInstance().getSettings().getProperty(ReminderPropertyDefaults.KEY_AUTO_CLOSE_FRAME_REMINDERS_IF_EMTPY, "true").equals("true"), false);
  }
  
  public void close(final PanelReminder item, boolean closeFrameIfEmptry, boolean reschedule) {
    if(item != null) {
      final ReminderListItem reminder = item.getItem();
      final int minutes = item.getNextReminderTime(reschedule);
      item.stopTimer();
      
      mListReminders.remove(item);
      
      if(isVisible()) {
        mListReminders.repaint();
        mListReminders.revalidate();
      }
      
      mGlobalReminderList.removeWithoutChecking(reminder.getProgramItem());
      if (minutes != ReminderConstants.DONT_REMIND_AGAIN) {
        Program program = reminder.getProgram();
        mGlobalReminderList.add(program, new ReminderContent(minutes, reminder
            .getComment()));
        mGlobalReminderList.unblockProgram(program);
      }
      
      SwingUtilities.invokeLater(() -> {
        ReminderPlugin.getInstance().updateRootNode(true);
      });
      
      ReminderListDialog.updateReminderList();
    }
    
    updateButtons();
    
    if(mListReminders.getComponentCount() < 1 && closeFrameIfEmptry) {
      close();
    }
  }
  
  public void updateReminder(ReminderListItem item) {
    boolean repaint = false;
    
    for(int i = 0; i < mListReminders.getComponentCount(); i++) {
      PanelReminder panel = (PanelReminder)mListReminders.getComponent(i);
      
      if(panel.containsItem(item) && panel.update()) {
        panel.stopTimer();
        mListReminders.remove(panel);
        
        mGlobalReminderList.removeWithoutChecking(item.getProgramItem());
        if (item.getMinutes() != ReminderConstants.DONT_REMIND_AGAIN) {
          Program program = item.getProgram();
          mGlobalReminderList.add(program, new ReminderContent(item.getMinutes(), item
              .getComment()));
          mGlobalReminderList.unblockProgram(program);
        }
        
        repaint = true;
      }
    }
    
    if(repaint && isVisible()) {
      mListReminders.repaint();
      mListReminders.revalidate();
    }
    
    if(mListReminders.getComponentCount() < 1 && ReminderPlugin.getInstance().getSettings().getProperty(ReminderPropertyDefaults.KEY_AUTO_CLOSE_FRAME_REMINDERS_IF_EMTPY, "true").equals("true")) {
      close();
    }
  }
  
  public void removeReminder(ReminderListItem item) {
    for(int i = 0; i < mListReminders.getComponentCount(); i++) {
      PanelReminder panel = (PanelReminder)mListReminders.getComponent(i);
      
      if(panel.containsItem(item)) {
        panel.stopTimer();
        mListReminders.remove(panel);
      }
    }
    
    if(isVisible()) {
      mListReminders.repaint();
      mListReminders.revalidate();
      mScrollPane.repaint();
      mScrollPane.revalidate();
    }
    
    updateButtons();
    
    if(mListReminders.getComponentCount() < 1 && ReminderPlugin.getInstance().getSettings().getProperty(ReminderPropertyDefaults.KEY_AUTO_CLOSE_FRAME_REMINDERS_IF_EMTPY, "true").equals("true")) {
      close();
    }
  }

  @Override
  public void close() {
    setVisible(false);
  }
  
  private synchronized void updateHeight() {
    if (ReminderPropertyDefaults.getPropertyDefaults().getValueFromProperties(ReminderPropertyDefaults.KEY_AUTO_RESIZE_ENABLED,ReminderPlugin.getInstance().getSettings()).equals("true") && (mThreadUpdateHeight == null || !mThreadUpdateHeight.isAlive())) {
      mThreadUpdateHeight = new Thread("UPDATE HEIGHT THREAD") {
        @Override
        public void run() {
          try {
            sleep(100);
          } catch (InterruptedException e) {/* Ignore */}

          for (int g = 0; g < 2; g++) {
            int heightWithoutScroll = 0;

            Container c = getContentPane();

            int height = c.getInsets().top + c.getInsets().bottom;
            
            int space = 0;

            for (int i = 1; i < c.getComponentCount(); i++) {
              Insets insets = ((JComponent) c.getComponent(i)).getInsets();
              space = Math.max(space,
                  ((JComponent) c.getComponent(i)).getPreferredSize().height + insets.top + insets.bottom);
            }

            heightWithoutScroll = height + space;
            
            if(mListReminders.getComponentCount() > 0) {
              Dimension scrnSize = Toolkit.getDefaultToolkit().getScreenSize();
              Rectangle winSize = GraphicsEnvironment.getLocalGraphicsEnvironment().getMaximumWindowBounds();
              
              final Insets insets = getInsets();
              
              int maxWindowHeight = winSize.height;
              int titleBarHeight = insets.top + insets.bottom;
              int taskBarHeight = scrnSize.height - winSize.height;
              int remindersHeight = 0;
              
              for(int i = 0; i < mListReminders.getComponentCount(); i++) {
                remindersHeight += Math.max(mListReminders.getComponent(i).getPreferredSize().height, mListReminders.getComponent(i).getHeight());
              }
              
              int futureHeight = remindersHeight+heightWithoutScroll+titleBarHeight+3;
              
              if(futureHeight < maxWindowHeight) {
                setSize(Math.max(Sizes.dialogUnitXAsPixel(400, FrameReminders.this),getWidth()), futureHeight);
              }
              else {
                setSize(Math.max(Sizes.dialogUnitXAsPixel(400, FrameReminders.this),getWidth()), maxWindowHeight + insets.bottom + (insets.top == 0 ? -30 : 0));
              }
              
              int xPos = Integer.parseInt(ReminderPlugin.getInstance().getSettings().getProperty(ReminderPropertyDefaults.KEY_FRAME_REMINDERS_XPOS,"0"));
              int winBorder = xPos - winSize.width + getWidth();
              
              if(winBorder == 0) {
            	  xPos = winSize.width - getWidth() + insets.right - 1;
              }
              else if(xPos <= insets.left) {
                xPos = 0 - insets.left+1;
              }
              
              if(ReminderPropertyDefaults.getPropertyDefaults().getValueFromProperties(ReminderPropertyDefaults.KEY_AUTO_RESIZE_TYPE, ReminderPlugin.getInstance().getSettings()).equals(ReminderPropertyDefaults.VALUE_AUTO_RESIZE_TYPE_TOP)) {
                setLocation(xPos, winSize.y);
              }
              else {
                int y = scrnSize.height-getHeight()+insets.bottom;
                
                if((scrnSize.height - winSize.y) != winSize.height) {
                  y -= taskBarHeight;
                }
                
                setLocation(xPos, y);
              }
            }
            
            try {
              sleep(500);
            } catch (InterruptedException e) {/* Ignore */}
          }
        }
      };
      mThreadUpdateHeight.setPriority(Thread.MIN_PRIORITY);
      mThreadUpdateHeight.start();
    }
  }
  
  public void openShow() {
    updateButtons();
    
    if(!isVisible()) {
  	  setVisible(true);
  	}
  	      
  	SwingUtilities.invokeLater(() -> {
  	  if((getExtendedState() & JFrame.ICONIFIED) == JFrame.ICONIFIED) {
  	    setExtendedState(JFrame.NORMAL);
  	  }
  	  
      setExtendedState(getExtendedState());
      setAlwaysOnTop(true);
      toFront();
      requestFocus();
      setAlwaysOnTop(false);
  	});
  }
}
