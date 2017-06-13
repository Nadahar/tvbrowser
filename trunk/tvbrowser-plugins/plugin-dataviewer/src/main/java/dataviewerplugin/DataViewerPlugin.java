package dataviewerplugin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.text.DateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.Properties;

import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.BoxLayout;
import javax.swing.Icon;
import javax.swing.JDialog;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.SwingUtilities;
import javax.swing.UIManager;
import javax.swing.table.TableColumn;

import compat.PersonaCompat;
import compat.PersonaCompatListener;
import compat.PluginCompat;
import compat.UiCompat;
import util.io.IOUtilities;
import util.ui.Localizer;
import util.ui.UiUtilities;
import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
import devplugin.PluginCenterPanel;
import devplugin.PluginCenterPanelWrapper;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.ProgramFieldType;
import devplugin.SettingsTab;
import devplugin.Version;

/**
 * DataViewer for TV-Browser.
 *
 * @author René Mach
 *
 */
public final class DataViewerPlugin extends Plugin implements Runnable {

  static final Localizer mLocalizer = util.ui.Localizer
      .getLocalizerFor(DataViewerPlugin.class);

  private Object[][] mDataTable;
  private Channel[] mChannels;
  private String[] mDateString;
  private Channel[][] mChannelTable;
  private Date[] mDates;
  private Thread mThread;
  private Properties mProperties;
  private JDialog mDialog;
  private ArrayList<Program>[][] mErrData;
  private JProgressBar mProgress;
  private Date mToday = Date.getCurrentDate();
  private boolean mReactOnDataUpdate = false;
  private int mMinChannelWidth = 20;
  private int mMinRowHeight = 10;
  private int mMinColumnWidth = 20;

  private static final Version mVersion = new Version(1,21,0,true);

  private static DataViewerPlugin mInstance;

  private Font mTableFont;
  
  private PluginCenterPanelWrapper mWrapper;
  private JPanel mCenterPanelWrapper;
  private PersonaCompatListener mPersonaListener;
  /**
   * Creates an instance of this class.
   */
  public DataViewerPlugin() {
    mInstance = this;
  }

  /**
   * Gets the instance of this plugin.
   *
   * @return The instance of this plugin.
   */
  public static DataViewerPlugin getInstance() {
    return mInstance;
  }

  public static Version getVersion() {
    return mVersion;
  }

  /** Plugin Info */
  @Override
  public PluginInfo getInfo() {
    return new PluginInfo(DataViewerPlugin.class,mLocalizer.msg("data","DataViewerPlugin"),mLocalizer.msg("info",
    "Lists the available program data."), "Ren\u00e9 Mach", "GPL");
  }

  @Override
  public void onDeactivation() {
    if (mDialog != null && mDialog.isVisible()) {
      mDialog.dispose();
    }
  }
  
  public void onActivation() {
    mMinRowHeight = UIManager.getFont("Label.font").getSize()+4;
    
    SwingUtilities.invokeLater(new Runnable() {      
      public void run() {
        mCenterPanelWrapper = UiCompat.createPersonaBackgroundPanel();
        mCenterPanelWrapper.setBorder(BorderFactory.createEmptyBorder(5,5,5,5));
        mCenterPanelWrapper.setLayout(new BorderLayout());
        mWrapper = new PluginCenterPanelWrapper() {
          
          @Override
          public PluginCenterPanel[] getCenterPanels() {
            return new PluginCenterPanel[] {new DataViewerCenterPanel()};
          }
        };
        
        if(mReactOnDataUpdate && (mThread == null || !mThread.isAlive())) {
          addCenterPanel();
        }
      }
    });
  }
  
  private void startThread() {
    mThread = new Thread(this);
    mThread.setPriority(Thread.MIN_PRIORITY);
    mThread.start();
  }
  
  private void addCenterPanel() {
    SwingUtilities.invokeLater(new Runnable() {
      public void run() {
        mCenterPanelWrapper.add(getMainPanel(true),BorderLayout.CENTER);
        mCenterPanelWrapper.updateUI();
        mPersonaListener.updatePersona();
      }
    });    
  }

