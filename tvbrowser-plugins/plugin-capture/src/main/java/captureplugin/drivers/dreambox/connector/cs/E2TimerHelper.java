package captureplugin.drivers.dreambox.connector.cs;

import java.io.IOException;
import java.io.InputStream;
import java.io.StringReader;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.SocketTimeoutException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Collections;
import java.util.Comparator;
import java.util.GregorianCalendar;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.TimeZone;
import java.util.TreeMap;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.xml.parsers.ParserConfigurationException;
import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import org.apache.commons.codec.binary.Base64;
import org.xml.sax.InputSource;
import org.xml.sax.SAXException;

import captureplugin.drivers.dreambox.DreamboxConfig;
import captureplugin.drivers.dreambox.connector.DreamboxChannel;
import captureplugin.drivers.dreambox.connector.DreamboxStateHandler;
import captureplugin.drivers.utils.ProgramTime;
import devplugin.ProgramFieldType;

/**
 * @author fishhead
 * 
 */
public class E2TimerHelper {

  // Konstanten
  /** Sendername */
  public static final String SERVICENAME = "e2servicename";
  /** Sender-Reference */
  public static final String SERVICEREFERENCE = "e2servicereference";
  /** eit */
  public static final String EIT = "e2eit";
  /** Titel */
  public static final String NAME = "e2name";
  /** Beschreibung */
  public static final String DESCRIPTION = "e2description";
  /** ausgeschaltet */
  public static final String DISABLED = "e2disabled";
  /** Anfangszeit */
  public static final String TIMEBEGIN = "e2timebegin";
  /** Endezeit */
  public static final String TIMEEND = "e2timeend";
  /** Dauer */
  public static final String DURATION = "e2duration";
  /** umschalten */
  public static final String JUSTPLAY = "e2justplay";
  /** nach der Aufnahme */
  public static final String AFTEREVENT = "e2afterevent";
  /** Aufnahmeort TDT */
  public static final String LOCATION = "e2location";
  /** Aufnahmeort AAF */
  private static final String DIRNAME = "e2dirname";
  /** Kategorie */
  public static final String TAGS = "e2tags";
  /** Wiederholungen */
  public static final String REPEATED = "e2repeated";

  /** Comparator fuer Timer-Liste */
  private final TimerCompare TIMER_COMPARE = new TimerCompare();

  // Logger
  private static final Logger mLog = Logger.getLogger(E2TimerHelper.class.getName());
  // Class
  private static final Map<String, E2TimerHelper> singletonMap = new HashMap<String, E2TimerHelper>();
  // Member
  private List<Map<String, String>> mTimers;
  private final DreamboxConfig mConfig;
  private Thread mThread;

  /**
   * Factory
   * 
   * @param config
   *          for dreambox
   * 
   * @return timerHelper
   * 
   */
  public static E2TimerHelper getInstance(DreamboxConfig config) {
    String id = config.getId();
    E2TimerHelper singleton = null;
    synchronized (singletonMap) {
      singleton = singletonMap.get(id);
      if (singleton == null) {
        singleton = new E2TimerHelper(config);
        singletonMap.put(id, singleton);
      }
    }
    return singleton;
  }

  /**
   * Konstruktor
   * 
   * @param config
   * @param thread
   */
  private E2TimerHelper(DreamboxConfig config) {
    mLog.setLevel(Level.INFO);    
    this.mConfig = config;
    this.mTimers = null;
    run();
  }

  /**
   * Tries to parse a Long
   * 
   * @param longStr
   *          String with Long-Value
   * @return long-Value or -1
   */
  static long getLong(String longStr) {
    if (longStr.contains(".")) {
      longStr = longStr.substring(0, longStr.indexOf('.'));
    }

    try {
      return Long.parseLong(longStr);
    } catch (NumberFormatException e) {
      mLog.warning(e.getLocalizedMessage());
    }

    return -1;
  }

  /**
   * Get Calendar from Seconds
   * 
   * @param time
   * 
   * @return Calendar
   */
  public static Calendar getAsCalendar(String time) {
    Calendar cal = new GregorianCalendar();
    cal.setTimeInMillis(getLong(time) * 1000);
    return cal;
  }

