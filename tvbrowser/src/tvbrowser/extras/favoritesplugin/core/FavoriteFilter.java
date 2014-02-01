package tvbrowser.extras.favoritesplugin.core;

import tvbrowser.extras.favoritesplugin.FavoritesPlugin;
import devplugin.Program;
import devplugin.ProgramFilter;

public class FavoriteFilter implements ProgramFilter {
  private Favorite mFavorite;
  
  public FavoriteFilter(Favorite favorite) {
    mFavorite = favorite;
  }

  @Override
  public boolean accept(Program program) {
    return mFavorite.accept(program);
  }

  @Override
  public String getName() {
    return FavoritesPlugin.getInstance().toString() + ": " + mFavorite.getName();
  }
  
  @Override
  public boolean equals(Object obj) {
    if(obj instanceof FavoriteFilter) {
      return mFavorite.equals(((FavoriteFilter)obj).mFavorite);
    }
    
    return false;
  }
  
  public String getKeyValue() {
    return String.valueOf(mFavorite.getFilterKey());
  }
}
