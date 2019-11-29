//import java.io.*;
//import java.text.DecimalFormat;
//import java.util.*;
//import java.util.regex.Matcher;
//import java.util.regex.Pattern;
//
//public class Parse {
//
//    private static IStemmer porterStemmer;
//    private static HashSet<String> stopWords;
//    private Map<String, Double> helpDictionary;
//    private HashSet<Character> delimiters;
//    private String docNo;
//
//    //<editor-fold des="initiate static variables">
//    private final double THOUSAND = Math.pow(10,3);
//    private final double MILLION = Math.pow(10,6);
//    private final double BILLION = Math.pow(10,9);
//    private final double TRILLION = Math.pow(10,12);
//
//    //patterns for input text
//    private static Pattern $PRICEMB = Pattern.compile("((\\$)(([1-9][0-9]*)|0)(\\s)(million|billion))");
//    private static Pattern $PRICE = Pattern.compile("(\\$)(([1-9][0-9]*)|0)");
//    private static Pattern PRICEUS = Pattern.compile("(([1-9][0-9]*)|0)(\\s)(billion|million|trillion)(\\s)(U.S.)(\\s)(dollars)");
//    private static Pattern PRICEBIG$ = Pattern.compile("(([1-9][0-9]*)|0)(\\s)(m|bn)(\\s)(Dollars)");
//    private static Pattern PRICEFRAC = Pattern.compile("(([1-9][0-9]*)|0)(\\s)([1-9][0-9]*\\/[1-9][0-9]*)(\\s)(Dollars)");
//    private static Pattern PRICE$ = Pattern.compile("(([1-9][0-9]*)|0+)(\\s)(Dollars)");
//    private static Pattern DDMONTH_LOWER = Pattern.compile("((3[0|1]|[1|2][0-9]|[0-9])(\\s)(January|February|March|April|May|June|July|August|September|October|November|December|Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec))");
//    private static Pattern DDMONTH_UPPER = Pattern.compile("((3[0|1]|[1|2][0-9]|[0-9])(\\s)(JANUARY|FEBRUARY|MARCH|APRIL|MAY|JUNE|JULY|AUGUST|SEPTEMBER|OCTOBER|NOVEMBER|DECEMBER|JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC))");
//    private static Pattern MONTHDD_LOWER = Pattern.compile("((January|February|March|April|May|June|July|August|September|October|November|December|Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec))");
//    private static Pattern MONTHDD_UPPER = Pattern.compile("((JANUARY|FEBRUARY|MARCH|APRIL|MAY|JUNE|JULY|AUGUST|SEPTEMBER|OCTOBER|NOVEMBER|DECEMBER|JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)(\\s)(3[0|1]|[1|2][0-9]|[0-9]))|(()(\\s)(3[0|1]|[1|2][0-9]|[0-9]))");
//    private static Pattern MONTHYEAR_LOWER = Pattern.compile("((January|February|March|April|May|June|July|August|September|October|November|December|Jan|Feb|Mar|Apr|May|Jun|Jul|Aug|Sep|Oct|Nov|Dec)(\\s)((1|2)(\\d)(\\d)(\\d)))");
//    private static Pattern MONTHYEAR_UPPER = Pattern.compile("((JANUARY|FEBRUARY|MARCH|APRIL|MAY|JUNE|JULY|AUGUST|SEPTEMBER|OCTOBER|NOVEMBER|DECEMBER|JAN|FEB|MAR|APR|MAY|JUN|JUL|AUG|SEP|OCT|NOV|DEC)(\\s)((1|2)(\\d)(\\d)(\\d)))");
//    private static Pattern PERCENT2 = Pattern.compile("(([1-9][0-9]*)|0)(\\s)(percent|percentage)");
//    private static Pattern PERCENT1 = Pattern.compile("(([1-9][0-9]*)|0)(\\%)");
//    private static Pattern PHRASE = Pattern.compile("(\\w)(\\-)(\\w)|(\\w)(\\-)(\\w)(\\-)(\\w)|(\\w)(\\-)(([1-9][0-9]*)|0)|(([1-9][0-9]*)|0)(\\-)(\\w)|(([1-9][0-9]*)|0)(\\-)(([1-9][1-9]*)|0)");
//    private static Pattern BETWEEN = Pattern.compile("(\\s)(between)(\\s)(([1-9]([0-9])*)|0)(\\s)(and)(\\s)(([1-9]([0-9])*)|0)(\\s)");
//    private static Pattern NUMBER = Pattern.compile("^([1-9][0-9]*)|0$");
//    private static Pattern DOUBLE_NUMBER = Pattern.compile("^(([1-9][0-9]*)|0)(\\.)(([1-9][0-9]*)|0)$");
//
//    //new laws
//    private static Pattern EMAIL = Pattern.compile("\\w+([.-]?\\w+)*@\\w+([.-]?\\w+)*(\\.\\w{2,3})+");
//    //add phone numbers, http address
//    //</editor-fold>
//
//    public Parse(String stopWordsPath) {
//        stopWords = readStopWords(stopWordsPath);
//        porterStemmer= new Stemmer();
//        setHelpDic(helpDictionary);
//        char[] deli= new char[]{'\'', '(', '[', '{', ')', ']', '}', ',', '.', ';', '/', '\\', '-',
//                '#', '!', '?', '*', ':', '`', '|', '&', '^', '*', '@', };
//        delimiters = new HashSet(Arrays.asList(deli));
//    }
//
//    //read all stop words from the given file and add them into an array list
//    private HashSet<String> readStopWords(String stopWordsPath) {
//        HashSet<String> temp = new HashSet<>();
//        File stopWordsFile = new File(stopWordsPath);
//        try {
//            Scanner sc = new Scanner(stopWordsFile);
//            while (sc.hasNext()) {
//                temp.add(sc.nextLine());
//            }
//        } catch (FileNotFoundException e) {
//            e.printStackTrace();
//        }
//        return temp;
//    }
//
//    private void setHelpDic(Map<String, Double> helpDictionary) {
//        helpDictionary.put("thousand", Math.pow(10, 3));
//        helpDictionary.put("million", Math.pow(10, 6));
//        helpDictionary.put("billion", Math.pow(10, 9));
//        helpDictionary.put("trillion", Math.pow(10, 12));
//        helpDictionary.put("m", Math.pow(10, 6));
//        helpDictionary.put("bn", Math.pow(10, 9));
//    }
//
//    /**
//     *
//     * @param doc to parse
//     * @return a hashmap containing all terms that appeared in doc
//     */
//    private HashMap<String, Term> parse(Document doc) {
//        this.docNo= doc.getDocNo();
//        HashMap<String,Term> docTerms= new HashMap<>();
//        String[] singleWords = doc.getText().split(" "); //check with string builder
//        //go over every word in the text
//        for (int i = 0; i < singleWords.length; i++) {
//            String word = singleWords[i];
//            if (word.isEmpty())
//                continue;
//            cleanWord(word);
//            char firstChar = word.charAt(0);
//            boolean found = false;
//            //$ price
//            if (firstChar == '$') {
//                String numberInWord = word.substring(1);
//                if (isNumeric(numberInWord)) {
//                    //the word is in format $number and number>million
//                    if (((i + 1) < singleWords.length)) {
//                        Matcher match = $PRICEMB.matcher(word + " " + singleWords[i + 1]);
//                        if (match.find()) {
//                            found=true;
//                            saveAsNumDollars(docTerms, numberInWord);
//                            i++;
//                        }
//                    }
//                    if (! found){ //didn't find the pattern or the last word in text
//                        if (Double.parseDouble(numberInWord) > Math.pow(10, 6)) { // $ over a million number
//                            double num = Double.parseDouble(numberInWord) / Math.pow(10, 6);
//                            saveAsNumMDollars(docTerms, num);
//                        } else { //$ less than million number
//                            double num = Double.parseDouble(numberInWord);
//                            saveAsNumDollars(docTerms, ""+num);
//                        }
//                    }
//                }
//            }
//            // price...(4,3,2) / regular number(1) / percent(2,1) / num-num(1) / date(2)
//            else if (Character.isDigit(firstChar)){
//                //phrase
//                if (word.contains("-")){
//                    Matcher match = PHRASE.matcher(word);
//                    if (match.find()) {
//                        String[] splittedWord= word.split("-");
//                        if (isNumeric(splittedWord[0]) && isNumeric(splittedWord[1])) {  //range as num-num
//                            saveWordAsIs(docTerms, splittedWord[0]);
//                            saveWordAsIs(docTerms, splittedWord[1]);
//                        }
//                        saveWordAsIs(docTerms, word ,doc.getDocNo() );
//
//                    }
//                }
//                // 4 words - price U.S
//                else if (((i + 3) < singleWords.length)){
//                    Matcher match = PRICEUS.matcher(word + " " + singleWords[i + 1]+ " " + singleWords[i + 2]+ " " + singleWords[i + 3]);
//                    if (match.find()) {
//                        saveAsNumMDollars(docTerms,Double.parseDouble(word)*(helpDictionary.get(singleWords[i + 1])/THOUSAND));
//                        i+=3;
//                    }
//                }
//                // 3 words
//                else if (((i + 2) < singleWords.length)){
//                    // price bn/m dollars
//                    String threeWords=word + " " + singleWords[i + 1]+ " " + singleWords[i + 2];
//                    Matcher match = PRICEBIG$.matcher(threeWords);
//                    if (match.find()) {
//                        saveAsNumMDollars(docTerms,Double.parseDouble(word)*(helpDictionary.get(singleWords[i + 1])/THOUSAND));
//                        i+=2;
//                    }
//                    else if (Double.parseDouble(word) <MILLION){  //price (less than million) fraction dollars
//                        match= PRICEFRAC.matcher(threeWords);
//                        if (match.find()) {
//                            saveAsNumDollars(docTerms,word+ " " + singleWords[i + 1]);
//                            i+=2;
//                        }
//                    }
//                }
//                // 2 words
//                else if (((i + 1) < singleWords.length)){
//                    handle_2_letters(word, docTerms, i);
//
//
//                }
//
//
//            }
//            //entities, between/ phrases/ capital/ date
//            else if (Character.isLetter(firstChar)){  //put before numbers
//                //entities
//
//                //between - 4 words
//                if (word.equals("between")|| word.equals("Between")){
//                    if (((i + 3) < singleWords.length)) {
//                        Matcher match = BETWEEN.matcher(word + " " + singleWords[i + 1]+ " " + singleWords[i + 2]+ " " + singleWords[i + 3]);
//                        if (match.find()) {
//                            saveWordAsIs(docTerms, singleWords[i + 1]+ "-" + singleWords[i + 3], i);
//                            saveWordAsIs(docTerms, singleWords[i + 1], i+3);
//                            saveWordAsIs(docTerms, singleWords[i + 3], i+3);
//                            i+=3;
//                        }
//                    }
//                }
//                //date - 2 words
//
//                //phrase - one word
//                else if (word.contains("-")){
//                    Matcher match = PHRASE.matcher(word);
//                    if (match.find()) {
//                        saveWordAsIs(docTerms, word, i);
//                    }
//                }
//                else{  //one word
//                    //use porter stemmer to stem word
//                    porterStemmer.add(word.toCharArray(), word.length());
//                    porterStemmer.stem();
//                    word= porterStemmer.toString();
//                    checkFirstLetter(word, docTerms, i);
//
//                }
//
//
//            }
//        }
//        return docTerms;
//    }
//
//    private void checkFirstLetter(String word, HashMap<String,Term> docTerms, int position) {
//        Term term = docTerms.get(word);
//        //if first letter is capital
//        if (Character.isUpperCase(word.charAt(0))){
//            if (term!=null) {  //appears in docTerms
//                if (term.isStartsWithCapital())
//                    term.updateDocsInfo(docNo, position);
//            }
//            else{
//                String upperCase=word.toUpperCase();
//                term=new Term (upperCase, 1);
//                docTerms.put(upperCase, term);
//            }
//        }
//        else{   //first letter isn't capital
//            if (term!=null){ //appears in docTerms
//                String lowerCase=word.toLowerCase();
//                term.setValue(lowerCase);
//
//            }
//        }
//
//    }
//
//
//    private void handle_2_letters(String word, HashMap<String, Term> docTerms) {
//    }
//
//
//    //saving formats
//    private void saveAsNumMDollars(HashMap<String, Term> docTerms, double num){
//        String key= num + " M Dollars";
//        Term term;
//        //new term
//        if (! docTerms.containsKey(key)){
//           // term = new Term(key, 1);
//           // docTerms.put(key, Term);
//        }
//        else{
//            term=docTerms.get(key);
//          //  term.increaseIdf;
//
//        }
//
//    }
//
//    private void saveAsNumDollars(HashMap<String, Term> docTerms, String num){
//        docTerms.put(num + " Dollars", this.docNo);
//    }
//
//    private void saveWordAsIs(HashMap<String, Term> docTerms,String word, String docNO){
//        docTerms.put(word, docNO);
//    }
//
//    private void saveAsPercent(HashMap<String, Term> docTerms, String word, String docNo) {
//        docTerms.put(word+"%", docNo);
//    }
//
//
//
//    /**
//     * Removes all delimiters from the beginning or end of the input word
//     * @param word a string to clean
//     */
//    private void cleanWord(String word) {
//        if (delimiters.contains(word.charAt(0)))  //remove first char
//            word = word.substring(1);
//        int len = word.length() - 1;
//        if (delimiters.contains(word.charAt(len)))  //remove last char
//            word = word.substring(0, len);
//    }
//
//    /**
//     *
//     * @param strNum
//     * @return true if strNum is a number, otherwise returns false
//     */
//    //checks if the string is a number
//    private boolean isNumeric(String strNum) {
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
//
//    /**
//     *
//     * @param d - a double number to handle
//     * @return a string in the format the number d should be saves as
//     */
//    private String handleNumbers(double d) {
//        //keep only 3 digits after the point
//        String add = "";
//        double divideIn = 1.0;
//        if (d >= 1000 && d < MILLION) {
//            add = "K";
//            divideIn = THOUSAND;
//        } else if (d >= MILLION && d < BILLION) {
//            add = "M";
//            divideIn = MILLION;
//        } else if (d > BILLION) {
//            add = "B";
//            divideIn = TRILLION;
//        }
//        d = d / divideIn;
//        String rounded = (new DecimalFormat("##.000")).format(d);
//        rounded += add;
//        return rounded;
//    }
//
//
//    /**
//     * checks if the string is a fraction of format number/number
//     * @param frac a string suspected to be a fraction
//     * @return true if frac is a fraction, otherwise returns false
//     */
//    private boolean isFraction(String frac) {
//        if (frac.contains("/")) {
//            int index = frac.indexOf("/");
//            String numeratorS = frac.substring(0, index);
//            String denominatorS = frac.substring(index + 1);
//            try {
//                int numerator = Integer.parseInt(numeratorS);
//                int enominator = Integer.parseInt(denominatorS);
//            } catch (NumberFormatException | NullPointerException nfe) {
//                return false;
//            }
//            return true;
//        }
//        return false;
//    }
//
//    private boolean isAllLetters(String w1) {
//        if (w1.contains(","))
//            w1 = w1.replace(",", "");
//        else if (w1.contains("."))
//            w1 = w1.replace(".", "");
//        return w1.chars().allMatch(Character::isLetter);
//    }
//
//    //checks if the words is a stop word
//    private boolean isAStopWord(String w) {
//        return stopWords.contains(w);
//    }
//
//
//
//
//
//}
