package devplugin;

import junit.framework.TestCase;

public class DateTest extends TestCase {

  public void testCreateDDMMYYYY() {
    Date d = Date.createDDMMYYYY("12.12.2009", ".");
    assertEquals(d.getDayOfMonth(), 12);
    assertEquals(d.getMonth(), 12);
    assertEquals(d.getYear(), 2009);

    d = Date.createDDMMYYYY("12.12", ".");
    assertNull(d);

    d = Date.createDDMMYYYY("12.1222.2009", ".");
    assertNull(d);

    d = Date.createDDMMYYYY("12.12.09", ".");
    assertEquals(d.getDayOfMonth(), 12);
    assertEquals(d.getMonth(), 12);
    assertEquals(d.getYear(), 9);

    d = Date.createDDMMYYYY(null, ".");
    assertNull(d);
  }

  public void testCreateYYMMDD() {
    Date d = Date.createYYMMDD("2009.12.12", ".");
    assertNull(d);

    d = Date.createYYMMDD("12.12", ".");
    assertNull(d);

    d = Date.createYYMMDD("09.12.12", ".");
    assertEquals(d.getDayOfMonth(), 12);
    assertEquals(d.getMonth(), 12);
    assertEquals(d.getYear(), 2009);

    d = Date.createYYMMDD(null, ".");
    assertNull(d);
  }

  public void testCreateYYYYMMDD() {
    Date d = Date.createYYYYMMDD("2009.12.12", ".");
    assertEquals(d.getDayOfMonth(), 12);
    assertEquals(d.getMonth(), 12);
    assertEquals(d.getYear(), 2009);

    d = Date.createYYYYMMDD("12.12", ".");
    assertNull(d);

    d = Date.createYYYYMMDD("09.12.12", ".");
    assertEquals(d.getDayOfMonth(), 12);
    assertEquals(d.getMonth(), 12);
    assertEquals(d.getYear(), 9);

    d = Date.createDDMMYYYY("2009.12.1222", ".");
    assertNull(d);

    d = Date.createYYYYMMDD(null, ".");
    assertNull(d);
  }

}
