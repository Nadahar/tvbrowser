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

  private devplugin.Date mStartDate;

  private String mDescription;

  private String mTitle;

  private String mSubTitle;

  private String mGenre;

  private String mEpisode;

  private String mActors;

  private String mDirectors;

  private int mStartHour;

  private int mStartMin;

  private int mEndHour;

  private int mEndMin;

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

  private int mState = 0;

  private boolean mMultipleGenres = false;

  private boolean mMultipleActors = false;

  private boolean mMultipleDirectors = false;

  private String mEpisodeNumber;

  private String mAspect;

  private String mDate;

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
    switch (mState) {
      case (STATUS_PROG): {
        if ("title".equals(qName)) {
          mState = STATUS_TITLE;
          mTitle = "";
        } else if ("desc".equals(qName)) {
          mState = STATUS_DESC;
          mDescription = "";
        } else if ("category".equals(qName)) {
          mState = STATUS_GENRE;
          if (mMultipleGenres) {
            mGenre = mGenre + ", ";
          } else {
            mGenre = "";
          }
        } else if ("episode-num".equals(qName)) {
          if ("onscreen".equals(attributes.getValue("system"))) {
            mState = STATUS_EPISODE;
          }
          if ("xmltv_ns".equals(attributes.getValue("system"))) {
            mState = STATUS_EPISODE_NUMBER;
          }
        } else if ("actor".equals(qName)) {
          mState = STATUS_ACTORS;
          if (mMultipleActors) {
            mActors = mActors + ", ";
          } else {
            mActors = "";
          }
        } else if ("director".equals(qName)) {
          mState = STATUS_DIRECTORS;
          if (mMultipleDirectors) {
            mDirectors = mDirectors + ", ";
          } else {
            mDirectors = "";
          }
        } else if ("aspect".equals(qName)) {
          mState = STATUS_ASPECT;
          mAspect = "";
        } else if ("date".equals(qName)) {
          mState = STATUS_DATE;
          mAspect = "";
        } else if ("sub-title".equals(qName)) {
          mState = STATUS_SUBTITLE;
          mSubTitle = "";
        } else if (!"credits".equals(qName) && !"video".equals(qName)) {
          mLog.info("Unknown tag in SweDB data: " + qName);
        }
        break;
      }
      case (STATUS_WAITING): {
        if ("programme".equals(qName)) {
          mDescription = "";
          mTitle = "";
          mGenre = "";
          mEpisode = "";
          mActors = "";
          mDirectors = "";
          mEpisodeNumber = "";
          mAspect = "";
          mDate = "";
          mSubTitle = "";

          mMultipleGenres = false;
          mMultipleActors = false;
          mMultipleDirectors = false;
          try {
            String time = attributes.getValue("start");
            SimpleDateFormat format = new SimpleDateFormat("yyyyMMddkkmmss ZZZZ");
            
            Calendar cal = Calendar.getInstance();            
            cal.setTime(format.parse(time.substring(0,20)));
            cal.setTimeInMillis(cal.getTimeInMillis() - cal.getTimeZone().getRawOffset());
            
            mStartDate = new devplugin.Date(cal.get(Calendar.YEAR), 
                cal.get(Calendar.MONTH)+1, cal.get(Calendar.DAY_OF_MONTH));

            mStartHour = cal.get(Calendar.HOUR_OF_DAY);
            mStartMin = cal.get(Calendar.MINUTE);
            
            mState = STATUS_PROG;
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

              mEndHour = cal.get(Calendar.HOUR_OF_DAY);
              mEndMin = cal.get(Calendar.MINUTE);
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
    switch (mState) {
      case (STATUS_TITLE): {
        mTitle = mTitle + new String(ch, start, length);
        break;
      }
      case (STATUS_DESC): {
        mDescription = mDescription + new String(ch, start, length);
        break;
      }
      case (STATUS_GENRE): {
        mGenre = mGenre + new String(ch, start, length);
        break;
      }
      case (STATUS_EPISODE): {
        mEpisode = mEpisode + new String(ch, start, length);
        break;
      }
      case (STATUS_ACTORS): {
        mActors = mActors + new String(ch, start, length);
        break;
      }
      case (STATUS_DIRECTORS): {
        mDirectors = mDirectors + new String(ch, start, length);
        break;
      }
      case (STATUS_EPISODE_NUMBER): {
        mEpisodeNumber = mEpisodeNumber + new String(ch, start, length);
        break;
      }
      case (STATUS_ASPECT): {
        mAspect = mAspect + new String(ch, start, length);
        break;
      }
      case (STATUS_DATE): {
        mDate = mDate + new String(ch, start, length);
        break;
      }
      case (STATUS_SUBTITLE): {
        mSubTitle = mSubTitle + new String(ch, start, length);
        break;
      }

    }
  }

  public void endElement(String uri, String localName, String qName) {
    switch (mState) {
      case (STATUS_TITLE): {
        if ("title".equals(qName)) {
          mState = STATUS_PROG;
        }
        break;
      }
      case (STATUS_DESC): {
        if ("desc".equals(qName)) {
          mState = STATUS_PROG;
        }
        break;
      }
      case (STATUS_GENRE): {
        if ("category".equals(qName)) {
          mState = STATUS_PROG;
          mMultipleGenres = true;
        }
        break;
      }
      case (STATUS_EPISODE): {
        if ("episode-num".equals(qName)) {
          mState = STATUS_PROG;
        }
        break;
      }
      case (STATUS_EPISODE_NUMBER): {
        if ("episode-num".equals(qName)) {
          mState = STATUS_PROG;
        }
        break;
      }
      case (STATUS_ACTORS): {
        if ("actor".equals(qName)) {
          mState = STATUS_PROG;
          mMultipleActors = true;
        }
        break;
      }
      case (STATUS_DIRECTORS): {
        if ("director".equals(qName)) {
          mState = STATUS_PROG;
          mMultipleDirectors = true;
        }
        break;
      }
      case (STATUS_ASPECT): {
        if ("aspect".equals(qName)) {
          mState = STATUS_PROG;
        }
        break;
      }
      case (STATUS_DATE): {
        if ("date".equals(qName)) {
          mState = STATUS_PROG;
        }
        break;
      }
      case (STATUS_SUBTITLE): {
        if ("sub-title".equals(qName)) {
          mState = STATUS_PROG;
        }
        break;
      }

      case (STATUS_PROG): {
        if ("programme".equals(qName)) {
          mState = STATUS_WAITING;
          if (!mDayProgsHashTable.containsKey(mStartDate.toString())) {
            mMcdp = new MutableChannelDayProgram(mStartDate, mChannel);
            mDayProgsHashTable.put(mStartDate.toString(), mMcdp);
          } else {
            mMcdp = mDayProgsHashTable.get(mStartDate.toString());
          }
          MutableProgram prog = new MutableProgram(mMcdp.getChannel(), mStartDate,
                  mStartHour, mStartMin, true);

          int progLength = (mEndHour * 60) + mEndMin - (mStartHour * 60) - mStartMin;
          // Assumption: If the program length is less than 0, the program spans midnight
          if (progLength < 0) {
            progLength += 24 * 60; // adding 24 hours to the length
          }
          // Only allow program length for 12 hours.... This will take care of
          // possible DST problems
          if ((progLength > 0) && (progLength < 12 * 60)) {
            prog.setLength(progLength);
          }
          prog.setTitle(mTitle);
          // Since we don't have a field for sub titles, we add it to description
          mDescription = (mSubTitle + "\n" + mDescription).trim();

          String shortDesc = MutableProgram.generateShortInfoFromDescription(mDescription);           
          prog.setShortInfo(shortDesc);
                    
          if (((DataHydraChannelGroup)mChannel.getGroup()).isShowRegister() && "true".equals(mDataService.getProperties().getProperty(SweDBTvDataService.SHOW_REGISTER_TEXT, "true"))) {
            mDescription += "\n\n" + mLocalizer.msg("register", "Please Register at {0}", mChannel.getWebpage());
          }

          prog.setDescription(mDescription);

          if (mGenre.length() > 0) {
            mGenre = mGenre.substring(0, 1).toUpperCase() + mGenre.substring(1);
            prog.setTextField(ProgramFieldType.GENRE_TYPE, mGenre);
          }
          if (mActors.length() > 0) {
            prog.setTextField(ProgramFieldType.ACTOR_LIST_TYPE, mActors);
          }
          if (mDirectors.length() > 0) {
            prog.setTextField(ProgramFieldType.DIRECTOR_TYPE, mDirectors);
          }
          if (mEpisode.length() > 0) {
            prog.setTextField(ProgramFieldType.EPISODE_TYPE, mEpisode);
          }
          if (mEpisodeNumber.length() > 0) {
            // format is   season/totalseasons.episodenum/totalepisode.part/totalparts
            // where current numbers start at 0, while total numbers start at 1
            String[] ep = mEpisodeNumber.split("\\.");
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
          if (mAspect.length() > 0) {
            if (mAspect.equalsIgnoreCase("4:3")) {
              info = info | Program.INFO_VISION_4_TO_3;
            }
            if (mAspect.equalsIgnoreCase("16:9")) {
              info = info | Program.INFO_VISION_16_TO_9;
            }
          }
          if (mGenre.length() > 0) {
            if (mGenre.toLowerCase().indexOf("series") > -1) {
              info = info | Program.INFO_CATEGORIE_SERIES;
            } else if (mGenre.toLowerCase().indexOf("movie") > -1) {
              info = info | Program.INFO_CATEGORIE_MOVIE;
            }
          }
          if (info != 0) {
            prog.setInfo(info);
          }
          if (mDate.length() > 0) {
            if (mDate.length() == 4) {
              int year = Integer.parseInt(mDate);
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
