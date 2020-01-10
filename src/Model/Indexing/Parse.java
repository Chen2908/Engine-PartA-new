package Model.Indexing;
import org.apache.commons.lang3.StringUtils;

import java.io.*;
import java.text.DecimalFormat;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * This class represents a parser object in the corpus processing.
 * It receives a batch of documents to parse, and deconstructs each on them into terms.
 * In creates a temporary dictionary of <String, Term> pairs of each batch and returns it.
 */
public class Parse {

    private static IStemmer porterStemmer;
    private static HashSet<String> stopWords;
    private HashMap<String, Double> helpDicNumbers;
    private HashMap<String, String> helpDicMonths;
    private String docNo;
    private Set<Character> delimiters;
    private int textLength;
    private boolean stem;
    private HashMap<String, Term> docTerms;
    private HashSet<String> myStopWords;


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
    private static Pattern PHRASEWORD = Pattern.compile("((?<!\\d)(\\w+)(\\-)(\\w+)(?!\\d))|((?<!\\d)(\\w+)(\\-)(\\w+)(\\-)(\\w+)(?!\\d))|((?<!\\d)(\\w+)(\\-)(([1-9][0-9]*)|0)(?!\\w))");
    private static Pattern PHRASENUM = Pattern.compile("((?<!\\w)(([1-9][0-9]*)|0)(\\-)(\\w+)(?!\\d))|((?<!\\w)(([1-9][0-9]*)|0)(\\-)(([1-9][1-9]*)|0)(?!\\w))|((?<!\\w)(([1-9][0-9]*)|0)(\\-)(([1-9][1-9]*)|0)(\\-)(([1-9][1-9]*)|0)(?!\\w))");
    private static Pattern BETWEEN = Pattern.compile("(\\s)(between)(\\s)(([1-9]([0-9])*)|0)(\\s)(and)(\\s)(([1-9]([0-9])*)|0)(\\s)");
    private static Pattern FRACTION = Pattern.compile("(([1-9][0-9]*)|0)(\\/)([1-9][0-9]*)");

    //new laws
    private static Pattern TIMEGMT = Pattern.compile("(\\d)(\\d)(\\d)(\\d)(\\s)(GMT)");
    private static Pattern TIMETOTIMEGMT = Pattern.compile("(\\d)(\\d)(\\d)(\\d)(\\-)(\\d)(\\d)(\\d)(\\d)(\\s)(GMT)");
    private static Pattern TIMEAM = Pattern.compile("(\\d)((a.m.)|(A.M.))");
    private static Pattern TIMEPM = Pattern.compile("(\\d)((p.m.)|(P.M.))");
    private static Pattern PHONENUM = Pattern.compile("((\\()(\\d)(\\d)(\\d)(\\))(\\s)(\\d)(\\d)(\\d)(\\-)(\\d)(\\d)(\\d)(\\d)(?!\\w))");


    //</editor-fold>

    /**
     * Constructor with parameters
     * @param stopWordsPath - the path where the stop words file is saved
     * @param stemming - true if stemming should be applied, otherwise false
     */
    public Parse(String stopWordsPath, boolean stemming) {
        docTerms = new HashMap<>();
        this.stem = stemming;
        this.stopWords = readStopWords(stopWordsPath);
        this.porterStemmer = new Stemmer();
        this.helpDicNumbers = new HashMap<>();
        setHelpDicNum();
        this.helpDicMonths = new HashMap<>();
        setHelpDicMon();
        this.delimiters = Stream.of('\'', '(', '[', '{', ')', ']', '}', ',', '.', ';', '/', '\\', '-', '\'',
                '#', '!', '?', ':', '`', '|', '&', '^', '*', '@', '+', '"', '�', '¥').collect(Collectors.toSet());
        myStopWords= new HashSet<>();
        setMyStopWords();
    }

    public HashSet<String> getStopWords() {
        return stopWords;
    }

    /**
     * parse wrapper for batch
     *
     * @param docs to parse, sends all the necessary parameters to working parse
     * @return hash map of the documents' terms
     */
    public HashMap<String, Term> parse(List<Document> docs) {
        setDocTerms();
        for (Document doc : docs) {
            parse(doc.getText(), doc.getDocNum(), doc.getDate());
        }
        return docTerms;
    }

