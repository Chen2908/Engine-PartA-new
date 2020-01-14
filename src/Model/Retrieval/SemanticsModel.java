package Model.Retrieval;

import com.medallia.word2vec.Searcher.Match;
import com.medallia.word2vec.Word2VecModel;
import javafx.util.Pair;

import java.io.File;
import java.util.ArrayList;
import java.util.List;

public class SemanticsModel implements Semantic{

    public SemanticsModel() {
    }

    public List<Pair<String, Double>> termWithSimilarMeaning(String term) {
        List<Pair<String, Double>> similarTerms = new ArrayList<>();
        Word2VecModel model;
        String filename = "Resources/word2vec.c.output.model.txt";
        File file = new File(filename);
        try {
            model = Word2VecModel.fromTextFile(file);
            com.medallia.word2vec.Searcher s = model.forSearch();
            List<Match> matches = s.getMatches(term, 4);
            for (Match match : matches) {
                String similarWord = match.match(); //matching word
                double similarityScore = match.distance();  // how close the match is to the original word
                similarTerms.add(new Pair(similarWord, similarityScore*0.5));
            }
        }catch(Exception e){
            System.out.println("Cannot get similar words");
        }
        if (similarTerms.isEmpty())
            return similarTerms;
        else
            return similarTerms.subList(1, similarTerms.size());
    }


    //test
    public static void main(String[] args) {
        SemanticsModel testSemantics = new SemanticsModel();
        double startTime = System.currentTimeMillis();
        List<Pair<String, Double>> print = testSemantics.termWithSimilarMeaning("Falkland");
        System.out.println("Falkland  : " + (System.currentTimeMillis()-startTime)/1000 + " seconds");
        System.out.println(print);

    }
}
