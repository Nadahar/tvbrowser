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

  
  // Bolle Edit: New Method createDialog(), with formatted text (table added), add_info ,
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
    String stylesheetDef = "<head><style type=\"text/css\">"
        + ".headlineSmall { line-height:100%;text-indent:0em;font-size:16 pt; color:#000000;font-family:Verdana, Arial, Helvetica;}"
        + ".headlineFilm { line-height:110%;margin-bottom:5 px;text-indent:0em;font-size:20 pt; color:#003366; font-weight:bold;font-family:Verdana, Arial, Helvetica;}"
        + ".category { line-height:100%;font-size:12 pt; color:#000000; font-family: Verdana, Arial, Helvetica;}"
        + ".uTRow {line-height:100%;margin-left:20 px;font-size:12 pt;  font-family:Verdana, Arial, Helvetica; }"
        + ".reciepient { line-height:100%;font-size:12 pt; color:#003366; font-family:Verdana, Arial, Helvetica;}"
        + "td { margin-right:5 px;margin-top:0 px;margin-bottom:0 px;vertical-align:top;padding-right:0 px;padding-top:0 px;padding-bottom:0 px;padding-left:0 px;}"
        + "</style></head>";

    text.append("<html>"+ stylesheetDef + "<body style=\"margin-left:0 px;\"><div class=\"headlineSmall\">&nbsp;")
        .append(mLocalizer.msg("movieAwardFor", "Movie Awards for"))
        .append(" </div><div class=\"headlineFilm\">&nbsp;").append(
            program.getTitle()).append("</div>");
    text.append("<table>");

    for (MovieAward maward : mMovieAwards) {
      for (Award award : maward.getAwardsFor(program)) {
        text.append("<tr  class=\"uTRow\"><td style=\"margin-left:10 px;\" valign=\"top\">&#x25CF;</td><td class=\"category\">").append(
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
          text.append("</td></tr><tr class=\"reciepient\"><td></td><td>");
          switch (award.getStatus()) {
          case NOMINATED:
            text.append(mLocalizer.msg("forNominee",
                        "nominee: {0}", award
                            .getRecipient()))
                .append("");
            break;
          case WINNER:
            text.append(mLocalizer.msg("forAwardee",
                        "awardee: {0}", award
                            .getRecipient()))
                .append("");
            break;
          case HONORED:
            text.append(mLocalizer.msg("forHonored",
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
          text.append("</td></tr><tr class=\"reciepient\"><td></td><td>"+award.getAdd_Info());
        }
        //
        text.append("</td></tr>");
      }
    }

    text.append("</table></body></html>");

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
