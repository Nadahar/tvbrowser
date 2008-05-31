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
      "actionfilm", "actionkom�die", "actionthriller", "agentenfilm",
      "alienaction", "alltags/familienfilm", "animationsfilm",
      "beziehungskom�die", "bikerfilm", "biographischerfilm", 
      "drama", "episodenfilm", "erotikfilm", "erotikthriller",
      "experimentalfilm", "familiendrama", "familienfilm", "fantasyabenteuer",
      "fantasykom�die", "fernsehfilm", "gangsterkom�die", "gaunerkom�die",
      "gesellschaftsdrama", "gesellschaftskom�die", "heimatfilm", "horrorfilm",
      "horrorkom�die", "horrorthriller", "italowestern", "jugenddrama",
      "jugendfilm", "katastrophenfilm", "katastrophenthriller", "kinderfilm",
      "kinoklassiker", "klassiker/weltliteratur", "kom�die", "kriegsdrama",
      "kriegsfilm", "krimi/thriller", "krimikom�die", "kriminalfilm",
      "krimithriller", "kurzfilm", "liebesdrama", "literaturverfilmung",
      "m�rchenfilm", "milieufilm", "musikfilm", "mysteryfilm", "pilotfilm",
      "politthriller", "portraitfilm(entwicklungsfilme)", "psychothriller",
      "roadmovie", "romantikkom�die", "romantischekom�die", "romanze",
      "sciencefiction", "sciencefictionkom�die", "scifiabenteuer",
      "scifikom�die", "spielfilm", "sportfilm", "tanzfilm", "teeniekom�die",
      "thriller", "thrillerkom�die", "tragikom�die", "tvdrama", "tvfilm",
      "tvkrimi", "tvmovie", "tvthriller", "western", "westernkom�die",
      "wildlifeserie", "zeichentrick/puppentrickfilm", "zeichentrickkom�die"

  };

  private final String[] SERIES = { "abenteuer/mysteryserie", "abenteuerserie",
      "action/abenteuerserie", "actionserie", "agentenserie",
      "animationsserie", "animeserie", "anwaltsserie", "arztserie",
      "comedyserie", "dramaserie", "erotikserie",
      "familienserie", "fernsehserie", "horrorserie", "kinderserie",
      "krankenhausserie", "kriegsserie", "krimiserie", "monumentalserie",
      "mysteryserie", "polizeiserie", "puppentrickserie",
      "sciencefictionserie", "scifiserie", "serie", "serienspecial", "sitcom",
      "soap", "thrillerserie", "westernserie", "zeichentrickserie",
      "zoodokusoap", "justizserie", "polit-/drama-serie", "mafia-serie"  };

  private final String[] NEWS = { "nachrichten", "nachrichtenmagazin",
  "regionalnachrichten" };

  private final String[] SHOW = { "musik", "show", "talkshow" };

  private final String[] MAGAZINE = { "auslandsmagazin", "automagazin",
      "boulevardmagazin", "buchjournal", "diskussion", 
      "erfindermagazin", "erotikmagazin", "filmmagazin",
      "freizeitmagazin", "fu�ballmagazin", "geschichtsmagazin", "gespr�ch",
      "gesundheitsmagazin", "interview", "kindermagazin", "kinomagazin",
      "kulturmagazin", "l�ndermagazin", "lifestylemagazin", "literaturmagazin",
      "magazin", "medienmagazin", "motorsportmagazin", "news+sportnachrichten",
      "newsmagazin", "politikmagazin", "politmagazin", "ratgebermagazin",
      "regionalmagazin", "reisemagazin", "reiseundreportagemagazin", 
      "reportageundreisemagazin", "servicemagazin", "societymagazin",
      "sozialmagazin", "technikmagazin", "umweltmagazin", "verbrauchermagazin",
      "wetter", "wirtschaftsmagazin", "wissenschaftsmagazin", "wissensmagazin",
   };

  private final String[] DOCUMENTARY = { "dokumentarfilm", "doku", "dokumentation", 
      "reisedokumentation",  "reisereportage", "reportage", "dokumentarserie",
      "zeitgeschichte"
  };
  
  private final String[] ARTS = { "theater", "konzert", "oper", "musikkonzert",
      "ballett", "schauspiel" ,"tanz", "musical", "operette", "tanztheater", "volkstheater"
  };
  
  private final String[] SPORTS = { "sport"
  };
  
  private final String[] OTHERS = { "werbung", "werbesendung", "dauerwerbesendung",
      "tvshop", "teleshopping", "teleshop", "infomercials"
  };

  private ArrayList<String> mMovies, mSeries, mNews, mShows, mMagazines, mDocumentary, mArts,
  mSports, mOthers, mUnknown;

  /**
   * Constructor
   */
  public InfoCategoryParser() {
    mMovies = new ArrayList<String>(Arrays.asList(MOVIES));
    mSeries = new ArrayList<String>(Arrays.asList(SERIES));
    mNews = new ArrayList<String>(Arrays.asList(NEWS));
    mShows = new ArrayList<String>(Arrays.asList(SHOW));
    mMagazines = new ArrayList<String>(Arrays.asList(MAGAZINE));
    mDocumentary = new ArrayList<String>(Arrays.asList(DOCUMENTARY));
    mArts = new ArrayList<String>(Arrays.asList(ARTS));
    mSports = new ArrayList<String>(Arrays.asList(SPORTS));
    mOthers = new ArrayList<String>(Arrays.asList(OTHERS));

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
  
  public boolean isDocumentary(String s) {
    s = s.replaceAll("(-| )", "");
    return mDocumentary.contains(s);
  }
  
  public boolean isArts(String s) {
    s = s.replaceAll("(-| )", "");
    return mArts.contains(s);
  }
  
  public boolean isSports(String s) {
    s = s.replaceAll("(-| )", "");
    return mSports.contains(s);
  }
  
  public boolean isOthers(String s) {
    s = s.replaceAll("(-| )", "");
    return mOthers.contains(s);
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
    } else if (isDocumentary(s)) {
      return Program.INFO_CATEGORIE_DOCUMENTARY;
    } else if (isArts(s)) {
      return Program.INFO_CATEGORIE_ARTS;
    } else if (isSports(s)) {
      return Program.INFO_CATEGORIE_SPORTS;
    } else if (isOthers(s)) {
      return Program.INFO_CATEGORIE_OTHERS;
    }

    if (!mUnknown.contains(s)) {
      mUnknown.add(s);
      System.out.println("Unkown Category : " + s);
    }

    return 0;
  }
}
