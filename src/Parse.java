import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

public class Parse {

    private static IStemmer porterStemmer;
    private static HashSet<String> stopWords;
    private HashMap<String, Double> helpDicNumbers;
    private HashMap<String, String> helpDicMonths;
    private String docNo;
    private Set<Character> delimiters;
    private Set<Character> smallDelimiters;
    private int textLength;
    private boolean stem;
    HashMap<String, Term> docTerms;


    //<editor-fold des="initiate static variables">
    private final double THOUSAND = Math.pow(10, 3);
    private final double MILLION = Math.pow(10, 6);
    private final double BILLION = Math.pow(10, 9);
    private final int AMTOPM = 12;

    //patterns for input text
    private static Pattern $PRICEMB = Pattern.compile("((\\$)(([1-9][0-9]*)|0)[(\\s)(\\-)](million|billion))");
    private static Pattern PRICEUS = Pattern.compile("(([1-9][0-9]*)|0)(\\s)(billion|million|trillion)(\\s)(U.S.)(\\s)(dollars)");
    private static Pattern PRICEBIG$ = Pattern.compile("(([1-9][0-9]*)|0)(\\s)(m|bn)(\\s)(Dollars)");
    private static Pattern PRICEFRAC = Pattern.compile("(([1-9][0-9]*)|0)(\\s)([1-9][0-9]*\\/[1-9][0-9]*)(\\s)(Dollars)");
    private static Pattern PRICE$ = Pattern.compile("(([1-9][0-9]*)|0+)(\\s)(Dollars)");
    private static Pattern DDMONTH_LOWER = Pattern.compile("(((3[0|1])|([1|2][0-9])|([0-9]))(\\s)(January|February|March|April|May|June|July|August|September|October|November|December|Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec))");
    private static Pattern DDMONTH_UPPER = Pattern.compile("(((3[0|1])|([1|2][0-9])|([0-9]))(\\s)(JANUARY|FEBRUARY|MARCH|APRIL|MAY|JUNE|JULY|AUGUST|SEPTEMBER|OCTOBER|NOVEMBER|DECEMBER|JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC))");
    private static Pattern MONTHDD_LOWER = Pattern.compile("((January|February|March|April|May|June|July|August|September|October|November|December|Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec))(\\s)(3[0|1]|[1|2][0-9]|[0-9])(\\s)");
    private static Pattern MONTHDD_UPPER = Pattern.compile("((JANUARY|FEBRUARY|MARCH|APRIL|MAY|JUNE|JULY|AUGUST|SEPTEMBER|OCTOBER|NOVEMBER|DECEMBER|JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC))(\\s)(3[0|1]|[1|2][0-9]|[0-9])(\\s)");
    private static Pattern MONTHYEAR_LOWER = Pattern.compile("((January|February|March|April|May|June|July|August|September|October|November|December|Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)(\\s)((1|2)(\\d)(\\d)(\\d)))");
    private static Pattern MONTHYEAR_UPPER = Pattern.compile("((JANUARY|FEBRUARY|MARCH|APRIL|MAY|JUNE|JULY|AUGUST|SEPTEMBER|OCTOBER|NOVEMBER|DECEMBER|JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)(\\s)((1|2)(\\d)(\\d)(\\d)))");
    private static Pattern PERCENT2 = Pattern.compile("(([1-9][0-9]*)|0)(\\s)(percent|percentage)");
    private static Pattern PHRASE = Pattern.compile("(\\w)(\\-)(\\w)|(\\w)(\\-)(\\w)(\\-)(\\w)|(\\w)(\\-)(([1-9][0-9]*)|0)|(([1-9][0-9]*)|0)(\\-)(\\w)|(([1-9][0-9]*)|0)(\\-)(([1-9][1-9]*)|0)");
    private static Pattern BETWEEN = Pattern.compile("(\\s)(between)(\\s)(([1-9]([0-9])*)|0)(\\s)(and)(\\s)(([1-9]([0-9])*)|0)(\\s)");
    private static Pattern FRACTION = Pattern.compile("(([1-9][0-9]*)|0)(\\/)([1-9][0-9]*)");
    //private static Pattern MARKS = Pattern.compile("\\w+[\\/\\(\\)\\[\\]]\\w+");

