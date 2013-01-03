package captureplugin.drivers.dreambox.connector.cs;

import java.awt.BorderLayout;
import java.awt.Desktop;
import java.awt.Dimension;
import java.io.IOException;
import java.io.InputStream;
import java.net.URI;
import java.net.URISyntaxException;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JEditorPane;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JSplitPane;
import javax.swing.ListSelectionModel;
import javax.swing.RowSorter;
import javax.swing.SortOrder;
import javax.swing.SwingUtilities;
import javax.swing.event.HyperlinkEvent;
import javax.swing.event.HyperlinkListener;
import javax.swing.event.TableModelEvent;
import javax.swing.event.TableModelListener;
import javax.swing.event.HyperlinkEvent.EventType;
import javax.swing.table.TableColumn;
import javax.swing.table.TableModel;
import javax.swing.table.TableRowSorter;

import util.ui.Localizer;
import captureplugin.drivers.dreambox.connector.DreamboxConnector;

/**
 * 
 * @author fishhead
 * 
 */
@SuppressWarnings("serial")
public class DreamboxInfoListPanel extends JPanelRefreshAbstract implements
    TableModelListener, HyperlinkListener {

  private static final int COL_CATEGORY = 0;
  private static final int COL_NAME = 1;
  private static final int COL_VALUE = 2;

  // Translator
  private static final Localizer mLocalizer = Localizer
      .getLocalizerFor(DreamboxInfoListPanel.class);
  // Logger
  private static final Logger mLog = Logger
      .getLogger(DreamboxInfoListPanel.class.getName());
  // Member
  private final DreamboxConnector mConnector;
  private final E2InfoHelper mInfoThread;

  /**
   * Konstruktor
   * 
   * @param connector
   * @param infoThread
   */
  public DreamboxInfoListPanel(DreamboxConnector connector,
      E2InfoHelper infoThread) {
    super();
    this.mConnector = connector;
    this.mInfoThread = infoThread;
    this.setPreferredSize(new Dimension(800, 600));
    this.add(new JLabel(mLocalizer.msg("panel", "Reading Info ...")));
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
    // get infos
    List<Map<String, String>> infos = mInfoThread.getInfos();

    // Header
    List<String> columnNames = new ArrayList<String>();
    columnNames.add(mLocalizer.msg("column0", "category"));
    columnNames.add(mLocalizer.msg("column1", "name"));
    columnNames.add(mLocalizer.msg("column2", "value"));

    // Combox Items
    Set<String> items = new TreeSet<String>();
    items.add("");

    // Mapping
    Map<String, String[]> map = new HashMap<String, String[]>();
    createMapping(map);

    // Data
    List<List<Object>> data = new ArrayList<List<Object>>();
    addRow(data, "CapturePlugin", "Autor", "fishhead");
    addRow(data, "CapturePlugin", "TV-Browser Version", "3.1");
    addRow(data, "CapturePlugin", "Version", "v0.47");
    addRow(data, "FTP", "Benutzername", mConnector.getConfig().getUserName());
    addRow(data, "Gerät", "Adresse", mConnector.getConfig().getDreamboxAddress());

    if (infos != null) {
      for (Map<String, String> info : infos) {
        for (Map.Entry<String, String> entry : info.entrySet()) {
          String key = entry.getKey();
          String value = entry.getValue();
          if (map.containsKey(key)) {
            addRow(data, map.get(key)[0], map.get(key)[1], value);
          } else {
            if (!key.contains("/") && (!value.isEmpty())) {
              String key2 = key.startsWith("e2") ? key.substring(2) : key;
              if (key.endsWith("id")) {
                String id = key.substring(key.length() - 3).toUpperCase();
                addRow(data, "x-info  " + id, key2, value);
              } else {
                // addRow(data, "x-info", key2, value);
              }
            }
          }
        }
      }
    }

    // TableModel
    TableModel model = new DreamboxMovieTableModel(data, columnNames);
    model.addTableModelListener(this);

    // Table
    mTable.setModel(model);
    // mTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    mTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    mTable.getTableHeader().setReorderingAllowed(false);
    mTable.setRowHeight(22);
    // Columns
    int colwidth[] = new int[] { 100, 150, 550 };
    for (int i = 0; i < columnNames.size(); i++) {
      TableColumn col = mTable.getColumnModel().getColumn(i);
      col.setIdentifier(columnNames.get(i));
      col.setPreferredWidth(colwidth[i]);
      // Renderer setzen
      switch (i) {
      case COL_CATEGORY:
        col.setCellRenderer(new LabelCellRenderer(JLabel.CENTER));
        break;
      case COL_NAME:
        col.setCellRenderer(new LabelCellRenderer(JLabel.CENTER));
        break;
      case COL_VALUE:
        col.setCellRenderer(new LabelCellRenderer(JLabel.CENTER));
        break;
      }
    }

    // Sortierung
    if (model.getColumnCount() >= 2) {
      TableRowSorter<TableModel> sorter = new TableRowSorter<TableModel>(model);
      List<RowSorter.SortKey> sortKeys = new ArrayList<RowSorter.SortKey>();
      sortKeys.add(new RowSorter.SortKey(0, SortOrder.ASCENDING));
      sortKeys.add(new RowSorter.SortKey(1, SortOrder.ASCENDING));
      sorter.setSortKeys(sortKeys);
      sorter.setMaxSortKeys(2);
      mTable.setRowSorter(sorter);
    }

    // Scrollpane fuer Tabelle
    JScrollPane scrollpaneTable = new JScrollPane(mTable);
    
    // Panel fuer Info-Liste
    this.removeAll();
    this.setLayout(new BorderLayout());
    this.add(scrollpaneTable, BorderLayout.CENTER);
    this.validate();
  }

  private void createMapping(Map<String, String[]> map) {
    map.put("e2hddinfo/model", "HDD,Modell".split("[,]"));
    map.put("e2hddinfo/free", "HDD,freier Platz".split("[,]"));
    map.put("e2hddinfo/capacity", "HDD,Gesamtkapazität".split("[,]"));

    map.put("e2lanip", "LAN,IP-Adresse".split("[,]"));
    map.put("e2lanmask", "LAN,Subnetzmaske".split("[,]"));
    map.put("e2langw", "LAN,Gateway".split("[,]"));
    // map.put("e2lanmac", "LAN,MAC-Adresse".split("[,]"));
    map.put("e2landhcp", "LAN,DHCP verwenden".split("[,]"));

    map.put("e2model", "SAT-Receiver,Modell".split("[,]"));
    map.put("e2imageversion", "SAT-Receiver,Image-Version".split("[,]"));
    map.put("e2enigmaversion", "SAT-Receiver,Enigma-Version".split("[,]"));
    map.put("e2tunerinfo/e2nim/name", "SAT-Receiver,Tuner-Name".split("[,]"));
    map.put("e2tunerinfo/e2nim/type", "SAT-Receiver,Tuner-Type".split("[,]"));

    map.put("e2serviceprovider", "Service,Anbieter".split("[,]"));
    map.put("e2servicename", "Service,Sender".split("[,]"));
    map.put("e2servicevideosize", "Service,Videogröße".split("[,]"));

    map.put("e2webifversion", "WEB,Interface-Version".split("[,]"));

    map.put("Fehlertext", new String[] { "Exception", "Fehlertext" });
  }

  /**
   * Zeile hinzufuegen
   * 
   * @param data
   * @param name
   * @param value
   */
  private void addRow(List<List<Object>> data, String category, String name,
      String value) {
    List<Object> v = new ArrayList<Object>();
    v.add(category); // 0
    v.add(name); // 1
    v.add(value); // 2
    data.add(v);
  }

  /**
   * Aenderungen in der Tabelle
   */
  public void tableChanged(TableModelEvent e) {
    int row = e.getFirstRow();
    int column = e.getColumn();
    String value = (String) mTable.getModel().getValueAt(row, column);
    mLog.fine("row:" + row + "  col:" + column + "  value:" + value);
  }

  /**
   * hyperlink clicked, open in browser
   */
  public void hyperlinkUpdate(HyperlinkEvent hle) {
    if (hle.getEventType() == EventType.ACTIVATED) {
      try {
        Desktop.getDesktop().browse(new URI(hle.getDescription()));
      } catch (IOException e) {
        mLog.log(Level.WARNING, "IOException", e);
      } catch (URISyntaxException e) {
        mLog.log(Level.WARNING, "URISyntaxException", e);
      }
    }
  }
}
