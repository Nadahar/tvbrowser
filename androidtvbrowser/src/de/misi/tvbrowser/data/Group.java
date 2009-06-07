package de.misi.tvbrowser.data;

import java.util.ArrayList;
import java.util.StringTokenizer;

/**
 * Created by IntelliJ IDEA.
* User: Michael
* Date: 10.05.2009
* Time: 22:28:44
* To change this template use File | Settings | File Templates.
*/
public class Group {

   private String groupid;
   private String name;
   private String author;
   private String description;
   private String[] urls;

   public Group(String groupid, String name, String author, String description, String urllist) {
      this.groupid = groupid;
      this.name = name;
      this.author = author;
      this.description = description;
      parseUrlList(urllist);
   }

   private void parseUrlList(String urlList) {
      ArrayList<String> urls = new ArrayList<String>();
      StringTokenizer stringTokenizer = new StringTokenizer(urlList, ";");
      while (stringTokenizer.hasMoreTokens())
         urls.add(stringTokenizer.nextToken());
      this.urls = urls.toArray(new String[urls.size()]);
   }

   public String getGroupid() {
      return groupid;
   }

   public String getName() {
      return name;
   }

   public String getAuthor() {
      return author;
   }

   public String getDescription() {
      return description;
   }

   public String[] getUrls() {
      return urls;
   }
}
