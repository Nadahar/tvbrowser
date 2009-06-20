package de.misi.tvbrowser.activities.search;

import android.database.Cursor;
import android.content.Intent;
import de.misi.tvbrowser.activities.AbstractBroadcastListActivity;
import de.misi.tvbrowser.data.DataLoader;

public class SearchResult extends AbstractBroadcastListActivity {

   public static final String SEARCHTEXT = "de.misi.tvbrowser.search.searchtext";
   public static final String SEARCHTYPE = "de.misi.tvbrowser.search.searchtype";
   public static final String SEARCHONLYFUTURE = "de.misi.tvbrowser.search.searchonlyfuture";

   @Override
   protected Cursor createQuery() {
      Intent intent = getIntent();
      return DataLoader.createSearchQuery(intent.getStringExtra(SEARCHTEXT), 
              intent.getIntExtra(SEARCHTYPE, 0),
              intent.getBooleanExtra(SEARCHONLYFUTURE, true));
   }
}
