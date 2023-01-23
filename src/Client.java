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
    BufferedReader scanner = new BufferedReader(new InputStreamReader(System.in));
    public static final String GREEN = "\033[0;32m";
    public static final String RED = "\u001B[31m";
    public static final String YELLOW = "\u001B[33m";
    public static final String RESET = "\033[0m";

    public static boolean answered = false;

    public Client(){
        SetUp setUp = new SetUp();
        setUp.loadSettings();
        serverPort = setUp.getServerPort();

        Thread thread = new Thread(() -> {
           try {
               String userInput;
               while ((userInput = scanner.readLine()) != null) {

                   if ((!userInput.matches("1") && !userInput.matches("2") && !userInput.matches("3") && !userInput.matches("4")) || !answered){

                       if ((userInput.matches("1") || userInput.matches("2") || userInput.matches("3") || userInput.matches("4")) && !answered)
                           answered = true;

                       writer.writeUTF(userInput);
                       writer.flush();
                   }

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

            System.out.println(YELLOW + "ATTENTION : If you want to be given a name by the server, simply press Enter.\n" + RESET);


        }
        catch (IOException e) {
            e.printStackTrace();
        }

    }

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
                if (sender.equals("server "))
                    System.out.println(RED + "[Server] : " + message + RESET);
                else
                    System.out.println(GREEN + "\n" + sender + "says " + message + "\n" + RESET);


                break;

            case "Question":
                scanner = new BufferedReader(new InputStreamReader(System.in));
                answered = false;
                String question = str[1];
                String[] options = str[2].split(",");

                System.out.println("\nQuestion: " + question);
                for (int i = 0; i < options.length; i++){
                    String option = options[i].substring(1, options[i].length() - 1);
                    System.out.println(option + " (" + (i + 1) );
                }

                break;

            default:
                System.out.println(text);
                answered = false;

        }
    }

    public static void main(String[] args) {
        Thread t = new Thread(new Client());
        t.start();
    }
}

