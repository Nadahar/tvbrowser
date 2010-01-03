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

import java.io.File;
import java.io.IOException;
import java.util.UUID;

import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.index.IndexWriter.MaxFieldLength;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.ScoreDoc;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;
import org.apache.lucene.store.FSDirectory;

public final class ImdbDatabase {
  private static final int MAX_FIELD_LENGTH = 200;
  private static final String[] TITLE_SUFFIX = { "(Fortsetzung)", "(Teil 1)", "(Teil 2)", "(Teil 3)", "(Teil 4)", "Part 1", "Part 2", "Part 3", "Part 4", "(1)", "(2)", "(3)", "(4)" };
  private static final String ITEM_TYPE = "ITEM_TYPE";
  private static final String TYPE_MOVIE = "TYPE_MOVIE";
  private static final String TYPE_AKA = "TYPE_AKA";
  private static final String TYPE_RATING = "TYPE_RATING";

  private static final String MOVIE_TITLE = "MOVIE_TITLE";
  private static final String MOVIE_TITLE_NORMALISED = "MOVIE_TITLE_NORMALISED";
  private static final String MOVIE_YEAR = "MOVIE_YEAR";
  private static final String MOVIE_ID = "MOVIE_ID";
  private static final String MOVIE_RATING = "MOVIE_RATING";
  private static final String MOVIE_VOTES = "MOVIE_VOTES";
  private static final String MOVIE_DISTRIBUTION = "MOVIE_DISTRIBUTION";

  private static final String EPISODE_TITLE = "EPISODE_TITLE";
  private static final String EPISODE_TITLE_NORMALISED = "EPISODE_TITLE_NORMALISED";

  private File mCurrentPath;

  private IndexSearcher mSearcher = null;
  private IndexWriter mWriter = null;

  public ImdbDatabase(final File imdbDatabase) {
    mCurrentPath = imdbDatabase;
    // make sure the directory exists
    if (!mCurrentPath.exists()) {
      try {
        mCurrentPath.mkdirs();
      } catch (Exception e) {
        e.printStackTrace();
      }
    }
  }

  public void deleteDatabase() {
    close();
    for (File f : mCurrentPath.listFiles()) {
      try {
        f.delete();
      } catch (SecurityException e) {
        e.printStackTrace();
      }
    }
    openForWriting();
  }

  public void init() {
    openForReading();
  }

  public boolean isInitialised() {
    try {
      if ((mSearcher != null) && (mSearcher.maxDoc() > 1)) {
        return true;
      }
    } catch (IOException e) {
      return false;
    }

    return false;
  }

  public String addTitle(final String movieTitle, final String episode, final int year) {
    String movieID = null;
    try {
      final Document doc = new Document();
      movieID = UUID.randomUUID().toString();
      doc.add(new Field(MOVIE_ID, movieID, Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO));
      doc.add(new Field(ITEM_TYPE, TYPE_MOVIE, Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO));
      doc.add(new Field(MOVIE_TITLE, movieTitle, Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO));
      doc.add(new Field(MOVIE_TITLE_NORMALISED, normalise(movieTitle), Field.Store.YES, Field.Index.NOT_ANALYZED,
          Field.TermVector.NO));
      doc.add(new Field(MOVIE_YEAR, Integer.toString(year), Field.Store.YES, Field.Index.NOT_ANALYZED,
          Field.TermVector.NO));
      if (episode != null && episode.length() > 0) {
        doc.add(new Field(EPISODE_TITLE, episode, Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO));
        doc.add(new Field(EPISODE_TITLE_NORMALISED, normalise(episode), Field.Store.YES, Field.Index.NOT_ANALYZED,
            Field.TermVector.NO));
      }
      mWriter.addDocument(doc);
    } catch (IOException e) {
      e.printStackTrace();
    }

    return movieID;
  }

