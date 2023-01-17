import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.*;

public class Server {

    //    private static HashSet<ObjectOutputStream> writers = new HashSet<ObjectOutputStream>();
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
//    private static boolean nameEntered = false;

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

//            while (true) {
//                // Engage thread handling
//
//               // Thread.sleep(1000);
////                while (clientCount < 3) {
//
//
//
//
////                }
////                for (int i=0; i<clients.size(); i++) {
////                    clients.get(i).start();
////                }
//
//
//
////                if (clientCount >= 3) {
////                }
////                t.start();
//                System.out.println("hello");
//
//                try {
//                    Thread.sleep(10000);
//                } catch (InterruptedException e) {
//                    e.printStackTrace();
//                }
//
//                if (clientCount >= 3) {
//                    startQuiz();
//                }
//
//               // if (clientCount > 2)
//                   // new sendQuestionThread().start();
//            }




    }

    public static void addClientCount() {
        clientCount++;
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
        //System.out.println(namesPlaceHolder.contains(receiver));
        int nameIndex = namesPlaceHolder.indexOf(receiver);
        //System.out.println(nameIndex);
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

//       while (true){
//           while (questionSent){
//               synchronized (qthread){
//                   qthread.wait(answerTime);
//               }
//
//               questionSent = false;
//           }
//
//           getScore();
//           synchronized (qthread){
//               qthread.wait(5000);
//           }
//       }
//        while (true) {
//
//            if (entryIter == null) {
//                entryIter = qoMap.entrySet().iterator();
//            }
//            currentEntry = entryIter.next();
//
//            sendMessage("Question/" + currentEntry.getKey() + "/" + currentEntry.getValue().get(0));
//            questionSent = true;
//
//           // Thread.sleep(answerTime);
//            questionSent = false;
//
//            getScore();
//
//            Thread.sleep(5000);
//
//            if (!entryIter.hasNext()) {
//                sendMessage("Quiz has ended!");
//                break;
//            }
//
//        }
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
        private Server serverHolder;
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
                sendPrivateMessage("PMessage/ wtf /" + "server", this.name);
            } else {
                System.out.println(message);
                if (!message.equals(""))
                    sendMessage("Please use correct syntax\n");
            }



//                        else if (!questionSent && !message.equals("-1")) {
//                sendMessage("Please wait until the question is shown.\n");
//            }

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
        }

        public void run() {
            try {
                // System.out.println("1");
                writer.writeUTF(" Please enter your name.");
                writer.flush();

                // name = reader.readLine();
                // Check username
                String chars = "0123456789ABCDEFGHIJKLMNOPQRSTUVWXYZabcdefghijk"
                        +"lmnopqrstuvwxyz!@#$%&";
                Random rnd = new Random();
                StringBuilder sb = new StringBuilder(4);
                for (int i = 0; i < 4; i++)
                    sb.append(chars.charAt(rnd.nextInt(chars.length())));


                while (true) {
                    name = reader.readUTF();
                    char[] abc =name.toCharArray();
                    System.out.println("l: " + abc.length);
                    for (int i=0; i<abc.length; i++) {
                        System.out.println(abc);
                    }
                    //System.out.println("drg");
                    if (name.equals("")) {
                        name = sb.toString();
                    }
                    synchronized (names) {
                        if (!names.containsKey(name)) {
                            names.put(name, 0);
//                            nameEntered = true;
//                            System.out.println(nameEntered);
                        }
                    }
                    synchronized (namesPlaceHolder) {
                        if (!namesPlaceHolder.contains(name)) {
                            namesPlaceHolder.add(name);
                            break;
                        }
                    }
                }

                //Server.addClientCount();
                // System.out.println(clientCount);

                sendMessage(name + " has joined the server!");
                System.out.println(name + " has joined the server!");

                // Update current online users
                //getOnlineUsers();

                // Accept messages from client and broadcast them.
                while (true) {
                    receive();
                }

//                while (true){
//                    Thread thread = new Thread(() -> {
//                        try {
//                            System.out.println("fuckkkkkkkkk");
//                            receive();
//                            System.out.println("noooooooooo");
//                        } catch (IOException e) {
//                            e.printStackTrace();
//                        }
//
//                    });
//
//                    thread.start();
//                    thread.join();
//
//                }

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

