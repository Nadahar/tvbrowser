package tvbrowser.extras.favoritesplugin.dlgs;

import java.util.Comparator;

public class FavoriteNodeComparator implements Comparator<FavoriteNode> {

  private static FavoriteNodeComparator instance;

  private FavoriteNodeComparator() {
    super();
  }

  public int compare(FavoriteNode node1, FavoriteNode node2) {
    return node1.compareTo(node2);
  }
  
  public static FavoriteNodeComparator getInstance() {
    if (instance == null) {
      instance = new FavoriteNodeComparator();
    }
    return instance;
  }
}