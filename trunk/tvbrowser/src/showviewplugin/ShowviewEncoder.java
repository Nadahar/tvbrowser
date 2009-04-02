/*
 * TV-Browser
 * Copyright (C) 04-2003 Martin Oberhauser (darras@users.sourceforge.net)
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE. See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 *
 * CVS information:
 *  $RCSfile$
 *   $Source$
 *     $Date$
 *   $Author$
 * $Revision$
 */
package showviewplugin;

import util.exc.TvBrowserException;
import devplugin.Channel;
import devplugin.Date;
import devplugin.Program;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class ShowviewEncoder {
  
  private static ShowviewEncoder mSingleton;
  
  
  private ShowviewEncoder() {
  }
  
  
  public static ShowviewEncoder getInstance() {
    if (mSingleton == null) {
      mSingleton = new ShowviewEncoder();
    }
    
    return mSingleton;
  }


  /**
   * Calculates the Showview Number for the given Program.
   * 
   * @param prog
   * @return The Showview number for the given Program.
   * @throws TvBrowserException When the calculation failed.
   */
  public String getShowviewNumberFor(Program prog)
    throws TvBrowserException
  {
    Channel channel = prog.getChannel();
    Date date  = prog.getDate();
    int year   = date.getYear();
    int month  = date.getMonth();
    int day    = date.getDayOfMonth();
    int hour   = prog.getHours();
    int minute = prog.getMinutes();
    int length = prog.getLength();
    
    return getShowviewNumberFor(channel, year, month, day, hour, minute, length);
  }


  /**
   * Calculates the Showview Number for the given Program.
   * 
   * @return The Showview number for the given Program.
   * @throws TvBrowserException When the calculation failed.
   */
  public String getShowviewNumberFor(Channel channel, int year, int month,
    int day, int hour, int minute, int length)
    throws TvBrowserException
  {
    // NOTE: Diese Methode ist eine Implementierung des Kodier-Algorithmus
    //       von Gerhard Zelczak siehe http://www.zelczak.com/showview.htm
    //       Daher stammen auch die Kommentare
    //       Diese HTML-Seiten sind auch im txt-Ordner archiviert
    if(length < 1)
      return null;
    
    year = year % 100;
    
    // Minutendifferenz ausrechnen
    int minDiff = minute % 5;
    
    // Minute und Dauer entsprechend anpassen
    minute -= minDiff;
    length += minDiff;
    
    // 1. Kanal (vorher 1 von der Showview-Leitzahl subtrahieren!)
    //    und Zeit-Index binär bestimmen.
    int channelNumber = ShowviewChannelTable.getChannelNumberFor(channel);
    if (channelNumber == -1) {
      throw new TvBrowserException(ShowviewEncoder.class, "error.1",
        "Didn't find a showview channel number (Leitzahl) for {0}", channel, new NoChannelNumberException());
    }
    
    // Leitzahl in k-Bits umrechnen
    
    // TODO: Hohe Kanäle: Ergeben richtigen Code (Zeit, Datum) mit falschem
    //       Kanal: ab NDR (19)
    // 18:  MTV     (109) !
    // 20:  Fehler
    // 28:  Fehler
    // 32:  ARD (1)
    // 35:  RTL (9)
    // 37:  Kabel 1 (8)
    // 38:  n-tv    (7)
    // 50:  MTV     (109) !
    // 98:  Kabel 1 (8)
    // 100: Sat 1   (5)
    // 117: Fehler
    // 136: RTL2    (9)
    
    if (channelNumber == 26) {
      // Sonderregel für HR3
      channelNumber += 18;
    }
    else if (channelNumber == 109) {
      // Sonderregel für MTV
      channelNumber -= 59; // - 91 geht auch
    }
    else if (channelNumber >= 19) {
      // Für diese Sender weiss ich die Umrechnung der Leitzahl noch nicht
      throw new TvBrowserException(ShowviewEncoder.class, "error.2",
        "Calculation does not work for this program", new NoChannelNumberException());
    }
    /*
    // Ergibt auch keine richtigen Sender...
    else if ((channelNumber >= (19 + 81)) && (channelNumber <= (31 + 81))) {
      // Aus Dekodierung, Schritt 7: Zumindest für Deutschland gibt es zumindest
      //   eine Spezialregel wie: "Falls die dezimale Darstellung der
      //   k-Bits zwischen 19 und 31 liegt, wird nicht 1 sondern 81 addiert,
      //   um den Kanal zu bekommen.".
      channelNumber -= 81;
    }
    */
    else {
      channelNumber--;
    }
    
    int timeIndex = ShowviewTimeTable.getInstance().getBestFit(hour, minute, length);

    // 2. Kanal- und Zeit-Index-Bits nach obigem Schritt 7 der Dekodierung zu
    //    Top2 und Newtop neu zusammensetzen und in dezimal umrechnen:
    int top2 = ((timeIndex     >> 2 & 0x1) << 4)
             + ((channelNumber >> 1 & 0x1) << 3)
             + ((timeIndex     >> 1 & 0x1) << 2)
             + ((channelNumber      & 0x1) << 1)
             +  (timeIndex          & 0x1);
    
    int newtop = ((timeIndex     >> 9 & 0x1) << 9)
               + ((channelNumber >> 4 & 0x1) << 8)
               + ((channelNumber >> 3 & 0x1) << 7)
               + ((timeIndex     >> 8 & 0x1) << 6)
               + ((timeIndex     >> 7 & 0x1) << 5)
               + ((timeIndex     >> 6 & 0x1) << 4)
               + ((timeIndex     >> 5 & 0x1) << 3)
               + ((timeIndex     >> 4 & 0x1) << 2)
               + ((timeIndex     >> 3 & 0x1) << 1)
               +  (channelNumber >> 2 & 0x1);
    
    if (newtop > 999) {
      // Das würde einen Code ergeben, der nicht "x00xxxxxx" entspricht.
      // Diese Art Code ist noch nicht geknackt
      throw new TvBrowserException(ShowviewEncoder.class, "error.2",
        "Calculation does not work for this program");
    }
    
    // 3. Zahl Bottom berechnen: von Tag 1 subtrahieren, das Ergebnis mit 32
    //    multiplizieren, 1 addieren.
    int bottom = ((day - 1) * 32) + 1;
    
    // 4. Aus Newtop wird Top berechnet:

    // Der Rest der zweistelligen Jahreszahl modulo 16 (um 1 erhöht) ergibt
    // die Anzahl der benötigten Durchgänge.
    int repeatCount = (year % 16) + 1;
    int top = newtop;
    int[] topArr = splitDigits(top);
    int newtopLength = getLength(newtop);
    int doLoopCount = 0;
    int offset = 0;
    do {
      for (int i = 0; i < repeatCount; i++) {
        // von der letzten Stelle wird die vorletzte Stelle subtrahiert
        // (modulo 10) um die letzte Stelle der neuen Zahl zu bekommen,
        // von der vorletzten die davor etc.
        for (int j = topArr.length - 1; j >= 1; j--) {
          topArr[j] = saveMod(topArr[j] - topArr[j - 1], 10);
        }
        // Von der ersten Stelle wird die letzte Stelle des Tages subtrahiert
        // (modulo 10) für die erste Stelle der neuen Zahl..
        topArr[0] = saveMod(topArr[0] - day, 10);
      }
      top = mergeDigits(topArr);

      // 5. Am einfachsten errechnet man jetzt wohl die benötigte Zahl Offset,
      //    indem man sie wie im Schritt 5 der Dekodierung aus Top berechnet.

      // Letzte Stelle der 'neuen Zahl' auf offset addieren
      for (int i = 1; i <= repeatCount; i++) {
        int newNumber = calculateNewNumber(day, top, newtopLength, i);
        offset += newNumber % 10;
      }
    
      doLoopCount++;
      if (doLoopCount > 100) {
        throw new TvBrowserException(ShowviewEncoder.class, "error.2",
          "Calculation does not work for this program");
      }

      // Falls Top weniger Stellen hat als Newtop, so wird das alles nochmals
      // durchlaufen, bis beide gleich lang sind.
    }
    while (getLength(top) < newtopLength);

    // Aus Dekodierung: Offset = normale Addition der Ziffern aus Top (Quersumme)
    // -> Also bei Kodierung die Quersumme von top draufaddieren
    for (int i = 0; i < topArr.length; i++) {
      offset += topArr[i];
    }

    // 6. Nun können wir aus der Formel
    //    Top2 = (Rem + Tag * (Monat+1) + Offset) modulo 32
    //    aus Schritt 6 der Dekodierung die einzige offene Zahl Rem berechnen.
    int rem = saveMod(top2 - day * (month + 1) - offset, 32);
    
    // 7. Jetzt muß noch Rem + Bottom + (Top * 1000) berechnet werden, um die
    //    Code-Zahl zu bekommen.
    int code = rem + bottom + (top * 1000);

    // 8. Letzter Schritt: die Showview-Zahl aus der Code-Zahl berechnen.
    //    Um die 1. Stelle der neuen Zahl zu bekommen, wird die 2. Stelle der
    //    Code-Zahl von der 1. Stelle der Code-Zahl subtrahiert (modulo 10).
    //    Für die 2. Stelle wird die 3. von der 2. Stelle subtrahiert (modulo 10)
    //    usw.
    //    Dieses Verfahren wird noch zweimal wiederholt. (Also instg. 3 mal)
    //    Das Verfahren endet erst, wenn die Showview-Zahl genauso lang ist wie
    //    die Code-Zahl. FERTIG!
    repeatCount = 3;
    doLoopCount = 0;
    int[] showviewArr = splitDigits(code);
    do {
      for (int i = 0; i < repeatCount; i++) {
        for (int j = 0; j < showviewArr.length - 1; j++) {
          showviewArr[j] = (showviewArr[j] - showviewArr[j + 1] + 10) % 10;
        }
      }
      
      doLoopCount++;
      if (doLoopCount > 100) {
        throw new TvBrowserException(ShowviewEncoder.class, "error.2",
          "Calculation does not work for this program");
      }
    }
    while (showviewArr[0] == 0);
    
    int showview = mergeDigits(showviewArr);

    // Minutendifferenz als 9. Stelle hinzufügen
    if (minDiff != 0) {
      showview += minDiff * 100000000;
    }
    
    // Code schön formatieren
    StringBuilder buffer = new StringBuilder();
    showviewArr = splitDigits(showview);
    for (int i = 0; i < showviewArr.length; i++) {
      buffer.append(showviewArr[i]);
      
      int digit = showviewArr.length - i - 1;
      if ((digit > 0) && (digit % 3 == 0)) {
        buffer.append('-');
      }
    }
    
    return buffer.toString();
  }
  

  private int calculateNewNumber(int day, int top, int newtopLength,
    int repeatCount)
  {
    int[] numberArr = splitDigits(top, newtopLength);    
    for (int i = 0; i < repeatCount; i++) {
      // Man addiert die erste Stelle von Top und die letzte Stelle des Tages,
      // um die erste Stelle der neuen Zahl zu bekommen (modulo 10).
      numberArr[0] = (numberArr[0] + day) % 10;
      for (int j = 1; j < numberArr.length; j++) {
        // Nun addiert man die zweite Stelle von Top auf die erste Stelle der
        // neuen Zahl (modulo 10), um die zweite Stelle der neuen Zahl zu
        // erhalten.
        // Für die restlichen Stellen verfährt man sinngemäß.
        numberArr[j] = (numberArr[j] + numberArr[j - 1]) % 10;
      }
    }
    return mergeDigits(numberArr);
  }


  /**
   * Splits a number in its digits.
   * 
   * @param number The number to split
   * @return The number split in digits
   */
  private int[] splitDigits(int number) {
    int length = getLength(number);
    return splitDigits(number, length);
  }


  /**
   * Splits a number in its digits.
   * 
   * @param number The number to split
   * @return The number split in digits
   */
  private int[] splitDigits(int number, int nrDigits) {
    int[] digitArr = new int[nrDigits];
    for (int i = 0; i < digitArr.length; i++) {
      int index = digitArr.length - i - 1;
      digitArr[index] = number % 10;
      number /= 10;
    }
    
    return digitArr;
  }
  
  
  /**
   * Merges the digits back to a number.
   * 
   * @param digitArr The digits.
   * @return The number.
   */
  private int mergeDigits(int[] digitArr) {
    int number = 0;
    for (int i = 0; i < digitArr.length; i++) {
      number = (number * 10) + (digitArr[i] % 10);
    }
    
    return number;
  }
  
  
  /**
   * Returns the number of digits.
   * 
   * @return the number of digits of the given number.
   */
  private int getLength(int number) {
    int length = 1;
    while (number >= 10) {
      number /= 10;
      length++;
    }
    
    return length;
  }
  
  
  /**
   * Calculate the modulo savely so that even negative numbers are calculated
   * correctly after the mathematical modulo.
   * <p>
   * Normally -13 % 10 will result in -3. This method returns the correct
   * result of 7.
   * <p>
   * Use this method every time when you substract numbers with modulo.
   * 
   * @param number The number to calculate the modulo for
   * @param base The modulo base
   * @return The number modulo base
   */
  private int saveMod(int number, int base) {
    return ((number % base) + base) % base;
  }

  protected static class NoChannelNumberException extends Exception {
    
  }
}