  public void addAkaTitle(final String movieId, final String title, final String episode, final int year) {
    try {
      final Document doc = new Document();
      doc.add(new Field(MOVIE_ID, movieId, Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO));
      doc.add(new Field(ITEM_TYPE, TYPE_AKA, Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO));
      doc.add(new Field(MOVIE_TITLE, title, Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO));
      doc.add(new Field(MOVIE_TITLE_NORMALISED, normalise(title), Field.Store.YES, Field.Index.NOT_ANALYZED,
          Field.TermVector.NO));
      doc.add(new Field(MOVIE_YEAR, Integer.toString(year), Field.Store.YES, Field.Index.NOT_ANALYZED,
          Field.TermVector.NO));
      if (episode != null && episode.length() > 0) {
        doc.add(new Field(EPISODE_TITLE, episode, Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO));
        doc.add(new Field(EPISODE_TITLE_NORMALISED, normalise(episode), Field.Store.YES, Field.Index.NOT_ANALYZED,
            Field.TermVector.NO));
      }
      mWriter.addDocument(doc);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private String normalise(final String str) {
    if (str == null) {
      return "";
    }
    if (str.length() == 0) {
      return str;
    }
    final String lowerCase = str.toLowerCase();
    final int length = lowerCase.length();
    final StringBuilder builder = new StringBuilder(length + 4);
    for (int i = 0; i < length; i++) {
      final char character = lowerCase.charAt(i);
      switch (character) {
      // replace umlauts
      case '\u00E4': {
        builder.append("ae");
        break;
      }
      case '\u00F6': {
        builder.append("oe");
        break;
      }
      case '\u00FC': {
        builder.append("ue");
        break;
      }
      case '\u00DF': {
        builder.append("ss");
        break;
      }
        // remove some special characters
      case ',':
      case '\'':
      case ':':
      case '-': {
        break;
      }
        // remove all double spaces
      case ' ': {
        if (builder.length() > 0 && builder.charAt(builder.length() - 1) != ' ') {
          builder.append(' ');
        }
        break;
      }
      default:
        builder.append(character);
      }
    }
    String result = builder.toString();
    // remove blank before the final ? or !
    if (result.length() > 2 && (result.endsWith("!") || result.endsWith("?"))
        && result.charAt(result.length() - 2) == ' ') {
      result = result.substring(0, result.length() - 2) + result.charAt(result.length() - 1);
    }
    return result;
  }

  public void addRating(final String movieId, final int rating, final int votes, final String distribution) {
    try {
      final Document doc = new Document();
      doc.add(new Field(MOVIE_ID, movieId, Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO));
      doc.add(new Field(ITEM_TYPE, TYPE_RATING, Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO));
      doc.add(new Field(MOVIE_RATING, Integer.toString(rating), Field.Store.YES, Field.Index.NOT_ANALYZED,
          Field.TermVector.NO));
      doc.add(new Field(MOVIE_VOTES, Integer.toString(votes), Field.Store.YES, Field.Index.NOT_ANALYZED,
          Field.TermVector.NO));
      doc.add(new Field(MOVIE_DISTRIBUTION, distribution, Field.Store.YES, Field.Index.NOT_ANALYZED,
          Field.TermVector.NO));
      mWriter.addDocument(doc);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void optimizeIndex() throws IOException {
    mWriter.commit();
    try {
      mWriter.optimize();
      mWriter.close();
      openForWriting();
    } catch (Exception e) {
      e.printStackTrace();
    }
  }

  public void openForReading() {
    close();
    if (!mCurrentPath.exists() || mCurrentPath.listFiles().length > 0) {
      try {
        // open index reader readonly for better performance
        FSDirectory directory = FSDirectory.open(mCurrentPath);
        final IndexReader reader = IndexReader.open(directory, true);
        mSearcher = new IndexSearcher(reader);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }
  }

  public void openForWriting() {
    if (!mCurrentPath.exists() || mCurrentPath.listFiles().length < 2) {
      try {
        mWriter = new IndexWriter(FSDirectory.open(mCurrentPath), new SimpleAnalyzer(), new MaxFieldLength(MAX_FIELD_LENGTH));
        mWriter.addDocument(new Document());
        mWriter.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else if (new File(mCurrentPath, "write.lock").exists()) {
      try {
        new File(mCurrentPath, "write.lock").delete();
      } catch (SecurityException e) {
        e.printStackTrace();
      }
    }

    if (mWriter != null) {
      try {
        mWriter.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    try {
      // open reader and writer
      mWriter = new IndexWriter(FSDirectory.open(mCurrentPath), new SimpleAnalyzer(), new MaxFieldLength(MAX_FIELD_LENGTH));
      FSDirectory directory = FSDirectory.open(mCurrentPath);
      final IndexReader reader = IndexReader.open(directory, true);
      mSearcher = new IndexSearcher(reader);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void close() {
    try {
      if (mWriter != null) {
        mWriter.commit();
        mWriter.close();
        mWriter = null;
      }
      if (mSearcher != null) {
        mSearcher.close();
        mSearcher = null;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public String getOrCreateMovieId(final String movieTitle, final String episode, final int year) {
    if (mSearcher == null) {
      return addTitle(movieTitle, episode, year);
    }
    try {
      final BooleanQuery bQuery = new BooleanQuery();
      bQuery.add(new TermQuery(new Term(ITEM_TYPE, TYPE_MOVIE)), BooleanClause.Occur.MUST);
      bQuery.add(new TermQuery(new Term(MOVIE_TITLE, movieTitle)), BooleanClause.Occur.MUST);
      bQuery.add(new TermQuery(new Term(MOVIE_YEAR, Integer.toString(year))), BooleanClause.Occur.MUST);
      if (episode != null && episode.length() > 0) {
        bQuery.add(new TermQuery(new Term(EPISODE_TITLE, episode)), BooleanClause.Occur.MUST);
      }

      final TopDocs topDocs = mSearcher.search(bQuery, null, 1);

      if (topDocs == null) {
        return null;
      }

      if (topDocs.totalHits > 0) {
        final Document document = mSearcher.doc(topDocs.scoreDocs[0].doc);
        return document.getField(MOVIE_ID).stringValue();
      } else {
        return addTitle(movieTitle, episode, year);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public String getMovieEpisodeId(final String title, final String episode, final String originalTitle, final String originalEpisode, final int year) {
    if (!isInitialised()) {
      return null;
    }
    String id = getEpisodeId(title, episode, year);
    if (id == null && originalTitle != null) {
      id = getEpisodeId(originalTitle, originalEpisode, year);
    }
    return id;
  }

  private String getEpisodeId(final String title, final String episode, final int year) {
    if (episode == null || episode.length() == 0 || !isInitialised()) {
      return null;
    }
    final String normalizedTitle = normalise(title);
    final String normalizedEpisode = normalise(episode);
    final BooleanQuery bQuery = new BooleanQuery();
    bQuery.add(new TermQuery(new Term(ITEM_TYPE, TYPE_MOVIE)), BooleanClause.Occur.MUST);
    bQuery.add(new TermQuery(new Term(MOVIE_TITLE_NORMALISED, normalizedTitle)), BooleanClause.Occur.MUST);
    bQuery.add(new TermQuery(new Term(EPISODE_TITLE_NORMALISED, normalizedEpisode)), BooleanClause.Occur.MUST);

    if (year > 0) {
      bQuery.add(new TermQuery(new Term(MOVIE_YEAR, Integer.toString(year - 1))), BooleanClause.Occur.SHOULD);
      bQuery.add(new TermQuery(new Term(MOVIE_YEAR, Integer.toString(year))), BooleanClause.Occur.SHOULD);
      bQuery.add(new TermQuery(new Term(MOVIE_YEAR, Integer.toString(year + 1))), BooleanClause.Occur.SHOULD);
    }

    try {
      final TopDocs hits = mSearcher.search(bQuery, 1);
      if (hits.totalHits > 0) {
        final Document document = mSearcher.doc(hits.scoreDocs[0].doc);
        printDocument(document);
        return document.getField(MOVIE_ID).stringValue();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  protected String getMovieId(final String title, final String episode, String originalTitle, String originalEpisode, final int year) {
    if (mSearcher == null) {
      return null;
    }

    final String normalizedTitle = normalise(title);
    final String normalizedEpisode = normalise(episode);

    // first search title
    String movieId = getMovieIdFromTitle(normalizedTitle, normalizedEpisode, year, TYPE_MOVIE);
    if (movieId != null) {
      return movieId;
    }

    // next search title in A.K.A. list
    movieId = getMovieIdFromTitle(normalizedTitle, normalizedEpisode, year, TYPE_AKA);
    if (movieId != null) {
      return movieId;
    }

    // next search only original title
    if (originalTitle != null) {
      String normalisedOriginal = normalise(originalTitle);
      String normalisedOrigEpisode = normalise(originalEpisode);
      movieId = getMovieIdFromTitle(normalisedOriginal, normalisedOrigEpisode, year, TYPE_MOVIE);
      if (movieId != null) {
        return movieId;
      }
      // original title in A.K.A. list (may happen with spelling alternatives)
      movieId = getMovieIdFromTitle(normalisedOriginal, normalisedOrigEpisode, year, TYPE_AKA);
      if (movieId != null) {
        return movieId;
      }
    }

    // and now try with shortened title if there is a common suffix
    for (String suffix : TITLE_SUFFIX) {
      if (title.endsWith(suffix)) {
        return getMovieId(title.substring(0, title.length() - suffix.length()).trim(), episode, null, null, year);
      }
      if (originalTitle != null && originalTitle.endsWith(suffix)) {
        return getMovieId(title, episode, originalTitle.substring(0, originalTitle.length() - suffix.length()).trim(), originalEpisode, year);
      }
    }
    
    // nothing found yet, so try everything again without year
    if (year > 0) {
      String id =  getMovieId(title, episode, originalTitle, originalEpisode, 0);
      if (id != null) {
        return id;
      }
    }

    // If the original Episode is given, try without it
    if (originalEpisode != null && originalEpisode.length() > 0) {
      String id =  getMovieId(title, episode, originalTitle, null, year);
      if (id != null) {
        return id;
      }
    }
    
    return null;
  }

  private String getMovieIdFromTitle(final String title, final String episode, final int year, String itemType) {
    if (!isInitialised()) {
      return null;
    }

    BooleanQuery bQuery = new BooleanQuery();
    bQuery.add(new TermQuery(new Term(ITEM_TYPE, itemType)), BooleanClause.Occur.MUST);
    bQuery.add(new TermQuery(new Term(MOVIE_TITLE_NORMALISED, title)), BooleanClause.Occur.MUST);
    if (episode != null && episode.length() > 0) {
      bQuery.add(new TermQuery(new Term(EPISODE_TITLE_NORMALISED, episode)), BooleanClause.Occur.MUST);
    }

    if (year > 0) {
      bQuery.add(new TermQuery(new Term(MOVIE_YEAR, Integer.toString(year - 1))), BooleanClause.Occur.SHOULD);
      bQuery.add(new TermQuery(new Term(MOVIE_YEAR, Integer.toString(year))), BooleanClause.Occur.SHOULD);
      bQuery.add(new TermQuery(new Term(MOVIE_YEAR, Integer.toString(year + 1))), BooleanClause.Occur.SHOULD);
    }
    try {
      TopDocs topDocs = mSearcher.search(bQuery, null, 1);

      if (topDocs.totalHits > 0) {
        final Document document = mSearcher.doc(topDocs.scoreDocs[0].doc);

        printDocument(document);

        return document.getField(MOVIE_ID).stringValue();
      }
    } catch (IOException e1) {
      e1.printStackTrace();
    }
    return null;
  }

  private void printDocument(final Document document) {
    /*
     * System.out.print(document.getField(MOVIE_TITLE).stringValue());
     * 
     * if (document.getField(EPISODE_TITLE) != null) { System.out.print(" : " +
     * document.getField(EPISODE_TITLE).stringValue()); }
     * 
     * System.out.println(" : " + document.getField(MOVIE_YEAR).stringValue() +
     * " : " + document.getField(MOVIE_ID).stringValue());
     */
  }

  public ImdbRating getRatingForId(final String id) {
    if (id == null || !isInitialised()) {
      return null;
    }
    try {
      final BooleanQuery bQuery = new BooleanQuery();
      bQuery.add(new TermQuery(new Term(ITEM_TYPE, TYPE_RATING)), BooleanClause.Occur.MUST);
      bQuery.add(new TermQuery(new Term(MOVIE_ID, id)), BooleanClause.Occur.MUST);

      final TopDocs topDocs = mSearcher.search(bQuery, null, 1);

      if (topDocs.totalHits > 0) {
        final Document document = mSearcher.doc(topDocs.scoreDocs[0].doc);

        return new ImdbRating(Integer.parseInt(document.getField(MOVIE_RATING).stringValue()), Integer
            .parseInt(document.getField(MOVIE_VOTES).stringValue()), document.getField(MOVIE_DISTRIBUTION)
            .stringValue(), id);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return null;
  }

  public ImdbMovie getMovieForId(final String id) {
    if (id == null || !isInitialised()) {
      return null;
    }
    try {
      final BooleanQuery bQuery = new BooleanQuery();
      bQuery.add(new TermQuery(new Term(ITEM_TYPE, TYPE_MOVIE)), BooleanClause.Occur.MUST);
      bQuery.add(new TermQuery(new Term(MOVIE_ID, id)), BooleanClause.Occur.MUST);

      final TopDocs topDocs = mSearcher.search(bQuery, null, 1);

      if (topDocs.totalHits > 0) {
        final Document document = mSearcher.doc(topDocs.scoreDocs[0].doc);

        ImdbMovie movie = new ImdbMovie();
        movie.setTitle(document.getField(MOVIE_TITLE).stringValue());
        movie.setYear(Integer.parseInt(document.getField(MOVIE_YEAR).stringValue()));
        if (document.getField(EPISODE_TITLE) != null) {
          movie.setEpisode(document.getField(EPISODE_TITLE).stringValue());
        }

        final BooleanQuery bQueryAKA = new BooleanQuery();
        bQueryAKA.add(new TermQuery(new Term(ITEM_TYPE, TYPE_AKA)), BooleanClause.Occur.MUST);
        bQueryAKA.add(new TermQuery(new Term(MOVIE_ID, id)), BooleanClause.Occur.MUST);
        final TopDocs topDocsAKA = mSearcher.search(bQueryAKA, null, 1);

        for (ScoreDoc sdoc:topDocsAKA.scoreDocs) {
          final Document doc = mSearcher.doc(sdoc.doc);
          final String title = doc.getField(MOVIE_TITLE).stringValue();
          final String episode;
          if (doc.getField(EPISODE_TITLE) != null) {
            episode = doc.getField(EPISODE_TITLE).stringValue();
          } else {
            episode = null;
          }
          
          movie.addAka(new ImdbAka(title, episode, Integer.parseInt(doc.getField(MOVIE_YEAR).stringValue())));
        }

        return movie;
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return null;
  }


  public String getDatabaseSizeMB() {
    if (!isInitialised()) {
      return "0";
    }
    long size = 0;
    for (File f : mCurrentPath.listFiles()) {
      size = size + f.length();
    }
    return String.valueOf(size / (1024 * 1024));
  }

}
