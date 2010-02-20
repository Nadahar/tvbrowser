package org.tvbrowser.android.activities.search;

import org.tvbrowser.android.activities.AbstractBroadcastListActivity;
import org.tvbrowser.android.data.DataLoader;

import android.content.Intent;
import android.database.Cursor;

public class SearchResult extends AbstractBroadcastListActivity {

   protected static final String SEARCH_TEXT = "org.tvbrowser.android.search.searchtext";
   protected static final String SEARCH_TYPE = "org.tvbrowser.android.search.searchtype";
   protected static final String SEARCH_ONLY_FUTURE = "org.tvbrowser.android.search.searchonlyfuture";

   @Override
   protected Cursor createQuery() {
      Intent intent = getIntent();
      return DataLoader.createSearchQuery(intent.getStringExtra(SEARCH_TEXT),
              intent.getIntExtra(SEARCH_TYPE, 0),
              intent.getBooleanExtra(SEARCH_ONLY_FUTURE, true));
   }
}
