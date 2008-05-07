package imdbplugin;

import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Program;
import devplugin.Version;
import devplugin.ActionMenu;
import util.misc.SoftReferenceCache;
import util.ui.Localizer;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.util.zip.GZIPInputStream;
import java.awt.event.ActionEvent;
import java.text.DecimalFormat;
import javax.swing.Icon;
import javax.swing.ImageIcon;
import javax.swing.AbstractAction;
import javax.swing.Action;

public class ImdbPlugin extends Plugin {
  /**
   * Translator
   */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ImdbPlugin.class);

  private static final Version mVersion = new Version(1, 0);

  private PluginInfo mPluginInfo;
  private ImdbDatabase mImdbDatabase;
  private SoftReferenceCache<Program, ImdbRating> mRatingCache = new SoftReferenceCache<Program, ImdbRating>();

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
    ImdbRating rating = mRatingCache.get(program);
    if (rating == null) {
      rating = mImdbDatabase.getRatingForId(mImdbDatabase.getMovieId(program.getTitle(), "", -1));
      mRatingCache.put(program, rating);
    }

    if (rating == null) {
      return null;
    }

    return new Icon[]{new ImdbIcon(rating)};
  }

  @Override
  public ActionMenu getContextMenuActions(Program program) {
    ImdbRating rating = mImdbDatabase.getRatingForId(mImdbDatabase.getMovieId(program.getTitle(), "", -1));
    if (rating != null) {
      AbstractAction action = new AbstractAction() {
        public void actionPerformed(ActionEvent evt) {
        }
      };
      action.putValue(Action.NAME, mLocalizer.msg("contextMenuDetails", "Details zur Imdb-Bewertung ({0})",  new DecimalFormat("##.#").format((double)rating.getRating() / 10)));
      action.putValue(Action.SMALL_ICON, new ImdbIcon(mRatingCache.get(program)));
      return new ActionMenu(action);
    }
    return null;
  }

  @Override
  public String getProgramTableIconText() {
    return mLocalizer.msg("iconText", "Imdb Rating");
  }

  public static void main(String[] args) {
    System.out.println("INIT DB");

    ImdbDatabase db = new ImdbDatabase(new File("testDb"));
    db.init();

    db.deleteDatabase();
    db.init();

    System.out.println("Parsing");

    ImdbParser parser = new ImdbParser(db);

    try {
      parser.parseAkaTitles(new GZIPInputStream(new FileInputStream(new File("/home/bodum/tmp/aka-titles.list.gz"))));
      db.close();
      db.reOpen();
      db.optimizeIndex();
      parser.parseRatings(new GZIPInputStream(new FileInputStream(new File("/home/bodum/tmp/ratings.list.gz"))));
    } catch (IOException e) {
      e.printStackTrace();
    }

    try {
      db.optimizeIndex();
    } catch (IOException e) {
      e.printStackTrace();
    }

    String id;

    id = db.getMovieId("Enterprise", "Babel One", 1983);
    if (id != null) {
      System.out.println(db.getRatingForId(id).getRating());
    } else {
      System.out.println("Not found!");
    }

    id = db.getMovieId("Star Trek: Enterprise", "Babel One", 1983);
    if (id != null) {
      System.out.println(db.getRatingForId(id).getRating());
    } else {
      System.out.println("Not found!");
    }

    db.close();
  }

}
