import org.apache.commons.lang3.StringUtils;

import java.io.File;
import java.io.FileNotFoundException;
import java.text.DecimalFormat;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Main {
    @SuppressWarnings("Duplicates")
    public static void main(String[] args) {
         final double THOUSAND = Math.pow(10, 3);
         final double MILLION = Math.pow(10, 6);
         final double BILLION = Math.pow(10, 9);
         final double TRILLION = Math.pow(10, 12);
        //Pattern PHONENUM = Pattern.compile("/\\(?([0-9]{3})\\)?([0-9]{3})\\2([0-9]{4})/");

        //Pattern DDMONTH_LOWER = Pattern.compile("((3[0|1]|[1|2][0-9]|[0-9])(\\s)(January|February|March|April|May|June|July|August|September|October|November|December|Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec))(\\s)");
//        Pattern $PRICEMB = Pattern.compile("((\\$)(([1-9][0-9]*)|0)(\\s)(million|billion))");
//        Pattern $PRICE = Pattern.compile("(\\$)(([1-9][0-9]*)|0)");
//        Pattern PRICEUS = Pattern.compile("(([1-9][0-9]*)|0)(\\s)(billion|million|trillion)(\\s)(U.S.)(\\s)(dollars)(\\s)");
//        Pattern PRICEBIG$ = Pattern.compile("(([1-9][0-9]*)|0)(\\s)(m|bn)(\\s)(Dollars)");
//        Pattern PRICEFRAC = Pattern.compile("(([1-9][0-9]*)|0)(\\s)([1-9][0-9]*\\/[1-9][0-9]*)(\\s)(Dollars)");
//        Pattern PRICE$ = Pattern.compile("(([1-9][0-9]*)|0+)(\\s)(Dollars)");
        //Pattern DDMONTH_LOWER = Pattern.compile("((3[0|1]|[1|2][0-9]|[0-9])(\\s)(January|February|March|April|May|June|July|August|September|October|November|December|Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec))(\\s)");
         //Pattern DDMONTH_UPPER = Pattern.compile("((3[0|1]|[1|2][0-9]|[0-9])(\\s)(JANUARY|FEBRUARY|MARCH|APRIL|MAY|JUNE|JULY|AUGUST|SEPTEMBER|OCTOBER|NOVEMBER|DECEMBER|JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC))(\\s)");
        // Pattern MONTHDD_LOWER = Pattern.compile("((January|February|March|April|May|June|July|August|September|October|November|December|Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec))");
//       Pattern MONTHDD_UPPER = Pattern.compile("((JANUARY|FEBRUARY|MARCH|APRIL|MAY|JUNE|JULY|AUGUST|SEPTEMBER|OCTOBER|NOVEMBER|DECEMBER|JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)(\\s)(3[0|1]|[1|2][0-9]|[0-9]))|(()(\\s)(3[0|1]|[1|2][0-9]|[0-9]))");
//        Pattern MONTHYEAR_LOWER = Pattern.compile("((January|February|March|April|May|June|July|August|September|October|November|December|Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)(\\s)((1|2)(\\d)(\\d)(\\d)))");
//        Pattern MONTHYEAR_UPPER = Pattern.compile("((JANUARY|FEBRUARY|MARCH|APRIL|MAY|JUNE|JULY|AUGUST|SEPTEMBER|OCTOBER|NOVEMBER|DECEMBER|JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)(\\s)((1|2)(\\d)(\\d)(\\d)))");
//        Pattern PERCENT = Pattern.compile("(([1-9][0-9]*)|0)(\\%)|(([1-9][0-9]*)0)(\\s)(percent|percentage)");
//        // Pattern PHRASE = Pattern.compile("(\\s)(\\w)(\\-)(\\w)(\\s)|(\\s)(\\w)(\\-)(\\w)(\\-)(\\w)(\\s)|(\\s)(\\w)(\\-)(([1-9][0-9]*)|0)(\\s)|(\\s)(([1-9][0-9]*)|0)(\\-)(\\w)(\\s)|(\\s)(([1-9][0-9]*)|0)(\\-)(([1-9][1-9]*)|0)(\\s)");
//        Pattern PHRASE = Pattern.compile("(\\w)(\\-)(\\w)");
//        Pattern BETWEEN = Pattern.compile("(\\s)(between)(\\s)(([1-9]([0-9])*)|0)(\\s)(and)(\\s)(([1-9]([0-9])*)|0)(\\s)");
//        Pattern NUMBER = Pattern.compile("^([1-9][0-9]*)|0$");
//        Pattern DOUBLE_NUMBER = Pattern.compile("^(([1-9][0-9]*)|0)(\\.)(([1-9][0-9]*)|0)$");
//        //new law
//        Pattern EMAIL = Pattern.compile("\\w+([.-]?\\w+)*@\\w+([.-]?\\w+)*(\\.\\w{2,3})+");
//        IStemmer porterStemmer= new Stemmer();
//
//        Pattern[] patternNames = {BETWEEN, PHRASE, EMAIL, DDMONTH_LOWER, PRICEUS, PRICEFRAC, PRICEBIG$, $PRICEMB, $PRICE, DDMONTH_LOWER, PHRASE, $PRICE, PRICEUS, PRICEFRAC, PRICE$, DDMONTH_UPPER, MONTHDD_LOWER, MONTHDD_UPPER, PERCENT, MONTHYEAR_LOWER, MONTHYEAR_UPPER, NUMBER, DOUBLE_NUMBER};
//        ArrayList<String> terms = new ArrayList<>();
//        String text = "United States Of America schools hospitals users lowering between 1 and 4 words-word $89 million lower-level 100 billion U.S. dollars 1 Jans 14 May $89 million Total between 3 and 4 between 3 and 4a- between 1 and 2 1 billion U.S dollars aba-ima aba-ima34 investments in energy-related research and May 1994 chen $25 4 4/5 Dollars 5-6a\n" +
//                "development in Japan during 1990 came to &yen;  5 billion U.S. dollars 91 bn dollars\n" +
//                "(7.5% of total R&amp;D investments for that year), of \n" +
//                "which Jan 4 \n" +
//                "&yen; 402 billion (43.9%) went towards R&amp;D on \n" +
//                "nuclear power \n" +
//                "and the remaining &yen;512.9 billion 56.1% was put $45 \n" +
//                "chen@gmail.com bla bla";

        Set<Character> delimiters = Stream.of('\'', '(', '[', '{', ')', ']', '}', ',', '.', ';', '/', '\\', '-',
                '#', '!', '?', '*', ':', '`', '|', '&', '^', '*', '@').collect(Collectors.toSet());
//        String word="##Chen).";
//        int first=0;
//        int last=word.length() - 1;
//        while(delimiters.contains(word.charAt(first)))  //prefix
//            first++;
//        while (delimiters.contains(word.charAt(last)))  //remove last char
//            last--;
//        String clean= StringUtils.substring(word,first, last+1);
//        System.out.println(clean);

//        String word="1242,";
//        int first = 0;
//        int last = word.length() - 1;
//        while (first> last && delimiters.contains(word.charAt(first)))  //prefix
//            first++;
//        while (last>0&& delimiters.contains(word.charAt(last)))  //suffix
//            last--;
//        System.out.println(StringUtils.substring(word, first, last + 1));



        String text = "Where and 1766 Marutis were 4 MAY. 10123, 123 Thousand 1010.56 55 Billion 10,123,000,000 (703) 733-6408 MAX 10% 20 percentage $1.723 22 3/4 Dollars $450,000 4 Mar $100 million 100 bn Dollars 100 billion U.S. dollars, 14 MAY. June 1994 10-part 10-14, Alexa Cohen. In 1993, 12,556 Hungarian-made Suzukis, 35O imported Swifts, and 1766 Marutis were sold in Hungary.  The export of over 10,000 cars \n" +
                "starts in April through Suzuki's Western European distributors. \n" +
                "Currently, 34 Hungarian companies deliver spare parts for the \n" +
                "Esztergom factory.  The proportion of domestic suppliers is expected \n" +
                "to reach 50 percent by late March, which, together with 1O percent \n" +
                "European suppliers, will give the 60-percent ratio that exempts \n" +
                "Hungarian Suzukis from European Union customs duty.";
//        Pattern PHONENUM = Pattern.compile("(\\()(\\d)(\\d)(\\d)(\\))(\\s)(\\d)(\\d)(\\d)(\\-)(\\d)(\\d)(\\d)(\\d)");
//
//        Matcher match3 = PHONENUM.matcher(text);
//        if (match3.find()) {
//            System.out.println(match3.group());
//        }
        int counter=0;
        double start = System.nanoTime()/Math.pow(10,6);
        ReadFile reader = new ReadFile("C:\\Users\\cheng\\Documents\\University\\3rd\\Aihzur\\corpus\\corpus");
        Document doc = reader.getNextDoc();
        Parse parser = new Parse("C:\\Users\\cheng\\Documents\\University\\3rd\\Aihzur\\stop words.txt");
        //HashMap<String, Term> result = parser.parse(text, " ", " ", true);
        while (doc!=null){
            long now=System.nanoTime();
            HashMap<String, Term> result = parser.parse(doc, true);
            doc = reader.getNextDoc();
            counter++;
        }
       double end = System.nanoTime()/Math.pow(10,6);
       System.out.println(end-start);
        System.out.println(counter);

    }
}




