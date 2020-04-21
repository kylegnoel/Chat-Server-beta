import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * [Add your documentation here]
 *
 * @author your name and section
 * @version date
 */
final class ChatServer {
    private static int uniqueId = 0;
    private final static List<ClientThread> clients = new ArrayList<>();
    private final int port;
    SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
    Date date = new Date();


    private ChatServer(int port) {
        this.port = port;
    }

    /*
     * This is what starts the ChatServer.
     * Right now it just creates the socketServer and adds a new ClientThread to a list to be handled
     */
    private void start() {
        try {
            ServerSocket serverSocket = new ServerSocket(port);
            while (true) {
                System.out.println("Waiting for client to connect...");
                Socket socket = serverSocket.accept();
                Runnable r = new ClientThread(socket, uniqueId++);
                Thread t = new Thread(r);
                clients.add((ClientThread) r);
                t.start();
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }

    public static boolean isValidUsername(String username) {
        if (clients.size() == 0) {
            return true;
        } else {
            for (ClientThread c : clients) {
                System.out.println(c.getUsername());
                if (c.getUsername().equals(username)) {
                    return false;
                }
            }
        }
        return true;
    }

    /*
     *  > java ChatServer
     *  > java ChatServer portNumber
     *  If the port number is not specified 1500 is used
     */
    public static void main(String[] args) {
        int chatServerPortNumber = 0;
        if (args.length == 0) {
            chatServerPortNumber = 1500;
        } else if (args.length == 1) {
            chatServerPortNumber = Integer.parseInt(args[0]);
        } else {
            System.out.println("Not supported.");
            return;
        }
        System.out.println("Port number is " + chatServerPortNumber);
        ChatServer server = new ChatServer(chatServerPortNumber);
        server.start();
    }


    /**
     * This is a private class inside of the ChatServer
     * A new thread will be created to run this every time a new client connects.
     *
     * @author your name and section
     * @version date
     */
    private final class ClientThread implements Runnable {
        Socket socket;
        ObjectInputStream sInput;
        ObjectOutputStream sOutput;
        int id;
        String username;
        ChatMessage cm;

        private ClientThread(Socket socket, int id) {
            this.id = id;
            this.socket = socket;
            try {
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput = new ObjectInputStream(socket.getInputStream());
                username = (String) sInput.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }
        }

        /*
         * This is what the client thread actually runs.
         */
        @Override
        public void run() {
            // Read the username sent to you by client
            while (true) {
                try {
                    cm = (ChatMessage) sInput.readObject();
                    if (cm.getType() == 1) {
                        String logoutMsg = username + " has logged out.";
                        broadcast(logoutMsg, username);
                        remove(this.id);
                        System.out.println(logoutMsg);
                        return;
                    } else {
                        broadcast(cm.toString(), username);
                    }
                } catch (IOException | ClassNotFoundException e) {
                    remove(this.id);
                    String lostConnectionMsg = username + " has lost connection.";
                    broadcast(lostConnectionMsg, "Server");
                    System.out.println(lostConnectionMsg);
                    return;
                }


                // Send message back to the client
//                try {
//                    sOutput.writeObject("Server: " + cm.toString() + " " + username);
//                } catch (IOException e) {
//                    e.printStackTrace();
//                }
            }
        }


        private void broadcast(String message, String sender) {
            if (clients.size() == 0) {
                System.out.println("There is no one in the chat group.");
            } else {
                for (ClientThread receiver : clients) {
                    writeMessage(message, sender, receiver);
                }
            }
        }

        private boolean writeMessage(String message, String sender, ClientThread receiver) {
            if (!socket.isConnected()) {
                return false;
            } else {
                System.out.printf("[%s] Broadcasting %s from %s to %s...\n", format.format(date), message,
                        sender, receiver.getUsername());
                Runnable r = new BroadcastThread(receiver, message, sender);
                Thread t = new Thread(r);
                t.start();
                return true;
            }
        }

        private void remove(int id) {
            for (ClientThread c : clients) {
                if (c.id == id) {
                    System.out.println("removing " + c.getUsername());
                    clients.remove(c);
                    break;
                }
            }
        }

        private void close() {
            try {
                sInput.close();
                sOutput.close();
                socket.close();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }


        public int getId() {
            return id;
        }

        public String getUsername() {
            return username;
        }

        public ChatMessage getCm() {
            return cm;
        }
    }

    private final class BroadcastThread implements Runnable {
        ClientThread receiver;
        String message;
        String sender;

        public BroadcastThread(ClientThread receiver, String message, String sender) {
            this.receiver = receiver;
            this.message = message;
            this.sender = sender;
        }

        @Override
        public void run() {
            try {
                String output = String.format("[%s] %s: %s", format.format(date), sender, message);
                receiver.sOutput.writeObject(output);
                receiver.sOutput.flush();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
    }
}
