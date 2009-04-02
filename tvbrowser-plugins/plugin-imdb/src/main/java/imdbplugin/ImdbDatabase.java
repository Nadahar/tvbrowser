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
import java.util.Iterator;
import java.util.UUID;

import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.Term;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.TermQuery;
import org.apache.lucene.search.TopDocs;

public final class ImdbDatabase {
  private static final String[] TITLE_SUFFIX = { "(Fortsetzung)", "(Teil 1)",
      "(Teil 2)", "(Teil 3)", "(Teil 4)" };
  private static final String ITEM_TYPE = "ITEM_TYPE";
  private static final String TYPE_MOVIE = "TYPE_MOVIE";
  private static final String TYPE_AKA = "TYPE_AKA";
  private static final String TYPE_RATING = "TYPE_RATING";

  private static final String MOVIE_TITLE = "MOVIE_TITLE";
  private static final String MOVIE_TITLE_NORMALISED = "MOVIE_TITLE_NORMALISED";
  private static final String MOVIE_YEAR = "MOVIE_YEAR";
  private static final String MOVIE_TYPE = "MOVIE_TYPE";
  private static final String MOVIE_ID = "MOVIE_ID";
  private static final String MOVIE_RATING = "MOVIE_RATING";
  private static final String MOVIE_VOTES = "MOVIE_VOTES";
  private static final String MOVIE_DISTRIBUTION = "MOVIE_DISTRIBUTION";

  private static final String EPISODE_TITLE = "EPISODE_TITLE";
  private static final String EPISODE_TITLE_NORMALISED = "EPISODE_TITLE_NORMALISED";

  private File mCurrentPath;

  private IndexSearcher mSearcher;
  private IndexWriter mWriter;

  public ImdbDatabase(final File imdbDatabase) {
    mCurrentPath = imdbDatabase;
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
    reOpen();
  }

