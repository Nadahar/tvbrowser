package dataviewerplugin;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ComponentAdapter;
import java.awt.event.ComponentEvent;
import java.awt.event.KeyAdapter;
import java.awt.event.KeyEvent;
import java.awt.event.MouseAdapter;
import java.awt.event.MouseEvent;
import java.awt.event.MouseMotionAdapter;
import java.awt.event.WindowAdapter;
import java.awt.event.WindowEvent;
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
import javax.swing.table.TableColumn;

import util.io.IOUtilities;
import util.ui.Localizer;
import util.ui.UiUtilities;
import devplugin.ActionMenu;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Plugin;
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
  private int mMinChannelWidth = 0;

  private static final Version mVersion = new Version(1,10);

  private static DataViewerPlugin mInstance;

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
    "Lists the available program data."), "René Mach", "GPL");
  }

  @Override
  public void onDeactivation() {
    if (mDialog != null && mDialog.isVisible()) {
      mDialog.dispose();
    }
  }

  @Override
  public void handleTvBrowserStartFinished() {
    mReactOnDataUpdate = true;
    mThread = new Thread(this);
    mThread.setPriority(Thread.MIN_PRIORITY);
    mThread.start();
  }

  private void showTable() {
    if (mDialog != null && mDialog.isVisible()) {
      mDialog.dispose();
    }

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
    channels.setGridColor(Color.WHITE);
    channels.setIntercellSpacing(new Dimension(0, 2));

    mMinChannelWidth = Math.max(channels.getPreferredSize().width-5, mMinChannelWidth);

    channels.setPreferredSize(new Dimension(mMinChannelWidth+5,channels.getPreferredSize().height));
    channels.setMaximumSize(channels.getPreferredSize());
    channels.setFocusable(false);

    channels.addKeyListener(new KeyAdapter() {
      @Override
      public void keyPressed(KeyEvent e) {
        channels.getRootPane().dispatchEvent(e);
      }
    });

    JScrollPane pane = new JScrollPane();
    pane.setViewportView(table);
    pane.setRowHeaderView(channels);

    pane.getRowHeader().setPreferredSize(
        new Dimension(mMinChannelWidth+5, pane.getPreferredSize().height));
    pane.getRowHeader().setMaximumSize(pane.getRowHeader().getPreferredSize());
    pane.getRowHeader().setSize(pane.getRowHeader().getPreferredSize());
    pane.getRowHeader().setBackground(Color.WHITE);

    pane.getViewport().setBackground(table.getBackground());

    JLabel green = new JLabel(mLocalizer.msg("green", "Green: "));
    JLabel orange = new JLabel(mLocalizer.msg("orange", "Orange: "));
    JLabel red = new JLabel(mLocalizer.msg("red", "Red: "));
    JLabel lastDownload = new JLabel(mLocalizer.msg("last",
    "Last data download was: ")
    + mProperties.getProperty("last", mLocalizer.msg("noLast",
    "no date available")));
    lastDownload.setBorder(BorderFactory.createEmptyBorder(0, 2, 5, 0));

    green.setOpaque(true);
    orange.setOpaque(true);
    red.setOpaque(true);

    green.setBackground(DataTableCellRenderer.COMPLETE);
    orange.setBackground(DataTableCellRenderer.UNCOMPLETE);
    red.setBackground(DataTableCellRenderer.NODATA);

    JLabel green1 = new JLabel("  "
        + mLocalizer.msg("green.1", "Data complete."));
    JLabel orange1 = new JLabel("  "
        + mLocalizer.msg("orange.1", "Uncomplete data."));
    JLabel red1 = new JLabel("  " + mLocalizer.msg("red.1", "No data."));

    JPanel colors = new JPanel();
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
        String toolTip = "<html>" + mDates[table.columnAtPoint(e.getPoint())]
                                           + " " + channels.getValueAt(table.rowAtPoint(e.getPoint()), 0);

        ArrayList<Program> list = mErrData[table.rowAtPoint(e.getPoint())][table
                                                              .columnAtPoint(e.getPoint())];

        if (list != null) {
          for (int i = 0; i < list.size(); i++) {
            Program p = list.get(i);

            toolTip += "<br>" + p.getTimeString() + " " + p.getTitle();
          }
        }

        toolTip += "</html>";

        table.setToolTipText(toolTip);
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

    JPanel l = new JPanel(new BorderLayout(0, 10));
    l.add(pane, BorderLayout.CENTER);
    l.add(info, BorderLayout.SOUTH);

    JOptionPane jp = new JOptionPane();
    jp.setMessageType(JOptionPane.PLAIN_MESSAGE);
    jp.setMessage(l);

    mDialog = jp.createDialog(getParentFrame(), mLocalizer.msg("data",
    "Data viewer"));
    mDialog.setResizable(true);
    mDialog.setModal(false);

    if (!mProperties.isEmpty()) {
      try {
        int x = Integer.parseInt(mProperties.getProperty("xpos", "0"));
        int y = Integer.parseInt(mProperties.getProperty("ypos", "0"));
        int width = Integer.parseInt(mProperties.getProperty("width", "620"));
        int height = Integer.parseInt(mProperties.getProperty("height", "390"));

        mDialog.setBounds(x, y, width, height);
      } catch (Exception ee) {}
    } else {
      mDialog.setSize(620, 390);
      mDialog.setLocationRelativeTo(getParentFrame());
    }

    mDialog.setVisible(true);
    mDialog.addComponentListener(new ComponentAdapter() {
      @Override
      public void componentHidden(ComponentEvent e) {
        savePosition();
      }
    });

    mDialog.addWindowListener(new WindowAdapter() {
      @Override
      public void windowClosing(WindowEvent e) {
        savePosition();
      }
    });
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
            handleTvBrowserStartFinished();
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

  private void savePosition() {
    mProperties.setProperty("xpos", String.valueOf(mDialog.getX()));
    mProperties.setProperty("ypos", String.valueOf(mDialog.getY()));
    mProperties.setProperty("width", String.valueOf(mDialog.getWidth()));
    mProperties.setProperty("height", String.valueOf(mDialog.getHeight()));
  }

  @Override
  public void handleTvDataUpdateFinished() {
    if(mReactOnDataUpdate && IOUtilities.getMinutesAfterMidnight() >= 1 && IOUtilities.getMinutesAfterMidnight() < 1440) {
      mProperties.setProperty("last", DateFormat.getDateTimeInstance(
          DateFormat.FULL, DateFormat.SHORT).format(
              new java.util.Date(System.currentTimeMillis())));
      handleTvBrowserStartFinished();
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

    for (Channel element : temp) {
      if((element.getCategories() & (Channel.CATEGORY_CINEMA | Channel.CATEGORY_EVENTS)) == 0) {
        ch.add(element);
        mMinChannelWidth = Math.max(mMinChannelWidth,UiUtilities.getStringWidth(new JTable().getFont(),element.getName()));
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
      int j = -1;
      boolean found = false;

      for (int i = 0; i < mChannels.length; i++) {
        channels[i] = new ArrayList<String>();
      }

      Program[] last = new Program[mChannels.length];

      do {
        found = false;

        for (int i = 0; i < mChannels.length; i++) {
          Iterator<Program> it = getPluginManager().getChannelDayProgram(
              Date.getCurrentDate().addDays(j), mChannels[i]);

          boolean complete = true;
          boolean picture = false;

          short startTimeLimit = 0;
          short endTimeLimit = 0;

          try {
            Method method = mChannels[i].getClass().getMethod("getStartTimeLimit", new Class[0]);
            startTimeLimit = ((Integer)method.invoke(mChannels[i],new Object[0])).shortValue();

            method = mChannels[i].getClass().getMethod("getEndTimeLimit", new Class[0]);
            endTimeLimit = ((Integer)method.invoke(mChannels[i],new Object[0])).shortValue();
          } catch (Exception e) {}

          if (it != null && it.hasNext()) {
            found = true;

            if (last[i] == null) {
              last[i] = it.next();
            }

            while (it.hasNext()) {
              Program p1 = it.next();
              picture = picture
                  || p1.hasFieldValue(ProgramFieldType.PICTURE_TYPE);

              if (j != -1) {
                int length = last[i].getStartTime() + last[i].getLength();

                if(endTimeLimit - startTimeLimit < 0) {
                  length = length >= 1439 ? length - 1439 : length;

                  if(length >= endTimeLimit && length < startTimeLimit) {
                    length = p1.getStartTime();
                  }
                }
                else if(startTimeLimit - endTimeLimit < 0) {
                  if(length >= endTimeLimit && startTimeLimit < length) {
                    length = p1.getStartTime();
                  }
                }

                length = length >= 1439 ? length - 1439 : length;

                if (p1.getStartTime() - acceptableGap > length || p1.getStartTime() + acceptableGap < length || p1.getLength() > acceptableDuration * 60) {
                  putInHashMap(err, p1, mChannels[i], channels[i].size());
                  complete = false;
                }
              }

              last[i] = p1;
            }

            if (endTimeLimit == startTimeLimit && (last[i].getStartTime() + last[i].getLength() <= last[i].getStartTime() && j != -1)) {
              putInHashMap(err, last[i], mChannels[i], channels[i].size());
              complete = false;
            }

            if (complete && j != -1) {
              channels[i].add("true" + (picture ? "pict" : ""));
            } else if (j != -1) {
              channels[i].add("uncomplete" + (picture ? "pict" : ""));
            }
          } else if (j != -1) {
            channels[i].add("false");
            last[i] = null;
          }
        }
        j++;

      } while (found || j == 0);
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
          mDates[j] = Date.getCurrentDate().addDays(j);
          mDateString[j] = Date.getCurrentDate().addDays(j).getDayOfMonth()
          + ".";
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

    if (mProgress != null) {
      mProgress.setIndeterminate(false);
      if (mDialog != null && mDialog.isVisible()) {
        showTable();
      }
    }
  }

  private void putInHashMap(HashMap<Channel, HashMap<Integer, ArrayList<Program>>> map, Program p, Channel channel, Integer n) {

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

  protected int getAcceptableGap() {
    return Integer.parseInt(mProperties.getProperty("acceptableGap","1"));
  }

  protected int getAcceptableDuration() {
	return Integer.parseInt(mProperties.getProperty("acceptableDuration","15"));
  }
  
  protected void setAcceptableDuration(int duration) {
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

	          handleTvBrowserStartFinished();
	        }
	      }.start();

	    }
	  }
  
  protected void setAcceptableGap(int gap) {
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

          handleTvBrowserStartFinished();
        }
      }.start();

    }
  }

  protected Icon getIcon() {
    return createImageIcon("actions",
        "format-justify-fill",16);
  }

  @Override
  public SettingsTab getSettingsTab() {
    return new DataViewerPluginSettingsTab();
  }
}
