package primarydatamanager.primarydataservice.util.html2txt;

import static org.junit.Assert.assertEquals;

import org.junit.Test;


/**
 * test case for primarydatamanager.primarydataservice.util.html2txt.Html2TxtConverterTest.
 *
 * @author uzi
 * @date 11.10.2010
 */
public class Html2TxtConverterTest {

  /**
   * test for convert(int, String).
   */
  @Test
  public void testConvertIntString() {
    assertEquals(Html2TxtConverter.convert(Html2TxtConverter.HIDE_ALL, "Vom Ratgeber bis zum Veranstaltungstipp<br />"), "Vom Ratgeber bis zum Veranstaltungstipp\n");
    assertEquals(Html2TxtConverter.convert(Html2TxtConverter.HIDE_ALL, " " + "<FONT COLOR=\"#202020\">Alf, When I'm Sixty-four</FONT><BR><FONT COLOR=\"#202020\">Die Seniorenparty</FONT><BR>".trim()), "Alf, When I'm Sixty-four\nDie Seniorenparty\n");
  }
}