  public void init() {
    reOpen();
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

  public String addTitle(final String movieTitle, final String episode,
      final int year, final String type) {
    String movieID = null;
    try {
      final Document doc = new Document();
      movieID = UUID.randomUUID().toString();
      doc.add(new Field(MOVIE_ID, movieID, Field.Store.COMPRESS,
          Field.Index.NOT_ANALYZED, Field.TermVector.NO));
      doc.add(new Field(ITEM_TYPE, TYPE_MOVIE, Field.Store.COMPRESS,
          Field.Index.NOT_ANALYZED, Field.TermVector.NO));
      doc.add(new Field(MOVIE_TITLE, movieTitle, Field.Store.COMPRESS,
          Field.Index.NOT_ANALYZED, Field.TermVector.NO));
      doc.add(new Field(MOVIE_TITLE_NORMALISED, normalise(movieTitle),
          Field.Store.COMPRESS, Field.Index.NOT_ANALYZED, Field.TermVector.NO));
      doc.add(new Field(MOVIE_YEAR, Integer.toString(year),
          Field.Store.COMPRESS, Field.Index.NOT_ANALYZED, Field.TermVector.NO));
      if (type != null) {
        doc.add(new Field(MOVIE_TYPE, type, Field.Store.YES,
            Field.Index.NOT_ANALYZED, Field.TermVector.NO));
      }
      if (episode != null) {
        doc.add(new Field(EPISODE_TITLE, episode, Field.Store.COMPRESS,
            Field.Index.NOT_ANALYZED, Field.TermVector.NO));
        doc
            .add(new Field(EPISODE_TITLE_NORMALISED, normalise(episode),
                Field.Store.COMPRESS, Field.Index.NOT_ANALYZED,
                Field.TermVector.NO));
      }
      mWriter.addDocument(doc);
    } catch (IOException e) {
      e.printStackTrace();
    }

    return movieID;
  }

  public void addAkaTitle(final String movieId, final String title,
      final String episode, final int year, final String type) {
    try {
      final Document doc = new Document();
      doc.add(new Field(MOVIE_ID, movieId, Field.Store.COMPRESS,
          Field.Index.NOT_ANALYZED, Field.TermVector.NO));
      doc.add(new Field(ITEM_TYPE, TYPE_AKA, Field.Store.COMPRESS,
          Field.Index.NOT_ANALYZED, Field.TermVector.NO));
      doc.add(new Field(MOVIE_TITLE, title, Field.Store.COMPRESS,
          Field.Index.NOT_ANALYZED, Field.TermVector.NO));
      doc.add(new Field(MOVIE_TITLE_NORMALISED, normalise(title),
          Field.Store.COMPRESS, Field.Index.NOT_ANALYZED, Field.TermVector.NO));
      doc.add(new Field(MOVIE_YEAR, Integer.toString(year),
          Field.Store.COMPRESS, Field.Index.NOT_ANALYZED, Field.TermVector.NO));
      if (type != null) {
        doc.add(new Field(MOVIE_TYPE, type, Field.Store.YES,
            Field.Index.NOT_ANALYZED, Field.TermVector.NO));
      }
      if (episode != null) {
        doc.add(new Field(EPISODE_TITLE, episode, Field.Store.COMPRESS,
            Field.Index.NOT_ANALYZED, Field.TermVector.NO));
        doc
            .add(new Field(EPISODE_TITLE_NORMALISED, normalise(episode),
                Field.Store.COMPRESS, Field.Index.NOT_ANALYZED,
                Field.TermVector.NO));
      }
      mWriter.addDocument(doc);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private String normalise(final String str) {
    if (str.length() == 0) {
      return str;
    }
    final String lowerCase = str.toLowerCase();
    final int length = lowerCase.length();
    final StringBuilder builder = new StringBuilder(length + 4);
    for (int i = 0; i < length; i++) {
      final char character = lowerCase.charAt(i);
      switch (character) {
      case 'ä': {
        builder.append("ae");
        break;
      }
      case 'ö': {
        builder.append("oe");
        break;
      }
      case 'ü': {
        builder.append("ue");
        break;
      }
      case 'ß': {
        builder.append("ss");
        break;
      }
      default:
        builder.append(character);
      }
    }
    return builder.toString();
  }

  public void addRating(final String movieId, final int rating,
      final int votes, final String distribution) {
    try {
      final Document doc = new Document();
      doc.add(new Field(MOVIE_ID, movieId, Field.Store.COMPRESS,
          Field.Index.NOT_ANALYZED, Field.TermVector.NO));
      doc.add(new Field(ITEM_TYPE, TYPE_RATING, Field.Store.COMPRESS,
          Field.Index.NOT_ANALYZED, Field.TermVector.NO));
      doc.add(new Field(MOVIE_RATING, Integer.toString(rating),
          Field.Store.YES, Field.Index.NOT_ANALYZED, Field.TermVector.NO));
      doc.add(new Field(MOVIE_VOTES, Integer.toString(votes), Field.Store.YES,
          Field.Index.NOT_ANALYZED, Field.TermVector.NO));
      doc.add(new Field(MOVIE_DISTRIBUTION, distribution, Field.Store.COMPRESS,
          Field.Index.NOT_ANALYZED, Field.TermVector.NO));
      mWriter.addDocument(doc);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void optimizeIndex() throws IOException {
    mWriter.optimize();
    mWriter.close();
    reOpen();
  }

  public void reOpen() {
    mSearcher = null;
    mWriter = null;

    if (!mCurrentPath.exists() || mCurrentPath.listFiles().length == 0) {
      try {
        mWriter = new IndexWriter(mCurrentPath, new SimpleAnalyzer());
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

    // if (new File(mCurrentPath, "segments.gen").exists()) {
    try {
      final IndexReader reader = IndexReader.open(mCurrentPath);
      mSearcher = new IndexSearcher(reader);
    } catch (IOException e) {
      e.printStackTrace();
    }
    // }

    try {
      mWriter = new IndexWriter(mCurrentPath, new SimpleAnalyzer());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void close() {
    try {
      if (mWriter != null) {
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

  public String getOrCreateMovieId(final String movieTitle,
      final String episode, final int year, final String type) {
    if (mSearcher == null) {
      return null;
    }
    try {
      final BooleanQuery bQuery = new BooleanQuery();
      bQuery.add(new TermQuery(new Term(ITEM_TYPE, TYPE_MOVIE)),
          BooleanClause.Occur.MUST);
      bQuery.add(new TermQuery(new Term(MOVIE_TITLE, movieTitle)),
          BooleanClause.Occur.MUST);
      bQuery.add(new TermQuery(new Term(MOVIE_YEAR, Integer.toString(year))),
          BooleanClause.Occur.MUST);
      if (episode != null) {
        bQuery.add(new TermQuery(new Term(EPISODE_TITLE, episode)),
            BooleanClause.Occur.MUST);
      }
      if (type != null) {
        bQuery.add(new TermQuery(new Term(MOVIE_TYPE, type)),
            BooleanClause.Occur.MUST);
      }

      final Hits hits = mSearcher.search(bQuery);

      if (hits == null) {
        return null;
      }

      final Iterator<Hit> it = hits.iterator();

      if (it.hasNext()) {
        final Hit hit = it.next();
        final Document document = hit.getDocument();
        return document.getField(MOVIE_ID).stringValue();
      } else {
        return addTitle(movieTitle, episode, year, type);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public String getMovieEpisodeId(final String title, final String episode,
      final String originalEpisode, final int year) {
    if (mSearcher == null) {
      return null;
    }
    String id = getEpisodeId(title, episode, year);
    if (id == null) {
      id = getEpisodeId(title, originalEpisode, year);
    }
    return id;
  }

  private String getEpisodeId(final String title, final String episode,
      final int year) {
    if (episode == null) {
      return null;
    }
    final String normalizedTitle = normalise(title);
    final String normalizedEpisode = normalise(episode);
    final BooleanQuery bQuery = new BooleanQuery();
    bQuery.add(new TermQuery(new Term(ITEM_TYPE, TYPE_MOVIE)),
        BooleanClause.Occur.MUST);
    bQuery.add(
        new TermQuery(new Term(MOVIE_TITLE_NORMALISED, normalizedTitle)),
        BooleanClause.Occur.MUST);
    bQuery.add(new TermQuery(new Term(EPISODE_TITLE_NORMALISED,
        normalizedEpisode)), BooleanClause.Occur.MUST);

    if (year > 0) {
      bQuery.add(
          new TermQuery(new Term(MOVIE_YEAR, Integer.toString(year - 1))),
          BooleanClause.Occur.SHOULD);
      bQuery.add(new TermQuery(new Term(MOVIE_YEAR, Integer.toString(year))),
          BooleanClause.Occur.SHOULD);
      bQuery.add(
          new TermQuery(new Term(MOVIE_YEAR, Integer.toString(year + 1))),
          BooleanClause.Occur.SHOULD);
    }

    try {
      final TopDocs hits = mSearcher.search(bQuery, 1);
      if (hits.totalHits > 0) {
        final Document document = mSearcher.doc(hits.scoreDocs[0].doc);
        printDocument(document);
        return document.getField(MOVIE_ID).stringValue();
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }

  public String getMovieId(final String title, final String episode,
      final int year) {
    if (mSearcher == null) {
      return null;
    }

    final String normalizedTitle = normalise(title);
    final String normalizedEpisode = normalise(episode);

    BooleanQuery bQuery = new BooleanQuery();
    bQuery.add(new TermQuery(new Term(ITEM_TYPE, TYPE_MOVIE)),
        BooleanClause.Occur.MUST);
    bQuery.add(
        new TermQuery(new Term(MOVIE_TITLE_NORMALISED, normalizedTitle)),
        BooleanClause.Occur.MUST);
    bQuery.add(new TermQuery(new Term(EPISODE_TITLE_NORMALISED,
        normalizedEpisode)), BooleanClause.Occur.MUST);

    if (year > 0) {
      bQuery.add(
          new TermQuery(new Term(MOVIE_YEAR, Integer.toString(year - 1))),
          BooleanClause.Occur.SHOULD);
      bQuery.add(new TermQuery(new Term(MOVIE_YEAR, Integer.toString(year))),
          BooleanClause.Occur.SHOULD);
      bQuery.add(
          new TermQuery(new Term(MOVIE_YEAR, Integer.toString(year + 1))),
          BooleanClause.Occur.SHOULD);
    }

    try {

      Hits hits = mSearcher.search(bQuery);
      Iterator<Hit> it = hits.iterator();

      if (it.hasNext()) {
        final Hit hit = it.next();
        final Document document = hit.getDocument();

        printDocument(document);

        return document.getField(MOVIE_ID).stringValue();
      }

      bQuery = new BooleanQuery();
      bQuery.add(new TermQuery(new Term(ITEM_TYPE, TYPE_AKA)),
          BooleanClause.Occur.MUST);
      bQuery.add(new TermQuery(
          new Term(MOVIE_TITLE_NORMALISED, normalizedTitle)),
          BooleanClause.Occur.MUST);
      if (year > 0) {
        bQuery.add(new TermQuery(new Term(MOVIE_YEAR, Integer
            .toString(year - 1))), BooleanClause.Occur.SHOULD);
        bQuery.add(new TermQuery(new Term(MOVIE_YEAR, Integer.toString(year))),
            BooleanClause.Occur.SHOULD);
        bQuery.add(new TermQuery(new Term(MOVIE_YEAR, Integer
            .toString(year + 1))), BooleanClause.Occur.SHOULD);
      }

      hits = mSearcher.search(bQuery);
      it = hits.iterator();

      if (it.hasNext()) {
        final Hit hit = (Hit) it.next();
        final Document document = hit.getDocument();

        printDocument(document);

        return document.getField(MOVIE_ID).stringValue();
      }

    } catch (IOException e) {
      e.printStackTrace();
    }

    for (String suffix : TITLE_SUFFIX) {
      if (title.endsWith(suffix)) {
        return getMovieId(title.substring(0, title.length() - suffix.length())
            .trim(), episode, year);
      }
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
    if (id == null) {
      return null;
    }
    try {
      final BooleanQuery bQuery = new BooleanQuery();
      bQuery.add(new TermQuery(new Term(ITEM_TYPE, TYPE_RATING)),
          BooleanClause.Occur.MUST);
      bQuery.add(new TermQuery(new Term(MOVIE_ID, id)),
          BooleanClause.Occur.MUST);

      final Hits hits = mSearcher.search(bQuery);
      final Iterator<Hit> it = hits.iterator();

      if (it.hasNext()) {
        final Hit hit = it.next();
        final Document document = hit.getDocument();

        return new ImdbRating(Integer.parseInt(document.getField(MOVIE_RATING)
            .stringValue()), Integer.parseInt(document.getField(MOVIE_VOTES)
            .stringValue()), document.getField(MOVIE_DISTRIBUTION)
            .stringValue(), id);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return null;
  }

}
