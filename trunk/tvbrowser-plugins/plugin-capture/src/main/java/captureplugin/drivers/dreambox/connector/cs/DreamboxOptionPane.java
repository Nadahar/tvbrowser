package captureplugin.drivers.dreambox.connector.cs;

import java.awt.BorderLayout;
import java.awt.Dimension;
import java.awt.Frame;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.ToolTipManager;

import util.ui.Localizer;
import captureplugin.CapturePlugin;
import captureplugin.drivers.dreambox.DreamboxConfig;
import captureplugin.drivers.dreambox.connector.DreamboxConnector;

/**
 * @author fishhead
 * 
 */
public class DreamboxOptionPane {
  // Logger
  private static final Logger mLog = Logger.getLogger(DreamboxOptionPane.class.getName());
  // Translator
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(DreamboxOptionPane.class);

  /**
   * Anzeige der Timer-Belegung
   * 
   * @param mConnector
   * @param parent
   */
  public static void showTimer(final DreamboxConnector mConnector) {

    // Config ermitteln
    final DreamboxConfig config = mConnector.getConfig();

    // Timer einlesen
    E2TimerHelper timerHelper = E2TimerHelper.getInstance(config);

    // Movies einlesen
    E2MovieHelper movieHelper = E2MovieHelper.getInstance(config, timerHelper.getThread());

    // Locations einlesen
    E2LocationHelper locationHelper = E2LocationHelper.getInstance(config, movieHelper.getThread());

    // Info einlesen
    E2InfoHelper infoThread = E2InfoHelper.getInstance(config, locationHelper.getThread());

    // Panel erstellen
    final DreamboxTimerListPanel dreamboxTimerListPanel = new DreamboxTimerListPanel(mConnector, timerHelper);
    // Panel fuer Timer-Chart
    final DreamboxTimerChartPanel dreamboxTimerChartPanel = new DreamboxTimerChartPanel(timerHelper);
    // Panel fuer Movie-Liste ohne Sorter
    final DreamboxMovieListPanel dreamboxMovieListPanel = new DreamboxMovieListPanel(mConnector, movieHelper);
    // Panel fuer Info-Liste ohne Sorter
    final DreamboxInfoListPanel dreamboxInfoListPanel = new DreamboxInfoListPanel(mConnector, infoThread);

    // TabbedPane
    final JTabbedPaneRefresh tabbedPane = new JTabbedPaneRefresh();
    tabbedPane.addChangeListener(tabbedPane);
    // tabs einhaengen
    tabbedPane.addTab(mLocalizer.msg("tab1Title", "Timer-List"), null, dreamboxTimerListPanel, mLocalizer.msg(
        "tab1ToolTip", "List of programmed timers"));
    tabbedPane.addTab(mLocalizer.msg("tab2Title", "Timer-Chart"), null, dreamboxTimerChartPanel, mLocalizer.msg(
        "tab2ToolTip", "Chart of programmed timers"));
    tabbedPane.addTab(mLocalizer.msg("tab3Title", "Movie-List"), null, dreamboxMovieListPanel, mLocalizer.msg(
        "tab3ToolTip", "List of recorded movies"));
    tabbedPane.addTab(mLocalizer.msg("tab4Title", "Info-List"), null, dreamboxInfoListPanel, mLocalizer.msg(
        "tab4ToolTip", "List of info"));

    // ToolTip laenger anzeigen
    int dismiss = ToolTipManager.sharedInstance().getDismissDelay();
    ToolTipManager.sharedInstance().setDismissDelay(10000);

    final CapturePlugin capturePlugin = CapturePlugin.getInstance();
    Frame frame;
    int widthFrame;
    int heightFrame;
    if (capturePlugin != null) {
      frame = capturePlugin.getSuperFrame();
      widthFrame = frame.getWidth();
      heightFrame = frame.getHeight();
    } else {
      // only for panel testing
      frame = null;
      widthFrame = 1366;
      heightFrame = 768;
    }
    JPanel panel = new JPanel(new BorderLayout());
    panel.add(tabbedPane);
    JOptionPane optionPane = new JOptionPane(panel, JOptionPane.INFORMATION_MESSAGE, JOptionPane.DEFAULT_OPTION);
    // begrenzen aufs Hauptfenster
    int widthOptionPane = optionPane.getPreferredSize().width;
    int heightOptionPane = optionPane.getPreferredSize().height;
    //
    int width = Math.min(widthFrame - 6, widthOptionPane);
    int height = Math.min(heightFrame - 32, heightOptionPane);
    optionPane.setPreferredSize(new Dimension(width, height));
    // Dialog
    JDialog dialog = optionPane.createDialog(/* frame, */
    mLocalizer.msg("title", "Display"));
    dialog.setVisible(true);

    // Backup timers.xml
    Thread thread = new Thread() {
      @Override
      public void run() {
        String remote = "/usr/local/share/enigma2/timers.xml"; // Image 0.4.5
        String local = System.getProperty("user.home") + File.separatorChar + "timers_" + config.getDreamboxAddress()
            + ".xml";
        FtpHelper ftpHelper = new FtpHelper();
        ftpHelper.cmd("OPEN", config.getDreamboxAddress());
        ftpHelper.cmd("LOGIN", config.getUserName(), config.getPassword());
        String s = ftpHelper.cmd("GET", remote);
        if (s == null) {
          remote = "/etc/enigma2/timers.xml"; // Image 0.5.1
          s = ftpHelper.cmd("GET", remote);
        }
        ftpHelper.cmd("CLOSE");
        if (s != null) {
          try {
            FileWriter fw = new FileWriter(local);
            fw.write(new String(s.getBytes("UTF-8")));
            fw.close();
            mLog.info("copy " + remote + " to " + local);
          } catch (IOException e) {
            mLog.log(Level.WARNING, "IOException", e);
          }
        }
      }
    };
    thread.start();

    ToolTipManager.sharedInstance().setDismissDelay(dismiss);
  }

  private DreamboxOptionPane() {
    mLog.setLevel(Level.INFO);    
  }

  /**
   * Tester
   * 
   * @param args
   */
  public static void main(String[] args) {
    JFrame frame = new JFrame("DreamboxInfoListPanel - Tester");
    frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
    frame.setPreferredSize(new Dimension(400, 300));

    JPanel panel = new JPanel();
    JButton button = new JButton("show");
    panel.add(button);
    button.addActionListener(new ActionListener() {

      public void actionPerformed(ActionEvent e) {
        DreamboxConfig dreamboxConfig = new DreamboxConfig();
        dreamboxConfig.setDreamboxAddress("dreambox");
        dreamboxConfig.setBeforeTime(1);
        dreamboxConfig.setAfterTime(10);
        dreamboxConfig.setTimeout(4000);
        dreamboxConfig.setUserName("root");
        dreamboxConfig.setPassword(new char[0]);
        dreamboxConfig.setMediaplayer("D:\\MultiMedia\\VLC\\vlc.exe");
        showTimer(new DreamboxConnector(dreamboxConfig));
      }

    });
    frame.getContentPane().add(panel);
    frame.pack();
    frame.setVisible(true);
  }
}
