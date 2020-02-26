import java.io.IOException;
import java.util.Arrays;

public class PA1 {
    public static void main(String[] args) {
        String cmd = args[0];
        args = Arrays.copyOfRange(args, 1, args.length);

        switch (cmd) {
            case "index":
                IndexFiles.main(args);
                break;
            case "rank":
                Rank.main(args);
                break;
            case "ndcg":
                try {
                    NdcgMain.main(args);
                } catch (IOException e) {
                    System.out.println("Error while running NDCG evaluation.");
                    e.printStackTrace();
                }
                break;
            default:
                System.out.println("Command: " + cmd + " not supported.");
        }
    }
}
