import java.io.*;
import java.net.Socket;

public class ClientHandler extends Thread{
//    private String name;
//    private Socket socket;
//    private InputStream fromClientStream;
//    private OutputStream toClientStream;
//    DataInputStream reader;
//    PrintWriter writer;
//
//
//    public ClientHandler(Socket socket) {
//        this.socket = socket;
//        clientCount++;
//        start();
//    }
//
//    private void receive() throws IOException {
//        String message = reader.readLine();
//        if (message == null) {
//            return;
//        }
//            /*else if (message != null && message.endsWith(currentEntry.getValue().trim().toLowerCase())) {
//                // Award player
//                sendMessage(name + " had the correct answer! 1 Point awarded!");
//                System.out.println("[Server] - " + name + " had the correct answer! 1 Point awarded!");
//                names.put(name, names.get(name) + 1);
//                // Unable to get more points
//                currentEntry.setValue("waiting for next question");
//                // Update score
//                getOnlineUsers();
//            } else if (message.contains("/SCORE")) {
//                System.out.println(currentEntry.getValue());
//                getScore();
//                getOnlineUsers();
//            } else if (message.contains("/QUIT")) {
//                getScore();
//                getOnlineUsers();
//            } else if (message.contains("/DISCONNECT")) {
//                getScore();
//                getOnlineUsers();
//            }
//
//             */
//        else
//            System.out.println(message);
//
//        // Send to all clients
//        for (PrintWriter writer : writers) {
//            writer.println(message);
//        }
//    }
//
//    public void run() {
//        try {
//            fromClientStream = socket.getInputStream();
//            toClientStream = socket.getOutputStream();
//
//            reader = new DataInputStream(fromClientStream);
//            writer = new PrintWriter(toClientStream, true);
//
//            writer.println(" Please enter your name.");
//            // Check username
//            while (true) {
//                name = reader.readLine();
//                if (name == null) {
//                    return;
//                }
//                synchronized (names) {
//                    if (!names.containsKey(name)) {
//                        names.put(name, 0);
//                        break;
//                    }
//                }
//            }
//            sendMessage(name + " has joined the server");
//
//            // Add printWriter to list
//            writers.add(writer);
//
//            // Update current online users
//            //getOnlineUsers();
//
//            // Accept messages from client and broadcast them.
//            while (true) {
//                receive();
//            }
//
//
//        } catch (IOException e) {
//            System.out.println(e);
//        }
//        finally {
//            if (name != null) {
//                names.remove(name);
//            }
//            if (writer != null) {
//                writers.remove(writer);
//            }
//            try {
//                socket.close();
//            } catch (IOException e) {
//                System.out.print(e);
//            }
//        }
//    }

}