    //new laws
    private static Pattern EMAIL = Pattern.compile("\\w+([.-]?\\w+)*@\\w+([.-]?\\w+)*(\\.\\w{2,3})+");
    private static Pattern TIMEGMT = Pattern.compile("(\\d)(\\d)(\\d)(\\d)(\\s)(GMT)");
    private static Pattern TIMETOTIMEGMT = Pattern.compile("(\\d)(\\d)(\\d)(\\d)(\\-)(\\d)(\\d)(\\d)(\\d)(\\s)(GMT)");
    private static Pattern TIMEAM = Pattern.compile("(\\d)((a.m.)|(A.M.))");
    private static Pattern TIMEPM = Pattern.compile("(\\d)((p.m.)|(P.M.))");
    private static Pattern PHONENUM = Pattern.compile("(\\()(\\d)(\\d)(\\d)(\\))(\\s)(\\d)(\\d)(\\d)(\\-)(\\d)(\\d)(\\d)(\\d)");


    //</editor-fold>

    public Parse(String stopWordsPath, boolean stemming) {
        docTerms = new HashMap<>();
        this.stem = stemming;
        this.stopWords = readStopWords(stopWordsPath);
        this.porterStemmer = new Stemmer();
        this.helpDicNumbers = new HashMap<>();
        setHelpDicNum(this.helpDicNumbers);
        this.helpDicMonths = new HashMap<>();
        setHelpDicMon(this.helpDicMonths);
        this.delimiters = Stream.of('\'', '(', '[', '{', ')', ']', '}', ',', '.', ';', '/', '\\', '-',
                '#', '!', '?', ':', '`', '|', '&', '^', '*', '@', '+', '"').collect(Collectors.toSet());
        // this.smallDelimiters= Stream.of('\'', '(', '[', '{', ')', ']', '}',  '/', '\\','"').collect(Collectors.toSet());
    }


    /**
     * parse cover for batch
     *
     * @param docs to parse, sends all the necessary parameters to working parse
     * @return hash map of the documents' terms
     */
    public HashMap<String, Term> parse(List<Document> docs) {
        setDocTerms();
        for (Document doc : docs) {
            parse(doc.getText(), doc.getDocNo(), doc.getDate());
        }
        return docTerms;
    }

    //init hashmap for each batch
    private void setDocTerms() {
        docTerms = new HashMap<>();
    }

