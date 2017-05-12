package tvbrowser.extras.reminderplugin;

import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
import java.util.ArrayList;

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
import tvbrowser.extras.reminderplugin.PanelReminder.InterfaceClose;
import util.ui.Localizer;
import util.ui.ScrollableJPanel;
import util.ui.TVBrowserIcons;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

public class FrameReminders extends JFrame implements InterfaceClose<PanelReminder>, WindowClosingIf {
  private static FrameReminders INSTANCE;
  
  private ReminderList mGlobalReminderList;
  private final ScrollableJPanel mListReminders;
  private final JScrollPane mScrollPane;
  
  private FrameReminders() {
    mListReminders = new ScrollableJPanel();
    mListReminders.setLayout(new BoxLayout(mListReminders, BoxLayout.Y_AXIS));
    mScrollPane = new JScrollPane(mListReminders);
    
    final JButton close = new JButton(Localizer.getLocalization(Localizer.I18N_CLOSE));
    close.addActionListener(e -> {
      close();
    });
    
    final JButton delete = new JButton(TVBrowserIcons.delete(TVBrowserIcons.SIZE_SMALL));
    delete.setToolTipText(Localizer.getLocalization(Localizer.I18N_DELETE));
    delete.addActionListener(e -> {
      for(int i = mListReminders.getComponentCount()-1; i >= 0; i--) {
        close((PanelReminder)mListReminders.getComponent(i), false);
      }
    });
    
    final JPanel content = new JPanel(new FormLayout("default,100dlu:grow,default","fill:100dlu:grow,5dlu,default"));
    content.setBorder(Borders.DIALOG);
    content.add(delete, CC.xy(1, 3));
    content.add(mScrollPane, CC.xyw(1, 1, 3));
    content.add(close, CC.xy(3, 3));
    
    setContentPane(content);
    getRootPane().setDefaultButton(close);
    
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
    
    Settings.layoutWindow("reminderFrameReminders", this, new Dimension(Sizes.dialogUnitXAsPixel(400, this), Sizes.dialogUnitYAsPixel(300, this)));
  }
  
  public static synchronized FrameReminders getInstance() {
    if(INSTANCE == null) {
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
  }

  @Override
  public void close(final PanelReminder item) {
    close(item, ReminderPlugin.getInstance().getSettings().getProperty(ReminderPropertyDefaults.KEY_AUTO_CLOSE_FRAME_REMINDERS_IF_EMTPY, "true").equals("true"));
  }
  
  public void close(final PanelReminder item, boolean closeFrameIfEmptry) {
    if(item != null) {
      final ReminderListItem reminder = item.getItem();
      final int minutes = item.getNextReminderTime();
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
    }
    
    if(mListReminders.getComponentCount() < 1 && ReminderPlugin.getInstance().getSettings().getProperty(ReminderPropertyDefaults.KEY_AUTO_CLOSE_FRAME_REMINDERS_IF_EMTPY, "true").equals("true")) {
      close();
    }
  }

  @Override
  public void close() {
    setVisible(false);
  }
  
  public void openShow() {
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
