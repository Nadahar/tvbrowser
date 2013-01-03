package captureplugin.drivers.dreambox.connector.cs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Desktop;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.ImageIcon;
import javax.swing.JComboBox;
import javax.swing.JLabel;
import javax.swing.JMenuItem;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.JSeparator;
import javax.swing.JTable;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
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

/**
 * 
 * @author fishhead
 * 
 */
@SuppressWarnings("serial")
public class DreamboxMovieListPanel extends JPanelRefreshAbstract implements
    TableModelListener, ActionListener {
  /**
   * TimerList Cellrenderer
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

    @Override
    public Component getTableCellRendererComponent(JTable table, Object value,
        boolean isSelected, boolean hasFocus, int row, int column) {
      // color
      if (isSelected) {
        setForeground(table.getSelectionForeground());
        setBackground(table.getSelectionBackground());
      } else {
        setForeground(table.getForeground());
        setBackground(table.getBackground());
      }
      // text, tooltip, icon
      if (value instanceof JLabel) {
        JLabel label = (JLabel) value;
        setText(label.getText());
        setIcon(label.getIcon());
        setToolTipText(getToolTipText(label.getText(), label.getToolTipText()));
      } else {
        String text = (value == null) ? "" : value.toString();
        setText(text);
        setToolTipText(text);
        setIcon(null);
      }
      setHorizontalAlignment(JLabel.LEFT);
      return this;
    }

    /**
     * ToolTip erzeugen
     * 
     * @param title
     * @param description
     * @return tooltip
     */
    private String getToolTipText(String title, String description) {
      if ((title == null) || (title.length() == 0)) {
        return null;
      }
      if ((description == null) || (description.length() == 0)) {
        return title;
      }
      // ToolTip aufbauen
      String toolTipText = fmtStyle(title,
          "color:black; font-weight:bold; font-size:medium;");
      // Beschreibung
      if ((description != null) && (description.length() > 0)) {
        toolTipText += fmtStyle(description,
            "padding-top:5px; color:black; width:250px; font-size:small;");
      }
      // HTML
      return fmtToolTip(toolTipText);
    }
  }

  // Service-Reference
  private static final String S_REF = "1:0:0:0:0:0:0:0:0:0:";

  // Columns
  static final int COL_NO = 0;
  private static final int COL_SERVICE = 1;
  static final int COL_TITLE = 2;
  private static final int COL_DATETIME = 3;
  private static final int COL_LENGTH = 4;
  private static final int COL_SIZE = 5;
  static final int COL_TAGS = 6;
  private static final int COL_FILENAME = 7;
  // Aktionen
  private static final String CMD_URL = "url";
  private static final String CMD_STREAM = "stream";
  private static final String CMD_DOWNLOAD = "download";
  private static final String CMD_DELETE = "delete";
  private static final String CMD_LOCATION_CHANGED = "locationChanged";
  // Logger
  private static final Logger mLog = Logger
      .getLogger(DreamboxMovieListPanel.class.getName());
  // Translator
  private static final Localizer mLocalizer = Localizer
      .getLocalizerFor(DreamboxMovieListPanel.class);
  // MovieLocation
  private static final String mRootLocation = "/hdd/movie/";
  // Member
  private DreamboxConnector mConnector = null;
  private final E2MovieHelper mMovieHelper;
  private String mLocation = mRootLocation;

  /**
   * Konstruktor
   * 
   * @param connector
   * @param movieHelper
   */
  public DreamboxMovieListPanel(DreamboxConnector connector,
      E2MovieHelper movieHelper) {
    super();
    mLog.setLevel(Level.INFO);    
    this.mConnector = connector;
    this.mMovieHelper = movieHelper;
    this.setPreferredSize(new Dimension(900, 600));
    this.add(new JLabel(mLocalizer.msg("panel", "Reading Movies ...")));
    // Popup erweitern
    // zelluloid
    JMenuItem menuItemUrl = new JMenuItem("zelluliod.de");
    menuItemUrl.setActionCommand(CMD_URL);
    menuItemUrl.addActionListener(this);
    menuItemUrl.setIcon(new ImageIcon(getClass()
        .getResource("images/paste.gif")));
    mPopupMenu.insert(menuItemUrl, 0);
    // Stream
    JMenuItem menuItemStream = new JMenuItem(mLocalizer.msg("stream", "Stream"));
    menuItemStream.setActionCommand(CMD_STREAM);
    menuItemStream.addActionListener(this);
    menuItemStream.setIcon(new ImageIcon(getClass().getResource(
        "images/stream.gif")));
    mPopupMenu.insert(menuItemStream, 1);
    // Download
    JMenuItem menuItemDownload = new JMenuItem(mLocalizer.msg("download",
        "Download"));
    menuItemDownload.setActionCommand(CMD_DOWNLOAD);
    menuItemDownload.addActionListener(this);
    menuItemDownload.setIcon(new ImageIcon(getClass().getResource(
        "images/import.gif")));
    mPopupMenu.insert(menuItemDownload, 2);
    // Stream
    JMenuItem menuItemDelete = new JMenuItem(mLocalizer.msg("delete", "Delete"));
    menuItemDelete.setActionCommand(CMD_DELETE);
    menuItemDelete.addActionListener(this);
    menuItemDelete.setIcon(new ImageIcon(getClass().getResource(
        "images/delete.gif")));
    mPopupMenu.insert(menuItemDelete, 3);
    // Separator
    mPopupMenu.insert(new JSeparator(), 4);
  }

  /**
   * Action
   */
  @Override
  public void actionPerformedDelegate(ActionEvent ae) {
    String actionCommand = ae.getActionCommand();
    if (actionCommand.equals(CMD_LOCATION_CHANGED)) {
      cmdLocationChanged(ae);
    } else if (ae.getActionCommand().equals(CMD_URL)) {
      int modelRow = mTable.convertRowIndexToModel(mTable.getSelectedRow());
      cmdUrl(modelRow);
    } else if (ae.getActionCommand().equals(CMD_STREAM)) {
      int modelRow = mTable.convertRowIndexToModel(mTable.getSelectedRow());
      cmdStream(modelRow);
    } else if (actionCommand.equals(CMD_DOWNLOAD)) {
      int modelRow = mTable.convertRowIndexToModel(mTable.getSelectedRow());
      cmdDownload(modelRow);
    } else if (actionCommand.equals(CMD_DELETE)) {
      int modelRow = mTable.convertRowIndexToModel(mTable.getSelectedRow());
      cmdDelete(modelRow);
    } else {
      mLog.warning(actionCommand);
    }
  }

  /**
   * stream
   * 
   * @param modelRow
   */
  private void cmdStream(int modelRow) {
    DreamboxConfig config = mConnector.getConfig();
    String filename = ((String) mTable.getModel().getValueAt(modelRow,
        COL_FILENAME));
    filename = filename.substring(mRootLocation.length());
    try {
      String url = "http://" + config.getDreamboxAddress() + "/file/?file="
          + URLEncoder.encode(filename, "UTF-8");
      String cmd = "\"" + config.getMediaplayer() + "\" " + url;
      mLog.info(cmd);
      Runtime.getRuntime().exec(cmd);
    } catch (UnsupportedEncodingException e) {
      mLog.log(Level.WARNING, "UnsupportedEncodingException", e);
    } catch (IOException e) {
      mLog.log(Level.WARNING, "IOException", e);
    }
  }

  /**
   * download
   * 
   * @param modelRow
   */
  private void cmdDownload(int modelRow) {
    DreamboxConfig config = mConnector.getConfig();
    String filename = ((String) mTable.getModel().getValueAt(modelRow,
        COL_FILENAME));
    filename = filename.substring(mRootLocation.length());
    try {
      String url = "http://" + config.getDreamboxAddress() + "/file/?file="
          + URLEncoder.encode(filename, "UTF-8") + "&root="
          + URLEncoder.encode(mRootLocation, "UTF-8");
      mLog.info(url);
      Desktop.getDesktop().browse(new URI(url));
    } catch (UnsupportedEncodingException e) {
      mLog.log(Level.WARNING, "UnsupportedEncodingException", e);
    } catch (IOException e) {
      mLog.log(Level.WARNING, "IOException", e);
    } catch (URISyntaxException e) {
      mLog.log(Level.WARNING, "URISyntaxException", e);
    }
  }

  /**
   * delete
   * 
   * @param modelRow
   */
  private void cmdDelete(int modelRow) {
    String filename = (String) mTable.getModel().getValueAt(modelRow,
        COL_FILENAME);
    String title = ((JLabel) mTable.getModel().getValueAt(modelRow, COL_TITLE))
        .getText();
    // Abfrage
    if (JOptionPane.showConfirmDialog(this, title, mLocalizer.msg(
        "deleteMovie", "Delete Movie..."), JOptionPane.YES_NO_OPTION) == JOptionPane.YES_OPTION) {

      Calendar cal = new GregorianCalendar();
      DreamboxConfig config = mConnector.getConfig();
      String sRef = S_REF + filename;
      String data = "";

      try {
        URL url = new URL("http://" + config.getDreamboxAddress()
            + "/web/moviedelete?sRef=" + URLEncoder.encode(sRef, "UTF-8"));
        URLConnection connection = url.openConnection();

        String userpassword = config.getUserName() + ":" + config.getPassword();
        String encoded = new String(Base64
            .encodeBase64(userpassword.getBytes()));
        connection.setRequestProperty("Authorization", "Basic " + encoded);

        connection.setConnectTimeout(config.getTimeout());
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
        boolean state = handler.getState().equalsIgnoreCase("true");
        if (state == true) {

          // Movie-Liste im Thread aktualisieren
          mMovieHelper.getMovies().remove(modelRow);
        } else {
          mLog.warning(state + " " + handler.getStatetext());
        }
      } catch (ParserConfigurationException e) {
        mLog.log(Level.WARNING, "ParserConfigurationException", e);
      } catch (SAXException e) {
        mLog.warning(data);
        mLog.log(Level.WARNING, "SAXException", e);
      } catch (MalformedURLException e) {
        mLog.log(Level.WARNING, "MalformedURLException", e);
      } catch (SocketTimeoutException e) {
        mLog.log(Level.WARNING, "SocketTimeoutException", e);
      } catch (IOException e) {
        mLog.log(Level.WARNING, "IOException", e);
      } catch (IllegalArgumentException e) {
        mLog.log(Level.WARNING, "IllegalArgumentException", e);
      }

      mLog.info("[" + mConnector.getConfig().getDreamboxAddress() + "] " + "DELETE movie - " + title + " - "
          + (new GregorianCalendar().getTimeInMillis() - cal.getTimeInMillis())
          + " ms");

      refresh();
    }
  }

  /**
   * show new location
   * 
   * @param ae
   */
  private void cmdLocationChanged(ActionEvent ae) {
    JComboBox comboBox = (JComboBox) ae.getSource();
    mLocation = (String) comboBox.getSelectedItem();
    mMovieHelper.setLocation(mLocation);
    refresh();
  }

  /**
   * Url lesen
   */
  private void cmdUrl(int modelRow) {
    String title = ((JLabel) mTable.getModel().getValueAt(modelRow, COL_TITLE))
        .getText();
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
    if (mMovieHelper.getMovies() == null) {
      this.add(new JLabel("Timeout"));
      this.validate();
      return;
    }

    // Header
    List<String> columnNames = new ArrayList<String>();
    columnNames.add(mLocalizer.msg("column0", "No"));
    columnNames.add(Localizer.getLocalization(Localizer.I18N_CHANNEL));
    columnNames.add(mLocalizer.msg("column2", "Title"));
    columnNames.add(mLocalizer.msg("column3", "Date"));
    columnNames.add(mLocalizer.msg("column4", "Length"));
    columnNames.add(mLocalizer.msg("column5", "Size"));
    columnNames.add(mLocalizer.msg("column6", "Tags"));
    columnNames.add(mLocalizer.msg("column7", "Filename"));

    // Data
    long movieSize = 0;
    int n = 0;
    List<List<Object>> data = new ArrayList<List<Object>>();
    for (Map<String, String> movie : mMovieHelper.getMovies()) {
      // extract values
      String e2servicename = movie.get(E2MovieHelper.SERVICENAME);
      String e2title = movie.get(E2MovieHelper.TITLE);
      String e2length = movie.get(E2MovieHelper.LENGTH);
      if (e2length.length() == 5) {
        e2length = "0" + e2length;
      }
      String e2description = movie.get(E2MovieHelper.DESCRIPTION);
      String e2descriptionextended = movie
          .get(E2MovieHelper.DESCRIPTIONEXTENDED);
      String e2tags = movie.get(E2MovieHelper.TAGS);
      String e2filename = movie.get(E2MovieHelper.FILENAME);
      long e2size = E2TimerHelper.getLong(movie.get(E2MovieHelper.FILESIZE));
      movieSize += e2size; // in Bytes
      // format date
      Calendar calTime = E2TimerHelper.getAsCalendar(movie
          .get(E2MovieHelper.TIME));
      // insert cr+lf
      e2description = e2description.replaceAll("\\n", "<br/>");
      e2descriptionextended = e2descriptionextended.replaceAll("\\n", "<br/>");
      // title with tooltip
      JLabel labelTitle = new JLabel(e2title);
      if (e2descriptionextended.length() > 0) {
        labelTitle.setToolTipText(e2descriptionextended);
      } else if (e2description.length() > 0) {
        labelTitle.setToolTipText(e2description);
      } else {
        labelTitle.setToolTipText(null);
      }

      // create row
      List<Object> v = new ArrayList<Object>();
      n++;
      v.add(n); // 0
      v.add(e2servicename); // 1
      v.add(labelTitle); // 2
      v.add(calTime.getTime()); // 3
      v.add(e2length); // 4
      v.add(e2size / 1024 / 1024); // 5
      v.add(e2tags); // 6
      v.add(e2filename); // 7

      // add row
      data.add(v);
    }

    // TableModel
    TableModel model = new DreamboxMovieTableModel(data, columnNames);
    model.addTableModelListener(this);

    // Table
    mTable.setModel(model);
    // mTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    mTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    // mTable.getTableHeader().setReorderingAllowed(false);
    mTable.setRowHeight(22);

    // Columns
    int colwidth[] = new int[] { 50, 120, 350, 115, 65, 50, 95, 0 };
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
      case COL_SERVICE:
        col.setCellRenderer(new LabelCellRenderer(JLabel.CENTER));
        break;
      case COL_TITLE:
        col.setCellRenderer(new TitleCellRenderer());
        col.setCellEditor(new LabelCellEditor(new JTextField()));
        break;
      case COL_DATETIME:
        col.setCellRenderer(new DateCellRenderer("dd.MM.yy  HH:mm"));
        break;
      case COL_LENGTH:
        col.setCellRenderer(new LabelCellRenderer(JLabel.CENTER));
        break;
      case COL_SIZE:
        col.setCellRenderer(new LabelCellRenderer(JLabel.CENTER));
        break;
      case COL_TAGS:
        Set<String> tags = mMovieHelper.getTags();
        if (tags == null) {
          tags = new TreeSet<String>();
          tags.add("");
        }
        Object[] oaTags = tags.toArray();
        JComboBox cmb = new JComboBox(oaTags);
        ComboBoxCellRenderer cellRenderer = new ComboBoxCellRenderer(oaTags);
        col.setCellRenderer(cellRenderer);
        col.setCellEditor(new ComboBoxCellEditor(cmb, cellRenderer.getModel()));
        break;
      case COL_FILENAME:
        columnModel.removeColumn(col);
        break;
      }
    }

    // Sortierung
    if (model.getColumnCount() >= 2) {
      TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
      List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
      sortKeys.add(new RowSorter.SortKey(COL_NO, SortOrder.ASCENDING));
      sorter.setSortKeys(sortKeys);
      sorter.setMaxSortKeys(2);
      mTable.setRowSorter(sorter);
    }

    // Scrollpane fuer Tabelle
    JScrollPane scrollpane = new JScrollPane(mTable);

    // ComboBox Location
    JComboBox cmbLocation = new JComboBox(E2LocationHelper.getInstance(
        mConnector.getConfig(), null).getLocations().toArray());
    cmbLocation.setSelectedItem(mLocation);
    cmbLocation.setActionCommand(CMD_LOCATION_CHANGED);
    cmbLocation.addActionListener(this);
    JPanel panelCmbLocation = new JPanel(new BorderLayout());
    panelCmbLocation.add(cmbLocation, BorderLayout.EAST);

    // Label Moviesize
    String txtMovieSize = String.format(" Speicherbelegung :  %d GB  /  %d MB",
        movieSize / 1024 / 1024 / 1024, movieSize / 1024 / 1024);
    panelCmbLocation.add(new JLabel(txtMovieSize), BorderLayout.WEST);

    // Panel fuer Timer-Chart
    this.removeAll();
    this.setLayout(new BorderLayout());
    this.add(panelCmbLocation, BorderLayout.NORTH);
    this.add(scrollpane, BorderLayout.CENTER);
    this.validate();
  }

  public void tableChanged(TableModelEvent e) {
    int row = e.getFirstRow();
    int column = e.getColumn();
    if (column == COL_TAGS) {
      // Werte aus Model holen
      String value = (String) mTable.getModel().getValueAt(row, column);
      String filename = (String) mTable.getModel()
          .getValueAt(row, COL_FILENAME)
          + ".meta";
      String title = ((JLabel) mTable.getModel().getValueAt(row, COL_TITLE))
          .getText();
      // FTP
      FtpHelper ftpHelper = new FtpHelper();
      DreamboxConfig config = mConnector.getConfig();
      ftpHelper.cmd("OPEN", config.getDreamboxAddress());
      ftpHelper.cmd("LOGIN", config.getUserName(), config.getPassword());
      String s = ftpHelper.cmd("GET", filename);
      String[] zeilen;
      if (s == null) {
        zeilen = new String[] { S_REF, title, "", "", "", "", "", "", "" };
        zeilen[3] = E2TimerHelper.getAsSeconds(new GregorianCalendar());
      } else {
        zeilen = s.split("[\\n]");
      }
      zeilen[4] = value; // tags
      String t = "";
      for (String zeile : zeilen) {
        t += zeile + "\n";
      }
      ftpHelper.cmd("PUT", filename, t);
      ftpHelper.cmd("CLOSE");
      // DEBUG
      mLog.fine("row:" + row + "  column:" + column + "  value:" + value
          + "  filename:" + filename);

      // Movie-Liste im Thread aktualisieren
      mMovieHelper.getMovies().get(row).put(E2MovieHelper.TAGS, value);

    } else if (column == COL_TITLE) {
      // Werte aus Model holen
      String value = (String) mTable.getModel().getValueAt(row, column);
      String filename = (String) mTable.getModel()
          .getValueAt(row, COL_FILENAME)
          + ".meta";
      // FTP
      FtpHelper ftpHelper = new FtpHelper();
      DreamboxConfig config = mConnector.getConfig();
      ftpHelper.cmd("OPEN", config.getDreamboxAddress());
      ftpHelper.cmd("LOGIN", config.getUserName(), config.getPassword());
      String s = ftpHelper.cmd("GET", filename);
      String[] zeilen;
      if (s == null) {
        zeilen = new String[] { S_REF, "", "", "", "", "", "", "", "" };
        zeilen[3] = E2TimerHelper.getAsSeconds(new GregorianCalendar());
      } else {
        zeilen = s.split("[\\n]");
      }
      zeilen[1] = value; // title
      String t = "";
      for (String zeile : zeilen) {
        t += zeile + "\n";
      }
      ftpHelper.cmd("PUT", filename, t);
      ftpHelper.cmd("CLOSE");
      // DEBUG
      mLog.fine("row:" + row + "  column:" + column + "  value:" + value
          + "  filename:" + filename);

      // Movie-Liste im Thread aktualisieren
      mMovieHelper.getMovies().get(row).put(E2MovieHelper.TITLE, value);
    }
  }
}
