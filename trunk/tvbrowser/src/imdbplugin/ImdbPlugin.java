package imdbplugin;

import devplugin.Plugin;
import devplugin.PluginInfo;
import devplugin.Version;
import devplugin.Program;

import java.util.zip.GZIPInputStream;
import java.io.FileInputStream;
import java.io.File;
import java.io.IOException;

import util.ui.Localizer;
import util.misc.SoftReferenceCache;

import javax.swing.Icon;
import javax.swing.ImageIcon;

public class ImdbPlugin extends Plugin {

  /** Translator */
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ImdbPlugin.class);

  private static final Version mVersion = new Version(1,0);
  private static final Icon DUMMY_ICON = new ImageIcon();

  private PluginInfo mPluginInfo;
  private ImdbDatabase mImdbDatabase;
  private SoftReferenceCache<Program, Icon> mRatingCache = new SoftReferenceCache<Program, Icon>();

  public ImdbPlugin() {
    mImdbDatabase = new ImdbDatabase(new File(Plugin.getPluginManager().getTvBrowserSettings().getTvBrowserUserHome(), "imdbDatabase"));
    mImdbDatabase.init();
  }

  @Override
  public PluginInfo getInfo() {
    if(mPluginInfo == null) {
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
    Icon icon = mRatingCache.get(program);
    if (icon == null) {
      icon = DUMMY_ICON;
      String movieId = mImdbDatabase.getMovieId(program.getTitle(),"", -1);

      if (movieId != null) {
        int rating = mImdbDatabase.getRatingForId(movieId);
        System.out.println(rating);
        if (rating >= 50) {
          icon = getPluginManager().getIconFromTheme(this, "actions", "view-refresh", 16);
        }
      }

      mRatingCache.put(program, icon);
    }

    if (icon == DUMMY_ICON) {
      return null;
    }

    return new Icon[] {icon};
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

    String  id;

    id = db.getMovieId("Enterprise", "Babel One", 1983);
    if (id != null) {
      System.out.println(db.getRatingForId(id));
    } else {
      System.out.println("Not found!");
    }

    id = db.getMovieId("Star Trek: Enterprise", "Babel One", 1983);
    if (id != null) {
      System.out.println(db.getRatingForId(id));
    } else {
      System.out.println("Not found!");
    }

    db.close();
  }

}
