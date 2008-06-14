/*
 * DataHydraDayParser.java
 *
 * Created on March 7, 2005, 11:59 AM
 */

package swedbtvdataservice;

import devplugin.Channel;
import devplugin.Program;
import devplugin.ProgramFieldType;
import org.xml.sax.Attributes;
import org.xml.sax.InputSource;
import org.xml.sax.SAXParseException;
import tvdataservice.MutableChannelDayProgram;
import tvdataservice.MutableProgram;

import java.io.ByteArrayInputStream;
import java.io.File;
import java.io.InputStream;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Hashtable;
import java.util.TimeZone;

import javax.xml.parsers.SAXParser;
import javax.xml.parsers.SAXParserFactory;

import util.ui.Localizer;

/**
 * @author Inforama
 */
public class DataHydraDayParser extends org.xml.sax.helpers.DefaultHandler {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(DataHydraDayParser.class);

  private static java.util.logging.Logger mLog
          = java.util.logging.Logger.getLogger(DataHydraDayParser.class.getName());

  private devplugin.Date start;

  private String desc;

  private String title;

  private String subTitle;

  private String genre;

  private String episode;

  private String actors;

  private String directors;

  private int startHour;

  private int startMin;

  private int endHour;

  private int endMin;

  private Hashtable<String, MutableChannelDayProgram> mDayProgsHashTable;

  private MutableChannelDayProgram mMcdp;

  private Channel mChannel;

  private final static int STATUS_WAITING = 0;

  private final static int STATUS_PROG = 1;

  private final static int STATUS_TITLE = 2;

  private final static int STATUS_DESC = 3;

  private final static int STATUS_GENRE = 4;

  private final static int STATUS_EPISODE = 5;

  private final static int STATUS_ACTORS = 6;

  private final static int STATUS_DIRECTORS = 7;

  private final static int STATUS_EPISODE_NUMBER = 8;

  private final static int STATUS_ASPECT = 9;

  private static final int STATUS_DATE = 10;

  private static final int STATUS_SUBTITLE = 11;

  private int state = 0;

  private boolean multipleGenres = false;

  private boolean multipleActors = false;

  private boolean multipleDirectors = false;

  private String episodeNumber;

  private String aspect;

  private String date;

  private SweDBTvDataService mDataService;

  /**
   * Creates a new instance of DataHydraDayParser
   */
  private DataHydraDayParser(Channel ch,
                           Hashtable<String, MutableChannelDayProgram> lht, SweDBTvDataService dataService) {
    mChannel = ch;
    mDayProgsHashTable = lht;
    mDataService = dataService;
  }

  public InputSource resolveEntity(String publicId, String systemId) {
//    System.out.println("testing");
    byte[] temp = new byte[0];
    // strib...
    return new InputSource(new ByteArrayInputStream(temp));
  }

  public void startElement(String uri, String localName, String qName,
                           Attributes attributes) {
    switch (state) {
      case (STATUS_PROG): {
        if ("title".equals(qName)) {
          state = STATUS_TITLE;
          title = "";
        } else if ("desc".equals(qName)) {
          state = STATUS_DESC;
          desc = "";
        } else if ("category".equals(qName)) {
          state = STATUS_GENRE;
          if (multipleGenres) {
            genre = genre + ", ";
          } else {
            genre = "";
          }
        } else if ("episode-num".equals(qName)) {
          if ("onscreen".equals(attributes.getValue("system"))) {
            state = STATUS_EPISODE;
          }
          if ("xmltv_ns".equals(attributes.getValue("system"))) {
            state = STATUS_EPISODE_NUMBER;
          }
        } else if ("actor".equals(qName)) {
          state = STATUS_ACTORS;
          if (multipleActors) {
            actors = actors + ", ";
          } else {
            actors = "";
          }
        } else if ("director".equals(qName)) {
          state = STATUS_DIRECTORS;
          if (multipleDirectors) {
            directors = directors + ", ";
          } else {
            directors = "";
          }
        } else if ("aspect".equals(qName)) {
          state = STATUS_ASPECT;
          aspect = "";
        } else if ("date".equals(qName)) {
          state = STATUS_DATE;
          aspect = "";
        } else if ("sub-title".equals(qName)) {
          state = STATUS_SUBTITLE;
          subTitle = "";
        } else if (!"credits".equals(qName) && !"video".equals(qName)) {
          mLog.info("Unknown tag in SweDB data: " + qName);
        }
        break;
      }
      case (STATUS_WAITING): {
        if ("programme".equals(qName)) {
          desc = "";
          title = "";
          genre = "";
          episode = "";
          actors = "";
          directors = "";
          episodeNumber = "";
          aspect = "";
          date = "";
          subTitle = "";

          multipleGenres = false;
          multipleActors = false;
          multipleDirectors = false;
          try {
            String time = attributes.getValue("start");
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddkkmmss ZZZZ");
            
            Calendar cal = Calendar.getInstance();            
            cal.setTime(format.parse(time.substring(0,20)));
            cal.setTimeInMillis(cal.getTimeInMillis() - cal.getTimeZone().getRawOffset());
            
            start = new devplugin.Date(cal.get(Calendar.YEAR), 
                cal.get(Calendar.MONTH)+1, cal.get(Calendar.DAY_OF_MONTH));

            startHour = cal.get(Calendar.HOUR_OF_DAY);
            startMin = cal.get(Calendar.MINUTE);
            
            state = STATUS_PROG;
          } catch (Exception E) {
            System.out.println("invalid start time format: "
                    + attributes.getValue("start"));
          }
          try {
            String time = attributes.getValue("stop");
            if (time.length() > 0) {
              SimpleDateFormat format = new SimpleDateFormat("yyyyMMddkkmmss ZZZZ");
              
              Calendar cal = Calendar.getInstance();
              
              cal.setTime(format.parse(time.substring(0,20)));
              cal.setTimeInMillis(cal.getTimeInMillis() - cal.getTimeZone().getRawOffset());              

              endHour = cal.get(Calendar.HOUR_OF_DAY);
              endMin = cal.get(Calendar.MINUTE);
            }
          } catch (Exception E) {E.printStackTrace();
            // System.out.println("invalid stop time format:
            // "+attributes.getValue("stop"));
          }
        }
        break;
      }
    }
  }

