package Model.Retrieval;
import javafx.util.Pair;
import java.util.List;

public interface Semantic {
    List<Pair<String, Double>> termWithSimilarMeaning(String term);
}
