package ds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

/**
 * This class is used to store a query sequence.
 */
public class Query {
    public List<String> queryWords;

    /**
     * Constructs a query.
     * @param query the query String.
     */
    public Query(String query) {
        queryWords = new ArrayList<>(Arrays.asList(query.split(" ")));
    }

    /**
     * Returns a String representation of the ds.Query.
     * @return the ds.Query as a String
     */
    public String toString() {
        String str = "";
        for (String word : queryWords) {
            str += word + " ";
        }
        return str.trim();
    }
}