//        for (int i = 0; i < singleWords.length; i++) {
//           // String word = singleWords[i];
//            String day = "14";
//            int dayInt= Integer.parseInt(day);
//            if (dayInt>0 && dayInt<10) {
//                day = "0" + day;
//            }
//            System.out.println(day);

//            if (Character.isUpperCase(word.charAt(0))) {
//                int curr = i + 1;
//                String temp = word;
//                while (curr < singleWords.length && Character.isUpperCase(singleWords[curr].charAt(0))) {
//                    temp += singleWords[curr++];
//                }
//                i = curr - 1;
//                System.out.println(temp+" "+ i);
//            }


//            porterStemmer.add(word.toCharArray(), word.length());
//            porterStemmer.stem();
//            word = porterStemmer.toString();
//            System.out.println(word);


        //String[] singleWords= doc.getText().split(" ");    check times!!!!
//        boolean bool = Character.isUpperCase('2');
//        for (int i = 0; i < singleWords.length; i++) {
//            String word = "between 3 and 4";
//                    //singleWords[i];
//            if (word.isEmpty())
//                continue;
//            // cleanWord(word);
//            char firstChar = word.charAt(0);
//            String found = "";
//            Matcher match = BETWEEN.matcher(word);
//            if (match.matches()) {
//                    terms.add(word);
//                }
//
//        }


