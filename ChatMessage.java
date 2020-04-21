

import java.io.Serializable;

/**
 *
 * [Add your documentation here]
 *
 * @author your name and section
 * @version date
 */
final class ChatMessage implements Serializable {
    private static final long serialVersionUID = 6898543889087L;
    private String message;
    private int type;

    // Here is where you should implement the chat message object.
    // Variables, Constructors, Methods, etc.
    public ChatMessage (String message, int type) {
        this.message = message;
        this.type = type;
    }

    public String getMessage() {
        return message;
    }

    public int getType() {
        return type;
    }

    @Override
    public String toString() {
        return message;
    }
}