    /**
     * @param text    - the text of the document
     * @param docNo   - the document identifier
     * @param docDate - the date of the document
     * @return hash map of the document's terms
     */
    public HashMap<String, Term> parse(String text, String docNo, String docDate) {
        this.docNo = docNo;
        String[] singleWords = StringUtils.split(text, " ;:\\\"{}\n\r\t=<>");
        this.textLength = singleWords.length;
        //go over every word in the text
        for (int i = 0; i < textLength; i++) {
            String word = singleWords[i];

            char firstChar = word.charAt(0);
            boolean found = false;
            //if the word contains / need to break it
            if (word.contains("/")) {
                found = checkFraction( word, i);
                if (found)
                    continue;
                String[] splitedUntil = StringUtils.split(word, "/");
                if (splitedUntil.length > 1) {
                    handle_splitted(splitedUntil, 0, splitedUntil.length-1,i);   //handle the words after split
                    word = splitedUntil[splitedUntil.length - 1]; //stay with the last word
                }
            }
            boolean separated = separatedWord(word);
            //the word ends with a separator, can be phone number or a term of one word
            if (separated) {
                if ((i + 1) < textLength) {
                    found = checkPhoneNum(word, singleWords[i + 1], i);
                    if (found)
                        i++;
                }
                if (!found) {
                    word = removeDeli(word);
                    if (word.isEmpty())
                        continue;
                    if (StringUtils.containsAny(word, "#?|*&{}()�¥"))
                        continue;
                    handle_1_word_term( word, i);
                    continue;
                }
            } else { //can be a term of more than one word
                if (StringUtils.containsAny(word, "#?|*&{}()�¥"))
                    continue;
                //$ price
                if (firstChar == '$') {
                    String numberInWord = word.substring(1);
                    if (isNumeric(numberInWord)[0]) {
                        //the word is in format $number and number>million
                        if (((i + 1) < textLength)) {
                            found = checkPriceMB(word, singleWords[i + 1], i);
                            if (found)
                                i++;
                        }
                    }
                }
                //entities, between/ phrases/ capital/ date
                else if (Character.isLetter(firstChar) && !word.contains("-")) {
                    //entities - 2 words and above
                    if (Character.isUpperCase(firstChar) && i + 1 < textLength) {
                        boolean finish = false;
                        int curr = i + 1;
                        String temp = word;
                        if (! StringUtils.containsAny(temp, ",.'") && capitalWord(temp)) {
                            while (!finish && curr < textLength && capitalWord(singleWords[curr])) {
                                String add = singleWords[curr];
                                finish = separatedWord(add);
                                if (finish) {
                                    add = removeDeli(add);
                                    if (add.isEmpty())
                                        continue;
                                }
                                if (StringUtils.containsAny(add, ",./")) {
                                    String[] sep = StringUtils.split(add, ".,/");
                                    add = sep[0];
                                    for (int k = 1; k < sep.length; k++) {
                                        handle_1_word_term(sep[k], curr);
                                    }
                                    finish = true;
                                }
                                if (!StringUtils.containsAny(add, "%?#|*&)(¥"))
                                    temp += " " + add;
                                else
                                    finish = true;
                                //limit the entity size
                                if (temp.length() >= 7)
                                    finish = true;
                                curr++;
                                found = true;
                            }
                            if (found) {
                                enterKey(temp, i, true);
                                //handle each word
                                if (docNo.equals("A-1")) {
                                    String[] splittedTemp = StringUtils.split(temp, " ");
                                    for (int k = 0; k < splittedTemp.length; k++) {
                                        handle_1_word_term(splittedTemp[k], i + k);
                                    }
                                }
                                i = curr - 1;
                            }
                        }
                    }
                    if (!found) {
                        //between - 4 words
                        if (word.equals("between") || word.equals("Between")) {
                            if (((i + 3) < textLength)) {
                                found = checkBetween(word, singleWords[i + 1], singleWords[i + 2], singleWords[i + 3], i);
                                if (found)
                                    i += 3;
                            }
                        }
                    }
                    if (!found) {
                        if (helpDicMonths.containsKey(word)) {   //word is a month
                            if (i + 1 < textLength) {
                                found = checkDates(word, singleWords[i + 1], i);
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
                            found = checkPhrasesTime(word, singleWords[i + 1], i);
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
                            found = checkPriceUS(price, singleWords[i + 1], singleWords[i + 2], singleWords[i + 3], i);
                            if (found)
                                i += 3;
                        }
                        // 3 words
                        if (!found && ((i + 2) < textLength)) {
                            found = checkPricebn(word, price, singleWords[i + 1], i);
                            if (found)
                                i += 2;
                        }
                        // 2 words
                        if (!found && ((i + 1) < textLength)) {
                            String lastWord = singleWords[i + 1];
                            if (separatedWord(lastWord)) {
                                lastWord = removeDeli(lastWord);
                                if (lastWord.isEmpty())
                                    continue;
                            }
                            found = handle_num_2_words(word, lastWord, i);
                            if (found)
                                i += 1;
                        }
                    }
                }
            }
            if (!found)
                handle_1_word_term(word, i);
        }
        return docTerms;
    }

    //reaches here if the original word contained / or ( or ) so each word should be handled
    private void handle_splitted(String[] splitted, int start, int end, int position) {
        for (int i = start; i < end; i++) {
            if (!StringUtils.containsAny(splitted[i], "#?|*&(){}¥"))
                handle_1_word_term(splitted[i], position);
        }
    }

    private boolean checkPriceMB(String word, String word2, int position) {
        word2=removeDeli(word2);
        word = word.replace(",", "");
        String twoWords = word + " " + word2;
        Matcher match = $PRICEMB.matcher(twoWords);
        if (match.find()) {
            double num = Double.parseDouble(word.substring(1));
            if (word2.equals("billion")) {
                num *= THOUSAND;
            }
            saveAsNumMDollars(num, position);
            return true;
        }
        return false;
    }

    private boolean checkPriceUS(String word1, String word2, String word3, String word4, int position) {
        word1 = word1.replace(",", "");
        String lastWord = word4;
        if (separatedWord(lastWord))
            lastWord = removeDeli(lastWord);
        Matcher match = PRICEUS.matcher(word1 + " " + word2 + " " + word3 + " " + lastWord);
        if (match.find()) {
            saveAsNumMDollars(Double.parseDouble(word1) * (helpDicNumbers.get(word2) / MILLION), position);
            return true;
        }
        return false;
    }

    private boolean checkPricebn(String word, String word1, String word2, int position) {
        // price bn/m dollars
        word = word.replace(",", "");
        String lastWord = word2;
        if (separatedWord(lastWord))
            lastWord = removeDeli(lastWord);
        String threeWords = word + " " + word1 + " " + lastWord;
        Matcher match = PRICEBIG$.matcher(threeWords);
        if (match.find()) {
            saveAsNumMDollars(Double.parseDouble(word) * (helpDicNumbers.get(word1) / MILLION), position);
            return true;
        } else if (Double.parseDouble(word) < MILLION) {  //price (less than million) fraction dollars
            match = PRICEFRAC.matcher(threeWords);
            if (match.find()) {

                saveAsNumDollars(word + " " + word1, position);
                return true;
            }
        }
        return false;
    }

    private boolean checkPhoneNum(String word, String word2, int position) {
        String [] splitted = StringUtils.split(word2, ",./()");
        if (splitted.length==0)
            return false;
        word2= splitted[0];
        if (splitted.length>1)
            handle_splitted(splitted, 1, splitted.length, position);

        String twoWords = word + " " + word2;
        Matcher match = PHONENUM.matcher(twoWords);
        if (match.find()) {
            if (word.charAt(0)!='(' || ((word.length() > 1 && word.charAt(0)=='(' && word.charAt(1)=='(')))
                word = word.substring(1);
            enterKey(word + " " + removeDeli(word2), position, false);
            return true;
        }
        return false;
    }

    private boolean checkBetween(String word1, String word2, String word3, String word4, int position) {
        Matcher match = BETWEEN.matcher(word1 + " " + word2 + " " + word3 + " " + word4);
        if (match.find()) {
            String lastWord = word4;
            if (separatedWord(lastWord))
                lastWord = removeDeli(lastWord);
            enterKey(word2 + "-" + lastWord, position, false);
            enterKey(word2, position + 1, false);
            enterKey(lastWord, position + 3, false);
            position += 3;
            return true;
        }
        return false;
    }


    private boolean checkPhrases(String word1, int position) {
        Matcher match2 = PHRASEWORD.matcher(word1);
        String[] splitted;
        String[] splittedBy1;
        String[] splittedBy2;

        if (match2.find()) {
            splitted = StringUtils.split(word1, "-");
            String wo1 = splitted[0];
            String wo2 = splitted[1];
            if (StringUtils.containsAny(wo1, ".,')(")) {
                    splittedBy1 = StringUtils.split(wo1, ".,'()");
                    wo1 = splittedBy1[splittedBy1.length - 1];
                    if (splittedBy1.length>1)
                        handle_splitted(splittedBy1, 0, splittedBy1.length - 1, position);
            }
             if (StringUtils.containsAny(wo2, ".,')(")) {
                 splittedBy2 = StringUtils.split(wo2, ".,'()");
                 if (splittedBy2.length>1)
                     handle_splitted(splittedBy2, 1, splittedBy2.length , position);
                 wo2 = splittedBy2[0];
             }
             enterKey(wo1+"-"+wo2, position, false);
             return true;
        }
        return false;
    }

    private boolean checkPhrasesTime(String word1, String word2, int position) {
        Matcher match = TIMETOTIMEGMT.matcher(word1 + " " + removeDeli(word2));
        if (match.find()) {
            saveAsTime(word1, position);
            return true;
        }
        return false;
    }

    private boolean checkDates(String word1, String word2, int position) {
        //date - 2 words
        String lastWord = word2;
        if (separatedWord(lastWord))
            lastWord = removeDeli(lastWord);
        String twoWords = word1 + " " + lastWord;
        //MMDD
        Matcher match1 = MONTHDD_LOWER.matcher(twoWords);
        Matcher match2 = MONTHDD_UPPER.matcher(twoWords);
        if ((match1.find() || match2.find()) && helpDicMonths.containsKey(lastWord)) {
            saveAsDateMMDD(word1, lastWord, position);
            return true;
        }
        //MMYYYY
        else {
            Matcher match3 = MONTHYEAR_LOWER.matcher(twoWords);
            Matcher match4 = MONTHYEAR_UPPER.matcher(twoWords);
            if ((match3.find() || match4.find()) && helpDicMonths.containsKey(word1)) {
                saveAsDateMMYYYY(word1, lastWord, position);
                return true;
            }
        }
        return false;
    }


    private void handle_1_word_term(String word, int position) {
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
                    saveAsNumMDollars(num, position);
                } else { //$ price less than million number
                    saveAsNumDollars(""+num, position);
                }
            }
        } else if (Character.isLetter(firstChar)) {
            if (word.contains("-")) {
                if (word.contains("--") || StringUtils.containsAny(word, "%$"))
                    return;
                checkPhrases(word, position);
                return;
            }
            //capital letter word
            //use porter stemmer to stem word
            if (!isAStopWord(word) && word.length() > 2) {
                if (StringUtils.containsAny(word, "$%"))
                    return;
                String[] wordsSeparated = {word};
                //split by comma and handle each word
                if (StringUtils.containsAny(word, ",'.")) {
                    wordsSeparated = StringUtils.split(word, ",'.");
                }
                for (String sep : wordsSeparated) {
                    if (!isAStopWord(sep)) {
                        sep = removeDeli(sep);
                        if (!sep.isEmpty()) {
                            if (stem) {
                                sep = stemmedWord(sep);
                                sep = removeDeli(sep);
                            }
                            if (sep.length() > 2)
                                checkFirstLetter(sep, position);
                        }
                    }
                }
                return;
            }
        } else if (Character.isDigit(firstChar)) {
            if (word.contains("-") && firstChar != '-') {
                if (word.contains("--")) //need to split here
                    return;
                Matcher match = PHRASENUM.matcher(word);
                if (match.find()) {
                    String[] splittedWord = word.split("-");
                    String first = splittedWord[0];
                    String second = splittedWord[1];
                    if (isNumeric(first)[0] && isNumeric(second)[0]) {  //range as num-num
                        enterKey(first, position, false);
                        enterKey(second, position, false);
                    }
                    String key = removeDeli(word);
                    if (!key.isEmpty()) {
                        enterKey(removeDeli(word), position, false);
                    }
                }
                return;
            } else {
                if (checkFraction(word, position)) {
                    return;
                }
                //num%- one word
                if (word.endsWith("%") && isNumeric(word.substring(0, word.length() - 1))[0]) {
                    String numberInWord = word.substring(0, word.length() - 1);
                    if (numberInWord.contains(","))
                        numberInWord=StringUtils.replace(numberInWord, ",", "");
                    saveAsPercent(numberInWord, position);
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
                        handleNumbers(number, position, negative);
                    }
                }
            }
        }
    }


    private String stemmedWord(String word) {
        porterStemmer.add(word.toCharArray(), word.length());
        porterStemmer.stem();
        return porterStemmer.toString();
    }

    private void checkFirstLetter(String word, int position) {
        if (word.length() > 2) {
            boolean needToUpdate = false;
            String originalKey = null;
            Term term = docTerms.get(word.toLowerCase());
            Term TERM = docTerms.get(word.toUpperCase());
            Term finalTerm = null;
            String format = word.toLowerCase();
            boolean capital = Character.isUpperCase(word.charAt(0));
            if (term == null && TERM == null) {
                if (capital)
                    format = word.toUpperCase();
            } else if (term != null) {
                format = word.toLowerCase();
                originalKey = format;
                finalTerm = term;
            } else if (capital && TERM != null) {
                format = word.toUpperCase();
                finalTerm = TERM;
            } else if (!capital && TERM != null) {
                originalKey = word.toUpperCase();
                format = word.toLowerCase();
                finalTerm = TERM;
                needToUpdate = true;
            }
            if (finalTerm != null) {  //appears in docTerms
                finalTerm.setValue(format);
                if (needToUpdate) {
                    docTerms.remove(originalKey);
                    docTerms.put(format, finalTerm);
                }
            } else {
                finalTerm = new Term(format, false);
                docTerms.put(format, finalTerm);
            }
            finalTerm.updatesDocsInfo(docNo, position);
        }
    }

    private boolean handle_num_2_words(String word1, String word2, int position) {
        if (!StringUtils.containsAny(word2, "#?|*&<>={}()�¥")) {
            //first word is a number
            boolean[] infoOnWord1 = isNumeric(word1);
            if (infoOnWord1[0]) {
                String noComma = word1;
                if (infoOnWord1[1]) {
                    noComma = StringUtils.replace(word1, ",", "");
                }
                String twoWords = noComma + " " + word2;
                return (checkPrice$(word1, noComma, twoWords, position) ||
                        checkMillionBillion(word1, word2, position) ||
                        checkPercent(word1, twoWords, position) ||
                        checkDDMM(word1, twoWords, word2, position) ||
                        checkTimePattern(word1, twoWords, position));
            }
        }
        return false;
    }

    private boolean checkPrice$(String word1, String noComma, String twoWords, int position) {
        //price dollars
        Matcher match1 = PRICE$.matcher(twoWords);
        if (match1.find()) {
            double number = Double.parseDouble(noComma);
            if (number > MILLION) { // $ over a million number
                number = number / THOUSAND;
                saveAsNumMDollars(number, position);
            } else { //$ less than million number
                saveAsNumDollars(""+number, position);
            }
            return true;
        }
        return false;
    }

    private boolean checkMillionBillion(String word1, String word2, int position) {
        if (word1.contains(","))
            word1=StringUtils.replace(word1, ",", "");
        boolean found = false;
        if (word2.equals("Million")) {
            enterKey(word1 + "M", position, false);
            found = true;
        } else if (word2.equals("Billion")) {
            enterKey(word1 + "B", position, false);
            found = true;
        } else if (word2.equals("Thousand")) {
            enterKey(word1 + "K", position, false);
            found = true;
        }
        return found;
    }

    private boolean checkPercent(String word1, String twoWords, int position) {
        //percent
        Matcher match1 = PERCENT2.matcher(twoWords);
        if (match1.find()) {
            saveAsPercent(word1, position);
            return true;
        }
        return false;
    }

    private boolean checkFraction(String word, int position) {
        Matcher match = FRACTION.matcher(word);
        if (match.find()) {
            word = removeDeli(word);
            if (!word.isEmpty()) {
                String[] numbers = word.split("/");
                if (isNumeric(numbers[0])[0] && isNumeric(numbers[1])[0]) {
                    enterKey(numbers[0] + "/" + numbers[1], position, false);
                    return true;
                }
            }
        }
        return false;
    }

    private boolean checkDDMM(String word1, String twoWords, String word2, int position) {
        //DD MM
        Matcher match1 = DDMONTH_LOWER.matcher(twoWords);
        Matcher match2 = DDMONTH_UPPER.matcher(twoWords);
        if ((match1.find() || match2.find()) && helpDicMonths.containsKey(word2) && !word1.contains(".")) {
            saveAsDateMMDD(word2, word1, position);
            return true;
        }
        return false;
    }

    private boolean checkTimePattern(String word1, String twoWords, int position) {
        boolean found = false;
        //time GMT
        Matcher match1 = TIMEGMT.matcher(twoWords);
        if (match1.find()) {
            saveAsTime(word1, position);
            found = true;
        } else {
            //time AM
            Matcher match2 = TIMEAM.matcher(twoWords);
            if (match2.find()) {
                saveAsTimeAM(word1, position);
                found = true;
            } else {
                //time PM
                Matcher match3 = TIMEPM.matcher(twoWords);
                if (match3.find()) {
                    saveAsTimePM(word1, position);
                    found = true;
                }
            }
        }
        return found;
    }


    private void enterKey(String key, int position, boolean isEntity) {
        boolean singleLetters = Character.isLetter(key.charAt(0)) && key.length() < 3;
        boolean theSame = allSameLetter(key);
        if (!singleLetters && !theSame) {
            Term term;
            if (!docTerms.containsKey(key)) {
                term = new Term(key, isEntity);
                docTerms.put(key, term);
            } else
                term = docTerms.get(key);
            term.updatesDocsInfo(docNo, position);
        }
    }

    private boolean allSameLetter(String key) {
        char first = key.charAt(0);
        if (Character.isLetter(first)) {
            for (int i = 1; i < key.length(); i++) {
                if (key.charAt(i) != first)
                    return false;
            }
            return true;
        }
        return false;
    }

    private void saveAsTimeAM(String word, int position) {
        String time = "";
        int len = word.length();
        if (len == 1) {
            time = "0" + word + ":" + "00";
        } else if (len == 2) {
            time = word + ":" + "00";
        } else {
            time = word;
        }
        enterKey(time, position, false);
    }

    private void saveAsTimePM(String word, int position) {
        if (isNumeric(word)[0]) {
            int value = Integer.parseInt(word) + AMTOPM;
            saveAsTimeAM("" + value, position);
        }
    }

    private void saveAsTime(String word, int position) {
        //0012-0024
        String time = word.substring(0, 2) + ":" + word.substring(2, 4);
        if (word.length() == 9 && word.charAt(4) == '-') {
            String time2 = word.substring(5, 7) + ":" + word.substring(7);
            time += "-" + time2;
        }
        enterKey(time, position, false);
    }

    //saving formats
    private void saveAsNumMDollars(double num, int position) {
        DecimalFormat format = new DecimalFormat("##.###");
        String key = format.format(num) + " M Dollars";
        enterKey(key, position, false);
    }

    private void saveAsNumDollars(String num, int position) {
        String key = num + " Dollars";
        enterKey(key, position, false);
    }

    private void saveAsPercent(String word, int position) {
        if (word.contains(","))
            word=StringUtils.replace(word, ",", "");
        String key = word + "%";
        enterKey(key, position, false);
    }

    private void saveAsDateMMDD(String month, String day, int position) {
        boolean[] infoOnNum = isNumeric(day);
        if (infoOnNum[0] && !infoOnNum[1]) {
            int dayInt = Integer.parseInt(day);
            if (dayInt > 0 && dayInt < 10) {
                day = "0" + day;
            }
            String monthNum = removeDeli(month);
            if (!monthNum.isEmpty()) {
                String key = helpDicMonths.get(monthNum) + "-" + day;
                enterKey(key, position, false);
            }
        }
    }

    private void saveAsDateMMYYYY(String month, String year, int position) {
        String key = year + "-" + helpDicMonths.get(month);
        enterKey(key, position, false);
    }

    /**
     * Removes all delimiters from the beginning or end of the input word
     *
     * @param word a string to clean
     */
    private String removeDeli(String word) {
        boolean startWithMinus=false;
        int first = 0;
        int last = word.length() - 1;
        while (first < last && delimiters.contains(word.charAt(first))) { //prefix
            if (word.charAt(first) == '-')
                startWithMinus = true;
            first++;
        }
        while (last > 0 && delimiters.contains(word.charAt(last)))  //suffix
            last--;
        String wordWithout= StringUtils.substring(word, first, last + 1);
        if (startWithMinus)
            wordWithout = '-'+ wordWithout;
        return wordWithout;
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
    private void handleNumbers(double number, int position, boolean negative) {
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

        enterKey("" + rounded, position, false);
    }


    //checks if the words is a stop word
    private boolean isAStopWord(String word) {
        return (stopWords.contains(word.toLowerCase()) || myStopWords.contains(word.toUpperCase()));
    }

    private boolean capitalWord(String word) {
        Boolean startWithCapital = false;
        if (word.length() > 1)
            startWithCapital = (Character.isUpperCase(word.charAt(0)) && !(Character.isUpperCase(word.charAt(1))));
        else if (word.length() == 1)
            startWithCapital = Character.isUpperCase(word.charAt(0));
        return startWithCapital;
    }


    //init hashmap for each batch
    private void setDocTerms() {
        docTerms = new HashMap<>();
    }

    //<editor-fold des="set help dictionaries"

    private void setHelpDicNum() {
        this.helpDicNumbers.put("thousand", Math.pow(10, 3));
        this.helpDicNumbers.put("million", Math.pow(10, 6));
        this.helpDicNumbers.put("billion", Math.pow(10, 9));
        this.helpDicNumbers.put("trillion", Math.pow(10, 12));
        this.helpDicNumbers.put("m", Math.pow(10, 6));
        this.helpDicNumbers.put("bn", Math.pow(10, 9));
    }

    private void setHelpDicMon() {
        this.helpDicMonths.put("JANUARY", "01");
        this.helpDicMonths.put("JAN", "01");
        this.helpDicMonths.put("January", "01");
        this.helpDicMonths.put("Jan", "01");
        this.helpDicMonths.put("FEBRUARY", "02");
        this.helpDicMonths.put("February", "02");
        this.helpDicMonths.put("FEB", "02");
        this.helpDicMonths.put("Feb", "02");
        this.helpDicMonths.put("MARCH", "03");
        this.helpDicMonths.put("March", "03");
        this.helpDicMonths.put("MAR", "03");
        this.helpDicMonths.put("Mar", "03");
        this.helpDicMonths.put("APRIL", "04");
        this.helpDicMonths.put("April", "04");
        this.helpDicMonths.put("APR", "04");
        this.helpDicMonths.put("Apr", "04");
        this.helpDicMonths.put("MAY", "05");
        this.helpDicMonths.put("May", "05");
        this.helpDicMonths.put("JUNE", "06");
        this.helpDicMonths.put("June", "06");
        this.helpDicMonths.put("JUN", "06");
        this.helpDicMonths.put("Jun", "06");
        this.helpDicMonths.put("JULY", "07");
        this.helpDicMonths.put("July", "07");
        this.helpDicMonths.put("JUL", "07");
        this.helpDicMonths.put("Jul", "07");
        this.helpDicMonths.put("AUGUST", "08");
        this.helpDicMonths.put("August", "08");
        this.helpDicMonths.put("AUG", "08");
        this.helpDicMonths.put("Aug", "08");
        this.helpDicMonths.put("SEPTEMBER", "09");
        this.helpDicMonths.put("September", "09");
        this.helpDicMonths.put("SEP", "09");
        this.helpDicMonths.put("Sep", "09");
        this.helpDicMonths.put("OCTOBER", "10");
        this.helpDicMonths.put("October", "10");
        this.helpDicMonths.put("OCT", "10");
        this.helpDicMonths.put("Oct", "10");
        this.helpDicMonths.put("NOVEMBER", "11");
        this.helpDicMonths.put("November", "11");
        this.helpDicMonths.put("NOV", "11");
        this.helpDicMonths.put("Nov", "11");
        this.helpDicMonths.put("DECEMBER", "12");
        this.helpDicMonths.put("December", "12");
        this.helpDicMonths.put("DEC", "12");
        this.helpDicMonths.put("Dec", "12");
    }

    private void setMyStopWords(){
        this.myStopWords.add("TABLERULE");
        this.myStopWords.add("TABLECELL");
        this.myStopWords.add("CELLRULE");
        this.myStopWords.add("CHJ");
        this.myStopWords.add("CVJ");
    }
    //</editor-fold>


    //<editor-fold des="read stop words"
    //read all stop words from the given file and add them into an array list
    private HashSet<String> readStopWords(String stopWordsPath) {
        String filePath = stopWordsPath + "\\stop_words.txt";
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
    //</editor-fold>"


}