//            //$ price
//            if (firstChar == '$') {
//                String numberInWord = word.substring(1);
//                if (isNumeric(numberInWord)) {
//                    //the word is in format $number and number>million
//                    if (((i + 1) < singleWords.length)) {
//                        Matcher match = $PRICEMB.matcher(word + " " + singleWords[i + 1]);
//                        if (match.find()) {
//                            found = match.group();
//                            terms.add(numberInWord + " M Dollars");
//                            i++;
//                        }
//                    }
//                    if (found.isEmpty()) { //didn't find the pattern or the last word in text
//                        if (Double.parseDouble(numberInWord) > Math.pow(10, 6)) { // $ over a million number
//                            double num = Double.parseDouble(numberInWord) / Math.pow(10, 6);
//                            terms.add("" + num + " M Dollars");
//                        } else { //$ less than million number
//                            double num = Double.parseDouble(numberInWord);
//                            terms.add("" + num + " Dollars");
//                        }
//                    }
//                }
//            }

//
//    @SuppressWarnings("Duplicates")
//    public static void cleanWord(String word) {
//        if (word.isEmpty())
//            return;
//        HashSet<Character> delimiters = new HashSet(Arrays.asList(new char[]{'\'', '(', '[', '{', ')', ']', '}', ',', '.', ';', '/', '\\', '-',
//                '#', '!', '?', '*', ':', '`', '|', '&', '^', '*', '@', '"'}));
//        if (delimiters.contains(word.charAt(0)))  //remove first char
//            word = word.substring(1);
//        int len = word.length() - 1;
//        if (delimiters.contains(word.charAt(len)))  //remove last char
//            word = word.substring(0, len);
//    }

    //checks if the string is a number