  /**
   * Get Seconds from Calendar
   * 
   * @param cal
   * 
   * @return long
   */
  public static String getAsSeconds(Calendar cal) {
    return Long.toString(cal.getTimeInMillis() / 1000);
  }
  
  private int getIntBoolean(String value) {
    if(value.toLowerCase().equals("false")) {
      return 0;
    }
    else if(value.toLowerCase().equals("true")) {
      return 1;
    }
    
    return Integer.parseInt(value);
  }

  /**
   * Wiederholende Timer erzeugen
   * 
   * @param timers
   * @return repeatedTimers
   */
  public List<Map<String, String>> getRepeatedTimers() {

    List<Map<String, String>> timers = getTimers();
    if (timers == null) {
      return timers;
    }

    Map<String, Map<String, String>> mapTimers = new TreeMap<String, Map<String, String>>();
    for (Map<String, String> timer : timers) {

      int e2repeated = getIntBoolean(timer.get(REPEATED));
      int e2justplay = getIntBoolean(timer.get(JUSTPLAY));
      if ((e2repeated > 0) && (e2justplay == 0)) {
        for (int i = 0; i < 7; i++) {
          Calendar calB = getAsCalendar(timer.get(TIMEBEGIN));
          Calendar calE = getAsCalendar(timer.get(TIMEEND));

          int pow2 = Integer.rotateLeft(1, i);
          if ((e2repeated & pow2) == pow2) {
            switch (pow2) {
            case (1):
              calB.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
              calE.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);
              break;
            case (2):
              calB.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
              calE.set(Calendar.DAY_OF_WEEK, Calendar.TUESDAY);
              break;
            case (4):
              calB.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
              calE.set(Calendar.DAY_OF_WEEK, Calendar.WEDNESDAY);
              break;
            case (8):
              calB.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
              calE.set(Calendar.DAY_OF_WEEK, Calendar.THURSDAY);
              break;
            case (16):
              calB.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
              calE.set(Calendar.DAY_OF_WEEK, Calendar.FRIDAY);
              break;
            case (32):
              calB.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
              calE.set(Calendar.DAY_OF_WEEK, Calendar.SATURDAY);
              break;
            case (64):
              calB.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
              calE.set(Calendar.DAY_OF_WEEK, Calendar.SUNDAY);
              break;
            default:
              break;
            }
            for (int j = 0; j < 4; j++) {
              if ((calB.getTimeInMillis() > new GregorianCalendar().getTimeInMillis())
                  && (calB.getTimeInMillis() > getAsCalendar(timer.get(TIMEBEGIN)).getTimeInMillis())) {
                Map<String, String> mapTimer = new TreeMap<String, String>();
                mapTimer.putAll(timer);
                mapTimer.put(TIMEBEGIN, getAsSeconds(calB));
                mapTimer.put(TIMEEND, getAsSeconds(calE));
                String key = getTimerKey(mapTimer);
                mapTimers.put(key, mapTimer);
              }
              calB.add(Calendar.DAY_OF_MONTH, 7);
              calE.add(Calendar.DAY_OF_MONTH, 7);
            }
          }
        }
      }
    }
    // Hinzufuegen und sortieren der Timer ueber TreeMap
    for (Map<String, String> timer : timers) {
      int e2justplay = getIntBoolean(timer.get(JUSTPLAY));
      if (e2justplay == 0) {
        String key = getTimerKey(timer);
        mapTimers.put(key, timer);
      }
    }
    List<Map<String, String>> listTimers = new ArrayList<Map<String, String>>();
    listTimers.addAll(mapTimers.values());
    mLog.fine("  getRepeatedTimers(" + (listTimers == null ? "null" : listTimers.size()) + ")");
    return listTimers;
  }

  /**
   * key erstellen
   * 
   * @param timer
   * @return key
   */
  private String getTimerKey(Map<String, String> timer) {
    return timer.get(TIMEBEGIN) + "-" + timer.get(TIMEEND) + ":" + timer.get(SERVICENAME);
  }

  /**
   * get timers
   * 
   * @return timer
   */
  public synchronized List<Map<String, String>> getTimers() {
    if (!mThread.isAlive() && (mTimers == null)) {
      run();
    }
    try {
      mThread.join();
    } catch (InterruptedException e) {
      while ((mThread.getState() == Thread.State.RUNNABLE) && (mTimers == null)) {
        try {
          Thread.sleep(100);
        } catch (InterruptedException e1) {
          mLog.log(Level.WARNING, "InterruptedException", e1);
        }
      }
    }
    mLog.fine(" getTimers(" + (mTimers == null ? "null" : mTimers.size()) + ") " + mConfig.getDreamboxAddress());
    
    return mTimers != null ? mTimers : new ArrayList<Map<String,String>>(0);
  }

  /**
   * number of timers
   * 
   * @return int timercount
   */
  public int getTimerCount() {
    List<Map<String, String>> timers = getTimers();
    mLog.info("[" + mConfig.getDreamboxAddress() + "] " + "  getTimerCount(): " + (timers == null ? "0" : timers.size()));
    return (timers == null ? 0 : timers.size());
  }

  /**
   * read timers from dreambox
   */
  private void run() {

    mThread = new Thread() {
      @Override
      public void run() {
        Calendar cal = new GregorianCalendar();

        if (mTimers == null) {

          String data = "";
          try {
            URL url = new URL("http://" + mConfig.getDreamboxAddress() + "/web/timerlist");
            URLConnection connection = url.openConnection();

            String userpassword = mConfig.getUserName() + ":" + mConfig.getPassword();
            String encoded = new String(Base64.encodeBase64(userpassword.getBytes()));
            connection.setRequestProperty("Authorization", "Basic " + encoded);

            connection.setConnectTimeout(mConfig.getTimeout());
            InputStream stream = connection.getInputStream();
            byte[] buf = new byte[1024];
            int len;
            while ((len = stream.read(buf)) != -1) {
              data += new String(buf, 0, len, "UTF-8");
            }
            stream.close();
            E2ListMapHandler handler = new E2ListMapHandler("e2timerlist", "e2timer");
            SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
            saxParser.parse(new InputSource(new StringReader(data)), handler);

            mTimers = handler.getList();

            // fix e2dirname / e2location
            for (Map<String, String> timer : mTimers) {
              if (timer.containsKey(DIRNAME)) {
                timer.put(LOCATION, timer.get(DIRNAME));
              }
            }

          } catch (ParserConfigurationException e) {
            mLog.log(Level.WARNING, "ParserConfigurationException", e);
          } catch (SAXException e) {
            mLog.warning(data);
            mLog.log(Level.WARNING, "SAXException", e);
          } catch (MalformedURLException e) {
            mLog.log(Level.WARNING, "MalformedURLException", e);
          } catch (SocketTimeoutException e) {
            mLog.log(Level.WARNING, "SocketTimeoutException", e);
          } catch (IOException e) {
            mLog.log(Level.WARNING, "IOException", e);
          } catch (IllegalArgumentException e) {
            mLog.log(Level.WARNING, "IllegalArgumentException", e);
          }
        }

        mLog.info("[" + mConfig.getDreamboxAddress() + "] " + "GET timerlist - "
            + (new GregorianCalendar().getTimeInMillis() - cal.getTimeInMillis()) + " ms");
      }
    };

    mThread.start();
  }

  /**
   * refresh timers
   */
  public void refresh() {
    mTimers = null;
    run();
  }

  /**
   * get thread
   * 
   * @return Thread
   */
  public Thread getThread() {
    return mThread;
  }

  /**
   * Position des Timers in der Liste
   * 
   * @param reference
   * @param begin
   * @param end
   * 
   * @return pos or -1 if not found
   */
  public int indexOfTimer(String reference, String begin, String end) {
    int i = 0;
    for (Map<String, String> timer : getTimers()) {
      if (timer.get(SERVICEREFERENCE).equals(reference) && timer.get(TIMEBEGIN).equals(begin)
          && timer.get(TIMEEND).equals(end)) {
        return i;
      }
      i++;
    }
    return -1;
  }

  /**
   * Position des Timers in der Liste
   * 
   * @param timer
   * @return pos or -1 if not found
   */
  public int indexOfTimer(Map<String, String> timer) {
    String reference = timer.get(SERVICEREFERENCE);
    String begin = timer.get(TIMEBEGIN);
    String end = timer.get(TIMEEND);
    return indexOfTimer(reference, begin, end);
  }

  /**
   * Create REC Timer
   * 
   * @param dreamboxChannel
   * @param prgTime
   * @param afterEvent
   * @param repeated
   * @param timezone
   * @param location
   * @param tags
   * @param useHdService
   * 
   * @return timer
   */
  public Map<String, String> createRecTimer(DreamboxChannel dreamboxChannel, ProgramTime prgTime, int afterEvent,
      int repeated, TimeZone timezone, String location, String tags, boolean useHdService, boolean useDescription) {

    Map<String, String> timer = new DefaultValueReturnMap<String, String>("");

    final String trenner = " / ";
    final String keineBeschreibung = "Diese Sendung hat noch keine Beschreibung.";
    final String textVon = "\n(Text von ";
    final int maxLen = 200;

    Calendar start = prgTime.getStartAsCalendar();
    start.setTimeZone(timezone);

    Calendar end = prgTime.getEndAsCalendar();
    end.setTimeZone(timezone);

    // Kurzbeschreibung
    String shortInfo = prgTime.getProgram().getShortInfo();
    if (!useDescription || (shortInfo == null) || (shortInfo.startsWith(keineBeschreibung))) {
      shortInfo = "";
    }

    // Beschreibung
    String description = prgTime.getProgram().getDescription();
    if (!useDescription || (description == null) || (description.startsWith(keineBeschreibung))) {
      description = "";
    }

    // Episode
    String episode = prgTime.getProgram().getTextField(ProgramFieldType.EPISODE_TYPE);
    if (episode != null) {
      shortInfo = episode + trenner + shortInfo;
    }

    // Beschreibung unterschiedlich zur Kurzbeschreibung?
    if ((shortInfo.length() < maxLen) && (description.length() > 0) && (!description.startsWith(shortInfo))) {
      // Kurzbeschreibung unterscheidet sich
      shortInfo = shortInfo + trenner + description;
    }
    if (shortInfo.endsWith(trenner)) {
      shortInfo = shortInfo.substring(0, shortInfo.length() - 3);
    }

    // (Text von ...) raus
    int p = shortInfo.lastIndexOf(textVon);
    if (p != -1) {
      shortInfo = shortInfo.substring(0, p);
    }

    // kuerzen
    if (shortInfo.length() > maxLen) {
      shortInfo = shortInfo.trim().substring(0, maxLen);
    }

    // Titel
    String title = prgTime.getTitle();

    // Sender Reference und Name
    String sRef = E2ServiceHelper.getServiceRef(dreamboxChannel.getReference(), useHdService);
    String sName = E2ServiceHelper.getServiceName(dreamboxChannel.getName(), useHdService);

    timer.put(SERVICEREFERENCE, sRef); // Sender-Reference
    timer.put(SERVICENAME, sName); // Sendername
    timer.put(TIMEBEGIN, Long.toString(start.getTimeInMillis() / 1000));
    timer.put(TIMEEND, Long.toString(end.getTimeInMillis() / 1000));
    timer.put(NAME, title); // Titel
    timer.put(DESCRIPTION, shortInfo); // Kurzbeschreibung
    timer.put(REPEATED, Integer.toString(repeated)); // Wiederholung
    timer.put(AFTEREVENT, Integer.toString(afterEvent)); // 0=nothing,1=standby,2=deep_standby,3=auto
    timer.put(DISABLED, "0"); // Enabled
    timer.put(JUSTPLAY, "0"); // Recording
    timer.put(LOCATION, location); // Aufzeichnungspfad
    timer.put(TAGS, tags); // Kategorie
    timer.put(DURATION, ""); // Dauer
    timer.put(EIT, ""); // EIT

    return timer;
  }

  /**
   * Create ZAP Before Timer
   * 
   * @param timer
   * 
   * @return zaptimer
   */
  public Map<String, String> createZapBeforeTimer(Map<String, String> timer) {

    Map<String, String> zapTimer = new DefaultValueReturnMap<String, String>(timer,"");

    zapTimer.put(SERVICEREFERENCE, timer.get(SERVICEREFERENCE)); // Kanal
    String timeBegin = Long.toString(Long.parseLong(timer.get(TIMEBEGIN)) - 60);
    zapTimer.put(TIMEBEGIN, timeBegin); // Anfangszeit in sec
    zapTimer.put(TIMEEND, timer.get(TIMEBEGIN)); // Endzeit in sec
    zapTimer.put(DESCRIPTION, "zap"); // Beschreibung
    zapTimer.put(JUSTPLAY, "1"); // nur umschalten
    zapTimer.put(AFTEREVENT, "0"); // nach dem Umschalten nichts tun
    zapTimer.put(LOCATION, ""); // Aufzeichnungspfad
    zapTimer.put(TAGS, ""); // Kategorie

    return zapTimer;
  }

  /**
   * Create ZAP After Timer
   * 
   * @param timer
   * 
   * @return zaptimer
   */
  public Map<String, String> createZapAfterTimer(Map<String, String> timer) {

    Map<String, String> zap2Timer = new DefaultValueReturnMap<String, String>(timer,"");

    zap2Timer.put(SERVICEREFERENCE, E2ServiceHelper.getServiceRef(timer.get(SERVICEREFERENCE), false)); // Kanal
    zap2Timer.put(TIMEBEGIN, timer.get(TIMEEND)); // Anfangszeit in sec
    String timeEnd = Long.toString(Long.parseLong(timer.get(TIMEEND)) + 60);
    zap2Timer.put(TIMEEND, timeEnd); // Endzeit in sec
    zap2Timer.put(DESCRIPTION, "zap SD"); // Beschreibung
    zap2Timer.put(JUSTPLAY, "1"); // nur umschalten
    zap2Timer.put(AFTEREVENT, "3"); // nach dem Umschalten auto
    zap2Timer.put(LOCATION, ""); // Aufzeichnungspfad
    zap2Timer.put(TAGS, ""); // Kategorie

    return zap2Timer;
  }

  /**
   * Timer hinzufuegen
   * 
   * @param timer
   * @param config
   * @return ok
   */
  public boolean timerAdd(Map<String, String> timer) {

    Calendar cal = new GregorianCalendar();

    String data = "";
    try {

      URL url = new URL("http://" + mConfig.getDreamboxAddress() + "/web/timeradd?"

      + "&sRef=" + URLEncoder.encode(timer.get(SERVICEREFERENCE), "UTF-8")

      + "&begin=" + timer.get(TIMEBEGIN)

      + "&end=" + timer.get(TIMEEND)

      + "&name=" + URLEncoder.encode(timer.get(NAME), "UTF-8")

      + "&description=" + URLEncoder.encode(timer.get(DESCRIPTION), "UTF-8")

      + "&dirname=" + URLEncoder.encode(timer.get(LOCATION), "UTF-8")

      + "&tags=" + URLEncoder.encode(timer.get(TAGS), "UTF-8")

      + "&afterevent=" + timer.get(AFTEREVENT)

      + "&eit=" + timer.get(EIT)

      + "&disabled=" + timer.get(DISABLED)

      + "&justplay=" + timer.get(JUSTPLAY)

      + "&repeated=" + timer.get(REPEATED)

      );

      URLConnection connection = url.openConnection();

      String userpassword = mConfig.getUserName() + ":" + mConfig.getPassword();
      String encoded = new String(Base64.encodeBase64(userpassword.getBytes()));
      connection.setRequestProperty("Authorization", "Basic " + encoded);

      boolean state = false;
      try {
        // Web-Interface AAF
        connection.setConnectTimeout(mConfig.getTimeout());
        InputStream stream = connection.getInputStream();
        byte[] buf = new byte[1024];
        int len;
        while ((len = stream.read(buf)) != -1) {
          data += new String(buf, 0, len, "UTF-8");
        }
        stream.close();
        DreamboxStateHandler handler = new DreamboxStateHandler();
        SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
        saxParser.parse(new InputSource(new StringReader(data)), handler);
        state = handler.getState().equalsIgnoreCase("true");
        if (state) {
          // Modell aktualisieren
          getTimers().add(timer);
          Collections.sort(getTimers(), TIMER_COMPARE);
          // Log
          mLog.info("[" + mConfig.getDreamboxAddress() + "] " + String.format("ADD %s - %s - %d ms", // msg
              "1".equals(timer.get(JUSTPLAY)) ? "ZAP" : "REC", // justplay
              handler.getStatetext(), // Fehlermeldung
              (new GregorianCalendar().getTimeInMillis() - cal.getTimeInMillis())) // Dauer
              );
        } else {
          mLog.warning(handler.getStatetext());
        }

      } catch (IOException e) {
        // Web-Interface TDT
        mLog.info("[" + mConfig.getDreamboxAddress() + "] " + e.getLocalizedMessage());
        state = true;
      }

      return state;

    } catch (UnsupportedEncodingException e) {
      mLog.log(Level.WARNING, "UnsupportedEncodingException", e);
    } catch (MalformedURLException e) {
      mLog.log(Level.WARNING, "MalformedURLException", e);
    } catch (IOException e) {
      mLog.log(Level.WARNING, "IOException", e);
    } catch (ParserConfigurationException e) {
      mLog.log(Level.WARNING, "ParserConfigurationException", e);
    } catch (SAXException e) {
      mLog.warning(data);
      mLog.log(Level.WARNING, "SAXException", e);
    }

    return false;
  }

  /**
   * Timer aendern
   * 
   * @param oldTimer
   * @param newTimer
   * @param config
   * @return ok
   */
  public boolean timerChange(Map<String, String> oldTimer, Map<String, String> newTimer) {

    Calendar cal = new GregorianCalendar();

    String data = "";
    try {

      URL url = new URL("http://" + mConfig.getDreamboxAddress() + "/web/timerchange?"

      + "&channelOld=" + URLEncoder.encode(oldTimer.get(SERVICEREFERENCE), "UTF-8")

      + "&beginOld=" + oldTimer.get(TIMEBEGIN)

      + "&endOld=" + oldTimer.get(TIMEEND)

      + "&deleteOldOnSave=1"

      + "&sRef=" + URLEncoder.encode(newTimer.get(SERVICEREFERENCE), "UTF-8")

      + "&begin=" + newTimer.get(TIMEBEGIN)

      + "&end=" + newTimer.get(TIMEEND)

      + "&name=" + URLEncoder.encode(newTimer.get(NAME), "UTF-8")

      + "&description=" + URLEncoder.encode(newTimer.get(DESCRIPTION), "UTF-8")

      + "&dirname=" + URLEncoder.encode(newTimer.get(LOCATION), "UTF-8")

      + "&tags=" + URLEncoder.encode(newTimer.get(TAGS), "UTF-8")

      + "&afterevent=" + newTimer.get(AFTEREVENT)

      + "&eit=" + newTimer.get(EIT)

      + "&disabled=" + newTimer.get(DISABLED)

      + "&justplay=" + newTimer.get(JUSTPLAY)

      + "&repeated=" + newTimer.get(REPEATED)

      );

      URLConnection connection = url.openConnection();

      String userpassword = mConfig.getUserName() + ":" + mConfig.getPassword();
      String encoded = new String(Base64.encodeBase64(userpassword.getBytes()));
      connection.setRequestProperty("Authorization", "Basic " + encoded);

      boolean state = false;
      try {
        // Web-Interface AAF
        connection.setConnectTimeout(mConfig.getTimeout());
        InputStream stream = connection.getInputStream();
        byte[] buf = new byte[1024];
        int len;
        while ((len = stream.read(buf)) != -1) {
          data += new String(buf, 0, len, "UTF-8");
        }
        stream.close();
        DreamboxStateHandler handler = new DreamboxStateHandler();
        SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
        saxParser.parse(new InputSource(new StringReader(data)), handler);
        state = handler.getState().equalsIgnoreCase("true");
        if (state) {
          // Modell aktualisieren
          getTimers().set(indexOfTimer(oldTimer), newTimer);
          Collections.sort(getTimers(), TIMER_COMPARE);

          // Log
          mLog.info("[" + mConfig.getDreamboxAddress() + "] " + String.format("CHG %s - %s - %d ms", // msg
              "1".equals(oldTimer.get(JUSTPLAY)) ? "ZAP" : "REC", // justplay
              handler.getStatetext(), // Fehlermeldung
              (new GregorianCalendar().getTimeInMillis() - cal.getTimeInMillis())) // Dauer
              );
        } else {
          mLog.warning(handler.getStatetext());
        }

      } catch (IOException e) {
        // Web-Interface TDT
        mLog.info("[" + mConfig.getDreamboxAddress() + "] " + e.getLocalizedMessage());
        state = true;
      }

      return state;

    } catch (UnsupportedEncodingException e) {
      mLog.log(Level.WARNING, "UnsupportedEncodingException", e);
    } catch (MalformedURLException e) {
      mLog.log(Level.WARNING, "MalformedURLException", e);
    } catch (IOException e) {
      mLog.log(Level.WARNING, "IOException", e);
    } catch (ParserConfigurationException e) {
      mLog.log(Level.WARNING, "ParserConfigurationException", e);
    } catch (SAXException e) {
      mLog.warning(data);
      mLog.log(Level.WARNING, "SAXException", e);
    }

    return false;
  }

  /**
   * Timer loeschen
   * 
   * @param timer
   * @param config
   * @return ok
   */
  public boolean timerDelete(Map<String, String> timer) {

    // Timer loeschen
    Calendar cal = new GregorianCalendar();

    String data = "";
    try {
      String userpassword = mConfig.getUserName() + ":" + mConfig.getPassword();
      String encoded = new String(Base64.encodeBase64(userpassword.getBytes()));

      URL url = new URL("http://" + mConfig.getDreamboxAddress() + "/web/timerdelete?"

      + "&sRef=" + URLEncoder.encode(timer.get(SERVICEREFERENCE), "UTF-8")

      + "&begin=" + timer.get(TIMEBEGIN)

      + "&end=" + timer.get(TIMEEND));

      URLConnection connection = url.openConnection();
      connection.setRequestProperty("Authorization", "Basic " + encoded);
      connection.setConnectTimeout(mConfig.getTimeout());
      InputStream stream = connection.getInputStream();
      byte[] buf = new byte[1024];
      int len;
      data = "";
      while ((len = stream.read(buf)) != -1) {
        data += new String(buf, 0, len, "UTF-8");
      }
      stream.close();
      DreamboxStateHandler handler = new DreamboxStateHandler();
      SAXParser saxParser = SAXParserFactory.newInstance().newSAXParser();
      saxParser.parse(new InputSource(new StringReader(data)), handler);
      boolean state = handler.getState().equalsIgnoreCase("true");
      if (state) {
        // Modell aktualisieren
        getTimers().remove(indexOfTimer(timer));
        Collections.sort(getTimers(), TIMER_COMPARE);
        // Log
        mLog.info("[" + mConfig.getDreamboxAddress() + "] " + String.format("DEL %s - %s - %d ms", // msg
            "1".equals(timer.get(JUSTPLAY)) ? "ZAP" : "REC", // justplay
            handler.getStatetext(), // Fehlermeldung
            (new GregorianCalendar().getTimeInMillis() - cal.getTimeInMillis())) // Dauer
            );
      } else {
        mLog.warning(handler.getStatetext());
      }
      return state;

    } catch (UnsupportedEncodingException e) {
      mLog.log(Level.WARNING, "UnsupportedEncodingException", e);
    } catch (MalformedURLException e) {
      mLog.log(Level.WARNING, "MalformedURLException", e);
    } catch (IOException e) {
      mLog.log(Level.WARNING, "IOException", e);
    } catch (ParserConfigurationException e) {
      mLog.log(Level.WARNING, "ParserConfigurationException", e);
    } catch (SAXException e) {
      mLog.warning(data);
      mLog.log(Level.WARNING, "SAXException", e);
    }
    return false;
  }

  /**
   * Comparator fuer Timer
   * 
   * @author fishhead
   * 
   */
  class TimerCompare implements Comparator<Map<String, String>> {

    public int compare(Map<String, String> t1, Map<String, String> t2) {
      // Timer nach Anfangs- und Endzeiten sortieren
      long t1B = E2TimerHelper.getAsCalendar(t1.get(TIMEBEGIN)).getTimeInMillis();
      long t1E = E2TimerHelper.getAsCalendar(t1.get(TIMEEND)).getTimeInMillis();
      long t2B = E2TimerHelper.getAsCalendar(t2.get(TIMEBEGIN)).getTimeInMillis();
      long t2E = E2TimerHelper.getAsCalendar(t2.get(TIMEEND)).getTimeInMillis();
      if (t1B < t2B) {
        return -1;
      } else if (t1B > t2B) {
        return +1;
      } else if (t1B == t2B) {
        if (t1E < t2E) {
          return -1;
        } else if (t1E > t2E) {
          return +1;
        }
      }
      return 0;
    }
  }

}
