package primarydatamanager.primarydataservice.util.httpsession;

import java.io.DataOutputStream;
import java.io.FileNotFoundException;
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
      String[] s=cookie.split(";");
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


  public InputStream openPage(URL page, boolean followRedirects)
     throws IOException
   {
    return openPage(page, followRedirects, null);
  }

  private InputStream openPage(URL page, boolean followRedirects, String referer)
     throws IOException
   {
     mUrlConnection = (HttpURLConnection) page.openConnection();
    if (mCookie!=null) {
      mUrlConnection.addRequestProperty("Cookie",mCookie);
    }
    if (referer!=null) {
      mUrlConnection.addRequestProperty("Referer", referer);
    }
     if (followRedirects) {
       mUrlConnection.setInstanceFollowRedirects(false);
       int response = mUrlConnection.getResponseCode();
       boolean redirect = (response >= 300 && response <= 399);

       // In the case of a redirect, we want to actually change the URL
       // that was input to the new, redirected URL
       if (redirect) {
         String loc = mUrlConnection.getHeaderField("Location");
         if (loc == null) {
           throw new FileNotFoundException("URL points to a redirect without "
             + "target location: " + page);
         }
         if (loc.startsWith("http")) {
           page = new URL(loc);
         } else {
           page = new URL(page, loc);
         }
         return openPage(page, followRedirects, referer);
       }
     }



    return mUrlConnection.getInputStream();

   }


  private InputStream getInputStream(URL page, HttpURLConnection con, boolean followRedirect) throws IOException {
    System.out.println("followRedirect");
     if (mCookie!=null) {
    //  con.addRequestProperty("Cookie",mCookie);
    }
     if (followRedirect) {
       con.setInstanceFollowRedirects(false);
       int response = con.getResponseCode();
       //int response = con.getResponseCode();
       boolean redirect = (response >= 300 && response <= 399);
       System.out.println("redirect: "+redirect);
       // In the case of a redirect, we want to actually change the URL
       // that was input to the new, redirected URL
       if (redirect) {
         String loc = con.getHeaderField("Location");
         if (loc == null) {
           throw new FileNotFoundException("URL points to a redirect without "
             + "target location: ");
         }
         if (loc.startsWith("http")) {
           page = new URL(loc);
         } else {
           page = new URL(page, loc);
         }
         return openPage(page, followRedirect, "http://presse.daserste.de/");
       }
     }



    return mUrlConnection.getInputStream();
  }



  public InputStream postForm(URL scriptUrl, Form form, boolean followRedirect) throws IOException {
    if (!followRedirect) {
      return postForm(scriptUrl, form);
    }
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
    StringBuilder contentBuf = new StringBuilder();
    for (int i=0; i<formFields.length; i++) {
      if (i>0) {
        contentBuf.append('&');
      }
      contentBuf.append(formFields[i].getKey()).append('=').append(
          formFields[i].getValue());
    }

    String content = contentBuf.toString().replaceAll(" ","+");

    out.writeBytes (content);
    out.flush ();
    out.close ();


    if (followRedirect) {
      return getInputStream(scriptUrl, (HttpURLConnection)uconn, true);
    }
    else {
      return uconn.getInputStream();
    }


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
    StringBuilder contentBuf = new StringBuilder();
    for (int i=0; i<formFields.length; i++) {
      if (i>0) {
        contentBuf.append('&');
      }
      contentBuf.append(formFields[i].getKey()).append('=').append(
          formFields[i].getValue());
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