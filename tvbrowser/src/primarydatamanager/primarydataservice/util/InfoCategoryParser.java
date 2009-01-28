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
      "beziehungskomödie", "bikerfilm", "biographischerfilm", 
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
      "comedyserie", "dramaserie", "erotikserie",
      "familienserie", "fernsehserie", "horrorserie", "kinderserie",
      "krankenhausserie", "kriegsserie", "krimiserie", "monumentalserie",
      "mysteryserie", "polizeiserie", "puppentrickserie",
      "sciencefictionserie", "scifiserie", "serie", "serienspecial", "sitcom",
      "soap", "thrillerserie", "westernserie", "zeichentrickserie",
      "zoodokusoap", "justizserie", "polit-/drama-serie", "mafia-serie" };

  private final String[] NEWS = { "nachrichten", "nachrichtenmagazin",
  "regionalnachrichten" };

  private final String[] SHOW = { "musik", "show", "talkshow","talk" };

  private final String[] MAGAZINE = { "auslandsmagazin", "automagazin",
      "boulevardmagazin", "buchjournal", "diskussion", 
      "erfindermagazin", "erotikmagazin", "filmmagazin",
      "freizeitmagazin", "fußballmagazin", "geschichtsmagazin", "gespräch",
      "gesundheitsmagazin", "interview", "kindermagazin", "kinomagazin",
      "kulturmagazin", "ländermagazin", "lifestylemagazin", "literaturmagazin",
      "magazin", "medienmagazin", "motorsportmagazin", "news+sportnachrichten",
      "newsmagazin", "politikmagazin", "politmagazin", "ratgebermagazin",
      "regionalmagazin", "reisemagazin", "reiseundreportagemagazin", 
      "reportageundreisemagazin", "servicemagazin", "societymagazin",
      "sozialmagazin", "technikmagazin", "umweltmagazin", "verbrauchermagazin",
      "wetter", "wirtschaftsmagazin", "wissenschaftsmagazin", "wissensmagazin"
   };

  private final String[] DOCUMENTARY = { "dokumentarfilm", "doku", "dokumentation", 
      "reisedokumentation",  "reisereportage", "reportage", "dokumentarserie",
      "zeitgeschichte", "dokuserie" 
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
