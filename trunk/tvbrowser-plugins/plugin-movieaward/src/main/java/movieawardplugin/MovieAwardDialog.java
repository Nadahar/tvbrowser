package movieawardplugin;

import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.ArrayList;

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

  private void createDialog(final ArrayList<MovieAward> mMovieAwards,
      final Program program) {
    setTitle(mLocalizer.msg("title", "Movie Awards"));

    final JPanel panel = (JPanel) getContentPane();
    panel.setBorder(Borders.DLU4_BORDER);
    panel.setLayout(new FormLayout("fill:min:grow", "fill:min:grow, 3dlu, pref"));

    final CellConstraints cc = new CellConstraints();

    final StringBuilder text = new StringBuilder();

    text.append("<html><body><h1>&nbsp;").append(
        mLocalizer.msg("movieAwardFor", "Movie Awards for")).append(" <i>")
        .append(program.getTitle()).append("</i></h1><br>");
    text.append("<ul>");

    for (MovieAward maward : mMovieAwards) {
      for (Award award : maward.getAwardsFor(program)) {
        text.append("<li>").append(maward.getName()).append(' ').append(
            award.getAwardYear()).append(": ");

        final String category = maward.getCategoryName(award.getCategory());
        switch(award.getStatus()) {
          case NOMINATED:
          text.append(mLocalizer.msg("nominated",
              "Nominated for the category {0}", category));
          break;
        case WINNER:
          text.append(mLocalizer.msg("winner", "Winner of the category {0}",
              category));
          break;
        case HONORED:
          text.append(mLocalizer.msg("honored", "Honored in the category {0}",
              category));
          break;
        }

        if (award.getRecipient() != null) {
          text.append("<br/><i>").append(
              mLocalizer.msg("for", "awardee: {0}", award.getRecipient()))
              .append("</i>");
        }
        text.append("</li>");
      }
    }

    text.append("</ul></body></html>");

    final JEditorPane pane = new JEditorPane("text/html", text.toString());
    pane.setEditable(false);
    panel.add(new JScrollPane(pane), cc.xy(1,1));

    final ButtonBarBuilder builder = new ButtonBarBuilder();

    final JButton ok = new JButton(Localizer.getLocalization(Localizer.I18N_OK));

    ok.addActionListener(new ActionListener() {
      public void actionPerformed(final ActionEvent e) {
        close();
      }
    });

    builder.addGlue();
    builder.addGridded(ok);

    panel.add(builder.getPanel(), cc.xy(1,3));

    setSize(Sizes.dialogUnitXAsPixel(300, this),
            Sizes.dialogUnitYAsPixel(200, this));

    getRootPane().setDefaultButton(ok);
    ok.requestFocusInWindow();
  }

  public void close() {
    setVisible(false);
  }
}
