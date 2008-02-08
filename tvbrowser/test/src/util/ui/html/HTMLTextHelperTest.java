package util.ui.html;

import junit.framework.TestCase;
import util.ui.html.HTMLTextHelper;

public class HTMLTextHelperTest extends TestCase {

  public void testConvertTextToHtml() {
    
    assertEquals("asfssdf lkasf <a href=\"http://www.heise.de\">www.heise.de</a> kjshd slkas fj�sldkfas", 
                HTMLTextHelper.convertTextToHtml("asfssdf lkasf http://www.heise.de kjshd slkas fj�sldkfas", true));
    assertEquals("asfssdf lkasf <a href=\"http://www.heise.de\">www.heise.de</a> kjshd slkas fj�sldkfas", 
                HTMLTextHelper.convertTextToHtml("asfssdf lkasf www.heise.de kjshd slkas fj�sldkfas", true));
    
  }
  
}