    /**
     * @param text    - the text of the document
     * @param docNo   - the document identifier
     * @param docDate - the date of the document
     * @return hash map of the document's terms
     */
    public HashMap<String, Term> parse(String text, String docNo, String docDate) {
        this.docNo = docNo;
        this.stem = stem;
        String[] singleWords = StringUtils.split(text, " ][=<>*:\\?|\"");
        this.textLength = singleWords.length;
        //go over every word in the text
        for (int i = 0; i < textLength; i++) {
            String word = singleWords[i];
            //wouldn't want to keep a single letter
            if(word.length()<2)
                continue;
            boolean separated = separatedWord(word);
            char firstChar = word.charAt(0);
            boolean found = false;
            //the word ends with a separator, can be phone number or a term of one word
            if (separated) {
                if ((i + 1) < textLength) {
                    found = checkPhoneNum(docTerms, word, singleWords[i + 1], i);
                    if (found)
                        i++;
                }
                if (!found) {
                    word = removeDeli(word);
                    handle_1_word_term(docTerms, word, i);
                    continue;
                }
            } else { //can be a term of more than one word
                //$ price
                if (firstChar == '$') {
                    String numberInWord = word.substring(1);
                    if (isNumeric(numberInWord)[0]) {
                        //the word is in format $number and number>million
                        if (((i + 1) < textLength)) {
                            found = checkPriceMB(docTerms, word, singleWords[i + 1], i);
                            if (found)
                                i++;
                        }
                    }
                }
                //entities, between/ phrases/ capital/ date
                else if (Character.isLetter(firstChar) && !word.contains("-")) {
                    String[] separatedWords = {word};
                    if (StringUtils.contains(word, "/\\)")) {
                        separatedWords = StringUtils.split(word, "/\\)");
                        enterKey(docTerms, separatedWords[0], i, false);
                        word = separatedWords[1];
                    }
                    //entities - 2 words and above
                    if (Character.isUpperCase(firstChar) && i + 1 < textLength) {
                        boolean finish = false;
                        int curr = i + 1;
                        boolean notEnt=false;
                        String temp = word.replaceAll("/", " ");
                        if(! temp.equals(word)){
                            String[] splittedEnt = StringUtils.split(word," ");
                            handle_splitted(splittedEnt, i);   //handle the words after split, not an entity
                            temp=splittedEnt[splittedEnt.length-1];
                            if (!Character.isUpperCase(temp.charAt(0)))
                                notEnt=true;
                        }
                        while (!notEnt && !finish && curr < textLength && capitalWord(singleWords[curr]) && !StringUtils.containsAny(singleWords[curr], "/\\)")) {
                            String add = singleWords[curr];
                            finish = separatedWord(add);
                            if (finish)
                                add = removeDeli(add);
                            temp += " " + add;
                            //limit the entity size
                            if (temp.length()>=6)
                                finish=true;
                            curr++;
                            found = true;
                        }
                        if (found) {
                            enterKey(docTerms, temp, i, true);
                            i = curr - 1;
                        }
                    }
                    if (!found) {
                        //between - 4 words
                        if (word.equals("between") || word.equals("Between")) {
                            if (((i + 3) < textLength)) {
                                found = checkBetween(docTerms, word, singleWords[i + 1], singleWords[i + 2], singleWords[i + 3], i);
                                if (found)
                                    i += 3;
                            }
                        }
                    }
                    if (!found) {
                        if (helpDicMonths.containsKey(word)) {   //word is a month
                            if (i + 1 < textLength) {
                                found = checkDates(docTerms, word, singleWords[i + 1], i);
                                if (found)
                                    i++;
                            }
                        }
                    }
                }
                // price...(4,3,2) / regular number(1) / percent(2,1) / num-num(1) / date(2)
                else if (Character.isDigit(firstChar)) {
                    if (word.contains("-")) {
                        if (word.contains("--"))  //need to split here
                            continue;
                        if ((i + 1) < textLength) {
                            found = checkPhrases(docTerms, word, singleWords[i + 1], i);
                            if (found)
                                i++;
                        }
                    }
                    boolean[] infoOnWord = isNumeric(word);
                    if (!found && infoOnWord[0]) {
                        String price = word;
                        if (infoOnWord[1]) {
                            price = StringUtils.replace(word, ",", "");
                        }
                        // 4 words - price U.S
                        if (((i + 3) < textLength)) {
                            found = checkPriceUS(docTerms, price, singleWords[i + 1], singleWords[i + 2], singleWords[i + 3], i);
                            if (found)
                                i += 3;
                        }
                        // 3 words
                        if (!found && ((i + 2) < textLength)) {
                            found = checkPricebn(docTerms, word, price, singleWords[i + 1], i);
                            if (found)
                                i += 2;
                        }
                        // 2 words
                        if (!found && ((i + 1) < textLength)) {
                            String lastWord = singleWords[i + 1];
                            if (separatedWord(lastWord))
                                lastWord = removeDeli(lastWord);
                            found = handle_num_2_words(word, lastWord, docTerms, i);
                            if (found)
                                i += 1;
                        }
                    }
                }
            }
            if (!found)
                handle_1_word_term(docTerms, word, i);
        }
        return docTerms;
    }

    private void handle_splitted(String[] splittedEnt, int position ) {
        int len= splittedEnt.length-1;
        for (int i=0; i<len; i++){
            handle_1_word_term(docTerms, splittedEnt[i], position);
        }
    }

    private boolean checkPriceMB(HashMap<String, Term> docTerms, String word, String word2, int position) {
        String twoWords = word + " " + removeDeli(word2);
        Matcher match = $PRICEMB.matcher(twoWords);
        if (match.find()) {
            saveAsNumMDollars(docTerms, Double.parseDouble(word.substring(1)), position);
            return true;
        }
        return false;
    }

