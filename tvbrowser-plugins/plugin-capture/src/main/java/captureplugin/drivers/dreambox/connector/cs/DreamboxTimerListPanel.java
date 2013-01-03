package captureplugin.drivers.dreambox.connector.cs;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.Insets;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.BorderFactory;
import javax.swing.ImageIcon;
import javax.swing.JButton;
import javax.swing.JCheckBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextArea;
import javax.swing.ListSelectionModel;
import javax.swing.RowFilter;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;
import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.codec.binary.Base64;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import util.ui.Localizer;
import captureplugin.drivers.dreambox.DreamboxConfig;
import captureplugin.drivers.dreambox.connector.DreamboxConnector;
import captureplugin.drivers.dreambox.connector.DreamboxStateHandler;
import captureplugin.drivers.utils.TimeDateSpinner;

/**
 * Timer-Liste anzeigen
 * 
 */
@SuppressWarnings("serial")
public class DreamboxTimerListPanel extends JPanelRefreshAbstract implements
    ActionListener, TableModelListener {

  /**
   * TimerList TitleCellrenderer
   */
  private class TitleCellRenderer extends DefaultTableCellRenderer {

    public TitleCellRenderer() {
    }

    /**
     * HTML fuer div style
     * 
     * @param s
     * @param style
     * 
     * @return div
     */
    private String fmtStyle(String s, String style) {
      return "<div style=\"" + style + "\">" + s + "</div>";
    }

    /**
     * HTML fuer ToolTip
     * 
     * @param s
     * @return
     */
    private String fmtToolTip(String s) {
      s = s.replaceAll("( / |\\n)", "<br /><br />");
      return "<html>" + "<body>" + fmtStyle(s, "padding:3px;") + "</body>"
          + "</html>";
    }

    /**
     * AfterEvent als Text
     * 
     * @param afterEvent
     * @return
     */
    private String getAfterEventText(int afterEvent) {
      String afterEventText = "";
      switch (afterEvent) {
      case 0:
        afterEventText = mLocalizer.msg("afterEventNothing", "Nothing");
        break;
      case 1:
        afterEventText = mLocalizer.msg("afterEventStandby", "Standby");
        break;
      case 2:
        afterEventText = mLocalizer
            .msg("afterEventDeepstandby", "Deep Standby");
        break;
      case 3:
        afterEventText = mLocalizer.msg("afterEventAuto", "Auto");
        break;
      }
      return " [ " + afterEventText + " ]";
    }

    /**
     * Repeated als Text
     * 
     * @param repeated
     * @return
     */
    private String getRepeatedText(int repeated) {
      String s = "";
      for (int i = 0; i < 7; i++) {
        int pot2 = Integer.rotateLeft(1, i);
        if ((repeated & pot2) == pot2) {
          s += "," + WEEKDAYS[i];
        }
      }
      s = (s.length() > 0) ? s.substring(1) : s;
      s = " [ " + s + " ]";
      return s;
    }

    /**
     * 
     */
    @SuppressWarnings("unchecked")
    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column) {

      // Timerwerte ermitteln
      Map<String, String> timer = (Map<String, String>) table.getModel()
          .getValueAt(table.convertRowIndexToModel(row), COL_TIMER);
      String description = timer.get(E2TimerHelper.DESCRIPTION);
      int repeated = Integer.parseInt(timer.get(E2TimerHelper.REPEATED));
      boolean disabled = timer.get(E2TimerHelper.DISABLED).equals("1");
      int justplay = Integer.parseInt(timer.get(E2TimerHelper.JUSTPLAY));
      int afterEvent = Integer.parseInt(timer.get(E2TimerHelper.AFTEREVENT));
      String location = timer.get(E2TimerHelper.LOCATION);
      Calendar timeBeg = E2TimerHelper.getAsCalendar(timer
          .get(E2TimerHelper.TIMEBEGIN));
      Calendar timeEnd = E2TimerHelper.getAsCalendar(timer
          .get(E2TimerHelper.TIMEEND));
      Calendar timeNow = GregorianCalendar.getInstance();
      String repeatedText = getRepeatedText(repeated);

      // color
      if (isSelected) {
        setForeground(table.getSelectionForeground());
        setBackground(table.getSelectionBackground());
      } else {
        setForeground(table.getForeground());
        setBackground(table.getBackground());
      }

      // Text
      String text = (value == null) ? "" : value.toString();
      if (repeated > 0) {
        text = text + repeatedText;
      }
      setText(text);

      long minutes = (timeEnd.getTimeInMillis() - timeBeg.getTimeInMillis()) / 1000 / 60;

      // ToolTip
      setToolTipText(getToolTipText(text, description, repeated, justplay,
          afterEvent, location, minutes));

      // Icon
      setIconTextGap(8);
      if (timeNow.getTimeInMillis() > timeEnd.getTimeInMillis()) {
        // abgelaufene kennzeichnen
        setIcon(new ImageIcon(getClass().getResource("images/expired.gif")));
      } else if (repeated > 0) {
        // wiederholungen
        setIcon(new ImageIcon(getClass().getResource("images/repeated.gif")));
      } else if (timeNow.getTimeInMillis() > timeBeg.getTimeInMillis()) {
        // laufene kennzeichnen
        setIcon(new ImageIcon(getClass().getResource("images/activ.gif")));
      } else if (justplay > 0) {
        // zap kennzeichnen
        setIcon(new ImageIcon(getClass().getResource("images/zap.gif")));
      } else {
        // other
        setIcon(null);
      }

      // Farben
      if (disabled) {
        // disabled kennzeichnen
        setForeground(new Color(245, 164, 000));
      } else if (timeNow.getTimeInMillis() > timeEnd.getTimeInMillis()) {
        // abgelaufene kennzeichnen
        setForeground(Color.gray);
      } else if (timeNow.getTimeInMillis() > timeBeg.getTimeInMillis()) {
        // laufene kennzeichnen
        setForeground(new Color(000, 164, 000));
      } else if (justplay > 0) {
        // zap kennzeichnen
        setForeground(new Color(200, 000, 000));
      } else if (repeated > 0) {
        // wiederholungen
        setForeground(Color.blue);
      } else {
        // other
        setForeground(Color.black);
      }

      // adjustment
      setHorizontalTextPosition(JLabel.LEFT);

      return this;
    }

    /**
     * ToolTip erzeugen
     * 
     * @param title
     * @param description
     * @param repeated
     * @param justplay
     * @param afterEvent
     * @param location
     * @param minutes
     * @return tooltip
     */
    private String getToolTipText(String title, String description,
        int repeated, int justplay, int afterEvent, String location,
        long minutes) {

      // ToolTip aufbauen
      String toolTipText = fmtStyle(title, "color:"
          + ((justplay == 0) ? "black" : "red")
          + "; font-weight:bold; font-size:medium;");
      // Beschreibung
      if ((description != null) && (description.length() > 0)) {
        toolTipText += fmtStyle(description,
            "padding-top:5px; color:black; width:250px; font-size:small;");
      }
      // Dauer
      toolTipText += fmtStyle(String.format("%d min", minutes),
          "padding-top:1px; color:black; font-size:x-small;");
      // AfterEvent
      String afterEventText = getAfterEventText(afterEvent);
      toolTipText += fmtStyle(mLocalizer.msg("afterEventText",
          "After Recording")
          + ": " + afterEventText,
          "padding-top:3px; color:green; font-size:x-small;");
      // Wiederholung
      if (repeated > 0) {
        toolTipText += fmtStyle(mLocalizer.msg("Repeating", "Repeating") + ": "
            + getRepeatedText(repeated),
            "padding-top:0px; color:blue; font-size:x-small;");
      }
      // Aufzeichnungsort
      toolTipText += fmtStyle(mLocalizer.msg("Location", "Location") + ": [ "
          + location + " ]", "padding-top:0px; color:gray; font-size:x-small;");
      // HTML
      return fmtToolTip(toolTipText);
    }
  }

  // Spaltennummern
  private static final int COL_NO = 0;
  private static final int COL_DATE = 1;
  private static final int COL_WEEKDAY = 2;
  private static final int COL_FROM = 3;
  private static final int COL_UNTIL = 4;
  private static final int COL_TITLE = 5;
  static final int COL_ACTION_DISABLE = 6;
  static final int COL_ACTION_EDIT = 7;
  static final int COL_ACTION_DELETE = 8;
  private static final int COL_SERVICE = 9;
  private static final int COL_TAGS = 10;
  static final int COL_TIMER = 11;

  // Logger
  private static final Logger mLog = Logger
      .getLogger(DreamboxTimerListPanel.class.getName());
  // Translator
  private static final Localizer mLocalizer = Localizer
      .getLocalizerFor(DreamboxTimerListPanel.class);

  // Konstanten
  private static final String CMD_URL_MENU = "UrlMenu";
  private static final String CMD_DELETE_BTN = "DeleteBtn";
  private static final String CMD_DELETE_MENU = "DeleteMenu";
  private static final String CMD_EDIT_BTN = "EditBtn";
  private static final String CMD_EDIT_MENU = "EditMenu";
  private static final String CMD_CLEANUP_MENU = "CleanMenu";
  private static final String CMD_FILTER_CHANGED = "FilterChanged";

  private static final String WEEKDAYS[] = new String[] {
      mLocalizer.msg("Mon", "Mon"), mLocalizer.msg("Tue", "Tue"),
      mLocalizer.msg("Wed", "Wed"), mLocalizer.msg("Thu", "Thu"),
      mLocalizer.msg("Fri", "Fri"), mLocalizer.msg("Sat", "Sat"),
      mLocalizer.msg("Sun", "Sun") };

  // Member
  private DreamboxConnector mConnector = null;
  private final E2TimerHelper mTimerHelper;
  private final JCheckBox mCbShowZap = new JCheckBox(mLocalizer.msg("showZap",
      "Show Zap-Timer"));
  private TableRowSorter<TableModel> mRowSorter;

  /**
   * Konstruktor
   * 
   * @param connector
   * @param timerHelper
   */
  public DreamboxTimerListPanel(DreamboxConnector connector,
      E2TimerHelper timerHelper) {
    super();
    mLog.setLevel(Level.INFO);    
    this.mConnector = connector;
    this.mTimerHelper = timerHelper;
    this.setPreferredSize(new Dimension(800, 600));
    this.add(new JLabel(mLocalizer.msg("panel", "Reading Timers ...")));
    // Edit
    JMenuItem menuItemEdit = new JMenuItem(mLocalizer.msg("EditTimer", "Edit"));
    menuItemEdit.setActionCommand(CMD_EDIT_MENU);
    menuItemEdit.addActionListener(this);
    menuItemEdit.setIcon(new ImageIcon(getClass()
        .getResource("images/edit.gif")));
    mPopupMenu.insert(menuItemEdit, 0);
    // Delete
    JMenuItem menuItemDelete = new JMenuItem(Localizer
        .getLocalization(Localizer.I18N_DELETE));
    menuItemDelete.setActionCommand(CMD_DELETE_MENU);
    menuItemDelete.addActionListener(this);
    menuItemDelete.setIcon(new ImageIcon(getClass().getResource(
        "images/delete.gif")));
    mPopupMenu.insert(menuItemDelete, 1);
    // zelluloid
    JMenuItem menuItemUrl = new JMenuItem("zelluliod.de");
    menuItemUrl.setActionCommand(CMD_URL_MENU);
    menuItemUrl.addActionListener(this);
    menuItemUrl.setIcon(new ImageIcon(getClass()
        .getResource("images/paste.gif")));
    mPopupMenu.insert(menuItemUrl, 2);
    // Separator
    mPopupMenu.insert(new JSeparator(), 3);
    // Clean Timer
    JMenuItem menuItemClean = new JMenuItem(mLocalizer.msg("CleanupTimer",
        "Cleanup"));
    menuItemClean.setActionCommand(CMD_CLEANUP_MENU);
    menuItemClean.addActionListener(this);
    menuItemClean.setIcon(new ImageIcon(getClass().getResource(
        "images/cleanup.gif")));
    mPopupMenu.insert(menuItemClean, 4);
  }

  /**
   * delegate actionPerformed
   */
  @Override
  public void actionPerformedDelegate(ActionEvent ae) {

    if (ae.getActionCommand().equals(CMD_EDIT_MENU)) {
      int modelRow = mTable.convertRowIndexToModel(mTable.getSelectedRow());
      rowTimerEdit(modelRow);

    } else if (ae.getActionCommand().equals(CMD_EDIT_BTN)) {
      int modelRow = mTable.convertRowIndexToModel(mTable.getEditingRow());
      rowTimerEdit(modelRow);

    } else if (ae.getActionCommand().equals(CMD_DELETE_MENU)) {
      int modelRow = mTable.convertRowIndexToModel(mTable.getSelectedRow());
      rowTimerDelete(modelRow);

    } else if (ae.getActionCommand().equals(CMD_DELETE_BTN)) {
      int modelRow = mTable.convertRowIndexToModel(mTable.getEditingRow());
      rowTimerDelete(modelRow);

    } else if (ae.getActionCommand().equals(CMD_URL_MENU)) {
      int modelRow = mTable.convertRowIndexToModel(mTable.getSelectedRow());
      cmdUrl(modelRow);

    } else if (ae.getActionCommand().equals(CMD_CLEANUP_MENU)) {
      cmdCleanup();

    } else if (ae.getActionCommand().equals(CMD_FILTER_CHANGED)) {
      cmdFilter();

    } else {
      mLog.info(ae.getActionCommand());
    }
  }

  private void cmdFilter() {
    mRowSorter.sort();
  }

  /**
   * remove expired timers
   */
  private void cmdCleanup() {
    Calendar cal = new GregorianCalendar();

    String data = "";
    try {
      String userpassword = mConnector.getConfig().getUserName() + ":"
          + mConnector.getConfig().getPassword();
      String encoded = new String(Base64.encodeBase64(userpassword.getBytes()));

      URL url = new URL("http://" + mConnector.getConfig().getDreamboxAddress()
          + "/web/timercleanup?cleanup=true");
      URLConnection connection = url.openConnection();
      connection.setRequestProperty("Authorization", "Basic " + encoded);
      connection.setConnectTimeout(mConnector.getConfig().getTimeout());
      InputStream stream = connection.getInputStream();
      byte[] buf = new byte[1024];
      int len;
      while ((len = stream.read(buf)) != -1) {
        data += new String(buf, 0, len, "UTF-8");
      }
      stream.close();

      DreamboxStateHandler handler = new DreamboxStateHandler();
      SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
      saxParser.parse(new InputSource(new StringReader(data)), handler);

      mLog.info("[" + mConnector.getConfig().getDreamboxAddress() + "] " + "CLEANUP - " + handler.getStatetext() + " - "
          + (new GregorianCalendar().getTimeInMillis() - cal.getTimeInMillis())
          + " ms");

      mTimerHelper.refresh();
      refresh();

    } catch (UnsupportedEncodingException e) {
      mLog.log(Level.WARNING, "UnsupportedEncodingException", e);
    } catch (MalformedURLException e) {
      mLog.log(Level.WARNING, "MalformedURLException", e);
    } catch (IOException e) {
      mLog.log(Level.WARNING, "IOException", e);
    } catch (ParserConfigurationException e) {
      mLog.log(Level.WARNING, "ParserConfigurationException", e);
    } catch (SAXException e) {
      mLog.warning(data);
      mLog.log(Level.WARNING, "SAXException", e);
    }
  }

  /**
   * Url lesen
   */
  private void cmdUrl(int modelRow) {
    String title = (String) mTable.getModel().getValueAt(modelRow, COL_TITLE);
    try {
      String url = "http://zelluloid.de/suche/index.php3?qstring="
          + URLEncoder.encode(title, "UTF-8");
      Desktop.getDesktop().browse(new URI(url));
    } catch (IOException e) {
      e.printStackTrace();
    } catch (URISyntaxException e) {
      e.printStackTrace();
    }
  }

  @SuppressWarnings("unchecked")
  private void rowTimerEdit(int modelRow) {
    // Werte aus Model holen
    DreamboxTimerTableModel model = (DreamboxTimerTableModel) mTable.getModel();
    Map<String, String> oldTimer = (Map<String, String>) model.getValueAt(
        modelRow, COL_TIMER);

    GridBagConstraints c = new GridBagConstraints();
    c.gridx = 0;
    c.gridy = 0;
    c.insets = new Insets(0, 2, 5, 2);
    c.fill = GridBagConstraints.HORIZONTAL;

    JPanel programPanel = new JPanel(new GridBagLayout());
    programPanel.add(new JLabel(oldTimer.get(E2TimerHelper.NAME)), c);
    programPanel.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg(
        "Title", "Title")));

    JPanel descrPanel = new JPanel(new GridBagLayout());
    JTextArea area = new JTextArea();
    area.setWrapStyleWord(true);
    area.setLineWrap(true);
    area.setText(oldTimer.get(E2TimerHelper.DESCRIPTION));
    area.setFont(area.getFont().deriveFont(14.0f));
    area.setColumns(200);
    area.setRows(2);
    JScrollPane scrollPane = new JScrollPane(area);
    scrollPane.setPreferredSize(new Dimension(360, 130));
    descrPanel.add(scrollPane, c);
    descrPanel.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg(
        "Description", "Description")));

    Calendar beginTime = E2TimerHelper.getAsCalendar(oldTimer
        .get(E2TimerHelper.TIMEBEGIN));

    Calendar endTime = E2TimerHelper.getAsCalendar(oldTimer
        .get(E2TimerHelper.TIMEEND));

    TimeDateSpinner beginSpinner = new TimeDateSpinner(beginTime.getTime());
    beginSpinner.setPreferredSize(new Dimension(145, beginSpinner
        .getPreferredSize().height));
    JPanel beginPanel = new JPanel(new GridBagLayout());
    beginPanel.add(beginSpinner, c);
    beginPanel.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg(
        "StartTime", "Start-Time")));

    JPanel endPanel = new JPanel(new GridBagLayout());
    TimeDateSpinner endSpinner = new TimeDateSpinner(endTime.getTime());
    endSpinner.setPreferredSize(new Dimension(145, endSpinner
        .getPreferredSize().height));
    endPanel.add(endSpinner, c);
    endPanel.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg(
        "EndTime", "End-Time")));

    DreamboxConfig config = mConnector.getConfig();

    String serviceRef = oldTimer.get(E2TimerHelper.SERVICEREFERENCE);
    boolean useHdService = E2ServiceHelper.isHdService(serviceRef);
    ProgramOptionPanel pgmOptPanel = new ProgramOptionPanel(E2LocationHelper
        .getInstance(config, null), E2MovieHelper.getInstance(config, null));

    pgmOptPanel.setRepeated(Integer.parseInt(oldTimer
        .get(E2TimerHelper.REPEATED)));
    pgmOptPanel.setSelectedAfterEvent(Integer.parseInt(oldTimer
        .get(E2TimerHelper.AFTEREVENT)));
    pgmOptPanel.setSelectedTag(oldTimer.get(E2TimerHelper.TAGS));
    pgmOptPanel.setSelectedLocation(oldTimer.get(E2TimerHelper.LOCATION));
    pgmOptPanel.setBorder(BorderFactory.createTitledBorder(mLocalizer.msg(
        "afterEventTitle", "afterEventTitle")));
    pgmOptPanel.setZapBeforeEvent(mTimerHelper.indexOfTimer(mTimerHelper
        .createZapBeforeTimer(oldTimer)) != -1);
    pgmOptPanel.hideOnlyCreateZapTimer();
    pgmOptPanel.setUseHdService(useHdService);
    // SD-Sender ermitteln
    String serviceRefSD = E2ServiceHelper.getServiceRef(serviceRef, false);
    if (!E2ServiceHelper.hasHdService(serviceRefSD)) {
      pgmOptPanel.hideUseHdService();
    }

    JPanel editPanel = new JPanel(new GridBagLayout());

    c.gridx = 0;
    c.gridy = 0;
    editPanel.add(programPanel, c);
    c.gridy++;
    editPanel.add(descrPanel, c);
    c.gridy++;
    editPanel.add(beginPanel, c);
    c.gridy++;
    editPanel.add(endPanel, c);
    c.gridy++;
    editPanel.add(pgmOptPanel, c);

    if (JOptionPane.showConfirmDialog(this, editPanel, "Edit Timer",
        JOptionPane.OK_CANCEL_OPTION, JOptionPane.PLAIN_MESSAGE) == JOptionPane.YES_OPTION) {

      Calendar beginCal = new GregorianCalendar();
      beginCal.setTime(beginSpinner.getDate());
      String e2timebegin = E2TimerHelper.getAsSeconds(beginCal);
      Calendar endCal = new GregorianCalendar();
      endCal.setTime(endSpinner.getDate());
      String e2timeend = E2TimerHelper.getAsSeconds(endCal);
      String e2description = area.getText();
      String e2location = pgmOptPanel.getSelectedLocation();
      String e2tags = pgmOptPanel.getSelectedTag();
      int afterevent = pgmOptPanel.getSelectedAfterEvent();
      int repeated = pgmOptPanel.getRepeated();
      useHdService = pgmOptPanel.isUseHdService();

      Map<String, String> newTimer = new HashMap<String, String>(oldTimer);
      newTimer.put(E2TimerHelper.SERVICEREFERENCE, E2ServiceHelper
          .getServiceRef(oldTimer.get(E2TimerHelper.SERVICEREFERENCE),
              useHdService));
      newTimer.put(E2TimerHelper.TIMEBEGIN, e2timebegin);
      newTimer.put(E2TimerHelper.TIMEEND, e2timeend);
      newTimer.put(E2TimerHelper.SERVICENAME, E2ServiceHelper.getServiceName(
          oldTimer.get(E2TimerHelper.SERVICENAME), useHdService));
      newTimer.put(E2TimerHelper.DESCRIPTION, e2description);
      newTimer.put(E2TimerHelper.LOCATION, e2location);
      newTimer.put(E2TimerHelper.TAGS, e2tags);
      newTimer.put(E2TimerHelper.AFTEREVENT, Integer.toString(afterevent));
      newTimer.put(E2TimerHelper.REPEATED, Integer.toString(repeated));

      // Change Timer
      if (mTimerHelper.timerChange(oldTimer, newTimer)) {

        // ZAP pruefen
        Map<String, String> oldZapBeforeTimer = mTimerHelper
            .createZapBeforeTimer(oldTimer);
        Map<String, String> newZapBeforeTimer = mTimerHelper
            .createZapBeforeTimer(newTimer);

        int idx = mTimerHelper.indexOfTimer(oldZapBeforeTimer);
        if (pgmOptPanel.isZapBeforeEvent()) {
          if (idx != -1) {
            mTimerHelper.timerChange(oldZapBeforeTimer, newZapBeforeTimer);
          } else {
            mTimerHelper.timerAdd(oldZapBeforeTimer);
          }
        } else {
          if (idx != -1) {
            mTimerHelper.timerDelete(oldZapBeforeTimer);
          } else {
            // nothing to do
          }
        }

        if (Boolean.getBoolean("captureplugin.ProgramOptionPanel.switchToSd")) {
          // ZAP2 pruefen
          Map<String, String> oldZap2Timer = mTimerHelper
              .createZapAfterTimer(oldTimer);
          Map<String, String> newZap2Timer = mTimerHelper
              .createZapAfterTimer(newTimer);

          idx = mTimerHelper.indexOfTimer(oldZap2Timer);
          if (pgmOptPanel.isUseHdService()) {
            if (idx != -1) {
              mTimerHelper.timerChange(oldZap2Timer, newZap2Timer);
            } else {
              mTimerHelper.timerAdd(oldZap2Timer);
            }
          } else {
            if (idx != -1) {
              mTimerHelper.timerDelete(oldZap2Timer);
            } else {
              // nothing to do
            }
          }
        }
        refresh();
      }
    }
  }

  @SuppressWarnings("unchecked")
  private void rowTimerDelete(int modelRow) {
    // Werte aus Model holen
    DreamboxTimerTableModel model = (DreamboxTimerTableModel) mTable.getModel();

    Map<String, String> recTimer = (Map<String, String>) model.getValueAt(
        modelRow, COL_TIMER);
    Map<String, String> zapBeforeTimer = mTimerHelper
        .createZapBeforeTimer(recTimer);
    Map<String, String> zapAfterTimer = mTimerHelper
        .createZapAfterTimer(recTimer);

    if (mTimerHelper.timerDelete(recTimer)) {
      if (mTimerHelper.indexOfTimer(zapBeforeTimer) != -1) {
        mTimerHelper.timerDelete(zapBeforeTimer);
      }
      if (mTimerHelper.indexOfTimer(zapAfterTimer) != -1) {
        mTimerHelper.timerDelete(zapAfterTimer);
      }
      refresh();
    }
  }

  /**
   * TimerList aktualisieren
   */
  @Override
  public void refresh() {
    Thread th = new Thread() {
      @Override
      public void run() {
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            refreshPanel();
          }
        });
      }
    };
    th.start();
  }

  /**
   * Panel neu aufbauen
   */
  void refreshPanel() {
    if (mTimerHelper.getTimers() == null) {
      this.add(new JLabel("Timeout"));
      this.validate();
      return;
    }

    // Header
    List<String> columnNames = new ArrayList<String>();
    columnNames.add(mLocalizer.msg("column0", "No.")); // 0
    columnNames.add(mLocalizer.msg("column1", "Date")); // 1
    columnNames.add(mLocalizer.msg("column2", "Day")); // 2
    columnNames.add(mLocalizer.msg("column3", "From")); // 3
    columnNames.add(mLocalizer.msg("column4", "Until")); // 4
    columnNames.add(mLocalizer.msg("column5", "Title")); // 5
    columnNames.add(mLocalizer.msg("column6", "Dis")); // 6
    columnNames.add(mLocalizer.msg("column7", "Edt")); // 7
    columnNames.add(mLocalizer.msg("column8", "Del")); // 8
    columnNames.add(Localizer.getLocalization(Localizer.I18N_CHANNEL)); // 9
    columnNames.add(mLocalizer.msg("column10", "Tags")); // 10
    columnNames.add("Timer"); // 11

    // Data
    List<List<Object>> data = new ArrayList<List<Object>>();

    JButton btnEdit = new JButton();
    btnEdit.setToolTipText(mLocalizer.msg("ToolTipEditTimer", "Edit Timer"));
    btnEdit.setActionCommand(CMD_EDIT_BTN);
    btnEdit.setIcon(new ImageIcon(getClass().getResource("images/edit.gif")));

    JButton btnDelete = new JButton();
    btnDelete.setToolTipText(mLocalizer.msg("ToolTipDeleteTimer",
        "Delete Timer"));
    btnDelete.setActionCommand(CMD_DELETE_BTN);
    btnDelete
        .setIcon(new ImageIcon(getClass().getResource("images/delete.gif")));

    int n = 0;
    for (Map<String, String> timer : mTimerHelper.getTimers()) {
      // Calendar
      Calendar timeBeg = E2TimerHelper.getAsCalendar(timer
          .get(E2TimerHelper.TIMEBEGIN));
      Calendar timeEnd = E2TimerHelper.getAsCalendar(timer
          .get(E2TimerHelper.TIMEEND));

      // Zeile aufbauen
      List<Object> v = new ArrayList<Object>();
      n++;
      v.add(n); // 0
      v.add(timeBeg.getTime()); // 1
      v.add(timeBeg.getTime()); // 2
      v.add(timeBeg.getTime()); // 3
      v.add(timeEnd.getTime()); // 4
      v.add(timer.get(E2TimerHelper.NAME)); // 5
      v.add(timer.get(E2TimerHelper.DISABLED).equals("0")); // 6
      v.add(btnEdit); // 7
      v.add(btnDelete); // 8
      v.add(timer.get(E2TimerHelper.SERVICENAME)); // 9
      v.add(timer.get(E2TimerHelper.TAGS)); // 10
      v.add(timer); // 11

      data.add(v);
    }

    // TableModel
    TableModel model = new DreamboxTimerTableModel(data, columnNames);
    model.addTableModelListener(this);

    mTable.setModel(model);
    // mTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    mTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    // mTable.getTableHeader().setReorderingAllowed(false);
    mTable.setRowHeight(22);

    // Columns
    int colwidth[] = new int[] { 50, 75, 45, 55, 55, 350, 25, 30, 30, 120, 70,
        0 };
    TableColumnModel columnModel = mTable.getColumnModel();
    for (int i = 0; i < columnModel.getColumnCount(); i++) {
      TableColumn col = columnModel.getColumn(i);
      col.setIdentifier(columnNames.get(i));
      col.setPreferredWidth(colwidth[i]);
      // Renderer setzen
      switch (i) {
      case COL_NO:
        col.setCellRenderer(new LabelCellRenderer(JLabel.CENTER));
        break;
      case COL_DATE:
        col.setCellRenderer(new DateCellRenderer("dd.MM.yy"));
        break;
      case COL_WEEKDAY:
        col.setCellRenderer(new DateCellRenderer("E"));
        break;
      case COL_FROM:
        col.setCellRenderer(new DateCellRenderer("HH:mm"));
        break;
      case COL_UNTIL:
        col.setCellRenderer(new DateCellRenderer("HH:mm"));
        break;
      case COL_TITLE:
        col.setCellRenderer(new TitleCellRenderer());
        break;
      case COL_ACTION_DISABLE:
        col.setCellRenderer(new BooleanCellRenderer(mLocalizer.msg(
            "ToolTipDisableTimer", "Disable/Enable Timer")));
        break;
      case COL_ACTION_EDIT:
        col.setCellRenderer(new ButtonCellRenderer());
        col.setCellEditor(new ButtonCellEditor(this));
        break;
      case COL_ACTION_DELETE:
        col.setCellRenderer(new ButtonCellRenderer());
        col.setCellEditor(new ButtonCellEditor(this));
        break;
      case COL_SERVICE:
        col.setCellRenderer(new LabelCellRenderer(JLabel.CENTER));
        break;
      case COL_TAGS:
        col.setCellRenderer(new LabelCellRenderer(JLabel.CENTER));
        break;
      case COL_TIMER:
        columnModel.removeColumn(col);
        break;
      }
    }

    // Sortierung
    if ((mTimerHelper.getTimers().size() > 0) && (model.getColumnCount() >= 2)) {
      mRowSorter = new TableRowSorter<TableModel>(model);
      List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
      sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
      mRowSorter.setSortKeys(sortKeys);
      mRowSorter.setMaxSortKeys(2);
      RowFilter<Object, Object> justplayFilter = new RowFilter<Object, Object>() {
        @SuppressWarnings("unchecked")
        @Override
        public boolean include(Entry<? extends Object, ? extends Object> entry) {
          // last col is the timer
          Object o = entry.getValue(COL_TIMER);
          if (o instanceof Map) {
            Map<String, String> timer = (Map<String, String>) o;
            boolean justplay = timer.get(E2TimerHelper.JUSTPLAY).equals("1");
            return mCbShowZap.isSelected() || !justplay;
          }
          return false;
        }
      };
      mRowSorter.setRowFilter(justplayFilter);
      mTable.setRowSorter(mRowSorter);
    }

    // Scrollpane fuer Tabelle
    JScrollPane scrollpane = new JScrollPane(mTable);

    JPanel panelFilter = new JPanel(new BorderLayout());
    mCbShowZap.setActionCommand(CMD_FILTER_CHANGED);
    mCbShowZap.addActionListener(this);
    panelFilter.add(mCbShowZap, BorderLayout.EAST);

    // Panel fuer Timer-Chart
    this.removeAll();
    this.setLayout(new BorderLayout());
    this.add(panelFilter, BorderLayout.NORTH);
    this.add(scrollpane, BorderLayout.CENTER);
    this.validate();
  }

  public void tableChanged(TableModelEvent e) {
    int row = e.getFirstRow();
    int column = e.getColumn();
    if (column == COL_ACTION_DISABLE) {
      // Werte aus Model holen
      Boolean timerEnabled = (Boolean) mTable.getModel()
          .getValueAt(row, column);

      rowTimerEnable(row, timerEnabled);
    }
  }

  @SuppressWarnings("unchecked")
  private void rowTimerEnable(int row, Boolean timerEnabled) {
    // Werte aus Model holen
    Map<String, String> oldTimer = (Map<String, String>) mTable.getModel()
        .getValueAt(row, COL_TIMER);
    Map<String, String> newTimer = new HashMap<String, String>(oldTimer);
    // enable/disable
    newTimer.put(E2TimerHelper.DISABLED, timerEnabled ? "0" : "1");

    // Change Timer
    if (mTimerHelper.timerChange(oldTimer, newTimer)) {
      Map<String, String> oldZapBeforeTimer = mTimerHelper
          .createZapBeforeTimer(oldTimer);
      Map<String, String> newZapBeforeTimer = mTimerHelper
          .createZapBeforeTimer(newTimer);
      if (mTimerHelper.timerChange(oldZapBeforeTimer, newZapBeforeTimer)) {
      }
      mLog.info("[" + mConnector.getConfig().getDreamboxAddress() + "] " + "Timer " + (timerEnabled ? "enabled" : "disabled") + " - "
          + oldTimer.get(E2TimerHelper.NAME));
      refresh();
    }
  }
}
