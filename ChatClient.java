import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.Socket;
import java.util.NoSuchElementException;
import java.util.Scanner;

/**
 * Creates clients to join the chat server.
 *
 * @author [Sole author] Shen Wei Leong L19
 * @version 4/24/2020
 */
final class ChatClient {
    private ObjectInputStream sInput;
    private ObjectOutputStream sOutput;
    private Socket socket;

    private final String server;
    private final String username;
    private final int port;


    static Scanner scan = new Scanner(System.in);

    private ChatClient(String server, int port, String username) {
        this.server = server;
        this.port = port;
        this.username = username;
    }

    /*
     * This starts the Chat Client
     */
    private boolean start() {
        // Create a socket
        try {
            socket = new Socket(server, port);
        } catch (IOException e) {
            System.out.println("Server is not running");
            return false;
        }

        // Create your input and output streams
        try {
            sInput = new ObjectInputStream(socket.getInputStream());
            sOutput = new ObjectOutputStream(socket.getOutputStream());
        } catch (IOException e) {
            e.printStackTrace();
        }

        // This thread will listen from the server for incoming messages
        Runnable r = new ListenFromServer();
        Thread t = new Thread(r);
        t.start();

        // After starting, send the clients username to the server.
        try {
            sOutput.writeObject(username);
        } catch (IOException e) {
            e.printStackTrace();
        }

        return true;
    }


    /*
     * This method is used to send a ChatMessage Objects to the server
     */
    private void sendMessage(ChatMessage msg) {
        try {
            if (!socket.isClosed()) {
                sOutput.writeObject(msg);
            }
        } catch (IOException e) {
            e.printStackTrace();
        }
    }


    /*
     * To start the Client use one of the following command
     * > java ChatClient
     * > java ChatClient username
     * > java ChatClient username portNumber
     * > java ChatClient username portNumber serverAddress
     *
     * If the portNumber is not specified 1500 should be used
     * If the serverAddress is not specified "localHost" should be used
     * If the username is not specified "Anonymous" should be used
     */
    public static void main(String[] args) {
        // Get proper arguments and override defaults
        String serverAddress = "";
        int portNumber = 0;
        String username = "";
        if (args.length == 0) {
            serverAddress = "localhost";
            portNumber = 1500;
            username = "Anonymous";
        } else if (args.length == 1) {
            username = args[0];
            serverAddress = "localhost";
            portNumber = 1500;
        } else if (args.length == 2) {
            username = args[0];
            portNumber = Integer.parseInt(args[1]);
            serverAddress = "localhost";
        } else if (args.length == 3) {
            username = args[0];
            portNumber = Integer.parseInt(args[1]);
            serverAddress = args[2];
        }

        // Create your client and start it
        ChatClient client = new ChatClient(serverAddress, portNumber, username);

        client.start();
//        // Send an empty message to the server
//        String s = String.format("%s joined the server", username);
//        client.sendMessage(new ChatMessage(s, 0));

    }


    /**
     * This is a private class inside of the ChatClient
     * It will be responsible for listening for messages from the ChatServer.
     * ie: When other clients send messages, the server will relay it to the client.
     *
     * @author [Sole author] Shen Wei Leong L19
     * @version 4/24/2020
     */
    private final class ListenFromServer implements Runnable {
        public void run() {
            Runnable r = new WriteThread();
            Thread t = new Thread(r);
            t.start();
            while (!socket.isClosed()) {
                try {
                    String msg = (String) sInput.readObject();
                    System.out.println(msg);
                    if (msg.equals("Username is already in use.")) {
                        sInput.close();
                        sOutput.close();
                        socket.close();
                    }
                } catch (IOException | ClassNotFoundException e) {
                    try {
                        sInput.close();
                        sOutput.close();
                        socket.close();

                    } catch (IOException ex) {
                        ex.printStackTrace();
                    }
                    return;
                }
            }
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

    private final class WriteThread implements Runnable {

        @Override
        public void run() {
            try {
                Thread.sleep(500);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
            while (!socket.isClosed()) {
                String input = "";
                String cmd = "";
                try {
                    input = scan.nextLine();
                    cmd = input.split(" ")[0];
                } catch (NoSuchElementException e) {
                    System.out.println("Force quited.");
                }
                if (cmd.equals("/logout")) {
                    try {
                        sendMessage(new ChatMessage(input, 1));
                        sInput.close();
                        sOutput.close();
                        socket.close();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else if (cmd.equals("/msg")) {
                    try {
                        String recipient = input.split(" ")[1];
                        if (recipient.equals(username)) {
                            System.out.println("You cannot send a dm to your self!");
                        } else {

                            String msg = input.substring(cmd.length() + recipient.length() + 2);
                            sendMessage(new ChatMessage(msg, 2, recipient));

                        }
                    } catch (StringIndexOutOfBoundsException | ArrayIndexOutOfBoundsException e) {
                        System.out.println("Invalid command.");
                    }

                } else if (cmd.equals("/list")) {
                    sendMessage(new ChatMessage(cmd, 3));
                } else {
                    sendMessage(new ChatMessage(input, 0));
                }
            }
        }

    }
}

