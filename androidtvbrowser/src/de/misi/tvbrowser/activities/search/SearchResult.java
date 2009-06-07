package de.misi.tvbrowser.activities.search;

import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.view.*;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;
import de.misi.tvbrowser.R;
import de.misi.tvbrowser.activities.info.InfoActivity;
import de.misi.tvbrowser.data.DataLoader;

import java.text.SimpleDateFormat;
import java.util.Date;

public class SearchResult extends ListActivity {

   public static final String SEARCHTEXT = "de.misi.tvbrowser.search.searchtext";
   public static final String SEARCHTYPE = "de.misi.tvbrowser.search.searchtype";

   private static final int MENU_ADDREMINDER = 1;
   private static final int MENU_EDITREMINDER = 2;
   private static final int MENU_DELETEREMINDER = 3;
   private static final int MENU_SEARCHFORREPEAT = 4;

   @Override
   protected void onCreate(Bundle bundle) {
      super.onCreate(bundle);
      BroadcastCursorAdapter adapter = new BroadcastCursorAdapter(this,
              R.layout.searchresultitem,
              DataLoader.createSearchQuery(getIntent()),
              new String[]{
                      DataLoader.BROADCAST_TITLE,
                      DataLoader.BROADCAST_STARTTIME,
                      DataLoader.CHANNEL_NAME
              },
              new int[]{
                      R.id.searchresult_title,
                      R.id.searchresult_starttime,
                      R.id.searchresult_channel
              });
      adapter.setDateFormat(getResources().getString(R.string.outputdateformat));
      setListAdapter(adapter);
   }

   @Override
   protected void onListItemClick(ListView listView, View view, int position, long id) {
      Intent intent = new Intent(this, InfoActivity.class);
      intent.putExtra(InfoActivity.EXTRA_ID, id);
      startActivity(intent);
   }

   @Override
   public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
      contextMenu.add(Menu.NONE, MENU_SEARCHFORREPEAT, Menu.NONE, R.string.menu_searchforrepeat);
      contextMenu.add(Menu.NONE, MENU_ADDREMINDER, Menu.NONE, R.string.menu_addreminder);
      contextMenu.add(Menu.NONE, MENU_EDITREMINDER, Menu.NONE, R.string.menu_editreminder);
      contextMenu.add(Menu.NONE, MENU_DELETEREMINDER, Menu.NONE, R.string.menu_deletereminder);
      super.onCreateContextMenu(contextMenu, view, contextMenuInfo);
   }

   @Override
   public boolean onContextItemSelected(MenuItem menuItem) {
      switch(menuItem.getItemId()) {
         case MENU_SEARCHFORREPEAT:
            Intent intent = new Intent(this, SearchActivity.class);
//            intent.putExtra(SearchActivity.SEARCHTEXT, broadcastTitle);
            startActivity(intent);
            return true;
      }
      return super.onContextItemSelected(menuItem);
   }

   private class BroadcastCursorAdapter extends SimpleCursorAdapter {

      private SimpleDateFormat format;

      private BroadcastCursorAdapter(Context context, int layout, Cursor cursor, String[] from, int[] to) {
         super(context, layout, cursor, from, to);
      }

      public void setDateFormat(String format) {
         this.format = new SimpleDateFormat(format);
      }

      @Override
      public View getView(int position, View convertView, ViewGroup viewGroup) {
         View view = super.getView(position, convertView, viewGroup);
         Cursor cursor = getCursor();
         if (cursor != null) {
            TextView starttime = (TextView) view.findViewById(R.id.searchresult_starttime);
            if (starttime != null) {
               cursor.moveToPosition(position);
               long id = cursor.getLong(cursor.getColumnIndex(DataLoader.BROADCAST_START_DATE_ID));
               Date date = DataLoader.getDateByDataId(id);
               if (date != null) {
                  starttime.setText(format.format(date) + " " +
                          cursor.getString(cursor.getColumnIndex(DataLoader.BROADCAST_STARTTIME)));
               }
            }
         }
         return view;
      }
   }
}
