package Model.Indexing;

import org.apache.commons.lang3.Pair;
import org.apache.commons.lang3.StringUtils;

import java.util.ArrayList;
import java.util.List;

public class DocCorpusInfo implements IWritable {

    //<editor-fold des="Fields">

    private static final int POW = 2;
    private int maxTf; // max term frequency
    private int numOfTerms;//the amount of term in the document
    private int numOfUniqTerms; // number of terms that appears one time in the doc
    private double sumOfTermsSquare; // the doc vector size square
    private List<Pair<String, Integer>> mostFreqEntities;
    private int numOfEntities;
    private final String entDel = ",";
    private final String del = ";";

    //</editor-fold>

    //<editor-fold des="Constructors">

    /**
     * Constructor
     */
    public DocCorpusInfo(){
        this.maxTf = 0;
        this.numOfUniqTerms = 0;
        this.sumOfTermsSquare = 0;
        this.numOfTerms = 0;
        this.numOfEntities = 0;
        this.mostFreqEntities = new ArrayList<>(5);
    }

    public DocCorpusInfo(String docInfo){
        this.mostFreqEntities = new ArrayList<>(5);
        String[] splitInfo = StringUtils.split(docInfo, del);
        this.maxTf = Integer.parseInt(splitInfo[0]);
        this.numOfTerms = Integer.parseInt(splitInfo[1]);
        this.numOfUniqTerms = Integer.parseInt(splitInfo[2]);
        this.sumOfTermsSquare = Double.parseDouble(splitInfo[3]);
        setEntitiesFromString(splitInfo[4]);
    }

    private void setEntitiesFromString(String entities){
        if (entities.length() < 1)
            return;
        String[] splitEnt = StringUtils.split(entities, entDel);
        for (int i = 0; i < splitEnt.length; i += 2)
            this.mostFreqEntities.add(new Pair(splitEnt[i], Integer.parseInt(splitEnt[i+1])));
    }

    //</editor-fold>

    //<editor-fold des="Setters">

    /**
     * This function get term frequency and update the objects fields:
     * max term frequency, num of uniq terms, file vector size (sigma tf square)
     * @param term - term
     */
    public void updateDoc(String docNum, Term term){
        setMaxTf(term.getDocTfi(docNum));
        increaseUniqTerms();
        addToVector(term.getDocTfi(docNum));
        this.numOfTerms += term.getDocTfi(docNum);
        if (term.isEntity())
            addEntity(docNum, term);
    }

    private void addEntity(String docNum, Term term){
        if (this.mostFreqEntities.size() < 5) {
            this.mostFreqEntities.add(new Pair<>(term.getValue(), term.getDocTfi(docNum)));
            return;
        }
        //finds min frequent entity
        Integer minTF = 0;
        Pair<String, Integer> minEntity = null;
        for (Pair<String, Integer> entity: this.mostFreqEntities)
            if (entity.right < minTF){
                minTF = entity.right;
                minEntity = entity;
            }
        //replace min entity with the current entity
        if (term.getTF() < minTF) {
            this.mostFreqEntities.remove(minEntity);
            this.mostFreqEntities.add(new Pair<>(term.getValue(), term.getTF()));
        }
    }

    /**
     * This function sets new Max Term frequency, only if it is max
     * @param maxTf - the term that appears the most in the Document
     */
    private void setMaxTf(int maxTf) {
        if (maxTf > this.maxTf)
            this.maxTf = maxTf;
    }

    /**
     * This function increase the num of uniq words
     */
    private void increaseUniqTerms(){ this.numOfUniqTerms++; }

    /**
     * This function ands to the Document vector the given number of term square
     * @param tf - number of terms
     */
    private void addToVector(int tf){
        this.sumOfTermsSquare += Math.pow(tf, POW);
    }

    //</editor-fold>

    //<editor-fold des="Getters">

    /**
     * This function returns the Max term frequency of the Max term frequency
     * @return Max term frequency
     */
    public int getMaxTf() { return maxTf; }

    /**
     * This function returns the number of uniq word in the doc
     * @return number of uniq word in the doc
     */
    public int getNumOfUniqTerms() { return numOfUniqTerms; }

    /**
     * This function returns the vector that contains all the terms frequency square
     * @return vector size
     */
    public double getSumOfTermsSquare() { return sumOfTermsSquare; }


    public int getNumOfTerms() { return numOfTerms; }

    //</editor-fold>

    //<editor-fold des="Interface Functions">

    /**
     * This function returns a StringBuilder of the object (the information to write to the file)
     * @return StringBuilder of the object information
     */
    public StringBuilder toFileString(){
        return new StringBuilder(maxTf + del + numOfTerms + del + numOfUniqTerms + del + sumOfTermsSquare +
                del + entitiesToString());
    }

    private StringBuilder entitiesToString(){
        StringBuilder entities = new StringBuilder();
        for (Pair<String, Integer> entity: this.mostFreqEntities)
            entities.append(entity.left + entDel + entity.right + entDel);
        return entities;
    }

    /**
     * This function creates a list of the file information that needs to be written
     * @return List of the the file information
     */
    @Override
    public List<StringBuilder> toFile() {
        List<StringBuilder> toWrite = new ArrayList<>();
        toWrite.add(toFileString());
        return toWrite;
    }

    /**
     * This function gets a line of information about that object and updates the information
     * @param toUpdate - a list with one line of the doc information
     * @return List with with one line with the updated information
     */
    public List<StringBuilder> update(List<String> toUpdate) {
        String[] update = toUpdate.get(0).split(del);
        int updatedMaxTf = maxTf + Integer.parseInt(update[0]);
        int updatedNumOfUniqTerms = numOfUniqTerms + Integer.parseInt(update[1]);
        double updatedSumOfTermsSquare = sumOfTermsSquare + Double.parseDouble(update[2]);
        List<StringBuilder> toWrite = new ArrayList<>();
        StringBuilder line = new StringBuilder(updatedMaxTf + del);
        line.append(updatedNumOfUniqTerms + del);
        line.append(updatedSumOfTermsSquare);
        toWrite.add(line);
        return toWrite;
    }


    //</editor-fold>

}
