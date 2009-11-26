/*
 * This program is free software: you can redistribute it and/or modify it under
 * the terms of the GNU General Public License as published by the Free Software
 * Foundation, either version 3 of the License, or (at your option) any later
 * version.
 * 
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 * 
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 */
package imdbplugin;

import java.io.BufferedInputStream;
import java.io.BufferedReader;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.zip.GZIPInputStream;

import util.io.IOUtilities;
import util.ui.Localizer;
import util.ui.progress.ProgressInputStream;
import devplugin.ProgressMonitor;


public class ImdbParser {
  private static final Localizer mLocalizer = Localizer.getLocalizerFor(ImdbParser.class);

  private static final java.util.logging.Logger mLog = java.util.logging.Logger
      .getLogger(ImdbParser.class.getName());

  private static final Pattern EPISODE_PATTERN = Pattern
      .compile("^(.*?)(?:\\W\\(\\#.*\\))?$");
  private static final Pattern MOVIE_PREFIX_PATTERN = Pattern.compile("(.*), ([A-Z][a-z']{0,2})");

  private static final int STEPS_TO_REPORT_PROGRESS = 1000;

  private ImdbDatabase mDatabase;
  private String mServer;
  private boolean mRunParser = true;

  public ImdbParser(final ImdbDatabase db, final String server) {
    mDatabase = db;
    mServer = server;
  }

  public void startParsing(final ProgressMonitor monitor) throws IOException {
    int ratingCount = 0;
    mDatabase.deleteDatabase();

    monitor.setMaximum(getFileSize(mServer));
    monitor.setMessage(mLocalizer.msg("download", "Downloading files"));

    mRunParser = true;

    ProgressInputStream progressInputStream = new ProgressInputStream(
        downloadFile(monitor, "aka-titles.list.gz"), monitor);

    if (mRunParser) {
      parseAkaTitles(new GZIPInputStream(progressInputStream), monitor);
    }
    if (mRunParser) {
      optimizeDatabase(monitor);
    }
    if (mRunParser) {
      progressInputStream = new ProgressInputStream(downloadFile(monitor,
          "ratings.list.gz"), monitor, progressInputStream.getCurrentPosition());
      ratingCount = parseRatings(new GZIPInputStream(progressInputStream), monitor);
    }

    if (mRunParser) {
      optimizeDatabase(monitor);
      ImdbPlugin.getInstance().setCurrentDatabaseVersion(ratingCount);
    } else {
      // Cancel was pressed, all Files have to be deleted
      mDatabase.deleteDatabase();
    }
    mDatabase.openForReading();
  }

  private BufferedInputStream downloadFile(final ProgressMonitor monitor,
      final String fileName) throws MalformedURLException, IOException,
      FileNotFoundException {
    final URL url = new URL(mServer + fileName);
    final File tempFile = File.createTempFile("imdb", null);
    IOUtilities.download(url, tempFile);
    final BufferedInputStream stream = new BufferedInputStream(
        new FileInputStream(
        tempFile));
    tempFile.deleteOnExit();
    return stream;
  }

  private void optimizeDatabase(final ProgressMonitor monitor)
      throws IOException {
    monitor.setMessage(mLocalizer.msg("optimize", "Optimizing database"));
    mDatabase.close();
    mDatabase.openForWriting();
    mDatabase.optimizeIndex();
  }

  private int getFileSize(final String server) {
    final String[] fileNames = new String[] { "aka-titles", "ratings" };
    final Integer[] fileSizes = new Integer[] { 5950067, 5067908 };

    try {
      
      int size = 0;
      final String filesizes = new String(IOUtilities
          .loadFileFromHttpServer(new URL(server + "filesizes")));

      for (int i = 0; i < fileNames.length; i++) {
        final Matcher m = Pattern.compile(
            "^" + fileNames[i] + "\\.list\\W(\\d*)$",
            Pattern.MULTILINE).matcher(filesizes);

        if (m.find()) {
          size += Integer.parseInt(m.group(1));
        } else {
          size += fileSizes[i];
        }
      }
      return size;
    } catch (IOException e) {
      e.printStackTrace();
    }

    // if an exception occurred, return the default sizes
    int size = 0;
    for (int i = 0; i < fileSizes.length; i++) {
      size += fileSizes[i];
    }
    return size;
  }

  public void stopParsing() {
    mRunParser = false;
  }