  public void characters(char[] ch, int start, int length) {
    switch (state) {
      case (STATUS_TITLE): {
        title = title + new String(ch, start, length);
        break;
      }
      case (STATUS_DESC): {
        desc = desc + new String(ch, start, length);
        break;
      }
      case (STATUS_GENRE): {
        genre = genre + new String(ch, start, length);
        break;
      }
      case (STATUS_EPISODE): {
        episode = episode + new String(ch, start, length);
        break;
      }
      case (STATUS_ACTORS): {
        actors = actors + new String(ch, start, length);
        break;
      }
      case (STATUS_DIRECTORS): {
        directors = directors + new String(ch, start, length);
        break;
      }
      case (STATUS_EPISODE_NUMBER): {
        episodeNumber = episodeNumber + new String(ch, start, length);
        break;
      }
      case (STATUS_ASPECT): {
        aspect = aspect + new String(ch, start, length);
        break;
      }
      case (STATUS_DATE): {
        date = date + new String(ch, start, length);
        break;
      }
      case (STATUS_SUBTITLE): {
        subTitle = subTitle + new String(ch, start, length);
        break;
      }

    }
  }

  public void endElement(String uri, String localName, String qName) {
    switch (state) {
      case (STATUS_TITLE): {
        if ("title".equals(qName)) {
          state = STATUS_PROG;
        }
        break;
      }
      case (STATUS_DESC): {
        if ("desc".equals(qName)) {
          state = STATUS_PROG;
        }
        break;
      }
      case (STATUS_GENRE): {
        if ("category".equals(qName)) {
          state = STATUS_PROG;
          multipleGenres = true;
        }
        break;
      }
      case (STATUS_EPISODE): {
        if ("episode-num".equals(qName)) {
          state = STATUS_PROG;
        }
        break;
      }
      case (STATUS_EPISODE_NUMBER): {
        if ("episode-num".equals(qName)) {
          state = STATUS_PROG;
        }
        break;
      }
      case (STATUS_ACTORS): {
        if ("actor".equals(qName)) {
          state = STATUS_PROG;
          multipleActors = true;
        }
        break;
      }
      case (STATUS_DIRECTORS): {
        if ("director".equals(qName)) {
          state = STATUS_PROG;
          multipleDirectors = true;
        }
        break;
      }
      case (STATUS_ASPECT): {
        if ("aspect".equals(qName)) {
          state = STATUS_PROG;
        }
        break;
      }
      case (STATUS_DATE): {
        if ("date".equals(qName)) {
          state = STATUS_PROG;
        }
        break;
      }
      case (STATUS_SUBTITLE): {
        if ("sub-title".equals(qName)) {
          state = STATUS_PROG;
        }
        break;
      }

      case (STATUS_PROG): {
        if ("programme".equals(qName)) {
          state = STATUS_WAITING;
          if (!mDayProgsHashTable.containsKey(start.toString())) {
            mMcdp = new MutableChannelDayProgram(start, mChannel);
            mDayProgsHashTable.put(start.toString(), mMcdp);
          } else {
            mMcdp = mDayProgsHashTable.get(start.toString());
          }
          MutableProgram prog = new MutableProgram(mMcdp.getChannel(), start,
                  startHour, startMin, true);

          int progLength = (endHour * 60) + endMin - (startHour * 60) - startMin;
          // Assumption: If the program length is less than 0, the program spans midnight
          if (progLength < 0) {
            progLength += 24 * 60; // adding 24 hours to the length
          }
          // Only allow program length for 12 hours.... This will take care of
          // possible DST problems
          if ((progLength > 0) && (progLength < 12 * 60)) {
            prog.setLength(progLength);
          }
          prog.setTitle(title);
          // Since we don't have a field for sub titles, we add it to description
          desc = (subTitle + "\n" + desc).trim();

          String shortDesc = MutableProgram.generateShortInfoFromDescription(desc);           
          prog.setShortInfo(shortDesc);
                    
          if (((DataHydraChannelGroup)mChannel.getGroup()).isShowRegister() && "true".equals(mDataService.getProperties().getProperty(SweDBTvDataService.SHOW_REGISTER_TEXT, "true"))) {
            desc += "\n\n" + mLocalizer.msg("register", "Please Register at {0}", mChannel.getWebpage());
          }

          prog.setDescription(desc);

          if (genre.length() > 0) {
            genre = genre.substring(0, 1).toUpperCase() + genre.substring(1);
            prog.setTextField(ProgramFieldType.GENRE_TYPE, genre);
          }
          if (actors.length() > 0) {
            prog.setTextField(ProgramFieldType.ACTOR_LIST_TYPE, actors);
          }
          if (directors.length() > 0) {
            prog.setTextField(ProgramFieldType.DIRECTOR_TYPE, directors);
          }
          if (episode.length() > 0) {
            prog.setTextField(ProgramFieldType.EPISODE_TYPE, episode);
          }
          if (episodeNumber.length() > 0) {
            // format is   season/totalseasons.episodenum/totalepisode.part/totalparts
            // where current numbers start at 0, while total numbers start at 1
            String[] ep = episodeNumber.split("\\.");
            if (ep.length > 0 && ep[0].length() > 0) {
              String[] seasons = ep[0].trim().split("/");
              if (seasons.length > 0 && seasons[0].trim().length() > 0) {
                int season = Integer.parseInt(seasons[0].trim()) + 1;
                if (season > 0) {
                  prog.setIntField(ProgramFieldType.SEASON_NUMBER_TYPE, season);
                }
              }
            }
            if (ep.length > 1 && ep[1].length() > 0) {
              String[] parts = ep[1].trim().split("/");
              if (parts.length == 2) {
                String currentString = parts[0].trim();
                if (currentString.length() > 0) {
                  int current = Integer.parseInt(currentString) + 1;
                  if (current > 0) {
                    prog.setIntField(ProgramFieldType.EPISODE_NUMBER_TYPE, current);
                  }
                }
                String totalString = parts[1].trim();
                if (totalString.length() > 0) {
                  int total = Integer.parseInt(totalString);
                  if (total > 0) {
                    prog.setIntField(ProgramFieldType.EPISODE_TOTAL_NUMBER_TYPE, total);
                  }
                }
              }
            }
          }
          int info = 0;
          if (aspect.length() > 0) {
            if (aspect.equalsIgnoreCase("4:3")) {
              info = info | Program.INFO_VISION_4_TO_3;
            }
            if (aspect.equalsIgnoreCase("16:9")) {
              info = info | Program.INFO_VISION_16_TO_9;
            }
          }
          if (genre.length() > 0) {
            if (genre.toLowerCase().indexOf("series") > -1) {
              info = info | Program.INFO_CATEGORIE_SERIES;
            } else if (genre.toLowerCase().indexOf("movie") > -1) {
              info = info | Program.INFO_CATEGORIE_MOVIE;
            }
          }
          if (info != 0) {
            prog.setInfo(info);
          }
          if (date.length() > 0) {
            if (date.length() == 4) {
              int year = Integer.parseInt(date);
              prog.setIntField(ProgramFieldType.PRODUCTION_YEAR_TYPE, year);
            }
          }
          prog.setProgramLoadingIsComplete();
          mMcdp.addProgram(prog);
        }
        break;
      }
    }
  }

  public void fatalError(SAXParseException e) {
  }

  public void error(SAXParseException e) {
  }

  public void warning(SAXParseException e) {
  }

  protected static void parseNew(InputStream in, Channel ch, devplugin.Date day,
                              Hashtable<String, MutableChannelDayProgram> ht, SweDBTvDataService dataService) throws Exception {
    SAXParserFactory fac = SAXParserFactory.newInstance();
    fac.setValidating(false);
    SAXParser sax = fac.newSAXParser();
    InputSource input = new InputSource(in);
    input.setSystemId(new File("/").toURI().toURL().toString());
    sax.parse(input, new DataHydraDayParser(ch, ht, dataService));
  }

}
