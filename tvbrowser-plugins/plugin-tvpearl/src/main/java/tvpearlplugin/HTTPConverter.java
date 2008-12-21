/*
 * TV-Pearl by Reinhard Lehrbaum
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
 */
package tvpearlplugin;

import java.util.Hashtable;
import java.util.Iterator;

public class HTTPConverter
{
    private Hashtable<String, Character> characterTable;

    public HTTPConverter()
    {
        characterTable = new Hashtable<String, Character>();
        initCharTable();
    }

    private void initCharTable()
    {
        characterTable.put("&quot;", '\u0022');
        characterTable.put("&amp;", '\u0026');
        characterTable.put("&lt;", '\u003C');
        characterTable.put("&gt;", '\u003E');
        characterTable.put("&nbsp;", '\u00A0');
        characterTable.put("&iexcl;", '\u00A1');
        characterTable.put("&cent;", '\u00A2');
        characterTable.put("&pound;", '\u00A3');
        characterTable.put("&curren;", '\u00A4');
        characterTable.put("&yen;", '\u00A5');
        characterTable.put("&brvbar;", '\u00A6');
        characterTable.put("&sect;", '\u00A7');
        characterTable.put("&uml;", '\u00A8');
        characterTable.put("&copy;", '\u00A9');
        characterTable.put("&ordf;", '\u00AA');
        characterTable.put("&laquo;", '\u00AB');
        characterTable.put("&not;", '\u00AC');
        characterTable.put("&shy;", '\u00AD');
        characterTable.put("&reg;", '\u00AE');
        characterTable.put("&macr;", '\u00AF');
        characterTable.put("&deg;", '\u00B0');
        characterTable.put("&plusmn;", '\u00B1');
        characterTable.put("&sup2;", '\u00B2');
        characterTable.put("&sup3;", '\u00B3');
        characterTable.put("&acute;", '\u00B4');
        characterTable.put("&micro;", '\u00B5');
        characterTable.put("&para;", '\u00B6');
        characterTable.put("&middot;", '\u00B7');
        characterTable.put("&cedil;", '\u00B8');
        characterTable.put("&sup1;", '\u00B9');
        characterTable.put("&ordm;", '\u00BA');
        characterTable.put("&raquo;", '\u00BB');
        characterTable.put("&frac14;", '\u00BC');
        characterTable.put("&frac12;", '\u00BD');
        characterTable.put("&frac34;", '\u00BE');
        characterTable.put("&iquest;", '\u00BF');
        characterTable.put("&Agrave;", '\u00C0');
        characterTable.put("&Aacute;", '\u00C1');
        characterTable.put("&Acirc;", '\u00C2');
        characterTable.put("&Atilde;", '\u00C3');
        characterTable.put("&Auml;", '\u00C4');
        characterTable.put("&Aring;", '\u00C5');
        characterTable.put("&AElig;", '\u00C6');
        characterTable.put("&Ccedil;", '\u00C7');
        characterTable.put("&Egrave;", '\u00C8');
        characterTable.put("&Eacute;", '\u00C9');
        characterTable.put("&Ecirc;", '\u00CA');
        characterTable.put("&Euml;", '\u00CB');
        characterTable.put("&Igrave;", '\u00CC');
        characterTable.put("&Iacute;", '\u00CD');
        characterTable.put("&Icirc;", '\u00CE');
        characterTable.put("&Iuml;", '\u00CF');
        characterTable.put("&ETH;", '\u00D0');
        characterTable.put("&Ntilde;", '\u00D1');
        characterTable.put("&Ograve;", '\u00D2');
        characterTable.put("&Oacute;", '\u00D3');
        characterTable.put("&Ocirc;", '\u00D4');
        characterTable.put("&Otilde;", '\u00D5');
        characterTable.put("&Ouml;", '\u00D6');
        characterTable.put("&times;", '\u00D7');
        characterTable.put("&Oslash;", '\u00D8');
        characterTable.put("&Ugrave;", '\u00D9');
        characterTable.put("&Uacute;", '\u00DA');
        characterTable.put("&Ucirc;", '\u00DB');
        characterTable.put("&Uuml;", '\u00DC');
        characterTable.put("&Yacute;", '\u00DD');
        characterTable.put("&THORN;", '\u00DE');
        characterTable.put("&szlig;", '\u00DF');
        characterTable.put("&agrave;", '\u00E0');
        characterTable.put("&aacute;", '\u00E1');
        characterTable.put("&acirc;", '\u00E2');
        characterTable.put("&atilde;", '\u00E3');
        characterTable.put("&auml;", '\u00E4');
        characterTable.put("&aring;", '\u00E5');
        characterTable.put("&aelig;", '\u00E6');
        characterTable.put("&ccedil;", '\u00E7');
        characterTable.put("&egrave;", '\u00E8');
        characterTable.put("&eacute;", '\u00E9');
        characterTable.put("&ecirc;", '\u00EA');
        characterTable.put("&euml;", '\u00EB');
        characterTable.put("&igrave;", '\u00EC');
        characterTable.put("&iacute;", '\u00ED');
        characterTable.put("&icirc;", '\u00EE');
        characterTable.put("&iuml;", '\u00EF');
        characterTable.put("&eth;", '\u00F0');
        characterTable.put("&ntilde;", '\u00F1');
        characterTable.put("&ograve;", '\u00F2');
        characterTable.put("&oacute;", '\u00F3');
        characterTable.put("&ocirc;", '\u00F4');
        characterTable.put("&otilde;", '\u00F5');
        characterTable.put("&ouml;", '\u00F6');
        characterTable.put("&divide;", '\u00F7');
        characterTable.put("&oslash;", '\u00F8');
        characterTable.put("&ugrave;", '\u00F9');
        characterTable.put("&uacute;", '\u00FA');
        characterTable.put("&ucirc;", '\u00FB');
        characterTable.put("&uuml;", '\u00FC');
        characterTable.put("&yacute;", '\u00FD');
        characterTable.put("&thorn;", '\u00FE');
        characterTable.put("&yuml;", '\u00FF');
        characterTable.put("&Alpha;", '\u0391');
        characterTable.put("&alpha;", '\u03B1');
        characterTable.put("&Beta;", '\u0392');
        characterTable.put("&beta;", '\u03B2');
        characterTable.put("&Gamma;", '\u0393');
        characterTable.put("&gamma;", '\u03B3');
        characterTable.put("&Delta;", '\u0394');
        characterTable.put("&delta;", '\u03B4');
        characterTable.put("&Epsilon;", '\u0395');
        characterTable.put("&epsilon;", '\u03B5');
        characterTable.put("&Zeta;", '\u0396');
        characterTable.put("&zeta;", '\u03B6');
        characterTable.put("&Eta;", '\u0397');
        characterTable.put("&eta;", '\u03B7');
        characterTable.put("&Theta;", '\u0398');
        characterTable.put("&theta;", '\u03B8');
        characterTable.put("&Iota;", '\u0399');
        characterTable.put("&iota;", '\u03B9');
        characterTable.put("&Kappa;", '\u039A');
        characterTable.put("&kappa;", '\u03BA');
        characterTable.put("&Lambda;", '\u039B');
        characterTable.put("&lambda;", '\u03BB');
        characterTable.put("&Mu;", '\u039C');
        characterTable.put("&mu;", '\u03BC');
        characterTable.put("&Nu;", '\u039D');
        characterTable.put("&nu;", '\u03BD');
        characterTable.put("&Xi;", '\u039E');
        characterTable.put("&xi;", '\u03BE');
        characterTable.put("&Omicron;", '\u039F');
        characterTable.put("&omicron;", '\u03BF');
        characterTable.put("&Pi;", '\u03A0');
        characterTable.put("&pi;", '\u03C0');
        characterTable.put("&Rho;", '\u03A1');
        characterTable.put("&rho;", '\u03C1');
        characterTable.put("&Sigma;", '\u03A3');
        characterTable.put("&sigmaf;", '\u03C2');
        characterTable.put("&sigma;", '\u03C3');
        characterTable.put("&Tau;", '\u03A4');
        characterTable.put("&tau;", '\u03C4');
        characterTable.put("&Upsilon;", '\u03A5');
        characterTable.put("&upsilon;", '\u03C5');
        characterTable.put("&Phi;", '\u03A6');
        characterTable.put("&phi;", '\u03C6');
        characterTable.put("&Chi;", '\u03A7');
        characterTable.put("&chi;", '\u03C7');
        characterTable.put("&Psi;", '\u03A8');
        characterTable.put("&psi;", '\u03C8');
        characterTable.put("&Omega;", '\u03A9');
        characterTable.put("&omega;", '\u03C9');
        characterTable.put("&thetasym;", '\u03D1');
        characterTable.put("&upsih;", '\u03D2');
        characterTable.put("&piv;", '\u03D6');
        characterTable.put("&forall;", '\u2200');
        characterTable.put("&part;", '\u2202');
        characterTable.put("&exist;", '\u2203');
        characterTable.put("&empty;", '\u2205');
        characterTable.put("&nabla;", '\u2207');
        characterTable.put("&isin;", '\u2208');
        characterTable.put("&notin;", '\u2209');
        characterTable.put("&ni;", '\u220B');
        characterTable.put("&prod;", '\u220F');
        characterTable.put("&sum;", '\u2211');
        characterTable.put("&minus;", '\u2212');
        characterTable.put("&lowast;", '\u2217');
        characterTable.put("&radic;", '\u221A');
        characterTable.put("&prop;", '\u221D');
        characterTable.put("&infin;", '\u221E');
        characterTable.put("&ang;", '\u2220');
        characterTable.put("&and;", '\u2227');
        characterTable.put("&or;", '\u2228');
        characterTable.put("&cap;", '\u2229');
        characterTable.put("&cup;", '\u222A');
        characterTable.put("&int;", '\u222B');
        characterTable.put("&there4;", '\u2234');
        characterTable.put("&sim;", '\u223C');
        characterTable.put("&cong;", '\u2245');
        characterTable.put("&asymp;", '\u2248');
        characterTable.put("&ne;", '\u2260');
        characterTable.put("&equiv;", '\u2261');
        characterTable.put("&le;", '\u2264');
        characterTable.put("&ge;", '\u2265');
        characterTable.put("&sub;", '\u2282');
        characterTable.put("&sup;", '\u2283');
        characterTable.put("&nsub;", '\u2284');
        characterTable.put("&sube;", '\u2286');
        characterTable.put("&supe;", '\u2287');
        characterTable.put("&oplus;", '\u2295');
        characterTable.put("&otimes;", '\u2297');
        characterTable.put("&perp;", '\u22A5');
        characterTable.put("&sdot;", '\u22C5');
        characterTable.put("&loz;", '\u25CA');
        characterTable.put("&lceil;", '\u2308');
        characterTable.put("&rceil;", '\u2309');
        characterTable.put("&lfloor;", '\u230A');
        characterTable.put("&rfloor;", '\u230B');
        characterTable.put("&lang;", '\u2329');
        characterTable.put("&rang;", '\u232A');
        characterTable.put("&larr;", '\u2190');
        characterTable.put("&uarr;", '\u2191');
        characterTable.put("&rarr;", '\u2192');
        characterTable.put("&darr;", '\u2193');
        characterTable.put("&harr;", '\u2194');
        characterTable.put("&crarr;", '\u21B5');
        characterTable.put("&lArr;", '\u21D0');
        characterTable.put("&uArr;", '\u21D1');
        characterTable.put("&rArr;", '\u21D2');
        characterTable.put("&dArr;", '\u21D3');
        characterTable.put("&hArr;", '\u21D4');
        characterTable.put("&bull;", '\u2022');
        characterTable.put("&prime;", '\u2032');
        characterTable.put("&Prime;", '\u2033');
        characterTable.put("&oline;", '\u203E');
        characterTable.put("&frasl;", '\u2044');
        characterTable.put("&weierp;", '\u2118');
        characterTable.put("&image;", '\u2111');
        characterTable.put("&real;", '\u211C');
        characterTable.put("&trade;", '\u2122');
        characterTable.put("&euro;", '\u20AC');
        characterTable.put("&alefsym;", '\u2135');
        characterTable.put("&spades;", '\u2660');
        characterTable.put("&clubs;", '\u2663');
        characterTable.put("&hearts;", '\u2665');
        characterTable.put("&diams;", '\u2666');
        characterTable.put("&OElig;", '\u0152');
        characterTable.put("&oelig;", '\u0153');
        characterTable.put("&Scaron;", '\u0160');
        characterTable.put("&scaron;", '\u0161');
        characterTable.put("&Yuml;", '\u0178');
        characterTable.put("&fnof;", '\u0192');
        characterTable.put("&ensp;", '\u2002');
        characterTable.put("&emsp;", '\u2003');
        characterTable.put("&thinsp;", '\u2009');
        characterTable.put("&zwnj;", '\u200C');
        characterTable.put("&zwj;", '\u200D');
        characterTable.put("&lrm;", '\u200E');
        characterTable.put("&rlm;", '\u200F');
        characterTable.put("&ndash;", '\u2013');
        characterTable.put("&mdash;", '\u2014');
        characterTable.put("&lsquo;", '\u2018');
        characterTable.put("&rsquo;", '\u2019');
        characterTable.put("&sbquo;", '\u201A');
        characterTable.put("&ldquo;", '\u201C');
        characterTable.put("&rdquo;", '\u201D');
        characterTable.put("&bdquo;", '\u201E');
        characterTable.put("&dagger;", '\u2020');
        characterTable.put("&Dagger;", '\u2021');
        characterTable.put("&hellip;", '\u2026');
        characterTable.put("&permil;", '\u2030');
        characterTable.put("&lsaquo;", '\u2039');
        characterTable.put("&rsaquo;", '\u203A');
        characterTable.put("&circ;", '\u02C6');
        characterTable.put("&tilde;", '\u02DC');

    }

    public String convertToString(String value)
    {
        String result = value;

        // Replace known characters
        Iterator<String> it = characterTable.keySet().iterator();

        while ((it != null) && (it.hasNext()))
        {
            String key = it.next();
            result = result.replaceAll(key, Character.toString(characterTable.get(key)));
        }

        return result;
    }

}
