package de.misi.tvbrowser.activities.search;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Spinner;
import de.misi.tvbrowser.R;

public class SearchActivity extends Activity {

   public static final String SEARCHTEXT = "de.misi.tvbrowser.search.prefedinedtext";

   private Spinner searchType;

   @Override
   protected void onCreate(Bundle bundle) {
      super.onCreate(bundle);
      setContentView(R.layout.search);
      Button searchButton = (Button) findViewById(R.id.search_execute);
      searchButton.setOnClickListener(new View.OnClickListener() {
         public void onClick(View view) {
            doSearch();
         }
      });
      searchType = (Spinner) findViewById(R.id.search_searchtype);
      String[] items = new String[]{
              getResources().getString(R.string.searchtype_onlytitle),
              getResources().getString(R.string.searchtype_allfields)
      };
      searchType.setAdapter(new ArrayAdapter<String>(this, android.R.layout.simple_spinner_item, items));
      String predefinedText = getIntent().getStringExtra(SEARCHTEXT);
      if (predefinedText != null && predefinedText.length() > 0) {
         EditText text = (EditText) findViewById(R.id.search_text);
         text.setText(predefinedText);
      }
   }

   private boolean doSearch() {
      EditText text = (EditText) findViewById(R.id.search_text);
      Intent intent = new Intent(this, SearchResult.class);
      intent.putExtra(SearchResult.SEARCHTEXT, text.getText().toString());
      intent.putExtra(SearchResult.SEARCHTYPE, searchType.getSelectedItemPosition());
      startActivity(intent);
      return true;
   }
}
