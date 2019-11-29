public interface IStemmer {

    //calls steps to stem the words
    void stem();

    //Add a character to the word being stemmed.
    void add(char ch);

    //Adds wLen characters to the word being stemmed contained in a portion of a char[] array.
    void add(char[] w, int wLen);

    String toString();

    int getResultLength();

    char[] getResultBuffer();

}
