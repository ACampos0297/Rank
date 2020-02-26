package utils;

import ds.Document;
import ds.Query;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;


public class LoadHandler {

    /**
     * Loads the training data.
     * @param feature_file_name the name of the feature file.
     * @return the mapping of ds.Query-url-ds.Document
     */
    public static Map<Query,Map<String, Document>> loadTrainData(String feature_file_name) throws Exception {
        File feature_file = new File(feature_file_name);
        if (!feature_file.exists() ) {
            System.err.println("Invalid feature file name: " + feature_file_name);
            return null;
        }

        BufferedReader reader = new BufferedReader(new FileReader(feature_file));
        String line, url= null;
        Query query = null;

        /* Feature dictionary: ds.Query -> (url -> ds.Document)  */
        Map<Query,Map<String, Document>> queryDict = new HashMap<>();

        while ((line = reader.readLine()) != null) {
            String[] tokens = line.split(":", 2);
            String key = tokens[0].trim();
            String value = tokens[1].trim();

            if (key.equals("query")) {
                query = new Query(value);
                queryDict.put(query, new HashMap<>());
            }
            else if (key.equals("url")) {
                url = value;
                queryDict.get(query).put(url, new Document(url));
            }
            else if (key.equals("title")) {
                queryDict.get(query).get(url).title = new String(value);
            }
            else if (key.equals("body_hits")) {
                if (queryDict.get(query).get(url).body_hits == null)
                    queryDict.get(query).get(url).body_hits = new HashMap<>();
                String[] temp = value.split(" ", 2);
                String term = temp[0].trim();
                List<Integer> positions_int;

                if (!queryDict.get(query).get(url).body_hits.containsKey(term)) {
                    positions_int = new ArrayList<>();
                    queryDict.get(query).get(url).body_hits.put(term, positions_int);
                } else
                    positions_int = queryDict.get(query).get(url).body_hits.get(term);

                String[] positions = temp[1].trim().split(" ");
                for (String position : positions)
                    positions_int.add(Integer.parseInt(position));

            }
            else if (key.equals("body_length"))
                queryDict.get(query).get(url).body_length = Integer.parseInt(value);
            else if (key.equals("title_length"))
                queryDict.get(query).get(url).title_length = Integer.parseInt(value);
            else if (key.equals("pagerank"))
                queryDict.get(query).get(url).page_rank = Integer.parseInt(value);
        }

        reader.close();

        return queryDict;
    }
}
