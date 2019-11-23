import com.sun.deploy.util.ArrayUtil;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class Parse {

    static HashSet<String> stopWords;
    private Map<String, Long> helpDictionary;
    private HashSet<Character> delimiters;
    TreeMap<String, String> terms;


    //patterns for input text
    private static Pattern $PRICEMB = Pattern.compile("((\\$)(([1-9][0-9]*)|0)(\\s)(million|billion))");
    private static Pattern $PRICE = Pattern.compile("(\\$)(([1-9][0-9]*)|0)");
    private static Pattern PRICEUS = Pattern.compile("(([1-9][0-9]*)|0)(\\s)(billion|million|trillion)(\\s)(U.S.)(\\s)(dollars)");
    private static Pattern PRICEBIG$ = Pattern.compile("(([1-9][0-9]*)|0)(\\s)(m|bn)(\\s)(Dollars)");
    private static Pattern PRICEFRAC = Pattern.compile("(([1-9][0-9]*)|0)(\\s)([1-9][0-9]*\\/[1-9][0-9]*)(\\s)(Dollars)");
    private static Pattern PRICE$ = Pattern.compile("(([1-9][0-9]*)|0+)(\\s)(Dollars)");
    private static Pattern DDMONTH_LOWER = Pattern.compile("((3[0|1]|[1|2][0-9]|[0-9])(\\s)(January|February|March|April|May|June|July|August|September|October|November|December|Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec))");
    private static Pattern DDMONTH_UPPER = Pattern.compile("((3[0|1]|[1|2][0-9]|[0-9])(\\s)(JANUARY|FEBRUARY|MARCH|APRIL|MAY|JUNE|JULY|AUGUST|SEPTEMBER|OCTOBER|NOVEMBER|DECEMBER|JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC))");
    private static Pattern MONTHDD_LOWER = Pattern.compile("((January|February|March|April|May|June|July|August|September|October|November|December|Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec))");
    private static Pattern MONTHDD_UPPER = Pattern.compile("((JANUARY|FEBRUARY|MARCH|APRIL|MAY|JUNE|JULY|AUGUST|SEPTEMBER|OCTOBER|NOVEMBER|DECEMBER|JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)(\\s)(3[0|1]|[1|2][0-9]|[0-9]))|(()(\\s)(3[0|1]|[1|2][0-9]|[0-9]))");
    private static Pattern MONTHYEAR_LOWER = Pattern.compile("((January|February|March|April|May|June|July|August|September|October|November|December|Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)(\\s)((1|2)(\\d)(\\d)(\\d)))");
    private static Pattern MONTHYEAR_UPPER = Pattern.compile("((JANUARY|FEBRUARY|MARCH|APRIL|MAY|JUNE|JULY|AUGUST|SEPTEMBER|OCTOBER|NOVEMBER|DECEMBER|JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)(\\s)((1|2)(\\d)(\\d)(\\d)))");
    private static Pattern PERCENT = Pattern.compile("(([1-9][0-9]*)|0)(\\%)|(([1-9][0-9]*)0)(\\s)(percent|percentage)");
    private static Pattern PHRASE = Pattern.compile("(\\w)(\\-)(\\w)|(\\w)(\\-)(\\w)(\\-)(\\w)|(\\w)(\\-)(([1-9][0-9]*)|0)|(([1-9][0-9]*)|0)(\\-)(\\w)|(([1-9][0-9]*)|0)(\\-)(([1-9][1-9]*)|0)");
    private static Pattern BETWEEN = Pattern.compile("(\\s)(between)(\\s)(([1-9]([0-9])*)|0)(\\s)(and)(\\s)(([1-9]([0-9])*)|0)(\\s)");
    private static Pattern NUMBER = Pattern.compile("^([1-9][0-9]*)|0$");
    private static Pattern DOUBLE_NUMBER = Pattern.compile("^(([1-9][0-9]*)|0)(\\.)(([1-9][0-9]*)|0)$");
    //new law
    private static Pattern EMAIL = Pattern.compile("\\w+([.-]?\\w+)*@\\w+([.-]?\\w+)*(\\.\\w{2,3})+");

    public Parse( String stopWordsPath) {
        stopWords = readStopWords(stopWordsPath);
        terms= new TreeMap<>();
        setHelpDic(helpDictionary);
        delimiters = new HashSet(Arrays.asList(new char[]{'\'', '(', '[', '{', ')', ']', '}', ',', '.', ';', '/', '\\', '-',
                '#', '!', '?', '*', ':', '`', '|', '&', '^', '*', '@', '"'}));
    }

    //read all stop words from the given file and add them into an array list
    private HashSet<String> readStopWords(String stopWordsPath) {
        HashSet<String> temp = new HashSet<>();
        File stopWordsFile = new File(stopWordsPath);
        try {
            Scanner sc = new Scanner(stopWordsFile);
            while (sc.hasNext()) {
                temp.add(sc.nextLine());
            }
        } catch (FileNotFoundException e) {
            e.printStackTrace();
        }
        return temp;
    }

    private void setHelpDic(Map<String, Long> helpDictionary) {
        helpDictionary.put("Thousand", 1000L);
        helpDictionary.put("Million", 1000000L);
        helpDictionary.put("Billion", 1000000000L);
        helpDictionary.put("Trillion", 1000000000000L);
    }

    private HashSet<String> parse(Document doc) {
        HashSet<String> docTerms= new HashSet<>();
        Pattern delimitersPattern = Pattern.compile("[ \\t\\n]");
        String[] singleWords = delimitersPattern.split(doc.getText());
        //String[] singleWords= doc.getText().split(" ");    check times!!!!
        for (int i = 0; i < singleWords.length; i++) {
            String word = singleWords[i];
            cleanWord(word);
            char firstChar = word.charAt(0);
            String found = "";
            //$ price
            if (firstChar == '$') {
                String numberInWord = word.substring(1);
                if (isNumeric(numberInWord)) {
                    //the word is in format $number and number>million
                    if (((i + 1) < singleWords.length)) {
                        Matcher match = $PRICEMB.matcher(word + " " + singleWords[i + 1]);
                        if (match.find()) {
                            found = match.group();
                            terms.put(numberInWord + " M Dollars", doc.getDocNo());
                            docTerms.add(numberInWord + " M Dollars");
                            i++;
                        }
                    }
                    if (found.isEmpty()){ //didn't find the pattern or the last word in text
                        if (Double.parseDouble(numberInWord) > Math.pow(10, 6)) { // $ over a million number
                            double num = Double.parseDouble(numberInWord) / Math.pow(10, 6);
                            docTerms.add("" + num + " M Dollars");
                            terms.put("" + num + " M Dollars", doc.getDocNo());
                        } else { //$ less than million number
                            double num = Double.parseDouble(numberInWord);
                            docTerms.add("" + num + " Dollars");
                            terms.put("" + num + " Dollars", doc.getDocNo());
                        }
                    }
                }
            }
            // price... / regular number / percent / num-num / date / phrase
            else if (Character.isDigit(firstChar)){
                //phrase
                if (word.contains("-")){
                    Matcher match = PHRASE.matcher(word);
                    if (match.find()) {
                        found = match.group();
                        String[] splittedWord= word.split("-");
                        if (isNumeric(splittedWord[1])) {
                            terms.put(splittedWord[0], doc.getDocNo());
                            terms.put(splittedWord[1], doc.getDocNo());
                            docTerms.add(splittedWord[0]);
                            docTerms.add(splittedWord[1]);
                        }
                        terms.put(word, doc.getDocNo());
                        docTerms.add(singleWords[i]);
                    }
                }
            }
            //entities, between/ phrases/ capital/ date
            else if (Character.isLetter(firstChar)){
                //entities

                //between
                if (word.equals("between")||word.equals("Between")){
                    if (((i + 3) < singleWords.length)) {
                        Matcher match = BETWEEN.matcher(word + " " + singleWords[i + 1]+ " " + singleWords[i + 2]+ " " + singleWords[i + 3]);
                        if (match.find()) {
                            found = match.group();
                            terms.put(singleWords[i + 1] + "-" + singleWords[i + 3], doc.getDocNo());
                            docTerms.add(singleWords[i + 1] + "-" + singleWords[i + 3]);
                            terms.put(singleWords[i + 1], doc.getDocNo());
                            docTerms.add(singleWords[i + 1]);
                            terms.put(singleWords[i + 3], doc.getDocNo());
                            docTerms.add(singleWords[i + 3]);
                            i+=3;
                        }
                    }
                }
                //phrase
                if (word.contains("-")){
                    Matcher match = PHRASE.matcher(word);
                    if (match.find()) {
                        found = match.group();
                        terms.put(word, doc.getDocNo());
                        docTerms.add(singleWords[i]);
                    }
                }
                //date

            }
        }

        return docTerms;

    }

    private void cleanWord(String word) {
        if (delimiters.contains(word.charAt(0)))  //remove first char
            word = word.substring(1);
        int len = word.length() - 1;
        if (delimiters.contains(word.charAt(len)))  //remove last char
            word = word.substring(0, len);
    }

    //handle numbers
    private String handleNumbers(double d) {
        //keep only 3 digits after the point
        String add = "";
        int divideIn = 1;
        if (d >= 1000 && d < 1000000) {
            add = "K";
            divideIn = 1000;
        } else if (d >= 1000000 && d < 100000000) {
            add = "M";
            divideIn = 1000000;
        } else if (d > 100000000) {
            add = "B";
            divideIn = 1000000000;
        }
        d = d / divideIn;
        String rounded = (new DecimalFormat("##.000")).format(d);
        rounded += add;
        return rounded;
    }

    //checks if the string is a number
    private boolean isNumeric(String strNum) {
        if (strNum.contains(",")) {
            strNum = strNum.replace(",", "");
        }
        try {
            double d = Double.parseDouble(strNum);
        } catch (NumberFormatException | NullPointerException nfe) {
            return false;
        }
        return true;
    }

    //checks if the string is a fraction of format number/number
    private boolean isFraction(String w2) {
        if (w2.contains("/")) {
            int index = w2.indexOf("/");
            String numeratorS = w2.substring(0, index);
            String denominatorS = w2.substring(index + 1);
            try {
                int numerator = Integer.parseInt(numeratorS);
                int enominator = Integer.parseInt(denominatorS);
            } catch (NumberFormatException | NullPointerException nfe) {
                return false;
            }
            return true;
        }
        return false;
    }

    private boolean isAllLetters(String w1) {
        if (w1.contains(","))
            w1 = w1.replace(",", "");
        else if (w1.contains("."))
            w1 = w1.replace(".", "");
        return w1.chars().allMatch(Character::isLetter);
    }

    //checks if the words is a stop word
    private boolean isAStopWord(String w) {
        return stopWords.contains(w);
    }

    public Pattern getPRICEFRAC() {
        return PRICEFRAC;
    }

    public void setPRICEFRAC(Pattern PRICEFRAC) {
        this.PRICEFRAC = PRICEFRAC;
    }

    private Pattern getPRICEBIG$() {
        return PRICEBIG$;
    }

    private void setPRICEBIG$(Pattern PRICEBIG$) {
        this.PRICEBIG$ = PRICEBIG$;
    }

//    private void handleWords(String allText) {
//            String term;
//            String[] words = allText.split("[ \t&+:;=?@#|'<>^*()!]");
//            int i=0;
//            String w1 = words[i];
//            String w2 = words[i+1];
//            if (w1 == "")
//                continue;
//            //numbers or price
//            if (isNumeric(w1)){
//                double d = Double.parseDouble(w1);
//                if (isFraction(w2)){
//                    term = w1+w2;
//                    i++;
//                    continue;
//                }
//                else if (helpDictionary.containsKey(w2)) {
//                    d *= helpDictionary.get(w2);
//                    i++;
//                }
//                term = handleNumbers(d);
//            }
//            //all letters
//            else if (isAllLetters(w1)){
//                char first = w1.charAt(0);
//                if (Character.isUpperCase(first)) //check in all the corpus!!!!!!
//                    term = w1.toUpperCase();
//                else
//                    term= w1;
//            }
//            // percent
//            else if (w1.charAt(w1.length()-1)=='%')
//                term = w1;
//            else if (w2.equals("percent") || w2.equals("percentage")) {
//                term = w1 + "%";
//                i++;
//            }
//            //price
//            else if ((w1.charAt(0)=='$' && isNumeric(w1.substring(1)) || isNumeric(w1) && w2.equals("Dollars"))){
//
//            }


//            if(! isAStopWord(w))
//                term = w1;


}
