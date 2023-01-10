import java.io.*;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

public class Client implements Runnable {
    Socket mSocket;
    int port = 8080;
    String serverAddress = "127.0.0.1";
    ObjectInputStream reader;
    ObjectOutputStream writer;
    Scanner scanner = new Scanner(System.in);

    public Client(){


        try {
            mSocket = new Socket(serverAddress, port);
            System.out.println("connected to server ....");
            writer = new ObjectOutputStream(mSocket.getOutputStream());
            reader = new ObjectInputStream(mSocket.getInputStream());

            String msg = reader.readUTF();
            System.out.println("Server :" + msg);

            //Thread thread = new Thread(reader);

            writer.writeUTF(scanner.nextLine());
            writer.flush();
//
//            msg = reader.readUTF();
//            System.out.println(msg);
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

               // scanner.nextLine();
//                if(scanner.hasNext()){
//                    writer.writeUTF(scanner.nextLine());
//                    writer.flush();
//                }
               // decode(text);
            }
            catch (IOException e) {
                System.out.println(e);
            }

        }

    }

    public void showMessage(String text){
        String[] str = text.split("/");

        switch (str[0]) {
            case "Question":
                String question = str[1];
                String[] options = str[2].split(",");

                System.out.println("Question: " + question);
                for (int i = 0; i < options.length; i++){
                    String option = options[i].substring(1, options[i].length() - 1);
                    System.out.println((i + 1) + ") " + option + "\n");
                }
               // scanner.nextInt();
                try {

                    class sendAnswer extends Thread {
                        private String answer;

                        public sendAnswer() {
                            answer = "-1";
                        }

                        @Override
                        public void run() {
                            Scanner sc = new Scanner(System.in);
                            int tmp = sc.nextInt();
                            answer = Integer.toString(tmp);
                        }

                        public String myStop() {
                            super.stop();
                            return answer;
                        }
                    }

                    sendAnswer sa = new sendAnswer();
                    sa.start();
                    sa.join(15000);
                    String out = sa.myStop();
                    //System.out.println(out);
                    writer.writeUTF(out);
                    writer.flush();

                } catch (IOException | InterruptedException e) {
                    e.printStackTrace();
                }
                //doest work if the client decides not to answer a question because scanner waits for input

                break;

            default:
                System.out.println(text);
//            case "scoreboard":
//                String[] list=str[1].substring(1,str[1].length()-1).split(",");
//                showScoreBoard(list);
//                break;
//            case "msg":
//                showPrivateMsg(str[1],str[2]);
//                break;
//            case "GlobalMsg":
//                showGlobalMsg(str[1],str[2]);
//                break;
            //input.useDelimiter("~~end~~");

        }
    }

    public static void main(String[] args) {
        Thread t = new Thread(new Client());
        t.start();
    }
}

