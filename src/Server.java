import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {

    private static Map<Integer, ObjectOutputStream> writers = new HashMap<>();
    private static Map<String, Integer> names = new HashMap<>();
    private static ArrayList<String> namesPlaceHolder = new ArrayList<>();
    private static Map<String, ArrayList<String>> qoMap = new HashMap<>();
    private static Iterator<Map.Entry<String, ArrayList<String>>> entryIter = null;
    private static Map.Entry<String, ArrayList<String>> currentEntry;
    private static ArrayList<Integer> answers = new ArrayList<>();
    private static ServerSocket listener;
    private static int S_PORT;
    private static int clientCount = 0;
    private static final int answerTime = 20000;
    private static boolean questionSent = false;
    private static ArrayList<Thread> clients = new ArrayList<>();

    public static void main(String[] args) throws IOException, InterruptedException {
        SetUp setUp = new SetUp();
        setUp.loadSettings();
        S_PORT = setUp.getServerPort();

        System.out.println("Quiz server started!\nAwaiting participants...");

        listener = new ServerSocket(S_PORT);
        while (clientCount < 3) {
            class accepter extends Thread {
                public int cc = 0;

                @Override
                public void run() {

                    while (true) {
                        try {
                            Socket socket = listener.accept();
                            Thread t = new Thread(new ClientHandler(socket, cc));
                            clients.add(t);
                            t.start();
                            cc++;
                        } catch (IOException e) {
                            e.printStackTrace();
                        }
                        //            listener.close();
                    }
                }

                public int myStop() {
                    super.stop();
                    return cc;
                }
            }

            accepter ac = new accepter();
            ac.start();
            ac.join(30000);
            clientCount = ac.myStop();
        }

        startQuiz();

    }

    //sends a message to every connected socket in a loop
    private static void sendMessage(String message) {
        for (int i = 0; i < writers.size(); i++) {
            ObjectOutputStream writer = writers.get(i);
            try {
                writer.writeUTF(message);
                writer.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }

        }
    }

    private static void sendPrivateMessage(String message, String receiver) {
        int nameIndex = namesPlaceHolder.indexOf(receiver);
        ObjectOutputStream pMWriter = writers.get(nameIndex);

        try {
            pMWriter.writeUTF(message);
            pMWriter.flush();
        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private static void startQuiz() throws InterruptedException {
        SetUp newGame = new SetUp();
        qoMap = newGame.loadQuestions();
        answers = newGame.getAnswers();

        Thread qthread = new Thread(new Quiz());
        qthread.start();

    }

    private static class Quiz implements Runnable{

        @Override
        public void run() {
            while (true) {

                if (entryIter == null) {
                    entryIter = qoMap.entrySet().iterator();
                }
                currentEntry = entryIter.next();

                sendMessage("Question/" + currentEntry.getKey() + "/" + currentEntry.getValue().get(0));
                questionSent = true;

                try {
                    Thread.sleep(answerTime);

                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
                questionSent = false;

                getScore();
                try {
                    Thread.sleep(5000);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }

                if (!entryIter.hasNext()) {
                    sendMessage("Quiz has ended!");
                    break;
                }

            }
        }
    }

    public static void getScore() {
        try {
            System.out.println("\n");
            for (String users : names.keySet()) {
                sendMessage(users + " has " + names.get(users) + " point(s)!");
                System.out.println(users + " has " + names.get(users) + " point(s)!");
            }
        } catch (Exception exception) {
            System.out.println("Failed to update scoreboard :(");
        }
    }


    private static class ClientHandler implements Runnable {
        private String name;
        private Socket clientHolder;
        ObjectInputStream reader;
        ObjectOutputStream writer;


        public ClientHandler(Socket socket, int count) {
            this.clientHolder = socket;
            try {
                InputStream i = clientHolder.getInputStream();

                reader = new ObjectInputStream(i);
                writer = new ObjectOutputStream(clientHolder.getOutputStream());
                writers.put(count, writer);
            } catch (IOException e) {
                e.printStackTrace();
            }
            //start();
        }

        private String extractReceiverName(String input) {
            return input.substring(input.indexOf("message to") + 11, input.indexOf(":"));
        }

        private String extractSenderName(String input) {
            return input.substring(0, input.indexOf("message to"));
        }

        private void receive() throws IOException {
            String message = reader.readUTF();
            System.out.println("masage:  " + message);
            if (message == null) {
                return;
            }

            if (message.contains("message to")) {
                String receiver = extractReceiverName(message);
                String sender = extractSenderName(message);
                String[] content = message.split(":");
                sendPrivateMessage("PMessage/" + content[1] + "/" + sender, receiver);
            } else if (message.equals("1") || message.equals("2") || message.equals("3") || message.equals("4")) {
                if (message.equals(currentEntry.getValue().get(1))) {
                    System.out.println(name + " answered correctly!");
                    names.put(name, names.get(name) + 1);
                }
            } else if(message.matches("-?\\d+")) {
                sendPrivateMessage("PMessage/Your answer should be a number between 1 and 4!/server ", this.name);
            } else {
                System.out.println(message);
                if (!message.equals(""))
                    sendPrivateMessage("PMessage/Please use correct syntax!/server ", this.name);
            }

        }

        public void run() {
            try {
                writer.writeUTF(" Please enter your name.");
                writer.flush();

                Random rnd = new Random();
                String defaultName = "client" + rnd.nextInt(200);

                while (true) {
                    name = reader.readUTF();
                    if (name.equals("")) {
                        name = defaultName;
                    }
                    synchronized (names) {
                        if (!names.containsKey(name)) {
                            names.put(name, 0);

                        }
                    }
                    synchronized (namesPlaceHolder) {
                        if (!namesPlaceHolder.contains(name)) {
                            namesPlaceHolder.add(name);
                            break;
                        }
                    }
                }

                sendMessage(name + " has joined the server!");
                System.out.println(name + " has joined the server!");


                // Accept messages from client and broadcast them.
                while (true) {
                    receive();
                }


            } catch (IOException e) {
                System.out.println(e);
            } finally {
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