    private boolean checkPriceUS(HashMap<String, Term> docTerms, String word1, String word2, String word3, String word4, int position) {
        String lastWord = word4;
        if (separatedWord(lastWord))
            lastWord = removeDeli(lastWord);
        Matcher match = PRICEUS.matcher(word1 + " " + word2 + " " + word3 + " " + lastWord);
        if (match.find()) {
            saveAsNumMDollars(docTerms, Double.parseDouble(word1) * (helpDicNumbers.get(word2) / MILLION), position);
            return true;
        }
        return false;
    }

    private boolean checkPricebn(HashMap<String, Term> docTerms, String word, String word1, String word2, int position) {
        // price bn/m dollars
        String lastWord = word2;
        if (separatedWord(lastWord))
            lastWord = removeDeli(lastWord);
        String threeWords = word1 + " " + word2 + " " + lastWord;
        Matcher match = PRICEBIG$.matcher(threeWords);
        if (match.find()) {
            saveAsNumMDollars(docTerms, Double.parseDouble(word1) * (helpDicNumbers.get(word2) / MILLION), position);
            return true;
        } else if (Double.parseDouble(word1) < MILLION) {  //price (less than million) fraction dollars
            match = PRICEFRAC.matcher(threeWords);
            if (match.find()) {
                saveAsNumDollars(docTerms, word + " " + word1, position);
                return true;
            }
        }
        return false;
    }

    private boolean checkPhoneNum(HashMap<String, Term> docTerms, String word, String word2, int position) {
        String twoWords = word + " " + word2;
        Matcher match = PHONENUM.matcher(twoWords);
        if (match.find()) {
            enterKey(docTerms, word + " " + removeDeli(word2), position, false);
            return true;
        }
        return false;
    }

    private boolean checkBetween(HashMap<String, Term> docTerms, String word1, String word2, String word3, String word4, int position) {
        Matcher match = BETWEEN.matcher(word1 + " " + word2 + " " + word3 + " " + word4);
        if (match.find()) {
            String lastWord = word4;
            if (separatedWord(lastWord))
                lastWord = removeDeli(lastWord);
            enterKey(docTerms, word2 + "-" + lastWord, position, false);
            enterKey(docTerms, word2, position + 1, false);
            enterKey(docTerms, lastWord, position + 3, false);
            position += 3;
            return true;
        }
        return false;
    }


    private boolean checkPhrases(HashMap<String, Term> docTerms, String word1, String word2, int position) {
        Matcher match = TIMETOTIMEGMT.matcher(word1 + " " + removeDeli(word2));
        if (match.find()) {
            saveAsTime(docTerms, word1, position);
            return true;
        }
        Matcher match2 = PHRASE.matcher(word1);
        if (match2.find()) {
            if (word1.contains("/"))
                word1=word1.substring(0,word1.indexOf("/"));
            enterKey(docTerms, word1, position, false);
            return true;
        }
        return false;
    }

    private boolean checkDates(HashMap<String, Term> docTerms, String word1, String word2, int position) {
        //date - 2 words
        String lastWord = word2;
        if (separatedWord(lastWord))
            lastWord = removeDeli(lastWord);
        String twoWords = word1 + " " + lastWord;
        //MMDD
        Matcher match1 = MONTHDD_LOWER.matcher(twoWords);
        Matcher match2 = MONTHDD_UPPER.matcher(twoWords);
        if ((match1.find() || match2.find()) && helpDicMonths.containsKey(lastWord)) {
            saveAsDateMMDD(docTerms, word1, lastWord, position);
            return true;
        }
        //MMYYYY
        else {
            Matcher match3 = MONTHYEAR_LOWER.matcher(twoWords);
            Matcher match4 = MONTHYEAR_UPPER.matcher(twoWords);
            if ((match3.find() || match4.find()) && helpDicMonths.containsKey(word1)) {
                saveAsDateMMYYYY(docTerms, word1, lastWord, position);
                return true;
            }
        }
        return false;
    }


