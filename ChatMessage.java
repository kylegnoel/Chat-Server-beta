

import java.io.Serializable;

/**
 * This class creates a chat message object.
 *
 * @author [Sole author] Shen Wei Leong L19
 * @version 4/24/2020
 */
final class ChatMessage implements Serializable {
    private static final long serialVersionUID = 6898543889087L;
    private String message;
    private int type;
    private String recipient;

    // Here is where you should implement the chat message object.
    // Variables, Constructors, Methods, etc.

    public ChatMessage(String message, int type) {
        this.message = message;
        this.type = type;
    }

    public ChatMessage(String message, int type, String recipient) {
        this.message = message;
        this.type = type;
        this.recipient = recipient;
    }

    public String getMessage() {
        return message;
    }

    public int getType() {
        return type;
    }

    public String getRecipient() {
        return recipient;
    }

    @Override
    public String toString() {
        return message;
    }
}
