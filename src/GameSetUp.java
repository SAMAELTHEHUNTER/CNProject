import org.json.simple.JSONArray;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;


public class GameSetUp {

    private JSONArray questions;
    private int questionCount = 0;
    private ArrayList<Integer> answers = new ArrayList<>();

    public GameSetUp(){
        JSONParser jsonParser = new JSONParser();
        try {
            questions = (JSONArray) jsonParser.parse(new FileReader("questions.json"));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
    }

    public ArrayList<Integer> getAnswers(){
        return answers;
    }

    public Map<String, ArrayList<String>> loadQuestions(){
        Map<String, ArrayList<String>> qo = new HashMap<>();

        while (questionCount < questions.size()) {
            JSONObject obj = (JSONObject) questions.get(questionCount);
            String question = (String) obj.get("question");
            String options = obj.get("options").toString();
            String answer = obj.get("answer").toString();
            answers.add(Integer.parseInt(obj.get("answer").toString()));
            options = options.substring(1, options.length() - 1);
            qo.put(question, new ArrayList<String>());
            qo.get(question).add(options);
            qo.get(question).add(answer);
            questionCount ++;
        }
        // sendTime = System.currentTimeMillis();
        //sendToAll("question",question,options);
        return qo;
    }


}