    private void handle_1_word_term(HashMap<String, Term> docTerms, String word, int position) {
        //$price, number%, first capital, phrase, plain num, plain word with letters
        char firstChar = word.charAt(0);
        //$price
        if (firstChar == '$') {
            String numberInWord = word.substring(1);
            boolean[] infoOnWord = isNumeric(numberInWord);
            if (infoOnWord[0]) {
                String overMillion = numberInWord;
                //the word is in format $number
                if (infoOnWord[1]) {
                    overMillion = StringUtils.replace(numberInWord, ",", "");
                }
                double num = Double.parseDouble(overMillion);
                if (num > MILLION) { // $ price over a million number
                    num /= THOUSAND;
                    saveAsNumMDollars(docTerms, num, position);
                } else { //$ price less than million number
                    saveAsNumDollars(docTerms, numberInWord, position);
                }
            }
        } else if (Character.isLetter(firstChar) && word.length()>2) {
            //phrase - one word
            if (StringUtils.containsAny(word, "/)(\\")) {
                String[] wordsSeperated = StringUtils.split(word, "/()\\");
                for (String sep : wordsSeperated) {
                    if (!isAStopWord(sep)) {
                        if (stem) {
                            sep = stemmedWord(sep);
                            sep = removeDeli(sep);
                        }
                        if (sep.length() > 2)
                            enterKey(docTerms, sep, position, false);
                    }
                }
                return;
            }
            if (word.contains("-")) {
                if(word.contains("--")) //need to split here
                    return;
                Matcher match = PHRASE.matcher(word);
                if (match.find()) {
                    enterKey(docTerms, word, position, false);
                    return;
                }
            }
            if (word.contains(".")) {
                String noDots = StringUtils.replace(word, ".", "");
                if (!isAStopWord(noDots)) {
                    if (stem) {
                        noDots = stemmedWord(noDots);
                    }
                    if (noDots.length() > 1)
                        enterKey(docTerms, noDots, position, false);
                }
                return;
            }
            if (word.contains("@")) {
                Matcher match = EMAIL.matcher(word);
                if (match.find()) {
                    enterKey(docTerms, word, position, false);
                    return;
                }
            }
            //capital letter word
            //use porter stemmer to stem word
            if (!isAStopWord(word) && word.length()>2) {
                if (stem) {
                    word = stemmedWord(word);
                    word = removeDeli(word);
                }
                checkFirstLetter(word, docTerms, position);
//                String[] words={word};
//                if (StringUtils.containsAny(word,delimiters.toString())) {
//                    words= separatedByDelimiters(word);
//                }
//                for(String word1:words) {
//                    if (word1.length() > 1) {
//                        if (stem) {
//                            word1 = stemmedWord(word1);
//                            word1 = removeDeli(word1);
//                        }
//                        checkFirstLetter(word1, docTerms, position);
//                    }
//                }
            }
        } else if (Character.isDigit(firstChar)) {
            if (word.contains("-") && firstChar != '-') {
                Matcher match = PHRASE.matcher(word);
                if (match.find()) {
                    String[] splittedWord = word.split("-");
                    String first = splittedWord[0];
                    String second = splittedWord[1];
                    if (isNumeric(first)[0] && isNumeric(second)[0]) {  //range as num-num
                        enterKey(docTerms, first, position, false);
                        enterKey(docTerms, second, position, false);
                    }
                    enterKey(docTerms, removeDeli(word), position, false);
                    return;
                }
            } else {
                Matcher match = FRACTION.matcher(word);
                if (match.find()) {
                    String[] numbers = word.split("/");
                    if (isNumeric(numbers[1])[0]) {
                        enterKey(docTerms, numbers[0] + "_" + numbers[1], position, false);
                        return;
                    }
                }
                //num%- one word
                if (word.endsWith("%") && isNumeric(word.substring(0, word.length() - 1))[0]) {
                    String numberInWord = word.substring(0, word.length() - 1);
                    saveAsPercent(docTerms, numberInWord, position);
                } else {
                    boolean[] infoOnWord = isNumeric(word);
                    boolean negative = false;
                    if (infoOnWord[0]) {
                        //plain number
                        if (infoOnWord[1])
                            word = StringUtils.replace(word, ",", "");
                        if (firstChar == '-') {
                            negative = true;
                            word = word.substring(1);
                        }
                        double number = Double.parseDouble(word);
                        handleNumbers(docTerms, number, position, negative);
                    }
                }
            }
        }
    }


    private String[] separatedByDelimiters(String word) {
        String[] seperated = StringUtils.split(word, "/)(\\");
        return seperated;
    }

