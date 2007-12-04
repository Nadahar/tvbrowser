package primarydatamanager.primarydataservice.util;

import devplugin.Program;

import java.util.ArrayList;
import java.util.Arrays;

/**
 * This class helps guessing the category (movie, series, show) for a program
 * using the genre
 * 
 * @since 2.6
 */
public class InfoCategoryParser {

  private final String[] MOVIES = { "abenteuerfilm", "actiondrama",
      "actionfilm", "actionkomödie", "actionthriller", "agentenfilm",
      "alienaction", "alltags/familienfilm", "animationsfilm",
      "beziehungskomödie", "bikerfilm", "biographischerfilm", "dokumentarfilm",
      "drama", "episodenfilm", "erotikfilm", "erotikthriller",
      "experimentalfilm", "familiendrama", "familienfilm", "fantasyabenteuer",
      "fantasykomödie", "fernsehfilm", "gangsterkomödie", "gaunerkomödie",
      "gesellschaftsdrama", "gesellschaftskomödie", "heimatfilm", "horrorfilm",
      "horrorkomödie", "horrorthriller", "italowestern", "jugenddrama",
      "jugendfilm", "katastrophenfilm", "katastrophenthriller", "kinderfilm",
      "kinoklassiker", "klassiker/weltliteratur", "komödie", "kriegsdrama",
      "kriegsfilm", "krimi/thriller", "krimikomödie", "kriminalfilm",
      "krimithriller", "kurzfilm", "liebesdrama", "literaturverfilmung",
      "märchenfilm", "milieufilm", "musikfilm", "mysteryfilm", "pilotfilm",
      "politthriller", "portraitfilm(entwicklungsfilme)", "psychothriller",
      "roadmovie", "romantikkomödie", "romantischekomödie", "romanze",
      "sciencefiction", "sciencefictionkomödie", "scifiabenteuer",
      "scifikomödie", "spielfilm", "sportfilm", "tanzfilm", "teeniekomödie",
      "thriller", "thrillerkomödie", "tragikomödie", "tvdrama", "tvfilm",
      "tvkrimi", "tvmovie", "tvthriller", "western", "westernkomödie",
      "wildlifeserie", "zeichentrick/puppentrickfilm", "zeichentrickkomödie"

  };

  private final String[] SERIES = { "abenteuer/mysteryserie", "abenteuerserie",
      "action/abenteuerserie", "actionserie", "agentenserie",
      "animationsserie", "animeserie", "anwaltsserie", "arztserie",
      "comedyserie", "dokumentarserie", "dramaserie", "erotikserie",
      "familienserie", "fernsehserie", "horrorserie", "kinderserie",
      "krankenhausserie", "kriegsserie", "krimiserie", "monumentalserie",
      "mysteryserie", "polizeiserie", "puppentrickserie",
      "sciencefictionserie", "scifiserie", "serie", "serienspecial", "sitcom",
      "soap", "thrillerserie", "westernserie", "zeichentrickserie",
      "zoodokusoap" };

  private final String[] NEWS = { "nachrichten", "nachrichtenmagazin",
      "regionalnachrichten" };

  private final String[] SHOW = { "musik", "show", "talkshow" };

  private final String[] MAGAZINE = { "auslandsmagazin", "automagazin",
      "boulevardmagazin", "buchjournal", "diskussion", "doku", "dokumentation",
      "erfindermagazin", "erotikmagazin", "erotikmagazin", "filmmagazin",
      "freizeitmagazin", "fußballmagazin", "geschichtsmagazin", "gespräch",
      "gesundheitsmagazin", "interview", "kindermagazin", "kinomagazin",
      "kulturmagazin", "ländermagazin", "lifestylemagazin", "literaturmagazin",
      "magazin", "medienmagazin", "motorsportmagazin", "news+sportnachrichten",
      "newsmagazin", "politikmagazin", "politmagazin", "ratgebermagazin",
      "regionalmagazin", "reisedokumentation", "reisemagazin",
      "reiseundreportagemagazin", "reisereportage", "reportage",
      "reportageundreisemagazin", "servicemagazin", "societymagazin",
      "sozialmagazin", "technikmagazin", "umweltmagazin", "verbrauchermagazin",
      "wetter", "wirtschaftsmagazin", "wissenschaftsmagazin", "wissensmagazin",
      "zeitgeschichte" };

  private ArrayList<String> mMovies, mSeries, mNews, mShows, mMagazines,
      mUnknown;

  /**
   * Constructor
   */
  public InfoCategoryParser() {
    mMovies = new ArrayList<String>(Arrays.asList(MOVIES));
    mSeries = new ArrayList<String>(Arrays.asList(SERIES));
    mNews = new ArrayList<String>(Arrays.asList(NEWS));
    mShows = new ArrayList<String>(Arrays.asList(SHOW));
    mMagazines = new ArrayList<String>(Arrays.asList(MAGAZINE));

    mUnknown = new ArrayList<String>();
  }

  public boolean isMovie(String s) {
    s = s.replaceAll("(-| )", "");
    return mMovies.contains(s);
  }

  public boolean isSeries(String s) {
    s = s.replaceAll("(-| )", "");
    return mSeries.contains(s);
  }

  public boolean isNews(String s) {
    s = s.replaceAll("(-| )", "");
    return mNews.contains(s);
  }

  public boolean isShows(String s) {
    s = s.replaceAll("(-| )", "");
    return mShows.contains(s);
  }

  public boolean isMagazineOrInfotainment(String s) {
    s = s.replaceAll("(-| )", "");
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
