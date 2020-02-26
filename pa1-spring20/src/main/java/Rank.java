import ds.Document;
import ds.Pair;
import ds.Query;
import scorer.AScorer;
import scorer.BM25Scorer;
import scorer.BaselineScorer;
import scorer.VSMScorer;
import utils.IndexUtils;
import utils.LoadHandler;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class Rank {

    private static final String INDEX_DIR = "index";

    /**
     * Call this function to score and rank documents for some queries,
     * using a specified scoring function.
     * @param queryDict
     * @param scoreType
     * @return a mapping of queries to rankings
     */
    private static Map<Query,List<Document>> score(Map<Query, Map<String, Document>> queryDict, String scoreType) {
        AScorer scorer;
        IndexUtils utils = new IndexUtils(INDEX_DIR);
        switch (scoreType) {
            case "vsm":
                scorer = new VSMScorer(utils);
                break;
            case "bm25":
                scorer = new BM25Scorer(utils, queryDict);
                break;
            default:
                scorer = new BaselineScorer();
        }

        // ranking result Map.
        Map<Query, List<Document>> queryRankings = new HashMap<>();

        // loop through urls for query, getting scores.
        for (Query query : queryDict.keySet()) {
            // Pair of url and ranked relevance.
            List<Pair<Document,Double>> docAndScores = new ArrayList<>(queryDict.get(query).size());
            for (String url : queryDict.get(query).keySet()) {
                Document doc = queryDict.get(query).get(url);
                // force debug string to be 1-line and truncate to only includes only first 200 characters for rendering purpose
                String debugStr = scorer.getDebugStr(doc, query).trim().replace("\n", " ");
                doc.debugStr = debugStr.substring(0, Math.min(debugStr.length(), 200));

                double score = scorer.getSimScore(doc, query);
                docAndScores.add(new Pair<>(doc, score));
            }

            /* Sort urls for query based on scores. */
            docAndScores.sort((o1, o2) -> {
                Double v1 = o1.getSecond();
                Double v2 = o2.getSecond();
                return v1 < v2 ? 1 : v1 > v2 ? -1 : 0;
            });

            //put completed rankings into map
            List<Document> curRankings = new ArrayList<>();
            for (Pair<Document,Double> docAndScore : docAndScores)
                curRankings.add(docAndScore.getFirst());
            queryRankings.put(query, curRankings);
        }
        return queryRankings;
    }

    /**
     * Writes ranked results to file.
     * @param queryRankings the mapping of queries to rankings
     * @param outputFilePath the destination file path
     */
    public static void writeRankedResultsToFile(Map<Query,List<Document>> queryRankings,String outputFilePath) {
        try {
            File file = new File(outputFilePath);
            // If file doesn't exists, then create it
            if (!file.exists()) {
                file.createNewFile();
            }

            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            BufferedWriter bw = new BufferedWriter(fw);

            for (Query query : queryRankings.keySet()) {
                StringBuilder queryBuilder = new StringBuilder();
                for (String s : query.queryWords) {
                    queryBuilder.append(s);
                    queryBuilder.append(" ");
                }

                String queryStr = "query: " + queryBuilder.toString() + "\n";
                System.out.print(queryStr);
                bw.write(queryStr);

                for (Document res : queryRankings.get(query)) {
                    String urlString =
                            "  url: " + res.url + "\n" +
                                    "    title: " + res.title + "\n" +
                                    "    debug: " + res.debugStr + "\n";
                    System.out.print(urlString);
                    bw.write(urlString);
                }
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    /**
     * Print ranked results.
     * @param queryRankings the mapping of queries to rankings
     */
    public static void printRankedResults(Map<Query, List<Document>> queryRankings) {
        for (Query query : queryRankings.keySet()) {
            StringBuilder queryBuilder = new StringBuilder();
            for (String s : query.queryWords) {
                queryBuilder.append(s);
                queryBuilder.append(" ");
            }

            System.out.println("query: " + queryBuilder.toString());
            for (Document res : queryRankings.get(query)) {
                System.out.println(
                        "  url: " + res.url + "\n" +
                                "    title: " + res.title + "\n" +
                                "    debug: " + res.debugStr
                );
            }
        }
    }

    public static void main(String[] args) {
        if (args.length != 2) {
            System.err.println("Insufficient number of arguments: <sigFile> <taskOption>");
            return;
        }

        /* sigFile : args[0], path for signal file. */
        String sigPath = args[0];
        /* taskOption : args[1], baseline, vsm (Task 1), bm25 (Task 2) */
        String taskOption = args[1];

        /* start loading query pages to be ranked. */
        Map<Query, Map<String, Document>> queryDict = null;
        try {
            queryDict = LoadHandler.loadTrainData(sigPath);
        } catch (Exception e) {
            System.out.println("Error while reading: " + sigPath);
            e.printStackTrace();
        }

        /* score documents for queries */
        Map<Query,List<Document>> queryRankings = score(queryDict, taskOption);
        /* print out ranking result, keep this stdout format in your submission */
        printRankedResults(queryRankings);

        // print results and save them to file "ranked.txt" (to run with NdcgMain.java)
        String outputFilePath = "ranked.txt";
        writeRankedResultsToFile(queryRankings,outputFilePath);
    }
}
