package sharedchanneldataservice;

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
 
  

  public static Channel getChannel(String [] uniqueId){
  return Channel.getChannel(uniqueId[0], uniqueId[1], uniqueId[2], uniqueId[3]);
}
 
  public static String [] createChannelNameArray(String name, String country, String provider, String group){
    String channelNameArray[] = {name, country, provider, group};
    return channelNameArray;
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
}
