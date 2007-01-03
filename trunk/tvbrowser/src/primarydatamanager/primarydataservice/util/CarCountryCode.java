package primarydatamanager.primarydataservice.util;

import java.util.HashMap;

/**
 * 
 * 
 * @author Til Schneider, www.murfman.de
 */
public class CarCountryCode {
  
  private static HashMap<String, String> mCarCountryCodeHash;


  private static void init() {
    mCarCountryCodeHash = new HashMap<String, String>();
    
    // Quelle: http://www.schlaufuchs.at/list/l_akinte.htm
    mCarCountryCodeHash.put("A",   "Österreich");
    mCarCountryCodeHash.put("AFG", "Afghanistan");
    mCarCountryCodeHash.put("AL",  "Albanien");
    mCarCountryCodeHash.put("AND", "Andorra");
    mCarCountryCodeHash.put("ANG", "Angola");
    mCarCountryCodeHash.put("AUS", "Australien");
    mCarCountryCodeHash.put("AZ",  "Aserbaidschan");
    mCarCountryCodeHash.put("B",   "Belgien");
    mCarCountryCodeHash.put("BD",  "Bangladesch");
    mCarCountryCodeHash.put("BDS", "Barbados");
    mCarCountryCodeHash.put("BF",  "Burkina Faso");
    mCarCountryCodeHash.put("BG",  "Bulgarien");
    mCarCountryCodeHash.put("BH",  "Belize");
    mCarCountryCodeHash.put("BHT", "Bhutan");
    mCarCountryCodeHash.put("BIH", "Bosnien-Herzegowina");
    mCarCountryCodeHash.put("BOL", "Bolivien");
    mCarCountryCodeHash.put("BR",  "Brasilien");
    mCarCountryCodeHash.put("BRN", "Bahrain");
    mCarCountryCodeHash.put("BRU", "Brunei");
    mCarCountryCodeHash.put("BS",  "Bahamas");
    mCarCountryCodeHash.put("BY",  "Belarus (Weißrußland)");
    mCarCountryCodeHash.put("C",   "Kuba");
    mCarCountryCodeHash.put("CAM", "Kamerun");
    mCarCountryCodeHash.put("CD",  "Kongo"); // (ehem. ZR - Zaire)
    mCarCountryCodeHash.put("CDN", "Kanada");
    mCarCountryCodeHash.put("CH",  "Schweiz");
    mCarCountryCodeHash.put("CI",  "Elfenbeinküste");
    mCarCountryCodeHash.put("CL",  "Sri Lanka");
    mCarCountryCodeHash.put("CO",  "Kolumbien");
    mCarCountryCodeHash.put("COM", "Komoren");
    mCarCountryCodeHash.put("CR",  "Costa Rica");
    mCarCountryCodeHash.put("CV",  "Kap Verde");
    mCarCountryCodeHash.put("CY",  "Zypern");
    mCarCountryCodeHash.put("CZ",  "Tschechische Republik");
    mCarCountryCodeHash.put("D",   "Deutschland");
    mCarCountryCodeHash.put("DK",  "Dänemark");
    mCarCountryCodeHash.put("DOM", "Dominikanische Republik");
    mCarCountryCodeHash.put("DY",  "Benin");
    mCarCountryCodeHash.put("DZ",  "Algerien");
    mCarCountryCodeHash.put("E",   "Spanien");
    mCarCountryCodeHash.put("EAK", "Kenia");
    mCarCountryCodeHash.put("EAT", "Tansania");
    mCarCountryCodeHash.put("EAU", "Uganda");
    mCarCountryCodeHash.put("EC",  "Ecuador");
    mCarCountryCodeHash.put("ER",  "Eritrea");
    mCarCountryCodeHash.put("ES",  "El Salvador");
    mCarCountryCodeHash.put("EST", "Estland");
    mCarCountryCodeHash.put("ET",  "Ägypten");
    mCarCountryCodeHash.put("ETH", "Äthiopien");
    mCarCountryCodeHash.put("F",   "Frankreich");
    mCarCountryCodeHash.put("FIN", "Finnland");
    mCarCountryCodeHash.put("FJI", "Fidschi");
    mCarCountryCodeHash.put("FL",  "Liechtenstein");
    mCarCountryCodeHash.put("FR",  "Färöer");
    mCarCountryCodeHash.put("FSM", "Mikronesien");
    mCarCountryCodeHash.put("G",   "Gabun");
    mCarCountryCodeHash.put("GB",  "Großbritannien"); // Vereinigtes Königreich
    mCarCountryCodeHash.put("GBA", "Alderney");
    mCarCountryCodeHash.put("GBG", "Guernsey");
    mCarCountryCodeHash.put("GBJ", "Jersey");
    mCarCountryCodeHash.put("GBM", "Insel Man");
    mCarCountryCodeHash.put("GBZ", "Gibraltar");
    mCarCountryCodeHash.put("GCA", "Guatemala");
    mCarCountryCodeHash.put("GE",  "Georgien");
    mCarCountryCodeHash.put("GH",  "Ghana");
    mCarCountryCodeHash.put("GN",  "Guinea-Bissau");
    mCarCountryCodeHash.put("GQ",  "Äquatorialguinea");
    mCarCountryCodeHash.put("GR",  "Griechenland");
    mCarCountryCodeHash.put("GUY", "Guyana");
    mCarCountryCodeHash.put("H",   "Ungarn");
    mCarCountryCodeHash.put("HK",  "Hongkong");
    mCarCountryCodeHash.put("HN",  "Honduras");
    mCarCountryCodeHash.put("HR",  "Kroatien");
    mCarCountryCodeHash.put("I",   "Italien");
    mCarCountryCodeHash.put("IL",  "Israel");
    mCarCountryCodeHash.put("IND", "Indien");
    mCarCountryCodeHash.put("IR",  "Iran");
    mCarCountryCodeHash.put("IRL", "Irland");
    mCarCountryCodeHash.put("IRQ", "Irak");
    mCarCountryCodeHash.put("IS",  "Island");
    mCarCountryCodeHash.put("J",   "Japan");
    mCarCountryCodeHash.put("JA",  "Jamaika");
    mCarCountryCodeHash.put("JOR", "Jordanien");
    mCarCountryCodeHash.put("K",   "Kambodscha");
    mCarCountryCodeHash.put("KIR", "Kiribati");
    mCarCountryCodeHash.put("KS",  "Kirgisistan");
    mCarCountryCodeHash.put("KSA", "Saudi-Arabien");
    mCarCountryCodeHash.put("KWT", "Kuwait");
    mCarCountryCodeHash.put("KZ",  "Kasachstan");
    mCarCountryCodeHash.put("L",   "Luxemburg");
    mCarCountryCodeHash.put("LAO", "Laos");
    mCarCountryCodeHash.put("LAR", "Libyen");
    mCarCountryCodeHash.put("LB",  "Liberia");
    mCarCountryCodeHash.put("LS",  "Lesotho");
    mCarCountryCodeHash.put("LT",  "Litauen");
    mCarCountryCodeHash.put("LV",  "Lettland");
    mCarCountryCodeHash.put("M",   "Malta");
    mCarCountryCodeHash.put("MA",  "Marokko");
    mCarCountryCodeHash.put("MAL", "Malaysia");
    mCarCountryCodeHash.put("MC",  "Monaco");
    mCarCountryCodeHash.put("MD",  "Moldawien");
    mCarCountryCodeHash.put("MEX", "Mexiko");
    mCarCountryCodeHash.put("MGL", "Mongolei");
    mCarCountryCodeHash.put("MH",  "Marshall-Inseln");
    mCarCountryCodeHash.put("MK",  "Mazedonien");
    mCarCountryCodeHash.put("MOC", "Mosambik");
    mCarCountryCodeHash.put("MS",  "Mauritius");
    mCarCountryCodeHash.put("MV",  "Malediven");
    mCarCountryCodeHash.put("MW",  "Malawi");
    mCarCountryCodeHash.put("MYA", "Myanmar");
    mCarCountryCodeHash.put("N",   "Norwegen");
    mCarCountryCodeHash.put("NA",  "Niederländische Antillen");
    mCarCountryCodeHash.put("NAM", "Namibia");
    mCarCountryCodeHash.put("NAU", "Nauru");
    mCarCountryCodeHash.put("NEP", "Nepal");
    mCarCountryCodeHash.put("NIC", "Nicaragua");
    mCarCountryCodeHash.put("NL",  "Niederlande");
    mCarCountryCodeHash.put("NZ",  "Neuseeland");
    mCarCountryCodeHash.put("OM",  "Oman");
    mCarCountryCodeHash.put("P",   "Portugal");
    mCarCountryCodeHash.put("PA",  "Panama");
    mCarCountryCodeHash.put("PE",  "Peru");
    mCarCountryCodeHash.put("PK",  "Pakistan");
    mCarCountryCodeHash.put("PL",  "Polen");
    mCarCountryCodeHash.put("PNG", "Papua-Neuguinea");
    mCarCountryCodeHash.put("PY",  "Paraguay");
    mCarCountryCodeHash.put("Q",   "Katar");
    mCarCountryCodeHash.put("RA",  "Argentinien");
    mCarCountryCodeHash.put("RB",  "Botswana");
    mCarCountryCodeHash.put("RC",  "China"); // (Taiwan)
    mCarCountryCodeHash.put("RCA", "Zentralafrikanische Republik");
    mCarCountryCodeHash.put("RCB", "Kongo");
    mCarCountryCodeHash.put("RCH", "Chile");
    mCarCountryCodeHash.put("RG",  "Guinea");
    mCarCountryCodeHash.put("RH",  "Haiti");
    mCarCountryCodeHash.put("RI",  "Indonesien");
    mCarCountryCodeHash.put("RIM", "Mauretanien");
    mCarCountryCodeHash.put("RL",  "Libanon");
    mCarCountryCodeHash.put("RM",  "Madagaskar");
    mCarCountryCodeHash.put("RMM", "Mali");
    mCarCountryCodeHash.put("RN",  "Niger");
    mCarCountryCodeHash.put("RO",  "Rumänien");
    mCarCountryCodeHash.put("ROK", "Süd-Korea");
    mCarCountryCodeHash.put("ROU", "Uruguay");
    mCarCountryCodeHash.put("RP",  "Philippinen");
    mCarCountryCodeHash.put("RSM", "San Marino");
    mCarCountryCodeHash.put("RT",  "Togo");
    mCarCountryCodeHash.put("RU",  "Burundi");
    mCarCountryCodeHash.put("RUS", "Russische Föderation");
    mCarCountryCodeHash.put("RWA", "Ruanda");
    mCarCountryCodeHash.put("S",   "Schweden");
    mCarCountryCodeHash.put("SD",  "Swasiland");
    mCarCountryCodeHash.put("SGP", "Singapur");
    mCarCountryCodeHash.put("SK",  "Slowakische Republik");
    mCarCountryCodeHash.put("SLO", "Slowenien");
    mCarCountryCodeHash.put("SME", "Surinam");
    mCarCountryCodeHash.put("SN",  "Senegal");
    mCarCountryCodeHash.put("SP",  "Somalia");
    mCarCountryCodeHash.put("STP", "Sao Tome und Principe");
    mCarCountryCodeHash.put("SU",  "Sowjetunion (ehemalige)");
    mCarCountryCodeHash.put("SUD", "Sudan");
    mCarCountryCodeHash.put("SY",  "Seychellen");
    mCarCountryCodeHash.put("SYR", "Syrien");
    mCarCountryCodeHash.put("TCH", "Tschad");
    mCarCountryCodeHash.put("TG",  "Togo");
    mCarCountryCodeHash.put("THA", "Thailand");
    mCarCountryCodeHash.put("TJ",  "Tadschikistan");
    mCarCountryCodeHash.put("TM",  "Turkmenistan");
    mCarCountryCodeHash.put("TN",  "Tunesien");
    mCarCountryCodeHash.put("TO",  "Tonga");
    mCarCountryCodeHash.put("TR",  "Türkei");
    mCarCountryCodeHash.put("TT",  "Trinidad und Tobago");
    mCarCountryCodeHash.put("TUV", "Tuvalu");
    mCarCountryCodeHash.put("UA",  "Ukraine");
    mCarCountryCodeHash.put("UAE", "Vereinigte Arabische Emirate");
    mCarCountryCodeHash.put("USA", "USA"); // Vereinigte Staaten von Amerika
    mCarCountryCodeHash.put("UZ",  "Usbekistan");
    mCarCountryCodeHash.put("V",   "Vatikanstadt");
    mCarCountryCodeHash.put("VN",  "Vietnam");
    mCarCountryCodeHash.put("VU",  "Vanuatu");
    mCarCountryCodeHash.put("WAG", "Gambia");
    mCarCountryCodeHash.put("WAL", "Sierra Leone");
    mCarCountryCodeHash.put("WAN", "Nigeria");
    mCarCountryCodeHash.put("WD",  "Dominica");
    mCarCountryCodeHash.put("WG",  "Grenada");
    mCarCountryCodeHash.put("WL",  "Santa Lucia");
    mCarCountryCodeHash.put("WS",  "Samoa");
    mCarCountryCodeHash.put("WV",  "St. Vincent und die Grenadinen");
    mCarCountryCodeHash.put("YAR", "Jemen");
    mCarCountryCodeHash.put("YU",  "Jugoslawien");
    mCarCountryCodeHash.put("YV",  "Venezuela Z Sambia");
    mCarCountryCodeHash.put("ZA",  "Südafrika");
    mCarCountryCodeHash.put("ZR",  "Zaire"); // (jetzt CD - Dem. Republik Kongo)
    mCarCountryCodeHash.put("ZW",  "Simbabwe");
  }
  
  
  public static final String getCountryName(String carCountryCode) {
    if (mCarCountryCodeHash == null) {
      init();
    }
    
    return mCarCountryCodeHash.get(carCountryCode);
  }

}
