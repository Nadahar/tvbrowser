package imdbplugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.IOException;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JPanel;
import javax.swing.JProgressBar;
import javax.swing.SwingUtilities;

import util.exc.ErrorHandler;
import util.ui.Localizer;
import util.ui.progress.ProgressBarProgressMonitor;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;

import devplugin.ProgressMonitor;

public final class ImdbUpdateDialog extends JDialog {
  /**
   * Translator
   */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ImdbUpdateDialog.class);
  private String mServer;
  private ImdbDatabase mDatabase;
  private ImdbParser mParser;
  private ProgressMonitor mMonitor;
  private ImdbPlugin mPlugin;

  public ImdbUpdateDialog(ImdbPlugin plugin, JFrame frame, String server, ImdbDatabase db) {
    super(frame, true);
    mServer = server;
    mDatabase = db;
    mPlugin = plugin;
    createGui();
  }

  public ImdbUpdateDialog(ImdbPlugin plugin, JDialog dialog, String server, ImdbDatabase db) {
    super(dialog, true);
    mServer = server;
    mDatabase = db;
    mPlugin = plugin;
    createGui();
  }

  private void createGui() {
    setTitle(mLocalizer.msg("downloadingTitle","Downloading IMDb-Data"));

    JPanel panel = (JPanel) getContentPane();
    panel.setLayout(new FormLayout("fill:pref:grow", "pref, 3dlu, pref, fill:3dlu:grow, pref"));
    panel.setBorder(Borders.DLU4_BORDER);

    CellConstraints cc = new CellConstraints();

    JLabel label = new JLabel(mLocalizer.msg("downloadingMsg", "Processing IMDb data..."));
    panel.add(label, cc.xy(1,1));
    JProgressBar progressBar = new JProgressBar();
    panel.add(progressBar, cc.xy(1,3));

    JButton cancel = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL));
    cancel.addActionListener(new ActionListener() {
      public void actionPerformed(ActionEvent e) {
        cancelPressed();
      }
    });
    ButtonBarBuilder builder = new ButtonBarBuilder();
    builder.addGlue();
    builder.addGriddedButtons(new JButton[] {cancel});
    panel.add(builder.getPanel(), cc.xy(1,5));

    mMonitor = new ProgressBarProgressMonitor(progressBar, label);
    setSize(Sizes.dialogUnitXAsPixel(200, this), Sizes.dialogUnitYAsPixel(100, this));
  }

  private void cancelPressed() {
    setVisible(false);
  }

  @Override
  public void setVisible(boolean b) {
    if (b) {
      startThread();
    } else {
      stopThread();
    }
    super.setVisible(b);
  }

  private void startThread() {
    Thread thread = new Thread(new Runnable() {
      public void run() {
        
        mParser = new ImdbParser(mDatabase, mServer);
        try {
          mParser.startParsing(mMonitor);
        } catch (IOException e) {
          ErrorHandler.handle("Problems during processing of IMDb Data", e);
        }
        SwingUtilities.invokeLater(new Runnable() {
          public void run() {
            setVisible(false);
            mPlugin.updateCurrentDateAndClearCache();
          }
        });
      }
    }, "IMDb import");
    thread.setPriority(Thread.MIN_PRIORITY);
    thread.start();
  }

  private void stopThread() {
    if (mParser != null) {
      mParser.stopParsing();
    }
  }
}
