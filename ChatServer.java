import java.io.IOException;
import java.io.InvalidObjectException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ServerSocket;
import java.net.Socket;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/**
 * ChatServer allows user to join in and exchange message to each other
 *
 * @author [Sole author] Shen Wei Leong L19
 * @version 4/24/2020
 */
final class ChatServer {
    private static int uniqueId = 0;
    private final static List<ClientThread> CLIENTS = new ArrayList<>();
    private static ChatFilter chatFilter;
    private final int port;
    SimpleDateFormat format = new SimpleDateFormat("HH:mm:ss");
    Date date;


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
                Runnable r = null;
                try {
                    r = new ClientThread(socket, uniqueId++);
                } catch (InvalidObjectException e) {
                    System.out.println("Username in use, rejecting client.");
                }
                Thread t = new Thread(r);
                t.start();
                Thread.sleep(500);
                synchronized (CLIENTS) {
                    CLIENTS.add((ClientThread) r);
                }


            }
        } catch (IOException | InterruptedException e) {
            e.printStackTrace();
        }
    }

    /*
     *  > java ChatServer
     *  > java ChatServer portNumber
     *  If the port number is not specified 1500 is used
     */
    public static void main(String[] args) {
        int chatServerPortNumber = 0;
        String chatFilterFile = "";
        if (args.length == 0) {
            chatServerPortNumber = 1500;
            chatFilterFile = "badwords.txt";
        } else if (args.length == 1) {
            chatServerPortNumber = Integer.parseInt(args[0]);
            chatFilterFile = "badwords.txt";
        } else if (args.length == 2) {
            chatServerPortNumber = Integer.parseInt(args[0]);
            chatFilterFile = args[1];
        } else {
            System.out.println("Not supported.");
            return;
        }
        System.out.println("Port number is " + chatServerPortNumber);
        ChatServer server = new ChatServer(chatServerPortNumber);
        chatFilter = new ChatFilter(chatFilterFile);
        server.start();
    }


    /**
     * This is a private class inside of the ChatServer
     * A new thread will be created to run this every time a new client connects.
     *
     * @author [Sole author] Shen Wei Leong L19
     * @version 4/24/2020
     */
    private final class ClientThread implements Runnable {
        private Socket socket;
        private ObjectInputStream sInput;
        private ObjectOutputStream sOutput;
        private int id;
        private String username;
        private ChatMessage cm;

        private ClientThread(Socket socket, int id) throws InvalidObjectException {
            this.id = id;
            this.socket = socket;

            try {
                sOutput = new ObjectOutputStream(socket.getOutputStream());
                sInput = new ObjectInputStream(socket.getInputStream());
                username = (String) sInput.readObject();
            } catch (IOException | ClassNotFoundException e) {
                e.printStackTrace();
            }

            if (CLIENTS.size() > 0) {

                for (ClientThread clientThread : CLIENTS) {
                    if (clientThread != null) {
                        if (clientThread.getUsername().equals(username)) {
                            try {
                                sOutput.writeObject("Username is already in use.");
                                remove(id);
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                            throw new InvalidObjectException("Duplicated Username");
                        }
                    }
                }
            }

        }

        /*
         * This is what the client thread actually runs.
         */
        @Override
        public void run() {
            // Read the username sent to you by client

            if (!socket.isClosed()) {
                System.out.println(username + " has joined the server");
                writeMessage("Welcome " + username, "Server", this);
                broadcast(username + " has joined the server.", "Server");
            }
            while (!socket.isClosed()) {
                try {
                    cm = (ChatMessage) sInput.readObject();
                    if (cm.getType() == 1) {
                        String logoutMsg = username + " has logged out.";
                        broadcast(logoutMsg, username);
                        remove(this.id);
                        System.out.println(logoutMsg);
                        return;
                    } else if (cm.getType() == 2) {
                        directMessage(cm.toString(), cm.getRecipient());
                    } else if (cm.getType() == 3) {
                        CLIENTS.remove(null);
                        for (ClientThread clientThread : CLIENTS) {
                            try {
                                if (clientThread.username != this.username) {
                                    sOutput.writeObject(clientThread.username);
                                }
                            } catch (IOException e) {
                                e.printStackTrace();
                            }
                        }
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

        private void directMessage(String message, String name) {
            synchronized (CLIENTS) {
                for (ClientThread recipient : CLIENTS) {
                    if (recipient.username.equals(name)) {
                        writeMessage(message, this.username, recipient);
                        return;
                    }
                }
                writeMessage("Username not found. Message not sent", "Server", this);
            }
        }


        private void broadcast(String message, String sender) {
            if (CLIENTS.size() == 0) {
                System.out.println("There is no one in the chat group.");
            } else {
                if (!socket.isClosed()) {
                    CLIENTS.remove(null);
                    synchronized (CLIENTS) {
                        for (ClientThread receiver : CLIENTS) {
                            writeMessage(message, sender, receiver);
                        }
                    }
                }
            }
        }

        private boolean writeMessage(String message, String sender, ClientThread receiver) {
            if (!socket.isConnected()) {
                return false;
            } else {
                date = new Date();
                message = chatFilter.filter(message);
                System.out.printf("[%s] Writing %s from %s to %s...\n", format.format(date), message,
                        sender, receiver.getUsername());
                Runnable r = new BroadcastThread(receiver, message, sender);
                Thread t = new Thread(r);
                t.start();
                return true;
            }
        }

        private void remove(int idRemove) {
            for (ClientThread c : CLIENTS) {
                CLIENTS.remove(null);
                if (c.id == idRemove) {
                    System.out.println("removing " + c.getUsername());
                    synchronized (CLIENTS) {
                        CLIENTS.remove(c);
                        CLIENTS.remove(null);
                    }
                    c.close();
                    break;
                }
            }
        }

        private void close() {
            try {
                remove(id);
                sInput.close();
                sOutput.close();
                socket.close();
            } catch (IOException ignored) {
                System.out.println("Socket Closed");
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
    /**
     * This is a private class inside of the ChatClient
     * It will be responsible for listening for messages from the ChatServer.
     * ie: When other clients send messages, the server will relay it to the client.
     *
     * @author [Sole author] Shen Wei Leong L19
     * @version 4/24/2020
     */
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
                if (receiver.socket.isClosed()) {
                    return;
                } else {
                    if (!receiver.socket.isClosed()) {
                        String output = String.format("[%s] %s: %s", format.format(date), sender, message);
                        receiver.sOutput.writeObject(output);
                        receiver.sOutput.flush();
                    }
                }
            } catch (IOException e) {
                System.out.println("User not in server");
            }
        }
    }
}
