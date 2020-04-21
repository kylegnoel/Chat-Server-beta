import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.net.ConnectException;
import java.net.Socket;
import java.net.UnknownHostException;
import java.util.Scanner;

/**
 * [Add your documentation here]
 *
 * @author your name and section
 * @version date
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
            e.printStackTrace();
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
            sOutput.writeObject(msg);
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

        boolean validUsername = ChatServer.isValidUsername(username);
        System.out.println(validUsername);

        if (!validUsername) {
            System.out.println("Username is already in use.");
            return;
        }

        // Create your client and start it
        ChatClient client = new ChatClient(serverAddress, portNumber, username);
        client.start();

        // Send an empty message to the server
        String s = String.format("%s joined the server", username);
        client.sendMessage(new ChatMessage(s, 0));
    }


    /**
     * This is a private class inside of the ChatClient
     * It will be responsible for listening for messages from the ChatServer.
     * ie: When other clients send messages, the server will relay it to the client.
     *
     * @author your name and section
     * @version date
     */
    private final class ListenFromServer implements Runnable {
        boolean loop1 = true;

        public void run() {
            Runnable r = new WriteThread();
            Thread t = new Thread(r);
            t.start();
            while (t.isAlive()) {
                try {
                    String msg = (String) sInput.readObject();
                    System.out.println(msg);

                } catch (IOException | ClassNotFoundException e) {
                    loop1 = false;
                }
            }
        }
    }

    private final class WriteThread implements Runnable {

        @Override
        public void run() {
            while (true) {
                String input = scan.nextLine();
                if (input.equals("/logout")) {
                    try {
                        sendMessage(new ChatMessage(input, 1));
                        sInput.close();
                        sOutput.close();
                        return;
                    } catch (IOException e) {
                        e.printStackTrace();
                    }

                } else {
                    sendMessage(new ChatMessage(input, 0));
                }
            }
        }
    }
}