    private String stemmedWord(String word) {
        porterStemmer.add(word.toCharArray(), word.length());
        porterStemmer.stem();
        return porterStemmer.toString();
    }

    private void checkFirstLetter(String word, HashMap<String, Term> docTerms, int position) {
        if (word.length()>2) {
            Term term = docTerms.get(word);
            String format;
            boolean capital = Character.isUpperCase(word.charAt(0));
            //if first letter is capital and it appeared already with capital letter- all caps
            if (capital && (term == null || term.isStartsWithCapital())) {
                format = word.toUpperCase();
            } else {
                format = word.toLowerCase();
            }
            if (term != null) {  //appears in docTerms
                term.setValue(format);
            } else {
                term = new Term(format, false);
                docTerms.put(format, term);
            }
            term.updatesDocsInfo(docNo, position);
        }
    }


    private boolean handle_num_2_words(String word1, String word2, HashMap<String, Term> docTerms, int position) {
        //first word is a number
        boolean[] infoOnWord1 = isNumeric(word1);
        if (infoOnWord1[0]) {
            String noComma = word1;
            if (infoOnWord1[1]) {
                noComma = StringUtils.replace(word1, ",", "");
            }
            String twoWords = noComma + " " + word2;
            return (checkPrice$(docTerms, word1, noComma, twoWords, position) ||
                    checkMillionBillion(docTerms, word1, word2, position) ||
                    checkPercent(docTerms, word1, twoWords, position) ||
                    checkDDMM(docTerms, word1, twoWords, word2, position) ||
                    checkTimePattern(docTerms, word1, twoWords, position));
        }
        return false;
    }

    private boolean checkPrice$(HashMap<String, Term> docTerms, String word1, String noComma, String twoWords, int position) {
        //price dollars
        Matcher match1 = PRICE$.matcher(twoWords);
        if (match1.find()) {
            double number = Double.parseDouble(noComma);
            if (number > MILLION) { // $ over a million number
                number = number / THOUSAND;
                saveAsNumMDollars(docTerms, number, position);
            } else { //$ less than million number
                saveAsNumDollars(docTerms, word1, position);
            }
            return true;
        }
        return false;
    }

    private boolean checkMillionBillion(HashMap<String, Term> docTerms, String word1, String word2, int position) {
        boolean found = false;
        if (word2.equals("Million")) {
            enterKey(docTerms, word1 + "M", position, false);
            found = true;
        } else if (word2.equals("Billion")) {
            enterKey(docTerms, word1 + "B", position, false);
            found = true;
        } else if (word2.equals("Thousand")) {
            enterKey(docTerms, word1 + "K", position, false);
            found = true;
        }
        return found;
    }

    private boolean checkPercent(HashMap<String, Term> docTerms, String word1, String twoWords, int position) {
        //percent
        Matcher match1 = PERCENT2.matcher(twoWords);
        if (match1.find()) {
            saveAsPercent(docTerms, word1, position);
            return true;
        }
        return false;
    }

    private boolean checkDDMM(HashMap<String, Term> docTerms, String word1, String twoWords, String word2, int position) {
        //DD MM
        Matcher match1 = DDMONTH_LOWER.matcher(twoWords);
        Matcher match2 = DDMONTH_UPPER.matcher(twoWords);
        if ((match1.find() || match2.find()) && helpDicMonths.containsKey(word2) && !word1.contains(".")) {
            saveAsDateMMDD(docTerms, word2, word1, position);
            return true;
        }
        return false;
    }

    private boolean checkTimePattern(HashMap<String, Term> docTerms, String word1, String twoWords, int position) {
        boolean found = false;
        //time GMT
        Matcher match1 = TIMEGMT.matcher(twoWords);
        if (match1.find()) {
            saveAsTime(docTerms, word1, position);
            found = true;
        } else {
            //time AM
            Matcher match2 = TIMEAM.matcher(twoWords);
            if (match2.find()) {
                saveAsTimeAM(docTerms, word1, position);
                found = true;
            } else {
                //time PM
                Matcher match3 = TIMEPM.matcher(twoWords);
                if (match3.find()) {
                    saveAsTimePM(docTerms, word1, position);
                    found = true;
                }
            }
        }
        return found;
    }


