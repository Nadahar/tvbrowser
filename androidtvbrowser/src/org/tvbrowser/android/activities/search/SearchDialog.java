package org.tvbrowser.android.activities.search;

import org.tvbrowser.android.R;

import android.app.Dialog;
import android.content.Context;
import android.content.Intent;
import android.content.res.Resources;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.EditText;
import android.widget.Spinner;

public class SearchDialog extends Dialog {

  public static final String SEARCHTEXT = "org.tvbrowser.android.search.prefedinedtext";

  private Spinner mSearchType;
  private EditText mEditText;
  private CheckBox mOnlyFuture;
  private String mPredefinedText;
  private static SearchDialog instance = null;

  /**
   * singleton access method
   *
   * @param context
   * @return
   */
  public static SearchDialog getInstance(Context context) {
    if (instance == null) {
      instance = new SearchDialog(context);
    }
    return instance;
  }

  /**
   * singleton constructor
   *
   * @param context
   */
  private SearchDialog(Context context) {
    super(context);
    initialize();
  }

  public void setPredefinedText(String predefinedText) {
    mPredefinedText = predefinedText;
    updateContent();
  }

  private void initialize() {
    setContentView(R.layout.search);
    Resources resources = getContext().getResources();
    setTitle(R.string.search_title);
    Button searchButton = (Button) findViewById(R.id.search_execute);
    searchButton.setOnClickListener(new View.OnClickListener() {
      public void onClick(View view) {
        doSearch();
      }
    });
    mSearchType = (Spinner) findViewById(R.id.search_searchtype);
    String[] items = new String[] { resources.getString(R.string.searchtype_onlytitle),
        resources.getString(R.string.searchtype_allfields) };
    mSearchType.setAdapter(new ArrayAdapter<String>(getContext(), android.R.layout.simple_spinner_item, items));
    mEditText = (EditText) findViewById(R.id.search_text);
    mOnlyFuture = (CheckBox) findViewById(R.id.search_onlyfuture);
    mOnlyFuture.setChecked(true);
    updateContent();
  }

  private void updateContent() {
    if (mPredefinedText != null && mPredefinedText.length() > 0) {
      mEditText.setText(mPredefinedText);
    }
  }

  private void doSearch() {
    Intent intent = new Intent(getContext(), SearchResult.class);
    intent.putExtra(SearchResult.SEARCH_TEXT, mEditText.getText().toString());
    intent.putExtra(SearchResult.SEARCH_TYPE, mSearchType.getSelectedItemPosition());
    intent.putExtra(SearchResult.SEARCH_ONLY_FUTURE, mOnlyFuture.isChecked());
    getContext().startActivity(intent);
  }
}
