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
  
  @Override
  public boolean equals(Object obj) {
    if (obj == null) {
      return false;
    }
    if (obj == this) {
      return true;
    }
    if (!(obj instanceof ImdbAka)) {
      return false;
    }
    ImdbAka otherAka = (ImdbAka) obj;
    if (year != otherAka.year) {
      return false;
    }
    if (!title.equalsIgnoreCase(otherAka.title)) {
      return false;
    }
    if ((episode == null) != (otherAka.episode == null)) {
      return false;
    }
    if (episode != null && !episode.equalsIgnoreCase(otherAka.episode)) {
      return false;
    }
    return true;
  }
  
  @Override
  public int hashCode() {
    int hash = title.hashCode() + year;
    if (episode != null) {
      hash += episode.hashCode();
    }
    return hash;
  }
}
