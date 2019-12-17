import Model.*;

import java.util.HashMap;
import java.util.List;

public class Main {

    public static void main(String[] args){

        Thread.currentThread().setPriority(Thread.MAX_PRIORITY);
        Runtime runtime = Runtime.getRuntime();
        double usedMemoryBefore = runtime.totalMemory() - runtime.freeMemory();
        double startTime = System.currentTimeMillis();

        HashMap<String, Term> m = null;
        int numOfFiles = 0;


        ReadFile rf = new ReadFile("d:\\documents\\users\\chengal\\Downloads\\New folder");
        Parse p = new Parse("d:\\documents\\users\\chengal\\Downloads\\New folder", true);
        Indexer indexer = new Indexer("d:\\documents\\users\\chengal\\Downloads\\test",5, 0, 250);

        double lastTime = System.currentTimeMillis();
        List<Document> d = rf.getNextDocs(6000);
        int i = 0;
        while (d != null) {
//        for( int i = 0; i < 25 ; i++) {
            System.out.println(i++ + " - " + (System.currentTimeMillis() - lastTime)/60000);
            lastTime = System.currentTimeMillis();
            m = p.parse(d);
            indexer.setTerms(m);
            d = rf.getNextDocs(6000);
        }

        indexer.closeWriter();

        double usedMemoryAfter = runtime.totalMemory() - runtime.freeMemory();
        double endTime = System.currentTimeMillis();
        HashMap<String,Integer> dic = indexer.getDictionary();
        HashMap<String,Term> bellow = indexer.getBellowThreshHold();
        System.out.println("Number Of Words: " + dic.size());
        System.out.println("Number Of BellowThreshHold: " + bellow.size());
        System.out.println("Running Time: " + (endTime - startTime) / 60000 + " Min");
        System.out.println("Memory increased: " + (usedMemoryAfter - usedMemoryBefore) / Math.pow(2, 20) + " MB");
    }
}
