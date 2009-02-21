package tvbrowser.ui.configassistant;

import java.awt.BorderLayout;
import java.awt.Font;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.io.File;
import java.io.IOException;

import javax.swing.BorderFactory;
import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JFileChooser;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JOptionPane;
import javax.swing.JPanel;
import javax.swing.JTextArea;

import tvbrowser.TVBrowser;
import tvbrowser.core.Settings;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;
import util.ui.progress.Progress;
import util.ui.progress.ProgressWindow;


public class TvBrowserUpdateAssistant extends JDialog implements ActionListener,WindowClosingIf {

  private JButton mChooseBtn, mOkBtn, mCancelBtn;
  private JLabel mInfoLb;

  private boolean mListingsSuccessfullyImported = false;

  private int mResult = CANCEL;
  public static final int IMPORT_SUCCESS = 1;
  public static final int CANCEL = 2;
  public static final int CONFIGURE_TVBROWSER = 3;



  /** The localizer for this class. */
    private static final util.ui.Localizer mLocalizer
      = util.ui.Localizer.getLocalizerFor(TvBrowserUpdateAssistant.class);


  public TvBrowserUpdateAssistant(JFrame parent) {
    super(parent, true);
    setTitle(mLocalizer.msg("updateTo", "Update to TV-Browser {0}",TVBrowser.VERSION));

    UiUtilities.registerForClosing(this);
    
    JPanel contentPane=(JPanel)getContentPane();
    contentPane.setBorder(BorderFactory.createEmptyBorder(15,20,11,11));

    JLabel welcomeLabel = new JLabel(mLocalizer.msg("welcome","Welcome to TV-Browser {0}!",TVBrowser.VERSION));
    welcomeLabel.setFont(new Font("SansSerif", Font.BOLD, 18));
    welcomeLabel.setBorder(BorderFactory.createEmptyBorder(0,0,15,0));

    JTextArea lb = util.ui.UiUtilities.createHelpTextArea(mLocalizer.msg("explain","...", TVBrowser.VERSION));
    lb.setFont(new Font("SansSerif", Font.PLAIN, 14));

    JPanel importerPn = new JPanel(new BorderLayout());
    importerPn.setBorder(BorderFactory.createEmptyBorder(25,0,25,0));
    mChooseBtn = new JButton(mLocalizer.msg("chooseDirectory","Zu importierende TV-Daten auswählen..."));
    mInfoLb=new JLabel();
    mInfoLb.setFont(mInfoLb.getFont().deriveFont(Font.ITALIC));
    importerPn.add(mChooseBtn, BorderLayout.WEST);
    importerPn.add(mInfoLb, BorderLayout.CENTER);
    mChooseBtn.addActionListener(this);

    JPanel btnPn = new JPanel();
    btnPn.add(mOkBtn = new JButton(mLocalizer.msg("startTVBrowser","TV-Browser starten")));
    btnPn.add(mCancelBtn = new JButton(Localizer.getLocalization(Localizer.I18N_CANCEL)));

    mOkBtn.addActionListener(this);
    mCancelBtn.addActionListener(this);

    JPanel content = new JPanel(new BorderLayout());
    JPanel header = new JPanel(new BorderLayout());
    header.add(welcomeLabel, BorderLayout.NORTH);
    header.add(lb, BorderLayout.CENTER);

    content.add(header, BorderLayout.NORTH);
    content.add(importerPn, BorderLayout.CENTER);

    contentPane.setLayout(new BorderLayout());

    contentPane.add(content, BorderLayout.NORTH);
    contentPane.add(btnPn, BorderLayout.SOUTH);

    setSize(480,300);
  }


  public void actionPerformed(ActionEvent event) {
    Object source = event.getSource();
    if (source == mChooseBtn) {
      JFileChooser fileChooser = new JFileChooser();
      fileChooser.setFileSelectionMode(JFileChooser.DIRECTORIES_ONLY);
      int retVal = fileChooser.showOpenDialog(this);
      if (retVal == JFileChooser.APPROVE_OPTION) {
        File f=fileChooser.getSelectedFile();

        if (f!=null) {
          File destination = new File(Settings.propTVDataDirectory.getString());
          if (f.equals(destination)) {
            JOptionPane.showMessageDialog(this, mLocalizer.msg("error.1","Ungültiges Verzeichnis - Sie haben das bestehende Daten-Verzeichnis gewählt."));
          }
          else {
            ImportHandler importHandler = new ImportHandler(f);
            if (importHandler.getChannelCount()==0) {
              JOptionPane.showMessageDialog(this, mLocalizer.msg("error.2","The selected directory doesn't contain valid TV listings."), mLocalizer.msg("error.2.title","Invalid directory"), JOptionPane.WARNING_MESSAGE);
            }
            else {
              String[] options = new String[]{mLocalizer.msg("import","Importieren"),Localizer.getLocalization(Localizer.I18N_CANCEL)};
               int n = JOptionPane
                  .showOptionDialog(
                      this,
                      mLocalizer
                          .msg(
                              "question.1",
                              "Es können TV-Daten mit {0} Sendern importiert werden.\nJetzt importieren?",
                              importHandler.getChannelCount()),
                  mLocalizer.msg("question.1.title","Importieren"),
                  JOptionPane.YES_NO_OPTION,
                  JOptionPane.QUESTION_MESSAGE,
                  null,
                  options,
                  options[0]);
              if (n == JOptionPane.YES_OPTION) {
                  doTheImport(importHandler, destination);
              }
            }
          }
        }
      }
    }
    else if (source == mOkBtn) {
      if (!mListingsSuccessfullyImported) {
        String[] options = new String[]{mLocalizer.msg("option.1","TV-Browser starten und neu einrichten"),Localizer.getLocalization(Localizer.I18N_CANCEL)};
        int n = JOptionPane.showOptionDialog(this, mLocalizer.msg("question.2","Sie haben kein TV-Daten für den Import ausgewählt.\nSind Sie sicher, dass Sie evtl. vorhandene Sender-Einstellungen verwerfen und TV-Browser neu einrichten wollen?"),
                mLocalizer.msg("import","Importieren"),
                JOptionPane.YES_NO_OPTION,
                JOptionPane.QUESTION_MESSAGE,
                null,
                options,
                options[0]);
        if (n == JOptionPane.YES_OPTION) {
          mResult = CONFIGURE_TVBROWSER;
          setVisible(false);
        }
      }
      else { // mListingsSuccessfullyImported == true
        mResult = IMPORT_SUCCESS;
        setVisible(false);
      }
    }
    else if (source == mCancelBtn) {
      mResult = CANCEL;
      setVisible(false);
    }
  }



  private void doTheImport(final ImportHandler handler, final File destination) {
    ProgressWindow win = new ProgressWindow(this, mLocalizer.msg("status.1","Importiere TV-Daten..."));
    win.run(new Progress(){
      public void run() {
        try {
          handler.importTo(destination);
          mInfoLb.setText(mLocalizer.msg("status.2", "{0} Sender importiert",
              handler.getChannelCount()));
          mListingsSuccessfullyImported = true;
        }catch(IOException e) {
          util.exc.ErrorHandler.handle(mLocalizer.msg("error.3","Could not import TV listings"), e);
        }
      }
    });
  }

  public int getResult() {
    return mResult;
  }


  public void close() {
    mResult = CANCEL;
    setVisible(false);
  }

}
