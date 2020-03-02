package scorer;

import ds.Document;
import ds.Query;
import utils.IndexUtils;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

/**
 * Skeleton code for the implementation of a BM25 scorer in Task 2.
 */
public class BM25Scorer extends AScorer {

    /*
     *  TODO: You will want to tune these values
     */
    //sum to 1
    double titleweight  = 0.85;
    double bodyweight = 1-titleweight;

    //sum to 1
    // BM25-specific weights
    double btitle = 0.03; //best 0.1
    double bbody = 1-btitle;



    double k1 = 1;
    double pageRankLambda = 0.1;
    double pageRankLambdaPrime = 0.2;
    double pageRankLabmdaDoublePrime =1;

    // query -> url -> document
    Map<Query,Map<String, Document>> queryDict;

    // BM25 data structures--feel free to modify these
    // ds.Document -> field -> length
    Map<Document,Map<String,Double>> lengths;

    // field name -> average length
    Map<String,Double> avgLengths;

    // ds.Document -> pagerank score
    Map<Document,Double> pagerankScores;

    /**
     * Construct a scorer.BM25Scorer.
     * @param utils Index utilities
     * @param queryDict a map of query to url to document
     */
    public BM25Scorer(IndexUtils utils, Map<Query,Map<String, Document>> queryDict) {
        super(utils);
        this.queryDict = queryDict;
        this.calcAverageLengths();
    }

    /**
     * Set up average lengths for BM25, also handling PageRank.
     */
    public void calcAverageLengths() {
        lengths = new HashMap<>();
        avgLengths = new HashMap<>(); 
        pagerankScores = new HashMap<>();

        /*
         * TODO : Your code here
         * Initialize any data structures needed, perform
         * any preprocessing you would like to do on the fields,
         * accumulate lengths of fields.
         * handle pagerank.  
         */
        double titleLenAvg=0.0;
        double bodyLenAvg = 0.0;
        //calculate length of field in a given document for all documents
        for(Query qs : this.queryDict.keySet()){
            for(Document ds:this.queryDict.get(qs).values()){
                Map<String,Double> fieldLength = new HashMap<>();
                fieldLength.put("title",(double)ds.title_length);
                titleLenAvg+=ds.title_length;
                fieldLength.put("body",(double)ds.body_length);
                bodyLenAvg+=ds.body_length;
                lengths.put(ds,fieldLength);

                //Vj function
                pagerankScores.put(ds,1/(pageRankLambdaPrime+Math.exp(-1.0*ds.page_rank*pageRankLabmdaDoublePrime)));
            }
        }

        /*
         * TODO : Your code here
         * Normalize lengths to get average lengths for
         * each field (body, title).
         */
        titleLenAvg = titleLenAvg/(double)lengths.size();
        bodyLenAvg = bodyLenAvg/(double)lengths.size();
        avgLengths.put("title",titleLenAvg);
        avgLengths.put("body",bodyLenAvg);

    }

    /**
     * Get the net score.
     * @param tfs the term frequencies
     * @param q the ds.Query
     * @param tfQuery
     * @param d the ds.Document
     * @return the net score
     */
    public double getNetScore(Map<String,Map<String, Double>> tfs, Query q, Map<String,Double> tfQuery, Document d) {

        double score = 0.0;

        tfs.get("title").replaceAll((k,v)->v=titleweight*v);
        tfs.get("body").replaceAll((k,v)->v=bodyweight*v);

        tfs.get("body").forEach((k,v)->v=tfs.get("title").get(k)==null?v:v+tfs.get("title").get(k));


        //idf (double)this.utils.totalNumDocs()/(double)this.utils.docFreq(term)
        double wdt=0.0;
        for(String word : q.queryWords){
            wdt=tfs.get("body").get(word)==null?0.0:tfs.get("body").get(word);

            score+= (wdt/(k1+wdt))*((double)this.utils.totalNumDocs()/(double)this.utils.docFreq(word))+pageRankLambda*pagerankScores.get(d);
            //add lambda Vi

        }


        /*
         * TODO : Your code here
         * Use equation 3 first and then equation 4 in the writeup to compute the overall score
         * of a document d for a query q.
         */

        return score;
    }

    /**
     * Do BM25 Normalization.
     * @param tfs the term frequencies
     * @param d the ds.Document
     * @param q the ds.Query
     */
    public void normalizeTFs(Map<String,Map<String, Double>> tfs, Document d, Query q) {
        /*
         * TODO : Your code here
         * Use equation 2 in the writeup to normalize the raw term frequencies
         * in fields in document d.
         */
        tfs.get("title").replaceAll((k,v)->v=(double)v/((1-btitle)+btitle*(d.title_length)));
        tfs.get("body").replaceAll((k,v)->v=(double)v/((1-bbody)+bbody*(d.body_length)));
    }

    /**
     * Write the tuned parameters of BM25 to file.
     * Only used for grading purpose, you should NOT modify this method.
     * @param filePath the output file path.
     */
    private void writeParaValues(String filePath) {
        try {
            File file = new File(filePath);
            if (!file.exists()) {
                file.createNewFile();
            }
            FileWriter fw = new FileWriter(file.getAbsoluteFile());
            String[] names = {
                    "titleweight", "bodyweight", "btitle",
                    "bbody", "k1", "pageRankLambda", "pageRankLambdaPrime"
            };
            double[] values = {
                    this.titleweight, this.bodyweight, this.btitle,
                    this.bbody, this.k1, this.pageRankLambda,
                    this.pageRankLambdaPrime
            };
            BufferedWriter bw = new BufferedWriter(fw);
            for (int idx = 0; idx < names.length; ++ idx) {
                bw.write(names[idx] + " " + values[idx]);
                bw.newLine();
            }
            bw.close();
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    @Override
    /**
     * Get the similarity score.
     * @param d the ds.Document
     * @param q the ds.Query
     * @return the similarity score
     */
    public double getSimScore(Document d, Query q) {
        Map<String,Map<String, Double>> tfs = this.getDocTermFreqs(d,q);
        this.normalizeTFs(tfs, d, q);
        Map<String,Double> tfQuery = getQueryFreqs(q);

        // Write out the tuned BM25 parameters
        // This is only used for grading purposes.
        // You should NOT modify the writeParaValues method.
        writeParaValues("bm25Para.txt");
        return getNetScore(tfs,q,tfQuery,d);
    }

}
