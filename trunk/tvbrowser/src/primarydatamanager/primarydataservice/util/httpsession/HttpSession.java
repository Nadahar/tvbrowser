package primarydatamanager.primarydataservice.util.httpsession;

import java.io.DataOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.net.URLConnection;


public class HttpSession {
  
  private HttpURLConnection mUrlConnection;
  private String mCookie;
  
  public HttpSession() {
     
  }
  
  public HttpSession(URL url) throws IOException {
    mUrlConnection = (HttpURLConnection) url.openConnection();
   
    String cookie=mUrlConnection.getHeaderField("Set-Cookie");
    if (cookie!=null) {
      String s[]=cookie.split(";");
      if (s.length>0) {        
        mCookie=s[0];
      }         
    }
    System.out.println("cookie: "+mCookie);        
  }
  
  public InputStream openPage(URL url) throws IOException {
    mUrlConnection = (HttpURLConnection) url.openConnection();
    if (mCookie!=null) {
      mUrlConnection.addRequestProperty("Cookie",mCookie);
    } 
    return mUrlConnection.getInputStream();
  }
  
  public InputStream postForm(URL scriptUrl, Form form) throws IOException {
    
    
    URLConnection uconn=scriptUrl.openConnection();
    if (mCookie!=null) {
      uconn.addRequestProperty("Cookie",mCookie);
    } 
    uconn.setDoInput(true); 
    uconn.setDoOutput(true); 
    uconn.setUseCaches(false); 
    uconn.setRequestProperty("Content-Type", "application/x-www-form-urlencoded"); 
    
    DataOutputStream out = new DataOutputStream (uconn.getOutputStream ()); 
    Field[] formFields = form.getFields();
    StringBuffer contentBuf = new StringBuffer();
    for (int i=0; i<formFields.length; i++) {
      if (i>0) {
        contentBuf.append("&");
      }
      contentBuf.append(formFields[i].getKey()).append("=").append(formFields[i].getValue());
    }
    
    String content = contentBuf.toString().replaceAll(" ","+");
      
    out.writeBytes (content); 
    out.flush (); 
    out.close (); 
    
    return uconn.getInputStream();
    
   
     }
    
   
  
  public HttpURLConnection getURLConnection() {
    return mUrlConnection;
  }

  public void setCookie(String cookie) {
    mCookie = cookie;
  }
  
  public String getCookie() {
    return mCookie;
  }
  
}