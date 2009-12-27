package imdbplugin;

public class ImdbAka {
  private String title;
  private String episode;
  private int year;

  public ImdbAka(String title, String episode, int year) {
    this.title = title;  
    this.episode = episode;
    this.year = year;
  }

  public String getTitle() {
    return title;
  }

  public String getEpisode() {
    return episode;
  }

  public int getYear() {
    return year;
  }
}
