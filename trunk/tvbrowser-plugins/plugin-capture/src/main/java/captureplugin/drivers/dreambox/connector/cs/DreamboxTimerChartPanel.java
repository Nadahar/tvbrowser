package captureplugin.drivers.dreambox.connector.cs;

import java.awt.BorderLayout;
import java.awt.Component;
import java.awt.Dimension;
import java.awt.image.BufferedImage;
import java.awt.image.WritableRaster;
import java.util.Calendar;
import java.util.GregorianCalendar;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.TreeSet;
import java.util.Vector;

import javax.swing.ImageIcon;
import javax.swing.JLabel;
import javax.swing.JScrollPane;
import javax.swing.JTable;
import javax.swing.ListSelectionModel;
import javax.swing.SwingUtilities;
import javax.swing.table.DefaultTableCellRenderer;
import javax.swing.table.DefaultTableModel;
import javax.swing.table.TableColumn;
import javax.swing.table.TableColumnModel;
import javax.swing.table.TableModel;

import util.ui.Localizer;

/**
 * Timer-Chart
 * 
 */
@SuppressWarnings("serial")
public class DreamboxTimerChartPanel extends JPanelRefreshAbstract {

  /**
   * TimerChart Cellrenderer
   */
  private class TimerChartCellRenderer extends DefaultTableCellRenderer {

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

      if (value instanceof ImageIcon) {
        ImageIcon icon = (ImageIcon) value;
        setIcon(icon);
        setText(null);
        String toolTipText = icon.getDescription();
        if (toolTipText != null) {
          setToolTipText("<html><body>"
              + "<div style=\"font-size:small; padding:2px; \">"
              + toolTipText.replaceAll("[\\n]", "<br />") + "</div>"
              + "</body></html>");
        } else {
          setToolTipText(null);
        }
      } else {
        setIcon(null);
        super.setValue(value);
      }