//    @SuppressWarnings("Duplicates")
//    public static boolean isNumeric(String strNum) {
//        if (strNum.contains(",")) {
//            strNum = strNum.replace(",", "");
//        }
//        try {
//            double d = Double.parseDouble(strNum);
//        } catch (NumberFormatException | NullPointerException nfe) {
//            return false;
//        }
//        return true;
//    }


//        Pattern p1 = Pattern.compile(" ");
//
//        long now=System.nanoTime();
//        List<String> list = new ArrayList<String>();
//            int pos = 0, end;
//            while ((end = text.indexOf(' ', pos)) >= 0) {
//                list.add(text.substring(pos, end));
//                pos = end + 1;
//         }
//        long howmuchtime= System.nanoTime()-now;
//        long now2=System.nanoTime();
//        String[] st = text.split(" ");
//        long howmuchtime2= System.nanoTime()-now2;
//        long now3=System.nanoTime();
//        String[] st2 = p1.split(text);
//        long howmuchtime3= System.nanoTime()-now3;


//
//        String all_phrases = "";
//        String [] splitted;
//        String [] splitted2;
//
//        for (Pattern p :patternNames){
//            Matcher match = p.matcher(text);
//            String found="";
//            while (match.find()) {
//                long now=System.nanoTime();
//                found= match.group();
//               long matchTime=System.nanoTime()-now;
//               long now2=System.nanoTime();
//                splitted = p.split(text);
//                long splitTime1=System.nanoTime()-now2;
//                long now3=System.nanoTime();
//                splitted2=text.split(p.pattern());
//                long splitTime2=System.nanoTime()-now3;
//
//               // long now3=System.nanoTime();
//                //text = Arrays.toString(splitted);
//                text= String.join("", splitted);
//                //long joinTime=System.nanoTime()-now3;
//                all_phrases+= found + ", ";
//            }
//
//        }




//                String w1 = "Chen,";
//                if (w1.contains(","))
//                    w1=w1.replace(",", "");
//                else if (w1.contains("."))
//                    w1=w1.replace(".", "");
//                boolean ans = w1.chars().allMatch(Character::isLetter);
//                System.out.println(ans);

//                LinkedList<String> l = new LinkedList<>();
//                File f = new File("C:\\Users\\cheng\\Desktop\\1.txt");
//                try {
//                    Scanner sc = new Scanner(f);
//                    while(sc.hasNext()) {
//                        l.add(sc.nextLine());
//                    }
//
//                    StringBuilder allText = new StringBuilder();
//                    long startTime= System.nanoTime();
//                    for (String word : l) {
//                        allText.append(word);
//                    }
//                    long timeDiff=System.nanoTime()-startTime;
//                    System.out.println("It takes " + timeDiff + " nanoseconds using StringBuilder");
//
//                    String allText2 ="";
//                    long newStartTime= System.nanoTime();
//                    for (String word : l) {
//                        allText2+=word;
//                    }
//                    long newtimeDiff=System.nanoTime()-newStartTime;
//                    System.out.println("It takes " + newtimeDiff + " nanoseconds using String");
//                } catch (FileNotFoundException e) {
//                    e.printStackTrace();
//                }
//            }

//           String strNum="1,111,999";
//            if (strNum.contains(",")){
//                strNum= strNum.replace(",","");
//            }
//            try {
//                double d = Double.parseDouble(strNum);
//            } catch (NumberFormatException | NullPointerException nfe) {
//                System.out.println(false);
//            }
//            System.out.println(true);

//        //keep only 3 digits after the point
//        String add="";
//        String strNum = "10,123,000,123";
//        if (strNum.contains(",")){
//            strNum= strNum.replace(",","");
//        }
//        double d = Double.parseDouble(strNum);
//        if (d>=1000 && d<1000000){
//            add = "K";
//            d = d/1000;
//        }
//        else if (d>=1000000 && d<100000000){
//            add = "M";
//            d = d/1000000;
//        }
//        else if (d>100000000){
//            add = "B";
//            d = d/1000000000;
//        }
//        String rounded = (new DecimalFormat("##.000")).format(d);
//        rounded += add;
//        System.out.println(rounded);