  private void parseAkaTitles(final InputStream inputStream,
      final ProgressMonitor monitor) throws IOException {
    final BufferedReader reader = new BufferedReader(new InputStreamReader(
        inputStream, "ISO-8859-15"));
    String line = reader.readLine();

    final Pattern moviePattern = Pattern
        .compile("^(.*)\\((?:(\\d{4,4}).*|\\?\\?\\?\\?)\\)(?:\\W*\\((.*)\\))?(?:\\W\\{(.*)\\})?.*$");
    final Pattern akaPattern = Pattern
        .compile("^\\(aka (.*) \\((?:(\\d{4,4}).*|\\?\\?\\?\\?)\\)(?:\\W\\((.*)\\))?(?:\\W\\{(.*)\\})?\\).*$");

    String movieId = null;
    boolean startFound = false;
    int count = 0;
    while (line != null && mRunParser) {
      line = line.trim();
      if (!startFound && line.startsWith("==========")) {
        startFound = true;
      } else if (startFound) {

        if (line.length() > 0) {
          if (line.startsWith("(aka ")) {
            if (movieId != null) {
              final Matcher matcher = akaPattern.matcher(line);
              if (matcher.matches()) {
                final String title = cleanMovieTitle(matcher.group(1).trim());
                int year = -1;
                if (matcher.group(2) != null) {
                  year = Integer.parseInt(matcher.group(2));
                }
                final String episode = cleanEpisodeTitle(matcher.group(4));

                mDatabase.addAkaTitle(movieId, title, episode, year);
                if (++count % STEPS_TO_REPORT_PROGRESS == 0 || count == 1) {
                  monitor.setMessage(mLocalizer.msg("akaTitles",
                      "Alternative title {0}", count));
                }
              }
            }
            else {
              mLog.severe("Parse error: movieId unknown for alternative title");
            }
          } else {
            final Matcher matcher = moviePattern.matcher(line);
            if (matcher.matches()) {
              final String movieTitle = cleanMovieTitle(matcher.group(1).trim());
              int year = -1;
              if (matcher.group(2) != null) {
                year = Integer.parseInt(matcher.group(2));
              }
              final String episode = cleanEpisodeTitle(matcher.group(4));
              movieId = mDatabase.getOrCreateMovieId(movieTitle, episode, year);
            }
          }
        }
        else {
          // blank line, prepare for new movie
          movieId = null;
        }

      }

      line = reader.readLine();
    }
    reader.close();
  }

  private int parseRatings(final InputStream inputStream,
      final ProgressMonitor monitor) throws IOException {
    final Pattern ratingPattern = Pattern
        .compile("^(.*)(?:\\W\\((\\d{4,4}|\\?\\?\\?\\?).*?\\))(?:\\W\\((.*)\\))?(?:\\W\\{(.*)\\})?$");

    final BufferedReader reader = new BufferedReader(new InputStreamReader(
        inputStream, "ISO-8859-15"));
    String line = reader.readLine();

    boolean startFound = false;
    int count = 0;
    while (line != null && mRunParser) {
      line = line.trim();
      if (!startFound && line.startsWith("MOVIE RATINGS REPORT")) {
        startFound = true;
      } else if (startFound && line.startsWith("-------------")) {
        startFound = false;
      } else if (startFound && line.startsWith("New  Distribution  Votes  Rank  Title")) {
        // Ignore this line!
      } else if (startFound && line.length() > 0) {
        final String distribution = line.substring(0, 10);
        final int votes = Integer.parseInt(line.substring(11, 19).trim());
        final String ratingStr = line.substring(20, 25).trim();
        final int rating = Integer.parseInt(ratingStr.replaceAll("\\.", ""));
        final String title = line.substring(25).trim();

        final Matcher matcher = ratingPattern.matcher(title);
        if (matcher.matches()) {
          final String movieTitle = cleanMovieTitle(matcher.group(1).trim());
          int year = -1;
          String yearString = matcher.group(2);
          if (yearString != null) {
            if (!yearString.equals("????")) {
              try {
                year = Integer.parseInt(yearString);
              } catch (NumberFormatException e) {
                mLog.warning("unexpected year: " + yearString);
              }
            }
          }
          else {
            mLog.warning("unexpected null year");
          }
          final String episode = cleanEpisodeTitle(matcher.group(4));

          mDatabase.addRating(mDatabase.getOrCreateMovieId(movieTitle, episode, year), rating, votes, distribution);
          if (++count % STEPS_TO_REPORT_PROGRESS == 0 || count == 1) {
            monitor.setMessage(mLocalizer.msg("ratings", "Rating {0}", count));
          }
        }
        else {
          mLog.warning("Non matching line: " + line);
        }
      }


      line = reader.readLine();
    }

    reader.close();
    return count;
  }

  private String cleanEpisodeTitle(final String episode) {
    if (episode == null) {
      return "";
    }

    final Matcher m = EPISODE_PATTERN.matcher(episode);
    m.find();

    return m.group(1);
  }

  private String cleanMovieTitle(String movieTitle) {
    if (movieTitle.startsWith("\"") && movieTitle.endsWith("\"")) {
      movieTitle = movieTitle.substring(1, movieTitle.length() - 1);
    }

    final Matcher matcher = MOVIE_PREFIX_PATTERN.matcher(movieTitle);
    if (matcher.matches()) {
      movieTitle = matcher.group(2) + " " + matcher.group(1);
    }

    return movieTitle;
  }
}
