package imdbplugin;

import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.Version;
import devplugin.ActionMenu;
import util.misc.SoftReferenceCache;
import util.ui.Localizer;
import util.ui.UiUtilities;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.util.Properties;
import java.awt.event.ActionEvent;
import java.awt.Window;
import java.text.DecimalFormat;
import javax.swing.Icon;
import javax.swing.AbstractAction;
import javax.swing.Action;
import javax.swing.JOptionPane;
import javax.swing.JCheckBox;
import javax.swing.JComboBox;
import javax.swing.SwingUtilities;
import javax.swing.JDialog;
import javax.swing.JFrame;

public class ImdbPlugin extends Plugin {
  /**
   * Translator
   */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ImdbPlugin.class);

  private static final Version mVersion = new Version(1, 0);

  private PluginInfo mPluginInfo;
  private ImdbDatabase mImdbDatabase;
  private SoftReferenceCache<Program, ImdbRating> mRatingCache = new SoftReferenceCache<Program, ImdbRating>();
  private Properties mProperties;


  public ImdbPlugin() {
    mImdbDatabase = new ImdbDatabase(new File(Plugin.getPluginManager().getTvBrowserSettings().getTvBrowserUserHome(), "imdbDatabase"));
    mImdbDatabase.init();
  }

  @Override
  public PluginInfo getInfo() {
    if (mPluginInfo == null) {
      String name = mLocalizer.msg("pluginName", "Imdb Ratings");
      String desc = mLocalizer.msg("description", "Display Imdb ratings in programs");
      String author = "TV-Browser Team";

      mPluginInfo = new PluginInfo(ImdbPlugin.class, name, desc, author);
    }

    return mPluginInfo;
  }

  public static Version getVersion() {
    return mVersion;
  }

  @Override
  public Icon[] getProgramTableIcons(Program program) {
    ImdbRating rating = getRatingFor(program);
    if (rating == null) {
      return null;
    }

    return new Icon[]{new ImdbIcon(rating)};
  }

  private ImdbRating getRatingFor(Program program) {
    ImdbRating rating = mRatingCache.get(program);
    if (rating == null) {
      rating = mImdbDatabase.getRatingForId(mImdbDatabase.getMovieId(program.getTitle(), "", -1));
      mRatingCache.put(program, rating);
    }
    return rating;
  }

  @Override
  public ActionMenu getContextMenuActions(Program program) {
    ImdbRating rating = getRatingFor(program);
    if (rating != null) {
      AbstractAction action = new AbstractAction() {
        public void actionPerformed(ActionEvent evt) {
        }
      };
      action.putValue(Action.NAME, mLocalizer.msg("contextMenuDetails", "Details zur Imdb-Bewertung ({0})",  new DecimalFormat("##.#").format((double)rating.getRating() / 10)));
      action.putValue(Action.SMALL_ICON, new ImdbIcon(rating));
      return new ActionMenu(action);
    }
    return null;
  }

  @Override
  public String getProgramTableIconText() {
    return mLocalizer.msg("iconText", "Imdb Rating");
  }

  @Override
  public void handleTvBrowserStartFinished() {
    if (!mProperties.getProperty("dontAskCreateDatabase", "false").equals("true") && !mImdbDatabase.isInitialised()) {
      SwingUtilities.invokeLater(new Runnable(){
        public void run() {
          JCheckBox askAgain = new JCheckBox(mLocalizer.msg("dontShowAgain", "Don't show this message again"));
          Object[] shownObjects = new Object[2];
          shownObjects[0] = mLocalizer.msg("downloadData", "No IMDB-Database available, should I download the ImDB-Data now (aprox. 10MB) ?");
          shownObjects[1] = askAgain;

          int ret = JOptionPane.showConfirmDialog(getParentFrame(), shownObjects, mLocalizer.msg("downloadDataTitle","No data available"), JOptionPane.YES_NO_OPTION);

          if (askAgain.isSelected()) {
            mProperties.setProperty("dontAskCreateDatabase", "true");
          }

          if (ret == JOptionPane.YES_OPTION) {
            showUpdateDialog();
          }
        }
      });
    }
  }

  private void showUpdateDialog() {
    JComboBox box = new JComboBox(new String[] {"ftp.fu-berlin.de", "ftp.funet.fi", "ftp.sunet.se", "TEMP TEST"});
    Object[] shownObjects = new Object[2];
    shownObjects[0] = "Bitte den Server wählen:";
    shownObjects[1] = box;

    int ret = JOptionPane.showConfirmDialog(getParentFrame(), shownObjects, mLocalizer.msg("downloadDataTitle","No data available"), JOptionPane.OK_CANCEL_OPTION);

    if (ret == JOptionPane.OK_OPTION) {
      String server = null;
      switch (box.getSelectedIndex()) {
        case 0 : server = "ftp://ftp.fu-berlin.de/pub/misc/movies/database/";
                 break;
        case 1 : server = "ftp://ftp.funet.fi/pub/mirrors/ftp.imdb.com/pub/";
                 break;
        case 2 : server = "ftp://ftp.sunet.se/pub/tv+movies/imdb/";
                 break;
        case 3 : server = "file:///home/bodum/tmp/";
                 break;
      }
      Window w = UiUtilities.getBestDialogParent(getParentFrame());

      ImdbUpdateDialog dialog = null;
      if (w instanceof JFrame) {
        dialog = new ImdbUpdateDialog((JFrame) UiUtilities.getBestDialogParent(getParentFrame()), server, mImdbDatabase);
      } else {
        dialog = new ImdbUpdateDialog((JDialog) UiUtilities.getBestDialogParent(getParentFrame()), server, mImdbDatabase);
      }

      UiUtilities.centerAndShow(dialog);
    }
  }

  @Override
  public void loadSettings(Properties settings) {
    mProperties = settings;
  }

  @Override
  public Properties storeSettings() {
    return mProperties;
  }
}