    private void enterKey(HashMap<String, Term> docTerms, String key, int position, boolean isEntity) {
        boolean singleLetters= Character.isLetter(key.charAt(0)) && key.length()<3;
        if (!singleLetters) {
            Term term;
            if (!docTerms.containsKey(key)) {
                term = new Term(key, isEntity);
                docTerms.put(key, term);
            } else
                term = docTerms.get(key);
            term.updatesDocsInfo(docNo, position);
        }
    }

    private void saveAsTimeAM(HashMap<String, Term> docTerms, String word, int position) {
        String time = "";
        int len = word.length();
        if (len == 1) {
            time = "0" + word + ":" + "00";
        } else if (len == 2) {
            time = word + ":" + "00";
        } else {
            time = word;
        }
        enterKey(docTerms, time, position, false);
    }

    private void saveAsTimePM(HashMap<String, Term> docTerms, String word, int position) {
        if (isNumeric(word)[0]) {
            int value = Integer.parseInt(word) + AMTOPM;
            saveAsTimeAM(docTerms, "" + value, position);
        }
    }

    private void saveAsTime(HashMap<String, Term> docTerms, String word, int position) {
        //0012-0024
        String time = word.substring(0, 2) + ";" + word.substring(2, 4);
        if (word.length() == 9 && word.charAt(4) == '-') {
            String time2 = word.substring(5, 7) + ";" + word.substring(7);
            time += "-" + time2;
        }
        enterKey(docTerms, time, position, false);
    }

    //saving formats
    private void saveAsNumMDollars(HashMap<String, Term> docTerms, double num, int position) {
        DecimalFormat format = new DecimalFormat("0.###");
        String key = format.format(num) + " M Dollars";
        enterKey(docTerms, key, position, false);
    }

    private void saveAsNumDollars(HashMap<String, Term> docTerms, String num, int position) {
        String key = num + " Dollars";
        enterKey(docTerms, key, position, false);
    }

    private void saveAsPercent(HashMap<String, Term> docTerms, String word, int position) {
        String key = word + "%";
        enterKey(docTerms, key, position, false);
    }

    private void saveAsDateMMDD(HashMap<String, Term> docTerms, String month, String day, int position) {
        boolean[] infoOnNum = isNumeric(day);
        if (infoOnNum[0] && !infoOnNum[1]) {
            int dayInt = Integer.parseInt(day);
            if (dayInt > 0 && dayInt < 10) {
                day = "0" + day;
            }
            String monthNum = removeDeli(month);
            String key = helpDicMonths.get(monthNum) + "-" + day;
            enterKey(docTerms, key, position, false);
        }
    }

    private void saveAsDateMMYYYY(HashMap<String, Term> docTerms, String month, String year, int position) {
        String key = year + "-" + helpDicMonths.get(month);
        enterKey(docTerms, key, position, false);
    }

    /**
     * Removes all delimiters from the beginning or end of the input word
     *
     * @param word a string to clean
     */
    private String removeDeli(String word) {
        int first = 0;
        int last = word.length() - 1;
        while (first > last && delimiters.contains(word.charAt(first)))  //prefix
            if (word.charAt(first) != '-')
                first++;
        while (last > 0 && delimiters.contains(word.charAt(last)))  //suffix
            last--;
        return StringUtils.substring(word, first, last + 1);
    }

    private boolean separatedWord(String word) {
        return delimiters.contains(word.charAt(word.length() - 1));
    }

    /**
     * @param strNum
     * @return true if strNum is a number, otherwise returns false
     */
    //checks if the string is a number
    private boolean[] isNumeric(String strNum) {
        boolean[] result = {true, false};  //first index: is in a number, second index: whether in contains a comma
        if (strNum.isEmpty()) {
            result[0] = false;
            return result;
        }
        boolean negative = false;

        if (StringUtils.contains(strNum, ",")) {
            strNum = StringUtils.replace(strNum, ",", "");
            result[1] = true;
        }
        if (strNum.charAt(0) == '-') {
            strNum = strNum.substring(1);
            negative = true;
        }
        try {
            double dNum = Double.parseDouble(strNum);
        } catch (NumberFormatException | NullPointerException nfe) {
            result[0] = false;
        }
        return result;
    }

