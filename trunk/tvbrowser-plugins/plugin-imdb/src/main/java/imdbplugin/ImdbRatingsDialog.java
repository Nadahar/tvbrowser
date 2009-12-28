package imdbplugin;

import com.jgoodies.forms.builder.ButtonBarBuilder2;
import com.jgoodies.forms.builder.PanelBuilder;
import com.jgoodies.forms.factories.Borders;
import com.jgoodies.forms.layout.CellConstraints;
import com.jgoodies.forms.layout.FormLayout;
import com.jgoodies.forms.layout.RowSpec;
import com.jgoodies.forms.layout.Sizes;
import devplugin.Program;
import org.jfree.chart.ChartFactory;
import org.jfree.chart.ChartPanel;
import org.jfree.chart.JFreeChart;
import org.jfree.chart.axis.AxisLocation;
import org.jfree.chart.axis.NumberAxis;
import org.jfree.chart.plot.CategoryPlot;
import org.jfree.chart.plot.PlotOrientation;
import org.jfree.chart.renderer.category.BarRenderer;
import org.jfree.data.category.CategoryDataset;
import org.jfree.data.general.DatasetUtilities;
import util.ui.Localizer;
import util.ui.UiUtilities;
import util.ui.WindowClosingIf;
import util.ui.html.ExtendedHTMLDocument;
import util.ui.html.ExtendedHTMLEditorKit;

import javax.swing.JButton;
import javax.swing.JDialog;
import javax.swing.JEditorPane;
import javax.swing.JPanel;
import javax.swing.JScrollPane;
import javax.swing.SwingUtilities;
import java.awt.Color;
import java.awt.Frame;
import java.awt.Paint;
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

    JEditorPane editor = new JEditorPane();
    editor.setEditorKit(new ExtendedHTMLEditorKit());
    editor.setText(createText((ExtendedHTMLDocument)editor.getDocument()));
    editor.setEditable(false);
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
    setSize(Sizes.dialogUnitXAsPixel(300, this),
            Sizes.dialogUnitXAsPixel(200, this));
  }

  private String createText(ExtendedHTMLDocument doc) {
    StringBuilder builder = new StringBuilder();

    ImdbRating episode = plugin.getEpisodeRating(program);
    if (episode != null) {
      builder.append("<h3>").append(mLocalizer.msg("episodeRating", "Rating of Episode")).append(":</h3>");
      builder.append("<p style='margin-left:10px'>");
      builder.append(makeTextForRating(doc, episode));
      builder.append("</p>");
      builder.append("<br><hr>");
    }

    ImdbRating rating = plugin.getProgramRating(program);
    builder.append("<h3>").append(mLocalizer.msg("rating", "Rating")).append(":</h3>");
    builder.append("<p style='margin-left:10px'>");
    builder.append(makeTextForRating(doc, rating));
    builder.append("</p>");

    return builder.toString();
  }

  private String makeTextForRating(ExtendedHTMLDocument doc, ImdbRating rating) {
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
    builder.append(mLocalizer.msg("votes", "votes")).append(" : <i><b>").append(rating.getVotes()).append("</b></i><br><br>");
    builder.append(mLocalizer.msg("distribution", "Distribution")).append(":<br>");

    builder.append(doc.createCompTag(createChartForRating(rating))).append("<br>");

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

  private ChartPanel createChartForRating(ImdbRating rating) {
    final JFreeChart chart = ChartFactory.createBarChart(
        null,         // chart title
        mLocalizer.msg("rating", "Rating"),                 // domain axis label
        mLocalizer.msg("votesInPrecent","Votes in %"),                // range axis label
        createDataSet(rating),      // data
        PlotOrientation.HORIZONTAL, // orientation
        false,                       // include legend
        true,
        false
    );

    chart.setBackgroundPaint(Color.WHITE);

    final CategoryPlot plot = chart.getCategoryPlot();
    plot.setRangeAxisLocation(AxisLocation.BOTTOM_OR_LEFT);

    BarRenderer renderer = new BarRenderer(){
      @Override
      public Paint getItemPaint(int row, int column) {
        return Color.yellow;
      }
    };
    renderer.setDrawBarOutline(false);

    plot.setRenderer(renderer);

    final NumberAxis rangeAxis = (NumberAxis) plot.getRangeAxis();
    rangeAxis.setStandardTickUnits(NumberAxis.createIntegerTickUnits());

    return new ChartPanel(chart);
  }

  private CategoryDataset createDataSet(ImdbRating rating) {
    final double[][] data = new double[1][10];

    String dist = rating.getDistribution();
    for (int i=0;i<10;i++){
      if (dist.charAt(i) == '.') {
        data[0][i] = 0;
      } else if (dist.charAt(i) == '*') {
        data[0][i] = 100;
      } else {
        data[0][i] = (Integer.parseInt(""+dist.charAt(i))) * 10 + 5;
      }
    }

    String[] keys = {"1", "2", "3", "4", "5", "6", "7", "8", "9", "10"};

    return DatasetUtilities.createCategoryDataset(new String[]{"hallo"}, keys, data);
  }

  public void close() {
    setVisible(false);
  }
}