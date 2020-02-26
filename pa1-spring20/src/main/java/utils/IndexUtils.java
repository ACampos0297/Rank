package utils;

import org.apache.lucene.index.DirectoryReader;
import org.apache.lucene.index.IndexReader;
import org.apache.lucene.index.Term;
import org.apache.lucene.store.FSDirectory;

import java.io.IOException;
import java.nio.file.Paths;

/**
 * Wrapper utility class to access Lucene functions.
 */
public class IndexUtils {

    private IndexReader reader;

    public IndexUtils(String indexDir) {
        try {
            this.reader = DirectoryReader.open(FSDirectory.open(Paths.get(indexDir)));
        } catch (IOException e) {
            System.out.println("Seems like you need to index the data first!");
            e.printStackTrace();
        }
    }

    /**
     *
     * @param t input term
     * @return number of documents containing term t
     */
    public long docFreq(String t) {
        Term term = new Term("contents", t);
        try {
            return reader.docFreq(term);
        } catch (IOException e) {
            e.printStackTrace();
            System.out.println("Seems like you need to index the data first!");
            return 0;
        }
    }

    /**
     *
     * @return total number of docs used to build the index
     */
    public long totalNumDocs() {
        return reader.numDocs();
    }
}
