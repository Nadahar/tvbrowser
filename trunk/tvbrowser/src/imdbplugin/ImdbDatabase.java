package imdbplugin;

import org.apache.lucene.analysis.SimpleAnalyzer;
import org.apache.lucene.index.IndexWriter;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.document.Document;
import org.apache.lucene.document.Field;
import org.apache.lucene.document.DateTools;
import org.apache.lucene.search.IndexSearcher;
import org.apache.lucene.search.BooleanQuery;
import org.apache.lucene.search.Hits;
import org.apache.lucene.search.Hit;
import org.apache.lucene.search.BooleanClause;
import org.apache.lucene.search.TermQuery;

import java.io.File;
import java.io.IOException;
import java.util.Iterator;
import java.util.UUID;

public class ImdbDatabase {
  private static final String ITEM_TYPE = "ITEM_TYPE";
  private static final String TYPE_MOVIE = "TYPE_MOVIE";
  private static final String TYPE_AKA = "TYPE_AKA";
  private static final String TYPE_RATING = "TYPE_RATING";

  private static final String MOVIE_TITLE = "MOVIE_TITLE";
  private static final String MOVIE_YEAR = "MOVIE_YEAR";
  private static final String MOVIE_TYPE = "MOVIE_TYPE";
  private static final String MOVIE_ID = "MOVIE_ID";
  private static final String MOVIE_RATING = "MOVIE_RATING";
  private static final String MOVIE_VOTES = "MOVIE_VOTES";
  private static final String MOVIE_DISTRIBUTION = "MOVIE_DISTRIBUTION";

  private static final String EPISODE_TITLE = "EPISODE_TITLE";

  private File mCurrentPath;

  private IndexSearcher mSearcher;
  private IndexWriter mWriter;

  public ImdbDatabase(File imdbDatabase) {
    mCurrentPath = imdbDatabase;
  }

  public void deleteDatabase() {
    for (File f : mCurrentPath.listFiles()) {
      f.delete();
    }
  }

  public void init() {
    reOpen();
  }

