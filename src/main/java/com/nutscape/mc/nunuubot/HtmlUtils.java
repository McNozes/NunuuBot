package com.nutscape.mc.nunuubot;

import java.util.regex.Pattern;
import java.util.Map;
import java.util.Deque;

public class HtmlUtils {

    public static String substEscape(String s) {
        if (!mEscapePat.matcher(s).matches()) {
            return s;
        }

        Deque<Entry> entries = getOccurrences(s);
        if (entries.isEmpty()) {
            return s;
        }
        Entry e = entries.removeFirst();

        /*
         * Substitute in String.
         */
        StringBuilder b = new StringBuilder();
        for (int i=0; i < s.length();) {
            //System.out.println(e.mStart);
            if (i == e.mStart) {
                //System.out.println("aqui");
                b.append(e.mWord);
                i += (e.mEnd-e.mStart)+1;
                //System.out.println("i- " + i);
                //System.out.println(b.toString());

                if (entries.isEmpty()) {
                    b.append(s.substring(i));
                    i += s.length();
                } else {
                    e = entries.removeFirst();
                }
            } else {
                //System.out.println("i " + i);
                String segment = s.substring(i,e.mStart);
                b.append(segment);
                //System.out.println(b.toString());
                i += segment.length();
                //System.out.println("i2 " + i);
            }
        }
        return b.toString();
    }

    // Implementation ----------------------------

    private static class Entry {
        int mStart;
        int mEnd;
        String mWord;

        Entry(int start,int end,String word) {
            if (end - start < 2) {
                throw new IllegalArgumentException();
            }
            mStart = start;
            mEnd = end;
            mWord = word;
        }
    }

    private final static Pattern mEscapePat = Pattern.compile(".*&\\w+;.*");
    private final static Map<String,String> mEscapeMap = 
        new java.util.HashMap<>();

    static {
        initEscapeMap();
    }

    private static Deque<Entry> getOccurrences(String s) {
        Deque<Entry> entries = new java.util.ArrayDeque<>();

        int start = -1;
        for (int i=0; i < s.length(); ++i) {
            if (s.charAt(i) == '&') {
                start = i;
            }
            if (s.charAt(i) == ';' && start != -1){
                String word = mEscapeMap.get(s.substring(start+1,i));
                if (word != null) {
                    try {

                        Entry e = new Entry(start,i,word);
                        entries.addLast(e);

                    } catch (IllegalArgumentException e) { }
                }

                start = -1;
            }
        }

        return entries;
    }

