package imdbplugin;

import java.util.ArrayList;

public class ImdbMovie {
  private String title;
  private int year;
  private String episode;
  private ArrayList<ImdbAka> akaList = new ArrayList<ImdbAka>();
 
  public void setTitle(String title) {
    this.title = title;
  }

  public String getTitle() {
    return title;
  }

  public void setYear(int year) {
    this.year = year;
  }

  public int getYear() {
    return year;
  }

  public void setEpisode(String episode) {
    this.episode = episode;
  }

  public String getEpisode() {
    return episode;
  }

  public void addAka(ImdbAka imdbAka) {
    akaList.add(imdbAka);
  }

  public ImdbAka[] getAkas() {
    return akaList.toArray(new ImdbAka[akaList.size()]);
  }
}
