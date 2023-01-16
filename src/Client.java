import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;


//SENDERNAME message to RECIEVERNAME:message
public class Client implements Runnable {
    Socket mSocket;
    int serverPort;
    String serverAddress = "127.0.0.1";
    ObjectInputStream reader;
    ObjectOutputStream writer;
    //Scanner scanner = new Scanner(System.in);
    BufferedReader scanner = new BufferedReader(new InputStreamReader(System.in));
    public static final String GREEN = "\033[0;32m";
    public static final String RED = "\u001B[31m";
    public static final String RESET = "\033[0m";

    public boolean wasAMessage = true;
    public boolean answered = false;

    public Client(){
        SetUp setUp = new SetUp();
        setUp.loadSettings();
        serverPort = setUp.getServerPort();

        Thread thread = new Thread(() -> {
           try {
               String userInput;
               while ((userInput = scanner.readLine()) != null) {
               //while ((userInput = scanner.nextLine()) != null) {

                   if (!userInput.matches("-?\\d+") || !answered){

                       if (userInput.matches("-?\\d+") && !answered)
                           answered = true;

                       writer.writeUTF(userInput);
                       writer.flush();
                   }

                   // System.out.println("echo: " + in.readLine());
               }
           } catch (IOException e) {
               e.printStackTrace();
           }
        });

        thread.start();


            try {
            mSocket = new Socket(serverAddress, serverPort);
            System.out.println("connected to server ....");
            writer = new ObjectOutputStream(mSocket.getOutputStream());
            reader = new ObjectInputStream(mSocket.getInputStream());

            String msg = reader.readUTF();
            System.out.println("Server :" + msg);

            //Thread thread = new Thread(reader);

//            writer.writeUTF(scanner.readLine());
//            writer.flush();
//
//            msg = reader.readUTF();
//            System.out.println(msg);

        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

    public static boolean isInteger(String s) {
        try {
            Integer.parseInt(s);
        } catch(NumberFormatException e) {
            return false;
        } catch(NullPointerException e) {
            return false;
        }
        // only got here if we didn't return false
        return true;
    }

//    class sendAnswer extends Thread {
//        private String answer;
//        private String tmp;
//
//        public sendAnswer() {
//            tmp = "-1";
//        }
//
//        @Override
//        public void run() {
//            Scanner sc = new Scanner(System.in);
//            tmp = sc.nextLine();
//
////                            System.out.println(tmp);
//        }
//
//        public String myStop() {
//            //answer = Integer.toString(tmp);
//            answer = tmp;
////                            super.stop();
//            return answer;
//        }
//    }

    @Override
    public void run() {


        while (true) {

            try {
                String text = reader.readUTF();
                showMessage(text);
            }
            catch (IOException e) {
                System.out.println(e);
            }


        }

    }

    public void showMessage(String text) throws IOException {
        String[] str = text.split("/");

        switch (str[0]) {


            case "PMessage":
                String message = str[1];
                String sender = str[2];
                System.out.println(GREEN +  sender + "says " + message + "\n" + RESET);

//                try {
//                    class sendAnswer extends Thread {
//                        private String answer;
//                        private String tmp;
//
//                        public sendAnswer() {
//                            tmp = "-1";
//                        }
//
//                        @Override
//                        public void run() {
//                            Scanner sc = new Scanner(System.in);
//                            tmp = sc.nextLine();
////                            System.out.println(tmp);
//                        }
//
//                        public String myStop() {
//                            //answer = Integer.toString(tmp);
//                            answer = tmp;
////                            super.stop();
//                            return answer;
//                        }
//                    }
//                    sendAnswer sa = new sendAnswer();
//                    sa.start();
//                    sa.join();
//                    String out = sa.myStop();
//                    writer.writeUTF(out);
//                    writer.flush();
//
//
//
//
////                    System.out.println("jalil: " + out);
////                    if (out.equals("-1")) {
////                        System.out.println("please enter: ");
////                    }
////                    System.out.println("jalil: " + out);
//
//
//                } catch (IOException | InterruptedException e) {
//                    e.printStackTrace();
//                }

                break;

            case "Question":
                scanner = new BufferedReader(new InputStreamReader(System.in));
                //scanner = new Scanner(System.in);
                answered = false;
                String question = str[1];
                String[] options = str[2].split(",");

                System.out.println("\nQuestion: " + question + "\n");
                for (int i = 0; i < options.length; i++){
                    String option = options[i].substring(1, options[i].length() - 1);
                    System.out.println((i + 1) + ") " + option + "\n");
                }

                System.out.println(RED + "ATTENTION : If you don't have an answer for the given question, please press Enter.\n" + RESET);

//                String userInput;
//                while ((userInput = scanner.readLine()) != null) {
//                    writer.writeUTF(userInput);
//                    writer.flush();
//                   // System.out.println("echo: " + in.readLine());
//                }


//                while (wasAMessage && !answered){
//                    try {
//                        class sendAnswer extends Thread {
//                            private String answer;
//                            private String tmp;
//
//                            public sendAnswer() {
//                                tmp = "-1";
//                            }
//
//                            @Override
//                            public void run() {
//                                Scanner sc = new Scanner(System.in);
//                                tmp = sc.nextLine();
////                            System.out.println(tmp);
//                            }
//
//                            public String myStop() {
//                                //answer = Integer.toString(tmp);
//                                answer = tmp;
////                            super.stop();
//                                return answer;
//                            }
//                        }
//                        sendAnswer sa = new sendAnswer();
//                        sa.start();
//                        sa.join();
//                        String out = sa.myStop();
//                        writer.writeUTF(out);
//                        writer.flush();
//
//                        if (out.contains("message to")){
//                            wasAMessage = true;
//                        }
//                        else{
//                            answered = true;
//                            wasAMessage = false;
//                        }
//
//
//
//
////                    System.out.println("jalil: " + out);
////                    if (out.equals("-1")) {
////                        System.out.println("please enter: ");
////                    }
////                    System.out.println("jalil: " + out);
//
//
//                    } catch (IOException | InterruptedException e) {
//                        e.printStackTrace();
//                    }
//                }

                break;

            default:
                System.out.println(text);

        }
    }

    public static void main(String[] args) {
        Thread t = new Thread(new Client());
        t.start();
    }
}