  @Override
  public void handleTvBrowserStartFinished() {
    mReactOnDataUpdate = true;
    startThread();
  }

  private JPanel getMainPanel(boolean addPersonaListener) {
    final JTable table = new JTable(new DataTableModel(mDataTable, mDateString));
    table.setDefaultRenderer(Object.class, new DataTableCellRenderer());
    table.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    table.setGridColor(Color.white);
    table.setIntercellSpacing(new Dimension(2, 2));
    table.getTableHeader().setReorderingAllowed(false);
    table.getTableHeader().setResizingAllowed(false);
    
    for (int i = 0; i < table.getColumnCount(); i++) {
      TableColumn col = table.getColumnModel().getColumn(i);
      col.setPreferredWidth(35);
    }

    table.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        table.getRootPane().dispatchEvent(e);
      }
    });

    String[] head = { " " };

    final JTable channels = new JTable(new DataTableModel(mChannelTable, head));
    channels.setDefaultRenderer(Object.class, new ChannelTableCellRenderer());
    channels.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    channels.setGridColor(UIManager.getColor("Table.foreground"));
    channels.setIntercellSpacing(new Dimension(0, 2));

    mMinColumnWidth = table.getFontMetrics(table.getFont()).stringWidth("pict") + 8;
    mMinChannelWidth = Math.max(channels.getPreferredSize().width-5, mMinChannelWidth);
    
    if(table.getColumnCount() > 0) {
      mMinColumnWidth = Math.max(table.getColumnModel().getColumn(0).getPreferredWidth(),mMinColumnWidth);
    }
    
    for(int i = 0; i < table.getColumnCount(); i++) {
      table.getColumnModel().getColumn(i).setMinWidth(mMinColumnWidth);
    }
    
    mMinRowHeight = Math.max(table.getRowHeight(),mMinRowHeight);
    
    table.setRowHeight(mMinRowHeight);
    channels.setRowHeight(mMinRowHeight);
    
    channels.setPreferredSize(new Dimension(mMinChannelWidth+5,10000));
    channels.setFocusable(false);

    channels.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        channels.getRootPane().dispatchEvent(e);
      }
    });
    
    final JScrollPane pane = new JScrollPane();
    pane.setViewportView(table);
    pane.setRowHeaderView(channels);
    
    if(addPersonaListener) {
      final Dimension c = table.getPreferredScrollableViewportSize();
      
      pane.addComponentListener(new ComponentAdapter() {
        public void componentResized(ComponentEvent e) {
          if(pane.getViewport().getWidth() > c.width ) {
            int columnWidth = Math.max(mMinColumnWidth, pane.getViewport().getWidth() / table.getColumnCount());
            
            for(int i = 0; i < table.getColumnCount(); i++) {
              table.getColumnModel().getColumn(i).setPreferredWidth(columnWidth);
            }          
          }
          if(pane.getViewport().getHeight() > c.height) {
            int rowHeight = Math.max(mMinRowHeight,pane.getViewport().getHeight() / table.getRowCount());
            table.setRowHeight(rowHeight);
            channels.setRowHeight(rowHeight);
          }
        }
      });
    }

    pane.getRowHeader().setPreferredSize(
        new Dimension(mMinChannelWidth+5, pane.getPreferredSize().height));
    pane.getRowHeader().setMaximumSize(pane.getRowHeader().getPreferredSize());
    pane.getRowHeader().setSize(pane.getRowHeader().getPreferredSize());
    pane.getRowHeader().setBackground(table.getBackground());
    pane.getViewport().setBackground(table.getBackground());

    JPanel dummy = new JPanel() ;
    pane.setColumnHeaderView(dummy);
    pane.getColumnHeader().setOpaque(true);
    pane.getColumnHeader().setBackground(table.getBackground());
    
    JLabel green = new JLabel(" " + mLocalizer.msg("green", "Green: "));
    JLabel orange = new JLabel(" " + mLocalizer.msg("orange", "Orange: "));
    JLabel red = new JLabel(" " + mLocalizer.msg("red", "Red: "));
    final JLabel lastDownload = new JLabel(mLocalizer.msg("last",
    "Last data download was: ")
    + mProperties.getProperty("last", mLocalizer.msg("noLast",
    "no date available")));
    lastDownload.setBorder(BorderFactory.createEmptyBorder(0, 2, 5, 0));
    lastDownload.setOpaque(false);

    green.setOpaque(true);
    orange.setOpaque(true);
    red.setOpaque(true);

    green.setBackground(DataTableCellRenderer.COMPLETE);
    green.setForeground(Color.black);
    orange.setBackground(DataTableCellRenderer.UNCOMPLETE);
    orange.setForeground(Color.black);
    red.setBackground(DataTableCellRenderer.NODATA);
    red.setForeground(Color.black);

    final JLabel green1 = new JLabel("  " + mLocalizer.msg("green.1", "Data complete."));
    final JLabel orange1 = new JLabel("  " + mLocalizer.msg("orange.1", "Uncomplete data."));
    final JLabel red1 = new JLabel("  " + mLocalizer.msg("red.1", "No data."));
    

    JPanel colors = new JPanel();
    colors.setOpaque(false);
    colors.setLayout(new BoxLayout(colors, BoxLayout.X_AXIS));
    colors.add(Box.createRigidArea(new Dimension(2, 0)));
    colors.add(green);
    colors.add(green1);
    colors.add(Box.createRigidArea(new Dimension(20, 0)));
    colors.add(orange);
    colors.add(orange1);
    colors.add(Box.createRigidArea(new Dimension(20, 0)));
    colors.add(red);
    colors.add(red1);

    table.addMouseMotionListener(new MouseMotionAdapter() {
      @Override
      public void mouseMoved(MouseEvent e) {
        StringBuilder toolTip = new StringBuilder();
        toolTip.append("<html>").append(
            mDates[table.columnAtPoint(e.getPoint())]).append(' ').append(
            channels.getValueAt(table.rowAtPoint(e.getPoint()), 0));

        ArrayList<Program> list = mErrData[table.rowAtPoint(e.getPoint())][table
            .columnAtPoint(e.getPoint())];

        if (list != null) {
          for (int i = 0; i < list.size(); i++) {
            Program p = list.get(i);
            toolTip.append("<br>").append(p.getTimeString()).append(' ')
                .append(p.getTitle());
          }
        }

        toolTip.append("</html>");

        table.setToolTipText(toolTip.toString());
      }
    });
    table.addMouseListener(new MouseAdapter() {
      @Override
      public void mousePressed(final MouseEvent e) {
        if (SwingUtilities.isLeftMouseButton(e) && e.getClickCount() >= 2) {
          String value = (String) table.getValueAt(table.rowAtPoint(e
              .getPoint()), table.columnAtPoint(e.getPoint()));
          if (value.compareToIgnoreCase("false") != 0) {
            SwingUtilities.invokeLater(new Runnable() {
              public void run() {
                ArrayList<Program> list = mErrData[table.rowAtPoint(e.getPoint())][table
                                                                      .columnAtPoint(e.getPoint())];

                if (list != null) {
                  getPluginManager().scrollToProgram(list.get(0));
                } else {
                  getPluginManager().goToDate(
                      mDates[table.columnAtPoint(e.getPoint())]);
                  getPluginManager().scrollToChannel(
                      mChannels[table.rowAtPoint(e.getPoint())]);
                }
              }
            });
          }
        }
      }
    });

    JPanel info = new JPanel(new BorderLayout());
    info.add(lastDownload, BorderLayout.NORTH);
    info.add(colors, BorderLayout.SOUTH);
    info.setOpaque(false);
    
    JPanel l = new JPanel(new BorderLayout(0, 10));
    l.setOpaque(false);
    l.add(pane, BorderLayout.CENTER);
    l.add(info, BorderLayout.SOUTH);
    
    if(addPersonaListener) {
      pane.setCorner(JScrollPane.UPPER_RIGHT_CORNER, new JPanel());
      pane.getCorner(JScrollPane.UPPER_RIGHT_CORNER).setBackground(table.getBackground());
      pane.setBorder(BorderFactory.createEmptyBorder());
      table.getTableHeader().setOpaque(false);
      table.setOpaque(false);
      pane.setOpaque(false);      
      
      mPersonaListener = new PersonaCompatListener() {
        public void updatePersona() {
          if(PersonaCompat.getInstance().getHeaderImage() != null) {
            lastDownload.setForeground(PersonaCompat.getInstance().getTextColor());
            green1.setForeground(PersonaCompat.getInstance().getTextColor());
            orange1.setForeground(PersonaCompat.getInstance().getTextColor());
            red1.setForeground(PersonaCompat.getInstance().getTextColor());
          }
        }
      };
      PersonaCompat.getInstance().registerPersonaListener(mPersonaListener);
    }
    
    return l;
  }
  
  private void showTable() {
    if (mDialog != null && mDialog.isVisible()) {
      mDialog.dispose();
    }

    JOptionPane jp = new JOptionPane();
    jp.setMessageType(JOptionPane.PLAIN_MESSAGE);
    jp.setMessage(getMainPanel(false));

    mDialog = jp.createDialog(getParentFrame(), mLocalizer.msg("data",
    "Data viewer"));
    mDialog.setResizable(true);
    mDialog.setModal(false);

    layoutWindow("dataViewerDialog", mDialog, new Dimension(620,390));

    mDialog.setVisible(true);
  }

  @Override
  public ActionMenu getButtonAction() {
    AbstractAction action = new AbstractAction() {

      private static final long serialVersionUID = 1L;

      public void actionPerformed(ActionEvent e) {
        if (mThread == null || mThread != null && mThread.isAlive() || mToday.compareTo(Date.getCurrentDate()) != 0) {
          if (mProgress == null) {
            mProgress = new JProgressBar();
          }
          mProgress.setIndeterminate(true);

          JOptionPane jp = new JOptionPane();
          jp.setMessageType(JOptionPane.PLAIN_MESSAGE);

          JLabel label = new JLabel(mLocalizer.msg("nodata",
          "Data is being listed. Please wait or retry in 10 seconds."));

          Object[] msg = { mProgress, label };

          jp.setMessage(msg);
          mDialog = jp.createDialog(getParentFrame(), mLocalizer.msg("data",
          "Data viewer"));
          mDialog.setResizable(false);
          mDialog.setModal(false);

          mDialog.setLocationRelativeTo(getParentFrame());

          mDialog.setVisible(true);

          if(mThread == null || mToday.compareTo(Date.getCurrentDate()) != 0) {
            startThread();
          }

          return;
        }
        showTable();
      }
    };
    // Name of the buttons in the menu and the icon bar
    action.putValue(Action.NAME, mLocalizer.msg("data", "Data viewer"));
    // small icon
    action.putValue(Action.SMALL_ICON, createImageIcon("actions",
        "format-justify-fill",16));
    // big icon
    action.putValue(Plugin.BIG_ICON, createImageIcon("actions",
        "format-justify-fill",22));
    return new ActionMenu(action);
  }

  @Override
  public void handleTvDataUpdateFinished() {
    if(mReactOnDataUpdate && IOUtilities.getMinutesAfterMidnight() >= 1 && IOUtilities.getMinutesAfterMidnight() < 1440) {
      mProperties.setProperty("last", DateFormat.getDateTimeInstance(
          DateFormat.FULL, DateFormat.SHORT).format(
              new java.util.Date(System.currentTimeMillis())));
      startThread();
    }
  }

  @Override
  public Properties storeSettings() {
    return mProperties;
  }

  @Override
  public void loadSettings(Properties settings) {
    if (settings == null) {
      mProperties = new Properties();
    } else {
      mProperties = settings;
    }
  }

  public void run() {
    int acceptableGap = getAcceptableGap();
    int acceptableDuration = getAcceptableDuration();

    Channel[] temp = getPluginManager().getSubscribedChannels();
    ArrayList<Channel> ch = new ArrayList<Channel>();

    // get font in UI thread
    try {
      SwingUtilities.invokeAndWait(new Runnable() {

        public void run() {
          mTableFont = new JTable().getFont();
        }
      });
    } catch (InterruptedException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    } catch (InvocationTargetException e1) {
      // TODO Auto-generated catch block
      e1.printStackTrace();
    }
    for (Channel element : temp) {
      if((element.getCategories() & (Channel.CATEGORY_CINEMA | Channel.CATEGORY_EVENTS)) == 0) {
        ch.add(element);
        mMinChannelWidth = Math.max(mMinChannelWidth,UiUtilities.getStringWidth(mTableFont,element.getName()));
      }
    }

    mChannels = ch.toArray(new Channel[ch.size()]);
    mToday = Date.getCurrentDate();

    ArrayList<String>[] channels = new ArrayList[mChannels.length];
    HashMap<Channel, HashMap<Integer, ArrayList<Program>>> err = new HashMap<Channel, HashMap<Integer, ArrayList<Program>>>();

    if (channels.length < 1) {
      return;
    }

    {
      int dateOffset = -1;
      boolean found = false;

      for (int i = 0; i < mChannels.length; i++) {
        channels[i] = new ArrayList<String>();
      }

      Program[] last = new Program[mChannels.length];

      do {
        found = false;

        Date date = mToday.addDays(dateOffset);
        for (int channelIndex = 0; channelIndex < mChannels.length; channelIndex++) {
          Iterator<Program> it = getPluginManager().getChannelDayProgram(
              date, mChannels[channelIndex]);

          boolean complete = true;
          boolean picture = false;

          short startTimeLimit = 0;
          short endTimeLimit = 0;

          try {
            Method method = mChannels[channelIndex].getClass().getMethod("getStartTimeLimit", new Class[0]);
            startTimeLimit = ((Integer)method.invoke(mChannels[channelIndex],new Object[0])).shortValue();

            method = mChannels[channelIndex].getClass().getMethod("getEndTimeLimit", new Class[0]);
            endTimeLimit = ((Integer)method.invoke(mChannels[channelIndex],new Object[0])).shortValue();
          } catch (Exception e) {}

          if (it != null && it.hasNext()) {
            found = true;

            if (last[channelIndex] == null) {
              last[channelIndex] = it.next();
            }

            while (it.hasNext()) {
              Program p1 = it.next();
              picture = picture
                  || p1.hasFieldValue(ProgramFieldType.PICTURE_TYPE);
              /*
              if(p1.hasFieldValue(ProgramFieldType.PICTURE_TYPE)) {
                ImageIcon test = new ImageIcon(p1.getBinaryField(ProgramFieldType.PICTURE_TYPE));
                
                System.out.println(test.getIconWidth() + "x"+test.getIconHeight());
              }*/

              if (dateOffset != -1) {
                int length = last[channelIndex].getStartTime() + last[channelIndex].getLength();

                if(endTimeLimit - startTimeLimit < 0) {
                  length = length >= 1440 ? length - 1440 : length;

                  if(length >= endTimeLimit && length < startTimeLimit) {
                    length = p1.getStartTime();
                  }
                }
                else if(startTimeLimit - endTimeLimit < 0) {
                  if(length >= endTimeLimit && startTimeLimit < length) {
                    length = p1.getStartTime();
                  }
                }

                length = length >= 1440 ? length - 1440 : length;

                if (p1.getStartTime() - acceptableGap > length || p1.getStartTime() + acceptableGap < length || p1.getLength() > acceptableDuration * 60) {
                  putInHashMap(err, p1, mChannels[channelIndex], channels[channelIndex].size());
                  complete = false;
                }
              }

              last[channelIndex] = p1;
            }

            if (endTimeLimit == startTimeLimit && (last[channelIndex].getStartTime() + last[channelIndex].getLength() <= last[channelIndex].getStartTime() && dateOffset != -1)) {
              putInHashMap(err, last[channelIndex], mChannels[channelIndex], channels[channelIndex].size());
              complete = false;
            }

            if (complete && dateOffset != -1) {
              channels[channelIndex].add("true" + (picture ? "pict" : ""));
            } else if (dateOffset != -1) {
              channels[channelIndex].add("uncomplete" + (picture ? "pict" : ""));
            }
          } else if (dateOffset != -1) {
            channels[channelIndex].add("false");
            last[channelIndex] = null;
          }
        }
        dateOffset++;

      } while (found || dateOffset == 0);
    }
    mDataTable = new Object[mChannels.length][channels[0].size() - 1];
    mChannelTable = new Channel[mChannels.length][1];
    mErrData = new ArrayList[mChannels.length][channels[0].size() - 1];
    mDateString = new String[channels[0].size() - 1];
    mDates = new Date[channels[0].size() - 1];

    for (int i = 0; i < mChannels.length; i++) {
      mChannelTable[i][0] = mChannels[i];

      for (int j = 0; j < channels[0].size() - 1; j++) {
        if (i == 0) {
          mDates[j] = mToday.addDays(j);
          mDateString[j] = mDates[j].getDayOfMonth()+ ".";
        }
        if (err.containsKey(mChannels[i])) {

          HashMap<Integer, ArrayList<Program>> dates = err.get(mChannels[i]);

          if (dates.containsKey(j)) {
            mErrData[i][j] = dates.get(j);
          } else {
            mErrData[i][j] = null;
          }
        } else {
          mErrData[i][j] = null;
        }
        mDataTable[i][j] = channels[i].get(j);
      }
    }
    mCenterPanelWrapper.removeAll();
    addCenterPanel();
    
    if (mProgress != null) {
      mProgress.setIndeterminate(false);
      if (mDialog != null && mDialog.isVisible()) {
        SwingUtilities.invokeLater(new Runnable() {

          public void run() {
            showTable();
          }
        });
      }
    }
  }

  private static void putInHashMap(HashMap<Channel, HashMap<Integer, ArrayList<Program>>> map, Program p, Channel channel, Integer n) {

    if (map.containsKey(channel)) {
      HashMap<Integer, ArrayList<Program>> dates = map.get(channel);

      if (dates.containsKey(n)) {
        ArrayList<Program> list = dates.get(n);
        list.add(p);
      } else {
        ArrayList<Program> list = new ArrayList<Program>();
        list.add(p);
        dates.put(n, list);
      }
    } else {
      HashMap<Integer, ArrayList<Program>> dates = new HashMap<Integer, ArrayList<Program>>();
      ArrayList<Program> list = new ArrayList<Program>();
      list.add(p);
      dates.put(n, list);
      map.put(channel, dates);
    }
  }

  int getAcceptableGap() {
    return Integer.parseInt(mProperties.getProperty("acceptableGap","1"));
  }

  int getAcceptableDuration() {
	return Integer.parseInt(mProperties.getProperty("acceptableDuration","15"));
  }

  void setAcceptableDuration(int duration) {
	    if(Integer.parseInt(mProperties.getProperty("acceptableDuration","15")) != duration) {
	      mProperties.setProperty("acceptableDuration", String.valueOf(duration));

	      new Thread("Wait for table") {
	        @Override
	        public void run() {
	          while(mThread == null || mThread.isAlive()) {
	            try {
	              Thread.sleep(200);
	            }catch(Exception e) {}
	          }

	          startThread();
	        }
	      }.start();

	    }
	  }

  void setAcceptableGap(int gap) {
    if(Integer.parseInt(mProperties.getProperty("acceptableGap","1")) != gap) {
      mProperties.setProperty("acceptableGap", String.valueOf(gap));

      new Thread("Wait for table") {
        @Override
        public void run() {
          while(mThread == null || mThread.isAlive()) {
            try {
              Thread.sleep(200);
            }catch(Exception e) {}
          }

          startThread();
        }
      }.start();

    }
  }

  Icon getIcon() {
    return createImageIcon("actions",
        "format-justify-fill",16);
  }

  @Override
  public SettingsTab getSettingsTab() {
    return new DataViewerPluginSettingsTab();
  }
  
  public String getPluginCategory() {
    return PluginCompat.CATEGORY_OTHER;
  }
  
  public PluginCenterPanelWrapper getPluginCenterPanelWrapper() {
    return mWrapper;
  }
  
  private class DataViewerCenterPanel extends PluginCenterPanel {

    @Override
    public String getName() {
      // TODO Auto-generated method stub
      return mLocalizer.msg("data", "Data viewer");
    }

    @Override
    public JPanel getPanel() {
      // TODO Auto-generated method stub
      return mCenterPanelWrapper;
    }
    
  }
}