      return this;
    }
  }

  /**
   * TimeChart Model
   */
  private class TimerChartModel extends DefaultTableModel {

    public TimerChartModel(Vector<Vector<Object>> data, Vector<String> headers) {
      super(data, headers);
    }

    @Override
    public boolean isCellEditable(int row, int col) {
      return false;
    }
  }

  // Translator
  private static final Localizer mLocalizer = Localizer
      .getLocalizerFor(DreamboxTimerChartPanel.class);
  // Formate
  private static final String FMT_DATE = "%1$td.%1$tm.%1$ty"; // TT.MM.JJ
  private static final String FMT_VON_BIS = "%1$02d:00-%1$02d:59"; // HH:MM-HH:MM
  // Image-Groessen
  private static final int X_MAX = 12; // Pixelbreite
  private static final int Y_MAX = 30; // Pixelhoehe
  // Konstanten
  private final int ROW_MAX = 24;
  // BLAU
  private static final int[] RGB_BLUE = new int[] { 000, 000, 255 };
  // WEISS
  private static final int[] RGB_WHITE = new int[] { 255, 255, 255 };
  // GRAU
  private static final int[] RGB_GRAY = new int[] { 164, 164, 164 };
  // GELB
  private static final int[] RGB_YELLOW = new int[] { 245, 196, 000 };
  // Farben
  final int[][] rgb = new int[][] { { 0, 190, 0 }, { 0, 130, 60 },
      { 0, 190, 0 }, { 0, 130, 60 } };

  // Member
  private final E2TimerHelper mTimerHelper;
  private final int mHourOffset;

  /**
   * Konstruktor
   * 
   * @param timerHelper
   */
  public DreamboxTimerChartPanel(E2TimerHelper timerHelper) {
    super();
    this.mHourOffset = 6;
    this.mTimerHelper = timerHelper;
    int width = ((X_MAX + 1) * rgb.length + 5) * 7 + 75; // 1 Woche
    int height = (Y_MAX + 3) * ROW_MAX; // 24 Stunden
    this.setPreferredSize(new Dimension(width, height));
    this.add(new JLabel(mLocalizer.msg("panel", "Reading Timers ...")));
  }

  /**
   * Raster ermitteln
   * 
   * @param model
   * @param rowIndex
   * @param columnIndex
   * @param name
   * @return raster
   */
  private WritableRaster getRaster(TableModel model, int rowIndex,
      int columnIndex, String name) {
    ImageIcon icon = (ImageIcon) model.getValueAt(rowIndex, columnIndex);
    WritableRaster raster = ((BufferedImage) icon.getImage()).getRaster();
    if (name != null) {
      icon.setDescription((icon.getDescription() == null) ? name : icon
          .getDescription()
          + "\n" + name);
    }
    return raster;
  }

  /**
   * Liefert Row mit Offset
   * 
   * @param row
   * @param hourOffset
   * @return row
   */
  private int getViewRow(int row, int hourOffset) {
    row += hourOffset;
    if (row >= 24) {
      row -= 24;
    }
    return row;
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
   * 
   */
  private void refreshPanel() {
    if (mTimerHelper.getTimers() == null) {
      this.add(new JLabel("Timeout"));
      this.validate();
      return;
    }

    // Header
    Vector<String> header = new Vector<String>();
    header.add(mLocalizer.msg("column1", "Time"));
    Set<String> datSet = new TreeSet<String>();

    // Drei Wochen anlegen
    Calendar cal = new GregorianCalendar();
    cal.set(Calendar.MILLISECOND, 0);
    cal.set(Calendar.SECOND, 0);
    for (int i = 0; i < 21; i++) {
      String datBeg = String.format(FMT_DATE, cal.getTime());
      if (!datSet.contains(datBeg)) {
        header.add(datBeg);
        datSet.add(datBeg);
      }
      cal.add(Calendar.DAY_OF_MONTH, 1);
    }

    Calendar calBeg;
    Calendar calEnd;
    for (Map<String, String> timer : mTimerHelper.getTimers()) {
      // Calendar
      calBeg = E2TimerHelper.getAsCalendar(timer.get(E2TimerHelper.TIMEBEGIN));
      calBeg.add(Calendar.HOUR, -mHourOffset);
      calEnd = E2TimerHelper.getAsCalendar(timer.get(E2TimerHelper.TIMEEND));
      calEnd.add(Calendar.HOUR, -mHourOffset);

      // Ueberschrift
      String datBeg = String.format(FMT_DATE, calBeg.getTime());
      String datEnd = String.format(FMT_DATE, calEnd.getTime());
      // neue Spalte?
      if (!datSet.contains(datBeg)) {
        header.add(datBeg);
        datSet.add(datBeg);
      }
      if (!datSet.contains(datEnd)) {
        header.add(datEnd);
        datSet.add(datEnd);
      }
    }

    // leeres Modell erstellen
    Vector<Vector<Object>> data = new Vector<Vector<Object>>();
    for (int row = 0; row < ROW_MAX; row++) {
      Vector<Object> v = new Vector<Object>();
      for (int col = 0; col < header.size(); col++) {
        if (col == 0) {
          v.add(String.format(FMT_VON_BIS, getViewRow(row, mHourOffset)));
        } else {
          // Image erzeugen
          BufferedImage image = new BufferedImage((X_MAX + 1) * rgb.length,
              Y_MAX, BufferedImage.TYPE_3BYTE_BGR);
          WritableRaster raster = image.getRaster();
          // Kasten
          for (int y = 0; y < Y_MAX; y++) {
            // Linie
            for (int x = 0; x < ((X_MAX + 1) * rgb.length); x++) {
              raster.setPixel(x, y, RGB_WHITE);
            }
          }
          v.add(new ImageIcon(image));
        }
      }
      data.add(v);
    }

    // TableModel
    TableModel model = new TimerChartModel(data, header);

    // Tabelle
    mTable.setAutoResizeMode(JTable.AUTO_RESIZE_OFF);
    mTable.setModel(model);
    mTable.setDefaultRenderer(Object.class, new TimerChartCellRenderer());
    mTable.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
    mTable.setColumnSelectionAllowed(true);
    mTable.getTableHeader().setReorderingAllowed(false);
    mTable.setRowHeight(Y_MAX + 1);

    // Columns
    TableColumnModel columnModel = mTable.getColumnModel();
    for (int i = 0; i < columnModel.getColumnCount(); i++) {
      TableColumn col = columnModel.getColumn(i);
      String name = mTable.getColumnName(i);
      col.setIdentifier(name);
      if (i == 0) {
        col.setPreferredWidth(80);
      } else {
        col.setPreferredWidth(rgb.length * (X_MAX + 1) + 15);
      }
    }

    List<Map<String, String>> timers = mTimerHelper.getRepeatedTimers();

    // Timer anzeigen
    int idx = 0;
    String datVgl = null;
    for (Map<String, String> timer : timers) {

      calBeg = E2TimerHelper.getAsCalendar(timer.get(E2TimerHelper.TIMEBEGIN));
      calEnd = E2TimerHelper.getAsCalendar(timer.get(E2TimerHelper.TIMEEND));

      // ToolTip erzeugen
      String name = String.format(
          "<b>%1$s</b>  [%2$ta, %2$tH:%2$tM-%3$tH:%3$tM, %4$s]", timer
              .get(E2TimerHelper.NAME), calBeg.getTime(), calEnd.getTime(),
          timer.get(E2TimerHelper.SERVICENAME));

      // Farbcode benoetigt folgende Variablen
      boolean expired = calEnd.getTimeInMillis() < new GregorianCalendar()
          .getTimeInMillis();
      boolean repeated = Integer.parseInt(timer.get(E2TimerHelper.REPEATED)) > 0;
      boolean disabled = Integer.parseInt(timer.get(E2TimerHelper.DISABLED)) > 0;

      // Offset fuer Anzeige
      calBeg.add(Calendar.HOUR, -mHourOffset);
      calEnd.add(Calendar.HOUR, -mHourOffset);
      String datBeg = String.format(FMT_DATE, calBeg.getTime());
      String datEnd = String.format(FMT_DATE, calEnd.getTime());

      if (datVgl == null) {
        datVgl = datBeg;
      } else if (!datVgl.equals(datBeg)) {
        idx = 0;
        datVgl = datBeg;
      }

      int columnIndexBeg = 0;
      int columnIndexEnd = 0;
      try {
        columnIndexBeg = mTable.getColumn(datBeg).getModelIndex();
        columnIndexEnd = mTable.getColumn(datEnd).getModelIndex();
      } catch (IllegalArgumentException e) {
        continue;
      }
      int rowIndexBeg = calBeg.get(Calendar.HOUR_OF_DAY);
      int rowIndexEnd = calEnd.get(Calendar.HOUR_OF_DAY);
      int minBeg = calBeg.get(Calendar.MINUTE);
      int minEnd = calEnd.get(Calendar.MINUTE);
      int rowIndexEndMax = rowIndexEnd;
      int xOffset = idx * (X_MAX + 1);

      // Zeichenraster des ImageIcons
      WritableRaster raster;

      if (rowIndexBeg > rowIndexEnd) {
        rowIndexEndMax = 23;
        // Stunden am naechsten Tag anzeigen
        for (int rowIndex = 0; rowIndex <= rowIndexEnd; rowIndex++) {
          raster = getRaster(model, rowIndex, columnIndexEnd, name);
          // Kasten
          for (int y = 0; y < Y_MAX; y++) {
            setLine(raster, y, xOffset, rgb[idx], expired, disabled, repeated);
          }
        }
      }
      // Stunden am aktuellen Tag anzeigen
      for (int rowIndex = rowIndexBeg; rowIndex <= rowIndexEndMax; rowIndex++) {
        raster = getRaster(model, rowIndex, columnIndexBeg, name);
        // Kasten
        for (int y = 0; y < Y_MAX; y++) {
          setLine(raster, y, xOffset, rgb[idx], expired, disabled, repeated);
        }
      }

      // Beginn-Minuten korrigieren, aktueller Tag
      raster = getRaster(model, rowIndexBeg, columnIndexBeg, null);
      // Kasten
      for (int y = 0; y < minBeg * Y_MAX / 60; y++) {
        setLine(raster, y, xOffset, RGB_WHITE, false, false, false);
      }

      if (rowIndexBeg > rowIndexEnd) {
        // Ende-Minuten korrigieren naechster Tag
        raster = getRaster(model, rowIndexEnd, columnIndexEnd, null);
        // Kasten
        for (int y = minEnd * Y_MAX / 60; y < Y_MAX; y++) {
          setLine(raster, y, xOffset, RGB_WHITE, false, false, false);
        }
      } else {
        // Ende-Minuten korrigieren, aktueller Tag
        raster = getRaster(model, rowIndexEnd, columnIndexBeg, null);
        // Kasten
        for (int y = minEnd * Y_MAX / 60; y < Y_MAX; y++) {
          setLine(raster, y, xOffset, RGB_WHITE, false, false, false);
        }
      }

      // naechste Spalte
      idx += 1;
      if (idx >= rgb.length) {
        idx = 0;
      }
    }

    // Scrollpane fuer Tabelle
    JScrollPane scrollpane = new JScrollPane(mTable);

    // Panel fuer Timer-Chart
    this.removeAll();
    this.setLayout(new BorderLayout());
    this.add(scrollpane, BorderLayout.CENTER);
    this.validate();
  }

  /**
   * Linie zeichnen
   * 
   * @param raster
   * @param y
   * @param xOffset
   * @param color
   * @param expired
   * @param disabled
   * @param repeated
   */
  private void setLine(WritableRaster raster, int y, int xOffset, int[] color,
      boolean expired, boolean disabled, boolean repeated) {
    if (expired) {
      color = RGB_GRAY;
    } else if (disabled) {
      color = RGB_YELLOW;
    } else if (repeated) {
      color = RGB_BLUE;
    }
    for (int x = 0 + xOffset; x < X_MAX + xOffset; x++) {
      raster.setPixel(x, y, color);
    }
  }
}