    private static void initEscapeMap() {
        Map<String,String> m = mEscapeMap;
        m.put("lsquo"  ,  "‘");
        m.put("rsquo"  ,  "’");
        m.put("sbquo"  ,  "‚");
        m.put("ldquo"  ,  "“");
        m.put("rdquo"  ,  "”");
        m.put("bdquo"  ,  "„");
        m.put("dagger" ,  "†");
        m.put("Dagger" ,  "‡");
        m.put("permil" ,  "‰");
        m.put("lsaquo" ,  "‹");
        m.put("rsaquo" ,  "›");
        m.put("spades" ,  "♠");
        m.put("clubs"  ,  "♣");
        m.put("hearts" ,  "♥");
        m.put("diams"  ,  "♦");
        m.put("oline"  ,  "‾");
        m.put("larr"   ,  "←");
        m.put("uarr"   ,  "↑");
        m.put("rarr"   ,  "→");
        m.put("darr"   ,  "↓");
        m.put("#x2122" ,  "™");
        m.put("trade"  ,  "™");
        m.put("#00"    ,  "-");
        m.put("#08"    ,  " ");
        m.put("#09"    ,  " ");
        m.put("#10"    ,  " ");
        m.put("#11"    ,  " ");
        m.put("#32"    ,  " ");
        m.put("#33"    ,  "!");
        m.put("#34"    ,  "\"");
        m.put("quot"   ,  "\"");
        m.put("#35"    ,  "#");
        m.put("#36"    ,  "$");
        m.put("#37"    ,  "%");
        m.put("#38"    ,  "&");
        m.put("amp"    ,  "&");
        m.put("#39"    ,  "\'");
        m.put("#40"    ,  "(");
        m.put("#41"    ,  ")");
        m.put("#42"    ,  "*");
        m.put("#43"    ,  "+");
        m.put("#44"    ,  ",");
        m.put("#45"    ,  "-");
        m.put("#46"    ,  ".");
        m.put("#47"    ,  "/");
        m.put("frasl"  ,  "/");
        m.put("#48"    ,  "0");
        m.put("#49"    ,  "1");
        m.put("#50"    ,  "2");
        m.put("#51"    ,  "3");
        m.put("#52"    ,  "4");
        m.put("#53"    ,  "5");
        m.put("#54"    ,  "6");
        m.put("#55"    ,  "7");
        m.put("#56"    ,  "8");
        m.put("#57"    ,  "9");
        m.put("#58"    ,  ":");
        m.put("#59"    ,  ";");
        m.put("#60"    ,  "<");
        m.put("lt"     ,  "<");
        m.put("#61"    ,  "=");
        m.put("#62"    ,  ">");
        m.put("gt"     ,  ">");
        m.put("#63"    ,  "?");
        m.put("#64"    ,  "@");
        m.put("#65"    ,  "A");
        m.put("#66"    ,  "B");
        m.put("#67"    ,  "C");
        m.put("#68"    ,  "D");
        m.put("#69"    ,  "E");
        m.put("#70"    ,  "F");
        m.put("#71"    ,  "G");
        m.put("#72"    ,  "H");
        m.put("#73"    ,  "I");
        m.put("#74"    ,  "J");
        m.put("#75"    ,  "K");
        m.put("#76"    ,  "L");
        m.put("#77"    ,  "M");
        m.put("#78"    ,  "N");
        m.put("#79"    ,  "O");
        m.put("#80"    ,  "P");
        m.put("#81"    ,  "Q");
        m.put("#82"    ,  "R");
        m.put("#83"    ,  "S");
        m.put("#84"    ,  "T");
        m.put("#85"    ,  "U");
        m.put("#86"    ,  "V");
        m.put("#87"    ,  "W");
        m.put("#88"    ,  "X");
        m.put("#89"    ,  "Y");
        m.put("#90"    ,  "Z");
        m.put("#91"    ,  "[");
        m.put("#92"    ,  "\"");
        m.put("#93"    ,  "]");
        m.put("#94"    ,  "^");
        m.put("#95"    ,  "_");
        m.put("#96"    ,  "`");
        m.put("#97"    ,  "a");
        m.put("#98"    ,  "b");
        m.put("#99"    ,  "c");
        m.put("#100"   ,  "d");
        m.put("#101"   ,  "e");
        m.put("#102"   ,  "f");
        m.put("#103"   ,  "g");
        m.put("#104"   ,  "h");
        m.put("#105"   ,  "i");
        m.put("#106"   ,  "j");
        m.put("#107"   ,  "k");
        m.put("#108"   ,  "l");
        m.put("#109"   ,  "m");
        m.put("#110"   ,  "n");
        m.put("#111"   ,  "o");
        m.put("#112"   ,  "p");
        m.put("#113"   ,  "q");
        m.put("#114"   ,  "r");
        m.put("#115"   ,  "s");
        m.put("#116"   ,  "t");
        m.put("#117"   ,  "u");
        m.put("#118"   ,  "v");
        m.put("#119"   ,  "w");
        m.put("#120"   ,  "x");
        m.put("#121"   ,  "y");
        m.put("#122"   ,  "z");
        m.put("#123"   ,  "{");
        m.put("#124"   ,  "|");
        m.put("#125"   ,  "}");
        m.put("#227"   ,  "ã");
        m.put("atilde" ,  "ã");
        m.put("#228"   ,  "ä");
        m.put("#229"   ,  "a");
        m.put("auml"   ,  "ä");
        m.put("aring"  ,  "a");
        m.put("#230"   ,  "æ");
        m.put("#231"   ,  "ç");
        m.put("#232"   ,  "è");
        m.put("#233"   ,  "é");
        m.put("#234"   ,  "ê");
        m.put("#235"   ,  "ë");
        m.put("#236"   ,  "ì");
        m.put("#237"   ,  "í");
        m.put("#238"   ,  "}");
        m.put("#239"   ,  "ï");
        m.put("aelig"  ,  "æ");
        m.put("ccedil" ,  "ç");
        m.put("egrave" ,  "è");
        m.put("eacute" ,  "é");
        m.put("ecirc"  ,  "ê");
        m.put("euml"   ,  "ë");
        m.put("igrave" ,  "ì");
        m.put("iacute" ,  "í");
        m.put("icirc"  ,  "}");
        m.put("iuml"   ,  "ï");
        m.put("#240"   ,  "o");
        m.put("#241"   ,  "ñ");
        m.put("#242"   ,  "ò");
        m.put("#243"   ,  "ó");
        m.put("#244"   ,  "ô");
        m.put("#245"   ,  "õ");
        m.put("#246"   ,  "ö");
        m.put("#247"   ,  "÷");
        m.put("#248"   ,  "ø");
        m.put("#249"   ,  "ù");
        m.put("#250"   ,  "ú");
        m.put("#251"   ,  "û");
        m.put("#252"   ,  "ü");
        m.put("#253"   ,  "ý");
        m.put("#254"   ,  "þ");
        m.put("#255"   ,  "ÿ");
        m.put("eth"    ,  "o") ;
        m.put("ntilde" ,  "ñ");
        m.put("ograve" ,  "ò");
        m.put("oacute" ,  "ó");
        m.put("ocirc"  ,  "ô");
        m.put("otilde" ,  "õ");
        m.put("ouml"   ,  "ö");
        m.put("divide" ,  "÷");
        m.put("oslash" ,  "ø");
        m.put("ugrave" ,  "ù");
        m.put("uacute" ,  "ú");
        m.put("ucirc"  ,  "û");
        m.put("uuml"   ,  "ü");
        m.put("yacute" ,  "ý");
        m.put("thorn"  ,  "þ");
        m.put("yuml"   ,  "ÿ");
        m.put("Alpha"  ,  "Α");
        m.put("alpha"  ,  "α");
        m.put("Beta"   ,  "Β");
        m.put("beta"   ,  "β");
        m.put("Gamma"  ,  "Γ");
        m.put("gamma"  ,  "γ");
        m.put("Delta"  ,  "Δ");
        m.put("delta"  ,  "δ");
        m.put("Epsilon",  "Ε");
        m.put("epsilon",  "ε");
        m.put("Zeta"   ,  "Ζ");
        m.put("#126"   ,  "~");
        m.put("#133"   ,  "~");
        m.put("hellip" ,  "…");
        m.put("#150"   ,  "…");
        m.put("ndash"  ,  "–");
        m.put("#151"   ,  "–");
        m.put("mdash"  ,  "—");
        m.put("#152"   ,  "" );
        m.put("#153"   ,  "" );
        m.put("#154"   ,  "" );
        m.put("#155"   ,  "" );
        m.put("#156"   ,  "" );
        m.put("#157"   ,  "" );
        m.put("#158"   ,  "" );
        m.put("#159"   ,  "" );
        m.put("#160"   ,  "" );
        m.put("nbsp"   ,  "" );
        m.put("#161"   ,  "¡");
        m.put("iexcl"  ,  "¡");
        m.put("#162"   ,  "¢");
        m.put("cent"   ,  "¢");
        m.put("#163"   ,  "£");
        m.put("pound"  ,  "£");
        m.put("#164"   ,  "¤");
        m.put("curren" ,  "¤")   ;
        m.put("#165"   ,  "¥");
        m.put("yen"    ,  "¥");
        m.put("#166"   ,  "¦");
        m.put("brvbar" ,  "¦");
        m.put("brkbar" ,  "¦");
        m.put("#167"   ,  "§");
        m.put("sect"   ,  "§");
        m.put("#168"   ,  "§");
        m.put("uml"    ,  "¨");
        m.put("die"    ,  "¨");
        m.put("#169"   ,  "©");
        m.put("copy"   ,  "©");
        m.put("#170"   ,  "ª");
        m.put("ordf"   ,  "ª");
        m.put("#171"   ,  "«");
        m.put("laquo"  ,  "«");
        m.put("#172"   ,  "¬");
        m.put("not"    ,  "¬");
        m.put("#173"   ,  "\u00ad");
        m.put("shy"    ,  "\u00ad");
        m.put("#174"   ,  "®");
        m.put("reg"    ,  "®");
        m.put("#175"   ,  "¯");
        m.put("macr"   ,  "¯");
        m.put("hibar"  ,  "¯");
        m.put("#176"   ,  "°");
        m.put("deg"    ,  "°");
        m.put("#177"   ,  "±");
        m.put("plusmn" ,  "±");
        m.put("#178"   ,  "²");
        m.put("sup2"   ,  "²");
        m.put("#179"   ,  "³");
        m.put("sup3"   ,  "³");
        m.put("#180"   ,  "´");
        m.put("acute"  ,  "´");
        m.put("#181"   ,  "µ");
        m.put("micro"  ,  "µ");
        m.put("#182"   ,  "¶");
        m.put("para"   ,  "¶");
        m.put("#183"   ,  "·");
        m.put("middot" ,  "·");
        m.put("#184"   ,  "¸");
        m.put("cedil"  ,  "¸");
        m.put("#185"   ,  "¹");
        m.put("sup1"   ,  "¹");
        m.put("#186"   ,  "º");
        m.put("ordm"   ,  "º");
        m.put("#187"   ,  "»");
        m.put("raquo"  ,  "»");
        m.put("#188"   ,  "¼");
        m.put("frac14" ,  "¼");
        m.put("#189"   ,  "½");
        m.put("frac12" ,  "½");
        m.put("#190"   ,  "¾");
        m.put("frac34" ,  "¾");
        m.put("#191"   ,  "¿");
        m.put("iquest" ,  "¿");
        m.put("#192"   ,  "À");
        m.put("Agrave" ,  "À");
        m.put("#193"   ,  "Á");
        m.put("Aacute" ,  "Á");
        m.put("#194"   ,  "Â");
        m.put("Acirc"  ,  "Â");
        m.put("#195"   ,  "Ã");
        m.put("Atilde" ,  "Ã");
        m.put("#196"   ,  "Ä");
        m.put("Auml"   ,  "Ä");
        m.put("#197"   ,  "Å");
        m.put("Aring"  ,  "Å");
        m.put("#198"   ,  "Æ");
        m.put("AElig"  ,  "Æ");
        m.put("#199"   ,  "Ç");
        m.put("Ccedil" ,  "Ç");
        m.put("#200"   ,  "È");
        m.put("Egrave" ,  "È");
        m.put("#201"   ,  "É");
        m.put("Eacute" ,  "É");
        m.put("#202"   ,  "Ê");
        m.put("Ecirc"  ,  "Ê");
        m.put("#203"   ,  "Ë");
        m.put("Euml"   ,  "Ë");
        m.put("#204"   ,  "Ì");
        m.put("Igrave" ,  "Ì");
        m.put("#205"   ,  "Í");
        m.put("Iacute" ,  "Í");
        m.put("#206"   ,  "Î");
        m.put("Icirc"  ,  "Î");
        m.put("#207"   ,  "Ï");
        m.put("Iuml"   ,  "Ï");
        m.put("#208"   ,  "Ð");
        m.put("ETH"    ,  "Ð");
        m.put("#209"   ,  "Ñ");
        m.put("Ntilde" ,  "Ñ");
        m.put("#210"   ,  "Ò");
        m.put("Ograve" ,  "Ò");
        m.put("#211"   ,  "Ó");
        m.put("Oacute" ,  "Ó");
        m.put("#212"   ,  "Ô");
        m.put("Ocirc"  ,  "Ô");
        m.put("#213"   ,  "Õ");
        m.put("Otilde" ,  "Õ");
        m.put("#214"   ,  "Ö");
        m.put("Ouml"   ,  "Ö");
        m.put("#215"   ,  "×");
        m.put("times"  ,  "×");
        m.put("#216"   ,  "Ø");
        m.put("Oslash" ,  "Ø");
        m.put("#217"   ,  "Ù");
        m.put("Ugrave" ,  "Ù");
        m.put("#218"   ,  "Ú");
        m.put("Uacute" ,  "Ú");
        m.put("#219"   ,  "Û");
        m.put("Ucirc"  ,  "Û");
        m.put("#220"   ,  "Ü");
        m.put("Uuml"   ,  "Ü");
        m.put("#221"   ,  "Ý");
        m.put("Yacute" ,  "Ý");
        m.put("#222"   ,  "Þ");
        m.put("THORN"  ,  "Þ");
        m.put("#223"   ,  "ß");
        m.put("szlig"  ,  "ß");
        m.put("#224"   ,  "à");
        m.put("agrave" ,  "à");
        m.put("#225"   ,  "á");
        m.put("aacute" ,  "á");
        m.put("#226"   ,  "Â");
        m.put("acirc"  ,  "Â");
    }

    // test
    public static void main(String[] args) {
        String s = "teste &quot; teste";
        String t = substEscape(s);
        System.out.println(t);
    }
}

