package primarydatamanager.primarydataservice.util;

import devplugin.Program;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class helps guessing the category (movie, series, show) for a program using
 * the genre
 *
 * @since 2.6
 */
public class InfoCategoryParser {

  private final String[] MOVIES = {"spielfilm", "tv movie", "pilotfilm", "romantische komödie", "psychothriller", "dokumentarfilm",
          "horrorkomödie", "komödie", "thriller", "western", "science fiction", "science-fiction", "musikfilm", "heimatfilm",
          "tv-film", "horrorthriller", "jugendfilm", "tv-krimi", "erotikfilm", "fantasy", "drama", "animationsfilm", "abenteuerfilm",
          "zeichentrickkomödie", "kinderfilm", "actionfilm", "kriminalfilm", "horrorfilm", "actionthriller", "actionkomödie",
          "tv-drama", "kriegsdrama", "familiendrama", "gesellschaftsdrama", "literaturverfilmung", "experimentalfilm", "actiondrama",
          "kriegsfilm", "episodenfilm", "italowestern", "jugenddrama", "roadmovie", "gaunerkomödie", "beziehungskomödie",
          "familienfilm", "familienfilm", "bikerfilm", "liebesdrama", "sportfilm", "agentenfilm", "teeniekomödie",
          "katastrophenfilm", "krimikomödie", "politthriller", "tragikomödie", "thrillerkomödie", "romanze", "krimi", "tv-thriller",
          "westernkomödie", "romantikkomödie", "mysteryfilm", "science-fiction-komödie", "erotikthriller", "kurzfilm", "action-thriller",
          "krimi-thriller", "wildlife-serie", "sci-fi-komödie", "krimi-komödie", "kino-klassiker", "fantasy-komödie", "katastrophen-thriller",
          "gangster-komödie", "fantasy-abenteuer", "sci-fi-abenteuer", "alien-action", "krimi/thriller", "milieufilm",
          "biographischer film", "portraitfilm (entwicklungsfilme)", "alltags-/familienfilm", "krimi/thriller", "klassiker/weltliteratur",
          "märchenfilm", "gesellschaftskomödie", "tanzfilm", "zeichentrick-/puppentrickfilm" 
  };
  private final String[] SERIES = {"serie", "serienspecial", "soap", "sitcom", "familien-serie", "anwaltsserie", "krimi-serie",
          "action-serie", "science fiction-serie", "mystery-serie", "drama-serie", "abenteuer/mystery-serie",
          "action/abenteuer-serie", "arztserie", "dokumentarserie", "anime-serie", "abenteuerserie", "krimiserie", "actionserie",
          "science-fiction-serie", "zeichentrickserie", "polizeiserie", "erotikserie", "animationsserie", "puppentrickserie",
          "comedyserie", "familienserie", "kinderserie", "krankenhausserie", "westernserie", "agentenserie", "dramaserie",
          "kriegsserie", "horrorserie", "thrillerserie", "monumentalserie", "sci-fi-serie", "abenteuer-serie"};
  private final String[] NEWS = {"nachrichten"};
  private final String[] SHOW = {"show"};
  private final String[] MAGAZINE = {"magazin", "reportage", "news-magazin", "news + sportnachrichten", "wetter", "society magazin",
          "erotik-magazin", "erotikmagazin", "wissensmagazin", "motorsportmagazin", "kinomagazin", "reisereportage", "buchjournal",
          "reisemagazin", "service-magazin", "fußballmagazin", "wissens-magazin", "kinder-magazin"};

  private ArrayList<String> mMovies, mSeries, mNews, mShows, mMagazines, mUnknown;

  /**
   * Constructor
   */
  public InfoCategoryParser() {
    mMovies    = new ArrayList<String>(Arrays.asList(MOVIES));
    mSeries    = new ArrayList<String>(Arrays.asList(SERIES));
    mNews      = new ArrayList<String>(Arrays.asList(NEWS));
    mShows     = new ArrayList<String>(Arrays.asList(SHOW));
    mMagazines = new ArrayList<String>(Arrays.asList(MAGAZINE));

    mUnknown = new ArrayList<String>();
  }

  public boolean isMovie(String s) {
    return mMovies.contains(s);
  }

  public boolean isSeries(String s) {
    return mSeries.contains(s);
  }

  public boolean isNews(String s) {
    return mNews.contains(s);
  }

  public boolean isShows(String s) {
    return mShows.contains(s);
  }

  public boolean isMagazineOrInfotainment(String s) {
    return mMagazines.contains(s);
  }

  public int getCategory(String s) {
    s = s.trim().toLowerCase();

    if (s.length() == 0) {
      return 0;
    } else if (isMovie(s)) {
      return Program.INFO_CATEGORIE_MOVIE;
    } else if (isSeries(s)) {
      return Program.INFO_CATEGORIE_SERIES;
    } else if (isNews(s)) {
      return Program.INFO_CATEGORIE_NEWS;
    } else if (isShows(s)) {
      return Program.INFO_CATEGORIE_SHOW;
    } else if (isMagazineOrInfotainment(s)) {
      return Program.INFO_CATEGORIE_MAGAZINE_INFOTAINMENT;
    }

    if (!mUnknown.contains(s)) {
      mUnknown.add(s);
      System.out.println("Unkown Category : " + s);
    }

    return 0;
  }
}