  public String addTitle(String movieTitle, String episode, int year, String type) {
    String movieID = null;
    try {
      Document doc = new Document();
      movieID = UUID.randomUUID().toString();
      doc.add(new Field(MOVIE_ID, movieID, Field.Store.COMPRESS, Field.Index.UN_TOKENIZED, Field.TermVector.NO));
      doc.add(new Field(ITEM_TYPE, TYPE_MOVIE, Field.Store.YES, Field.Index.UN_TOKENIZED, Field.TermVector.NO));
      doc.add(new Field(MOVIE_TITLE, movieTitle, Field.Store.YES, Field.Index.UN_TOKENIZED, Field.TermVector.NO));
      doc.add(new Field(MOVIE_YEAR, Integer.toString(year), Field.Store.YES, Field.Index.UN_TOKENIZED, Field.TermVector.NO));
      if (type != null) {
        doc.add(new Field(MOVIE_TYPE, type, Field.Store.YES, Field.Index.UN_TOKENIZED, Field.TermVector.NO));
      }
      if (episode != null) {
        doc.add(new Field(EPISODE_TITLE, episode, Field.Store.YES, Field.Index.UN_TOKENIZED, Field.TermVector.NO));
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
      doc.add(new Field(MOVIE_ID, movieId, Field.Store.YES, Field.Index.UN_TOKENIZED, Field.TermVector.NO));
      doc.add(new Field(ITEM_TYPE, TYPE_AKA, Field.Store.YES, Field.Index.UN_TOKENIZED, Field.TermVector.NO));
      doc.add(new Field(MOVIE_TITLE, title, Field.Store.YES, Field.Index.UN_TOKENIZED, Field.TermVector.NO));
      doc.add(new Field(MOVIE_YEAR, Integer.toString(year), Field.Store.YES, Field.Index.UN_TOKENIZED, Field.TermVector.NO));
      if (type != null) {
        doc.add(new Field(MOVIE_TYPE, type, Field.Store.YES, Field.Index.UN_TOKENIZED, Field.TermVector.NO));
      }
      if (episode != null) {
        doc.add(new Field(EPISODE_TITLE, episode, Field.Store.YES, Field.Index.UN_TOKENIZED, Field.TermVector.NO));
      }
      mWriter.addDocument(doc);
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public void addRating(String movieId, int rating, int votes, String distribution) {
    try {
      Document doc = new Document();
      doc.add(new Field(MOVIE_ID, movieId, Field.Store.YES, Field.Index.UN_TOKENIZED, Field.TermVector.NO));
      doc.add(new Field(ITEM_TYPE, TYPE_RATING, Field.Store.YES, Field.Index.UN_TOKENIZED, Field.TermVector.NO));
      doc.add(new Field(MOVIE_RATING, Integer.toString(rating), Field.Store.YES, Field.Index.UN_TOKENIZED, Field.TermVector.NO));
      doc.add(new Field(MOVIE_VOTES, Integer.toString(votes), Field.Store.YES, Field.Index.UN_TOKENIZED, Field.TermVector.NO));
      doc.add(new Field(MOVIE_DISTRIBUTION, distribution, Field.Store.YES, Field.Index.UN_TOKENIZED, Field.TermVector.NO));
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
    if (!mCurrentPath.exists() || mCurrentPath.listFiles().length == 0) {
      try {
        mWriter = new IndexWriter(mCurrentPath, new SimpleAnalyzer());
        mWriter.addDocument(new Document());
        mWriter.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    } else if (new File(mCurrentPath, "write.lock").exists()){
      new File(mCurrentPath, "write.lock").delete();
    }
    
    if (mWriter != null) {
      try {
        mWriter.close();
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

    if (new File(mCurrentPath, "segments.gen").exists()) {
      try {
        IndexReader reader = IndexReader.open(mCurrentPath);
        mSearcher = new IndexSearcher(reader);
      } catch (IOException e) {
        e.printStackTrace();
      }
    }

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
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  public String getOrCreateMovieId(String movieTitle, String episode, int year, String type) {
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

  public String getMovieId(String title, String episode, int year) {
    if (mSearcher == null) {
      return null;
    }

    try {

      BooleanQuery bQuery = new BooleanQuery();
      bQuery.add(new TermQuery(new Term(ITEM_TYPE, TYPE_MOVIE)),BooleanClause.Occur.MUST);
      bQuery.add(new TermQuery(new Term(MOVIE_TITLE, title)),BooleanClause.Occur.MUST);
      bQuery.add(new TermQuery(new Term(EPISODE_TITLE, episode)),BooleanClause.Occur.MUST);

      if (year != -1) {
        bQuery.add(new TermQuery(new Term(MOVIE_YEAR, Integer.toString(year-1))),BooleanClause.Occur.SHOULD);
        bQuery.add(new TermQuery(new Term(MOVIE_YEAR, Integer.toString(year))),BooleanClause.Occur.SHOULD);
        bQuery.add(new TermQuery(new Term(MOVIE_YEAR, Integer.toString(year+1))),BooleanClause.Occur.SHOULD);
      }

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
      if (year != -1) {
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

    return null;
  }

  private void printDocument(Document document) {
    System.out.print(document.getField(MOVIE_TITLE).stringValue());

    if (document.getField(EPISODE_TITLE) != null) {
      System.out.print(" : " + document.getField(EPISODE_TITLE).stringValue());
    }

    System.out.println(" : " + document.getField(MOVIE_YEAR).stringValue() + " : " + document.getField(MOVIE_ID).stringValue());
  }

  public int getRatingForId(String id) {
    try {
      BooleanQuery bQuery = new BooleanQuery();
      bQuery.add(new TermQuery(new Term(ITEM_TYPE, TYPE_RATING)),BooleanClause.Occur.MUST);
      bQuery.add(new TermQuery(new Term(MOVIE_ID, id)),BooleanClause.Occur.MUST);

      Hits hits = mSearcher.search(bQuery);
      Iterator it = hits.iterator();

      if (it.hasNext()) {
        Hit hit = (Hit) it.next();
        Document document = hit.getDocument();

        return Integer.parseInt(document.getField(MOVIE_RATING).stringValue());
      }
    } catch (IOException e) {
      e.printStackTrace();
    }

    return -1;
  }
}
