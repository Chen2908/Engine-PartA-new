package Model.Retrieval;

import java.io.Serializable;
import java.util.*;

import com.google.gson.JsonObject;
import com.google.gson.JsonParser;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import javafx.util.Pair;

import java.net.*;
import java.io.*;


/**
 * This class enables adding semantics to the retrieval.
 * It includes a method that returns the 5 most similar words to a given term
 */
public class SemanticsAPI implements Semantic, Serializable {

    public JsonElement jsonElem;

    /**
     * A method that receives a term and finds 5 terms which are similar in meaning to the given term using
     * https://www.datamuse.com/api
     *
     * @param term - the term to search similar words to
     * @return an ArrayList containing 5 most similar in meaning word to term
     */
    public List<Pair<String, Double>> termWithSimilarMeaning(String term) {
        List<Pair<String, Double>> similarTerms = new ArrayList<>();
        StringBuilder readTerms = new StringBuilder();
        String text;
        try {
            //create the connection to the page with term
            URL datamuse = new URL("http://api.datamuse.com/words?rd=" + term + "&max=3");
            BufferedReader in = new BufferedReader(new InputStreamReader(datamuse.openStream()));
            text = in.readLine();
            if (text != null)
                readTerms.append(text);
            in.close();

            //parser for Json array
            JsonParser parser = new JsonParser();
            jsonElem = parser.parse(readTerms.toString());
            JsonArray terms = jsonElem.getAsJsonArray();

            //retrieve only the term
            for (JsonElement s : terms) {
                JsonObject jterms = (JsonObject) (s);
                similarTerms.add(new Pair(jterms.get("word").getAsString(),(double)(jterms.get("score").getAsInt())/120000));
            }

        } catch (Exception e) {
            System.out.println("error in reading similar terms");
        }
        return similarTerms;
    }

    //test
    public static void main(String[] args) {
        SemanticsAPI testSemantics = new SemanticsAPI();
        //test.rank(null, null);
        double startTime = System.currentTimeMillis();
        List<Pair<String, Double>> print = testSemantics.termWithSimilarMeaning("petroleum");
        System.out.println("petroleum: " + (System.currentTimeMillis()-startTime)/1000 + " seconds");
        System.out.println(print);


    }
}
