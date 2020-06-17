import java.io.*;
import java.util.ArrayList;

/**
 * This class filters out any unwanted words in the server. Accepts any files.
 *
 * @author [Sole author] Shen Wei Leong L19
 * @version 4/24/2020
 */
public class ChatFilter {
    File badWordsFile;
    ArrayList<String> badWords = new ArrayList<>();

    public ChatFilter(String badWordsFileName) {

        badWordsFile = new File(badWordsFileName);
        try {
            if (badWordsFile.length() == 0 || !badWordsFile.exists()) {
            } else {
                FileReader fr = new FileReader(badWordsFile);
                BufferedReader bfr = new BufferedReader(fr);
                String eachBadWords;
                while ((eachBadWords = bfr.readLine()) != null) {
                    badWords.add(eachBadWords);
                }
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public String filter(String msg) {
        String filteredMsg = msg;
        String filtering = "";
        for (String s : badWords) {
            if (msg.toLowerCase().contains(s.toLowerCase())) {
                for (int i = 0; i < s.length(); i++) {
                    filtering += "*";
                }
                s = "(?i)" + s;
                filteredMsg = msg.replaceAll(s, filtering);
            }
        }
        return filteredMsg;
    }
}
