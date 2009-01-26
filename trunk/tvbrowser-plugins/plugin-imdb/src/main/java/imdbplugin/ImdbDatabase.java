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

  public ImdbDatabase(File imdbDatabase) {
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
    boolean ret = false;

    try {
      if ((mSearcher != null) && (mSearcher.maxDoc() > 1)) {
        ret = true;
      }
    } catch (IOException e) {
      // Empty catch Block
    }

    return ret;
  }

  public String addTitle(String movieTitle, String episode, int year, String type) {
    String movieID = null;
    try {
      Document doc = new Document();
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

  public void addAkaTitle(String movieId, String title, String episode, int year, String type) {
    try {
      Document doc = new Document();
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

  private String normalise(String str) {
    // ToDo: replace this with better normalizer
    str = str.replaceAll("ä", "ae");
    str = str.replaceAll("ü", "ue");
    str = str.replaceAll("ö", "oe");
    str = str.replaceAll("ß", "ss");
    str = str.replaceAll("Ä", "Ae");
    str = str.replaceAll("Ü", "Ue");
    str = str.replaceAll("Ö", "Oe");
    return str;
  }

  public void addRating(String movieId, int rating, int votes, String distribution) {
    try {
      Document doc = new Document();
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
    } else if (new File(mCurrentPath, "write.lock").exists()){
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
        IndexReader reader = IndexReader.open(mCurrentPath);
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

  public String getOrCreateMovieId(String movieTitle, String episode, int year, String type) {
    if (mSearcher == null) {
      return null;
    }
    try {
      BooleanQuery bQuery = new BooleanQuery();
      bQuery.add(new TermQuery(new Term(ITEM_TYPE, TYPE_MOVIE)),BooleanClause.Occur.MUST);
      bQuery.add(new TermQuery(new Term(MOVIE_TITLE, movieTitle)),BooleanClause.Occur.MUST);
      bQuery.add(new TermQuery(new Term(MOVIE_YEAR, Integer.toString(year))),BooleanClause.Occur.MUST);
      if (episode != null) {
        bQuery.add(new TermQuery(new Term(EPISODE_TITLE, episode)),BooleanClause.Occur.MUST);
      }
      if (type != null) {
        bQuery.add(new TermQuery(new Term(MOVIE_TYPE, type)),BooleanClause.Occur.MUST);
      }

      Hits hits = mSearcher.search(bQuery);
      
      if (hits == null) {
        return null;
      }
     
      Iterator it = hits.iterator();

      if (it.hasNext()) {
        Hit hit = (Hit) it.next();
        Document document = hit.getDocument();
        return document.getField(MOVIE_ID).stringValue();
      } else {
        return addTitle(movieTitle, episode, year, type);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return null;
  }

  public String getMovieEpisodeId(String title, String episode,
      String originalEpisode, int year) {
    if (mSearcher == null) {
      return null;
    }
    String id = getEpisodeId(title, episode, year);
    if (id == null) {
      id = getEpisodeId(title, originalEpisode, year);
    }
    return id;
  }

  private String getEpisodeId(String title, String episode, int year) {
    if (episode == null) {
      return null;
    }
    BooleanQuery bQuery = new BooleanQuery();
    bQuery.add(new TermQuery(new Term(ITEM_TYPE, TYPE_MOVIE)),
        BooleanClause.Occur.MUST);
    bQuery.add(new TermQuery(new Term(MOVIE_TITLE, title)),
        BooleanClause.Occur.MUST);
    bQuery.add(new TermQuery(new Term(EPISODE_TITLE, episode)),
        BooleanClause.Occur.MUST);

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
      TopDocs hits = mSearcher.search(bQuery, 1);
      if (hits.totalHits > 0) {
        Document document = mSearcher.doc(hits.scoreDocs[0].doc);
        printDocument(document);
        return document.getField(MOVIE_ID).stringValue();
      }
    } catch (IOException e) {
      // TODO Auto-generated catch block
      e.printStackTrace();
    }
    return null;
  }

  public String getMovieId(String title, String episode, int year) {
    if (mSearcher == null) {
      return null;
    }

    BooleanQuery bQuery = new BooleanQuery();
    bQuery.add(new TermQuery(new Term(ITEM_TYPE, TYPE_MOVIE)),
        BooleanClause.Occur.MUST);
    bQuery.add(new TermQuery(new Term(MOVIE_TITLE, title)),
        BooleanClause.Occur.MUST);
    bQuery.add(new TermQuery(new Term(EPISODE_TITLE, episode)),
        BooleanClause.Occur.MUST);

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
      Iterator it = hits.iterator();

      if (it.hasNext()) {
        Hit hit = (Hit) it.next();
        Document document = hit.getDocument();

        printDocument(document);

        return document.getField(MOVIE_ID).stringValue();
      }

      bQuery = new BooleanQuery();
      bQuery.add(new TermQuery(new Term(ITEM_TYPE, TYPE_AKA)),BooleanClause.Occur.MUST);
      bQuery.add(new TermQuery(new Term(MOVIE_TITLE, title)),BooleanClause.Occur.MUST);
      if (year > 0) {
        bQuery.add(new TermQuery(new Term(MOVIE_YEAR, Integer.toString(year-1))),BooleanClause.Occur.SHOULD);
        bQuery.add(new TermQuery(new Term(MOVIE_YEAR, Integer.toString(year))),BooleanClause.Occur.SHOULD);
        bQuery.add(new TermQuery(new Term(MOVIE_YEAR, Integer.toString(year+1))),BooleanClause.Occur.SHOULD);
      }

      hits = mSearcher.search(bQuery);
      it = hits.iterator();

      if (it.hasNext()) {
        Hit hit = (Hit) it.next();
        Document document = hit.getDocument();

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

  private void printDocument(Document document) {
/*    System.out.print(document.getField(MOVIE_TITLE).stringValue());

    if (document.getField(EPISODE_TITLE) != null) {
      System.out.print(" : " + document.getField(EPISODE_TITLE).stringValue());
    }

    System.out.println(" : " + document.getField(MOVIE_YEAR).stringValue() + " : " + document.getField(MOVIE_ID).stringValue());
  */
  }

  public ImdbRating getRatingForId(String id) {
    if (id == null) {
      return null;
    }
    try {
      BooleanQuery bQuery = new BooleanQuery();
      bQuery.add(new TermQuery(new Term(ITEM_TYPE, TYPE_RATING)),BooleanClause.Occur.MUST);
      bQuery.add(new TermQuery(new Term(MOVIE_ID, id)),BooleanClause.Occur.MUST);

      Hits hits = mSearcher.search(bQuery);
      Iterator it = hits.iterator();

      if (it.hasNext()) {
        Hit hit = (Hit) it.next();
        Document document = hit.getDocument();

        return new ImdbRating(
                Integer.parseInt(document.getField(MOVIE_RATING).stringValue()),
                Integer.parseInt(document.getField(MOVIE_VOTES).stringValue()),
                document.getField(MOVIE_DISTRIBUTION).stringValue(),
                id
        );
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return null;
  }

}
