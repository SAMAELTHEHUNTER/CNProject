import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {

    private static HashSet<ObjectOutputStream> writers = new HashSet<ObjectOutputStream>();
    private static Map<String, Integer> names = new HashMap<>();
    private static Map<String, ArrayList<String>> qoMap = new HashMap<>();
    private static Iterator<Map.Entry<String, ArrayList<String>>> entryIter = null;
    private static Map.Entry<String, ArrayList<String>> currentEntry;
    private static ArrayList<Integer> answers = new ArrayList<>();
    private static ServerSocket listener;
    private static final int PORT = 6969;
    private static int clientCount = 0;
    private static final int answerTime = 3000;
    private static boolean questionSent = false;

    public static void main(String[] args) throws IOException, InterruptedException {
        System.out.println("Quiz server started!\nAwaiting participants...");

        listener = new ServerSocket(PORT);

        try {
            while (true) {
                // Engage thread handling
                Socket socket = listener.accept();
                clientCount++;
               // Thread.sleep(1000);
                Thread t = new Thread(new ClientHandler(socket));
                t.start();


                try {
                    Thread.sleep(10000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                startQuiz();

               // if (clientCount > 2)
                   // new sendQuestionThread().start();
            }
        }
        finally {
            listener.close();
        }

    }

    //sends a message to every connected socket in a loop
    private static void sendMessage(String message) {
        for(ObjectOutputStream writer: writers) {
            try {
                writer.writeUTF(message);
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private static void startQuiz() throws InterruptedException {
        GameSetUp newGame = new GameSetUp();
        qoMap = newGame.loadQuestions();
        answers = newGame.getAnswers();
        while (true){

            if (entryIter == null ) {
                entryIter = qoMap.entrySet().iterator();
            }
            currentEntry = entryIter.next();

            sendMessage("Question/" + currentEntry.getKey() + "/" + currentEntry.getValue().get(0));
            questionSent = true;

            Thread.sleep(answerTime);
            questionSent = false;

            getScore();

            Thread.sleep(5000);

            if (!entryIter.hasNext()){
                sendMessage("Quiz has ended!");
            }

        }

    }

    public static void getScore() {
        try {
            for(String users : names.keySet()) {
                sendMessage(users + " has " + names.get(users) + " point(s)!");
                System.out.println(users + " has " + names.get(users) + " point(s)!");
            }
        } catch (Exception exception) {
            System.out.println("getScore failed");
        }
    }


    private static class ClientHandler implements Runnable{
        private String name;
        private Socket clientHolder;
        private Server serverHolder;
        ObjectInputStream reader;
        ObjectOutputStream writer;


        public ClientHandler(Socket socket) {
            this.clientHolder = socket;
            clientCount++;

            try {
                InputStream i = clientHolder.getInputStream();

                reader = new ObjectInputStream(i);
                writer = new ObjectOutputStream(clientHolder.getOutputStream());
                writers.add(writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //start();
        }


        private void receive() throws IOException {
            String message = reader.readUTF();
            if (message == null) {
                return;
            }
            if (!questionSent){
                sendMessage("Please wait until the question is shown.\n");
            }
            else if (message.equals(currentEntry.getValue().get(1))){
            //    sendMessage(name + " answered correctly!");
                System.out.println(name + " answered correctly!");
                //update score
                names.put(name, names.get(name) + 1);
            }
            /*else if (message != null && message.endsWith(currentEntry.getValue().trim().toLowerCase())) {
                // Award player
                sendMessage(name + " had the correct answer! 1 Point awarded!");
                System.out.println("[Server] - " + name + " had the correct answer! 1 Point awarded!");
                names.put(name, names.get(name) + 1);
                // Unable to get more points
                currentEntry.setValue("waiting for next question");
                // Update score
                getOnlineUsers();
            } else if (message.contains("/SCORE")) {
                System.out.println(currentEntry.getValue());
                getScore();
                getOnlineUsers();
            } else if (message.contains("/QUIT")) {
                getScore();
                getOnlineUsers();
            } else if (message.contains("/DISCONNECT")) {
                getScore();
                getOnlineUsers();
            }

             */
            else
                System.out.println(message);

            // Send to all clients
            for (ObjectOutputStream writer : writers) {
                writer.writeUTF(message);
                writer.flush();
            }
        }

        public void run() {
            try {


               // System.out.println("1");
                writer.writeUTF(" Please enter your name.");
                writer.flush();

               // name = reader.readLine();
                // Check username
                while (true) {
                    name = reader.readUTF();
                    //System.out.println("drg");
                    if (name == null) {
                        return;
                    }
                    synchronized (names) {
                        if (!names.containsKey(name)) {
                            names.put(name, 0);
                            break;
                        }
                    }
                }

                sendMessage(name + " has joined the server!");
                System.out.println(name + " has joined the server!");

                // Update current online users
                //getOnlineUsers();

                // Accept messages from client and broadcast them.
                while (true) {
                    receive();
                }


            } catch (IOException e) {
                System.out.println(e);
            }
            finally {
                if (name != null) {
                    names.remove(name);
                }
                if (writer != null) {
                    writers.remove(writer);
                }
                try {
                    clientHolder.close();
                } catch (IOException e) {
                    System.out.print(e);
                }
            }
        }

    }

    }