    /**
     * @param number - a double number to handle
     * @return a string in the format the number d should be saves as
     */
    private void handleNumbers(HashMap<String, Term> docTerms, double number, int position, boolean negative) {
        //keep only 3 digits after the point
        String add = "";
        double divideIn = 1.0;
        if (negative)
            number *= -1;
        if (number >= THOUSAND && number < MILLION) {
            add = "K";
            divideIn = THOUSAND;
        } else if (number >= MILLION && number < BILLION) {
            add = "M";
            divideIn = MILLION;
        } else if (number > BILLION) {
            add = "B";
            divideIn = BILLION;
        }
        number = number / divideIn;
        String rounded = (new DecimalFormat("##.###")).format(number) + add;

        enterKey(docTerms, "" + rounded, position, false);
    }


    //checks if the words is a stop word
    private boolean isAStopWord(String word) {
        return stopWords.contains(word.toLowerCase());
    }

    private boolean capitalWord(String word) {
        Boolean startWithCapital = false;
        if (word.length() > 1)
            startWithCapital = (Character.isUpperCase(word.charAt(0)) && !(Character.isUpperCase(word.charAt(1))));
        return startWithCapital;
    }

    //<editor-fold des="set help dictionaries"
    private void setHelpDicNum(HashMap<String, Double> helpDictionary) {
        helpDictionary.put("thousand", Math.pow(10, 3));
        helpDictionary.put("million", Math.pow(10, 6));
        helpDictionary.put("billion", Math.pow(10, 9));
        helpDictionary.put("trillion", Math.pow(10, 12));
        helpDictionary.put("m", Math.pow(10, 6));
        helpDictionary.put("bn", Math.pow(10, 9));
    }

    private void setHelpDicMon(HashMap<String, String> helpDictionary) {
        helpDictionary.put("JANUARY", "01");
        helpDictionary.put("JAN", "01");
        helpDictionary.put("January", "01");
        helpDictionary.put("Jan", "01");
        helpDictionary.put("FEBRUARY", "02");
        helpDictionary.put("February", "02");
        helpDictionary.put("FEB", "02");
        helpDictionary.put("Feb", "02");
        helpDictionary.put("MARCH", "03");
        helpDictionary.put("March", "03");
        helpDictionary.put("MAR", "03");
        helpDictionary.put("Mar", "03");
        helpDictionary.put("APRIL", "04");
        helpDictionary.put("April", "04");
        helpDictionary.put("APR", "04");
        helpDictionary.put("Apr", "04");
        helpDictionary.put("MAY", "05");
        helpDictionary.put("May", "05");
        helpDictionary.put("JUNE", "06");
        helpDictionary.put("June", "06");
        helpDictionary.put("JUN", "06");
        helpDictionary.put("Jun", "06");
        helpDictionary.put("JULY", "07");
        helpDictionary.put("July", "07");
        helpDictionary.put("JUL", "07");
        helpDictionary.put("Jul", "07");
        helpDictionary.put("AUGUST", "08");
        helpDictionary.put("August", "08");
        helpDictionary.put("AUG", "08");
        helpDictionary.put("Aug", "08");
        helpDictionary.put("SEPTEMBER", "09");
        helpDictionary.put("September", "09");
        helpDictionary.put("SEP", "09");
        helpDictionary.put("Sep", "09");
        helpDictionary.put("OCTOBER", "10");
        helpDictionary.put("October", "10");
        helpDictionary.put("OCT", "10");
        helpDictionary.put("Oct", "10");
        helpDictionary.put("NOVEMBER", "11");
        helpDictionary.put("November", "11");
        helpDictionary.put("NOV", "11");
        helpDictionary.put("Nov", "11");
        helpDictionary.put("DECEMBER", "12");
        helpDictionary.put("December", "12");
        helpDictionary.put("DEC", "12");
        helpDictionary.put("Dec", "12");
    }
    //</editor-fold>

    //read all stop words from the given file and add them into an array list
    private HashSet<String> readStopWords(String stopWordsPath) {
        String filePath= stopWordsPath+"\\stop words.txt";
        HashSet<String> temp = new HashSet<String>();
        File stopWordsFile = new File(filePath);
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

}
