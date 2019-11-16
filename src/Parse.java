import java.io.*;
import java.text.DecimalFormat;
import java.util.HashSet;
import java.util.Map;
import java.util.Scanner;

public class Parse {

    private File file;
    static HashSet<String> stopWords;
    private Map<String, Long> helpDictionary;
    private HashSet<String> dictionary;

    public Parse(String filePath, String stopWordsPath) {
        setFilePath(filePath);
        stopWords = readStopWords(stopWordsPath);
        setHelpDic(helpDictionary);
        dictionary= new HashSet<>();
    }

    //create a file from the current document's path
    public void setFilePath(String filePath){
        file = new File(filePath);
    }

    //read all stop words from the given file and add them into an array list
    private  HashSet<String> readStopWords(String stopWordsPath) {
        HashSet<String> temp = new HashSet<>();
        File stopWordsFile = new File(stopWordsPath);
        try {
            Scanner sc = new Scanner(stopWordsFile);
            while(sc.hasNext()){
                temp.add(sc.nextLine());
            }
        }catch (FileNotFoundException e) {
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

    private void splitFileIntoWords(){
        try {
            FileReader fr = new FileReader(file);
            BufferedReader br = new BufferedReader(fr);
            String line = "";
            String [] words = {};
            try{
                while ((line = br.readLine()) != null){
                    words = line.trim().split("[ \t&+:;=?@#|'<>^*()!]");
                    handleWords(words);
                }
            } catch (IOException e1) {
                e1.printStackTrace();
            }
        }catch (FileNotFoundException e2){
           e2.printStackTrace();
        }
    }

    private void handleWords(String[] words) {
        for (int i=0; i<words.length-1; i++){
            String term;
            String w1 = words[i];
            String w2 = words[i+1];
            if (w1 == "")
                continue;
            //numbers or price
            if (isNumeric(w1)){
                double d = Double.parseDouble(w1);
                if (isFraction(w2)){
                    term = w1+w2;
                    i++;
                    continue;
                }
                else if (helpDictionary.containsKey(w2)) {
                    d *= helpDictionary.get(w2);
                    i++;
                }
                term = handleNumbers(d);
            }
            //all letters
            else if (isAllLetters(w1)){
                char first = w1.charAt(0);
                if (Character.isUpperCase(first)) //check in all the corpus!!!!!!
                    term = w1.toUpperCase();
                else
                    term= w1;
            }
            // percent
            else if (w1.charAt(w1.length()-1)=='%')
                term = w1;
            else if (w2.equals("percent") || w2.equals("percentage")) {
                term = w1 + "%";
                i++;
            }
            //price
            else if ((w1.charAt(0)=='$' && isNumeric(w1.substring(1)) || isNumeric(w1) && w2.equals("Dollars"))){

            }


//            if(! isAStopWord(w))
//                term = w1;

        }
    }

    private boolean isAllLetters(String w1) {
        if (w1.contains(","))
            w1=w1.replace(",", "");
        else if (w1.contains("."))
            w1=w1.replace(".", "");
        return w1.chars().allMatch(Character::isLetter);
    }

    //checks if the words is a stop word
    private boolean isAStopWord(String w) {
        return stopWords.contains(w);
    }

    //handle numbers
    private String handleNumbers(double d){
        //keep only 3 digits after the point
        String add="";
        int divideIn = 1;
        if (d>=1000 && d<1000000){
            add = "K";
            divideIn = 1000;
        }
        else if (d>=1000000 && d<100000000){
            add = "M";
            divideIn = 1000000;
        }
        else if (d>100000000){
            add = "B";
            divideIn= 1000000000;
        }
        d = d/divideIn;
        String rounded = (new DecimalFormat("##.000")).format(d);
        rounded += add;
        return rounded;
    }

    //checks if the string is a number
    private boolean isNumeric(String strNum) {
        if (strNum.contains(",")){
            strNum= strNum.replace(",","");
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
            String denominatorS = w2.substring(index+1);
            try{
                int numerator = Integer.parseInt(numeratorS);
                int enominator = Integer.parseInt(denominatorS);
            }catch (NumberFormatException | NullPointerException nfe) {
                return false;
            }
            return true;
        }
        return false;
    }



}
