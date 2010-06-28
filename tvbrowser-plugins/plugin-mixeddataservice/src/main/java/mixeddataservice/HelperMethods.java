package mixeddataservice;

import devplugin.Channel;

public class HelperMethods {

  public static String [] getChannelName (String uniqueChannelId, String defaultName){

    if (uniqueChannelId.equals("")){
      return createChannelNameArray(defaultName,"","","");
   }

    String [] uniqueId = uniqueChannelId.split("_");
    Channel chn = getChannel(uniqueId);
    if (chn==null){
      return createChannelNameArray(defaultName,"","","");
    }
    else{
      return createChannelNameArray(chn.getName(), uniqueId[2], chn.getDataServiceProxy().getInfo().getName(), uniqueId[1]);
    }
  }

  public static String[] createChannelNameArray(String name, String country, String provider, String group){
    String[] channelNameArray = {name, country, provider, group};
    return channelNameArray;
  }


  public static Channel getChannel(String [] uniqueId){
  return Channel.getChannel(uniqueId[0], uniqueId[1], uniqueId[2], uniqueId[3]);
}



  public static Channel getChannelFromId (String id, Channel[] channelList){
    Channel channel=null;
    if (id!=null){
      for (int i = 0; i < channelList.length; i++) {
        if (channelList[i].getUniqueId().equals(id)){
          channel = channelList[i];
         }
      }
    }
    return channel;
  }

  /**
   * Mix the info bits of two programs
   * @param currentBits
   * @param altBits
   * @return
   */
  public static int mixInfoBits (int currentBits, int altBits){
    int info = 0;
    int alt = altBits;
    int current = currentBits;
    for (int i=24; i>0; i--){
      if (current>=(1<<i)){
        info = info + (1<<i);
        current = current-(1<<i);
        if (alt>=(1<<i)){
          alt = alt-(1<<i);
        }
      }
      else{
        if (alt>=(1<<i)){
          info = info + (1<<i);
          alt = alt-(1<<i);
        }
      }
    }
    return info;
  }

  /**
   * delete doubles; delete 'Spielfilm' if category contains 'Serie''
   * @param rawString
   * @return
   */
  public static String cleanUpCategories (String rawString){
    boolean isSerial = false;
    boolean isTvMovie = false;
    String category = rawString;

    if (category.contains("Serie")) {
      isSerial = true;
    }
    if (category.contains("Fernsehfilm")) {
      isTvMovie = true;
    }

    String[] cats = category.split(", ");
    category = "";

    // Delete doubles
    for (int i = 0; i < cats.length; i++) {
      for (int j = 0; j < cats.length; j++) {
        if (i != j && !cats[j].equals("") && ((cats[i].equals(cats[j])) || (cats[i].toLowerCase().contains(cats[j].toLowerCase())))) {
          cats[j] = "";
        }
      }
    }
    // build new category string
    // if serial or tvmovie skip "Spielfilm"
    for (int i = 0; i < cats.length; i++) {
      if (!cats[i].equals("") && !((isSerial ||isTvMovie) && cats[i].equals("Spielfilm"))) {
        if (category.equals("")) {
          category = cats[i];
        } else {
          category = category + ", " + cats[i];
        }
      }
    }

    return category;
  }

}
