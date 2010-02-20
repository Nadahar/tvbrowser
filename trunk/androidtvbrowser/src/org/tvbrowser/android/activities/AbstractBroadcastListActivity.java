package org.tvbrowser.android.activities;

import org.tvbrowser.android.R;
import org.tvbrowser.android.TVBrowser;
import org.tvbrowser.android.Utility;
import org.tvbrowser.android.activities.info.InfoActivity;
import org.tvbrowser.android.activities.reminder.ReminderDialog;
import org.tvbrowser.android.activities.search.SearchDialog;
import org.tvbrowser.android.data.DataLoader;
import org.tvbrowser.android.data.Program;

import android.app.Dialog;
import android.app.ListActivity;
import android.content.Context;
import android.content.Intent;
import android.database.Cursor;
import android.os.Bundle;
import android.text.format.DateFormat;
import android.view.ContextMenu;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;
import android.widget.SimpleCursorAdapter;
import android.widget.TextView;

public abstract class AbstractBroadcastListActivity extends ListActivity {

   private long mBroadcastId;

   @Override
   protected void onCreate(Bundle bundle) {
      super.onCreate(bundle);
      setListAdapter(new BroadcastCursorAdapter(this,
              createQuery(),
              new String[]{
                      DataLoader.PROGRAM_TITLE,
                      DataLoader.PROGRAM_STARTTIME,
                      DataLoader.CHANNEL_NAME
              },
              new int[]{
                      R.id.searchresult_title,
                      R.id.searchresult_starttime,
                      R.id.searchresult_channel
              }));
   }

   protected abstract Cursor createQuery();

   @Override
   protected void onListItemClick(ListView listView, View view, int position, long id) {
      Intent intent = new Intent(this, InfoActivity.class);
      intent.putExtra(InfoActivity.EXTRA_ID, id);
      startActivity(intent);
   }

   @Override
   public void onCreateContextMenu(ContextMenu contextMenu, View view, ContextMenu.ContextMenuInfo contextMenuInfo) {
      contextMenu.add(Menu.NONE, TVBrowser.MENU_SEARCH_REPETITION, Menu.NONE, R.string.menu_searchforrepeat);
      mBroadcastId = ((AdapterView.AdapterContextMenuInfo) contextMenuInfo).id;
      Program.createProgramReminderContextMenu(contextMenu, DataLoader.getReminderId(mBroadcastId));
      super.onCreateContextMenu(contextMenu, view, contextMenuInfo);
   }

   @Override
   public boolean onContextItemSelected(MenuItem menuItem) {
      switch (menuItem.getItemId()) {
         case TVBrowser.MENU_SEARCH_REPETITION:
            SearchDialog searchDialog = SearchDialog.getInstance(this);
            AdapterView.AdapterContextMenuInfo adapterContextMenuInfo = (AdapterView.AdapterContextMenuInfo) menuItem.getMenuInfo();
            long broadcastId = adapterContextMenuInfo.id;
            Cursor cursor = DataLoader.getAllProgramInfos(broadcastId);
            if (cursor != null) {
               if (cursor.moveToFirst()) {
                  searchDialog.setPredefinedText(cursor.getString(cursor.getColumnIndex(DataLoader.PROGRAM_TITLE)));
               }
               cursor.close();
            }
            showDialog(TVBrowser.SEARCH_DIALOG_ID);
            return true;
         case TVBrowser.MENU_ADD_REMINDER:
         case TVBrowser.MENU_EDIT_REMINDER:
            showDialog(TVBrowser.REMINDER_DIALOG_ID);
            return true;
      }
      return super.onContextItemSelected(menuItem);
   }

   @Override
   protected Dialog onCreateDialog(int id) {
      switch (id) {
         case TVBrowser.REMINDER_DIALOG_ID:
            return ReminderDialog.getInstance(this, mBroadcastId);
         case TVBrowser.SEARCH_DIALOG_ID:
            return SearchDialog.getInstance(this);
      }
      return null;
   }

   private class BroadcastCursorAdapter extends SimpleCursorAdapter {

      private BroadcastCursorAdapter(Context context, Cursor cursor, String[] from, int[] to) {
         super(context, R.layout.searchresultitem, cursor, from, to);
      }

      @Override
      public View getView(int position, View convertView, ViewGroup viewGroup) {
         View view = super.getView(position, convertView, viewGroup);
         Cursor cursor = getCursor();
         if (cursor != null) {
            TextView starttime = (TextView) view.findViewById(R.id.searchresult_starttime);
            if (starttime != null) {
               cursor.moveToPosition(position);
               long time = Utility.getTimeInMillis(cursor.getLong(cursor.getColumnIndex(DataLoader.PROGRAM_START_DATE_ID)),
                       cursor.getInt(cursor.getColumnIndex(DataLoader.PROGRAM_STARTTIME)));
               starttime.setText(DateFormat.getMediumDateFormat(AbstractBroadcastListActivity.this).format(time) + " " +
                                 DateFormat.getTimeFormat(AbstractBroadcastListActivity.this).format(time));
            }
         }
         return view;
      }
   }
}
