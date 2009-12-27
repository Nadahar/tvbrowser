package imdbplugin;

import com.jgoodies.forms.builder.ButtonBarBuilder2;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.Sizes;
import devplugin.Program;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Rectangle;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;

public class ImdbRatingsDialog extends JDialog implements WindowClosingIf {
  /**
   * Translator
   */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ImdbRatingsDialog.class);

  private Program program;
  private ImdbPlugin plugin;

  public ImdbRatingsDialog(ImdbPlugin plug, Frame parentFrame, Program prog) {
    super(UiUtilities.getBestDialogParent(parentFrame));
    setModal(true);
    program = prog;
    plugin = plug;
    createGui();
    UiUtilities.registerForClosing(this);
  }

  private void createGui() {
    setTitle(mLocalizer.msg("title", "ImdB Rating for {0}", program.getTitle()));

    final FormLayout layout = new FormLayout("fill:min:grow");
    final PanelBuilder panel = new PanelBuilder(layout, (JPanel) getContentPane());

    panel.setBorder(Borders.DLU4_BORDER);

    layout.appendRow(RowSpec.decode("fill:min:grow"));

    final JEditorPane editor = UiUtilities.createHtmlHelpTextArea(createText(), Color.WHITE);
    editor.setBackground(Color.WHITE);
    editor.setOpaque(true);

    CellConstraints cc = new CellConstraints();

    final JScrollPane scroll = new JScrollPane(editor);
    // Scroll to the beginning
    Runnable runnable = new Runnable() {
      public void run() {
        scroll.getVerticalScrollBar().setValue(0);
      }
    };
    SwingUtilities.invokeLater(runnable);

    panel.add(scroll, cc.xy(1,panel.getRowCount()));

    layout.appendRow(RowSpec.decode("3dlu"));
    layout.appendRow(RowSpec.decode("pref"));

    JButton okButton = new JButton(Localizer.getLocalization(Localizer.I18N_OK));
    okButton.addActionListener(new ActionListener(){
      public void actionPerformed(ActionEvent e) {
        close();  
      }
    });

    ButtonBarBuilder2 buttonBuilder = new ButtonBarBuilder2();
    buttonBuilder.addGlue();
    buttonBuilder.addButton(new JButton[] {okButton});

    panel.add(buttonBuilder.getPanel(), cc.xy(1,panel.getRowCount()));

    getRootPane().setDefaultButton(okButton);
    setSize(Sizes.dialogUnitXAsPixel(200, this),
            Sizes.dialogUnitXAsPixel(150, this));
  }

  private String createText() {
    StringBuilder builder = new StringBuilder();

    ImdbRating episode = plugin.getEpisodeRating(program);
    if (episode != null) {
      builder.append("<h3>").append(mLocalizer.msg("episodeRating", "Rating of Episode")).append(":</h3>");
      builder.append("<p style='margin-left:10px'>");
      builder.append(makeTextForRating(episode));
      builder.append("</p>");
      builder.append("<br><hr>");
    }

    ImdbRating rating = plugin.getProgramRating(program);
    builder.append("<h3>").append(mLocalizer.msg("rating", "Rating")).append(":</h3>");
    builder.append("<p style='margin-left:10px'>");
    builder.append(makeTextForRating(rating));
    builder.append("</p>");

    return builder.toString();
  }

  private String makeTextForRating(ImdbRating rating) {
    final StringBuilder builder = new StringBuilder();
    final ImdbMovie movie = plugin.getDatabase().getMovieForId(rating.getMovieId());

    builder.append("<b>").append(movie.getTitle());
    if (movie.getYear() > 0) {
      builder.append(" (").append(movie.getYear()).append(")");
    }
    builder.append("</b><br>");

    if (movie.getEpisode() != null && movie.getEpisode().length() > 0) {
      builder.append("<i>").append(movie.getEpisode()).append("</i>").append("<br>");
    }

    builder.append("<br>");

    builder.append(mLocalizer.msg("rating", "Rating")).append(": <i><b>").append(rating.getRatingText()).append("</b>/10</i> - ");
    builder.append(mLocalizer.msg("votes", "votes")).append(" : <i>").append(rating.getVotes()).append("</i><br>");
    builder.append(mLocalizer.msg("distribution", "Distribution")).append(": <i>").append(rating.getDistribution()).append("</i><br>");

    ImdbAka[] akas = movie.getAkas();
    if (akas.length > 0) {
      builder.append("<br>").append(mLocalizer.msg("alternativeTitle", "Alternative title")).append(":<br><ul>");
      for (final ImdbAka aka:movie.getAkas()) {
        builder.append("<li>");
        builder.append(aka.getTitle());
        if (aka.getEpisode() != null && aka.getEpisode().length() > 0) {
          builder.append(" - ").append(aka.getEpisode());
        }
        builder.append(" (").append(aka.getYear()).append(")");
        builder.append("</li>");
      }
      builder.append("</ul>");

    }

    return builder.toString();
  }

  public void close() {
    setVisible(false);
  }
}