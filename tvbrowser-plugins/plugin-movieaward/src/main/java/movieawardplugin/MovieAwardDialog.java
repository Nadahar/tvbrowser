package movieawardplugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;
import java.util.logging.Logger;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import util.ui.Localizer;
import util.ui.WindowClosingIf;

import com.jgoodies.forms.builder.ButtonBarBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.Sizes;

import devplugin.Program;

public class MovieAwardDialog extends JDialog implements WindowClosingIf {
  /**
   * Translator
   */
  private static final transient Localizer mLocalizer = Localizer
      .getLocalizerFor(MovieAwardDialog.class);

  private static final Logger mLog = Logger.getLogger(MovieAwardDialog.class
      .getName());

  public MovieAwardDialog(final JFrame frame,
      final ArrayList<MovieAward> mMovieAwards, final Program program) {
    super(frame, true);
    createDialog(mMovieAwards, program);
  }

  public MovieAwardDialog(final JDialog dialog,
      final ArrayList<MovieAward> mMovieAwards, final Program program) {
    super(dialog, true);
    createDialog(mMovieAwards, program);
  }

  // Bolle Note: Old Method, but Difference between Nominee and Winner ,
  // add_Info, setCaretPosition(0) added.
  // After this method comes the new one with formatted Text
  private void createDialogold(final ArrayList<MovieAward> mMovieAwards,
      final Program program) {
    setTitle(mLocalizer.msg("title", "Movie Awards"));

    final JPanel panel = (JPanel) getContentPane();
    panel.setBorder(Borders.DLU4_BORDER);
    panel.setLayout(new FormLayout("fill:min:grow",
        "fill:min:grow, 3dlu, pref"));

    final CellConstraints cc = new CellConstraints();

    final StringBuilder text = new StringBuilder();

    text.append("<html><body><h1>&nbsp;").append(
        mLocalizer.msg("movieAwardFor", "Movie Awards for")).append(
        " <i>").append(program.getTitle()).append("</i></h1><br>");
    text.append("<ul>");

    for (MovieAward maward : mMovieAwards) {
      for (Award award : maward.getAwardsFor(program)) {
        text.append("<li>").append(maward.getName()).append(' ')
            .append(award.getAwardYear()).append(": ");

        final String category = maward.getCategoryName(award
            .getCategory());
        switch (award.getStatus()) {
        case NOMINATED:
          text.append(mLocalizer.msg("nominated",
              "Nominated for the category {0}", category));
          break;
        case WINNER:
          text.append(mLocalizer.msg("winner",
              "Winner of the category {0}", category));
          break;
        case HONORED:
          text.append(mLocalizer.msg("honored",
              "Honored in the category {0}", category));
          break;
        default:
          mLog.severe("Missing implementation for award status");
        }

        if (award.getRecipient() != null) {
          //
          //
          // Bolle edit: Difference between Nominee and Winner (and
          // Honored)added here in localizer/properties
          switch (award.getStatus()) {
          case NOMINATED:
            text.append("<br/><i>").append(
                mLocalizer.msg("forNominee", "nominee: {0}",
                    award.getRecipient())).append("</i>");
            break;
          case WINNER:
            text.append("<br/><i>").append(
                mLocalizer.msg("forAwardee", "awardee: {0}",
                    award.getRecipient())).append("</i>");
            break;
          case HONORED:
            text.append("<br/><i>").append(
                mLocalizer.msg("forHonored", "honored: {0}",
                    award.getRecipient())).append("</i>");
            break;
          default:
            mLog
                .severe("Missing implementation for award recipient");
          }
        }
        // Bolle Edit: Additional Information for Song and Animated
        // Short Subject added
        if (award.getAdd_Info() != null) {
          text.append("<br/>").append(award.getAdd_Info());
        }
        //
        text.append("</li>");
      }
    }

    text.append("</ul></body></html>");

    final JEditorPane pane = new JEditorPane("text/html", text.toString());
    pane.setEditable(false);
    panel.add(new JScrollPane(pane), cc.xy(1, 1));

    final ButtonBarBuilder builder = new ButtonBarBuilder();

    final JButton ok = new JButton(Localizer
        .getLocalization(Localizer.I18N_OK));

    ok.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        close();
      }
    });

    builder.addGlue();
    builder.addGridded(ok);

    panel.add(builder.getPanel(), cc.xy(1, 3));
    // Edit by Bolle 400/400 Pixel
    setSize(Sizes.dialogUnitXAsPixel(600, this), Sizes.dialogUnitYAsPixel(
        400, this));

    getRootPane().setDefaultButton(ok);
    ok.requestFocusInWindow();
    // Edit by Bolle: Scroll to the top of the dialog
    pane.setCaretPosition(0);
    //
  }

  // Bolle Edit: New Method createDialog(), with formatted text, add_info ,
  // Nominee and setCaretPosition(0)
  private void createDialog(final ArrayList<MovieAward> mMovieAwards,
      final Program program) {
    setTitle(mLocalizer.msg("title", "Movie Awards"));

    final JPanel panel = (JPanel) getContentPane();
    panel.setBorder(Borders.DLU4_BORDER);
    panel.setLayout(new FormLayout("fill:min:grow",
        "fill:min:grow, 3dlu, pref"));

    final CellConstraints cc = new CellConstraints();
    final StringBuilder text = new StringBuilder();

    // Bolle Edit: Styles/Classes for formatting text added to the
    // JEditorPane
    String stylesheetDef = new String();
    stylesheetDef = "<head><style type=\"text/css\">"
        + ".headlineSmall { line-height:100%;text-indent:0em;font-size:16 pt; color:#000000;font-family:Verdana, Arial, Helvetica;}"
        + ".headlineFilm { line-height:110%;text-indent:0em;font-size:20 pt; color:#003366; font-weight:bold;font-family:Verdana, Arial, Helvetica;}"
        + ".category { font-size:12 pt; color:#000000; font-family: Verdana, Arial, Helvetica;}"
        + ".uList { list-style-type:none; line-height:100%;margin-left:20 px;font-size:12 pt;  font-family:Verdana, Arial, Helvetica; }"
        + ".reciepient { font-size:12 pt; color:#003366; font-family:}"
        + "</style></head>";

    text.append("<html>"+ stylesheetDef + "<body style=\"margin-left:0 px;\"><div class=\"headlineSmall\">&nbsp;")
        .append(mLocalizer.msg("movieAwardFor", "Movie Awards for"))
        .append(" </div><div class=\"headlineFilm\">&nbsp;").append(
            program.getTitle()).append("</div>");
    text.append("<ul  class=\"uList\">");

    for (MovieAward maward : mMovieAwards) {
      for (Award award : maward.getAwardsFor(program)) {
        text.append("<li><div class=\"category\">•  ").append(
            maward.getName()).append(' ').append(
            award.getAwardYear()).append(": ");

        final String category = maward.getCategoryName(award
            .getCategory());
        switch (award.getStatus()) {
        case NOMINATED:
          text.append(mLocalizer.msg("nominated",
              "Nominated for the category {0}", category));
          break;
        case WINNER:
          text.append(mLocalizer.msg("winner",
              "Winner of the category {0}", category));
          break;
        case HONORED:
          text.append(mLocalizer.msg("honored",
              "Honored in the category {0}", category));
          break;
        default:
          mLog.severe("Missing implementation for award status");
        }

        if (award.getRecipient() != null) {
          //
          //
          // Bolle edit: Difference between Nominee and Winner (and
          // Honored)added here and in localizer/properties
          switch (award.getStatus()) {
          case NOMINATED:
            text.append("</div><div class=\"reciepient\">   ")
                .append(
                    mLocalizer.msg("forNominee",
                        "nominee: {0}", award
                            .getRecipient()))
                .append("");
            break;
          case WINNER:
            text.append("</div><div class=\"reciepient\">   ")
                .append(
                    mLocalizer.msg("forAwardee",
                        "awardee: {0}", award
                            .getRecipient()))
                .append("");
            break;
          case HONORED:
            text.append("</div><div class=\"reciepient\">   ")
                .append(
                    mLocalizer.msg("forHonored",
                        "honored: {0}", award
                            .getRecipient()))
                .append("");
            break;
          default:
            mLog
                .severe("Missing implementation for award recipient");
          }
        }
        // Bolle Edit: Additional Information for Song and Animated
        // Short Subject added
        if (award.getAdd_Info() != null) {
          text.append(award.getAdd_Info());
        }
        //
        text.append("</div></li>");
      }
    }

    text.append("</ul></body></html>");

    final JEditorPane pane = new JEditorPane("text/html", text.toString());
    pane.setEditable(false);
    panel.add(new JScrollPane(pane), cc.xy(1, 1));

    final ButtonBarBuilder builder = new ButtonBarBuilder();

    final JButton ok = new JButton(Localizer
        .getLocalization(Localizer.I18N_OK));

    ok.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        close();
      }
    });

    builder.addGlue();
    builder.addGridded(ok);

    panel.add(builder.getPanel(), cc.xy(1, 3));
    // Edit by Bolle Pixel
    setSize(Sizes.dialogUnitXAsPixel(620, this), Sizes.dialogUnitYAsPixel(
        310, this));

    getRootPane().setDefaultButton(ok);
    ok.requestFocusInWindow();
    // Edit by Bolle: Scroll to the top of the dialog
    pane.setCaretPosition(0);
    //
  }

  public void close() {
    setVisible(false);
  }

}
