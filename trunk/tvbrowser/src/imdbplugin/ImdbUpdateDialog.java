package imdbplugin;

import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.Sizes;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.builder.ButtonBarBuilder;

import javax.swing.JDialog;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JLabel;
import javax.swing.JProgressBar;
import javax.swing.JButton;

import util.ui.Localizer;
import util.exc.ErrorHandler;

import java.awt.event.ActionListener;
import java.awt.event.ActionEvent;
import java.io.IOException;

public class ImdbUpdateDialog extends JDialog {
  /**
   * Translator
   */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ImdbUpdateDialog.class);
  private String mServer;
  private ImdbDatabase mDatabase;
  private ImdbParser mParser;

  public ImdbUpdateDialog(JFrame frame, String server, ImdbDatabase db) {
    super(frame, true);
    mServer = server;
    mDatabase = db;
    createGui();
  }

  public ImdbUpdateDialog(JDialog dialog, String server, ImdbDatabase db) {
    super(dialog, true);
    mServer = server;
    mDatabase = db;
    createGui();
  }

  private void createGui() {
    setTitle(mLocalizer.msg("downloadingTitle","Downloading IMDb-Data"));

    JPanel panel = (JPanel) getContentPane();
    panel.setLayout(new FormLayout("fill:pref:grow", "pref, 3dlu, pref, fill:3dlu:grow, pref"));
    panel.setBorder(Borders.DLU4_BORDER);

    CellConstraints cc = new CellConstraints();

    panel.add(new JLabel(mLocalizer.msg("downloadingMsg","Processing Imdb Data...")), cc.xy(1,1));
    panel.add(new JProgressBar(), cc.xy(1,3));

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
    new Thread(new Runnable() {
      public void run() {
        mParser = new ImdbParser(mDatabase, mServer);
        try {
          mParser.startParsing();
        } catch (IOException e) {
          ErrorHandler.handle("Problems during processing of IMDb Data", e);
        }
        setVisible(false);
      }
    }).start();
  }

  private void stopThread() {
    if (mParser != null) {
      mParser.stopParsing();
    }
  }
}
