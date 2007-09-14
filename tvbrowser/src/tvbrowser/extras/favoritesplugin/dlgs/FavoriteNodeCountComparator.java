package tvbrowser.extras.favoritesplugin.dlgs;

import java.util.Comparator;

public class FavoriteNodeCountComparator implements Comparator<FavoriteNode> {

  private static FavoriteNodeCountComparator instance;

  public static FavoriteNodeCountComparator getInstance() {
    if (instance == null) {
      instance = new FavoriteNodeCountComparator();
    }
    return instance;
  }

  public int compare(FavoriteNode node1, FavoriteNode node2) {
    int result = node2.getAllPrograms(false).length-node1.getAllPrograms(false).length;
    
    if (result == 0 && !node1.isDirectoryNode() && !node2.isDirectoryNode()) {
      result = node1.getFavorite().getName().compareTo(node2.getFavorite().getName());
    }
    
    return result;
  }

  private FavoriteNodeCountComparator() {
    super();
  }

}
