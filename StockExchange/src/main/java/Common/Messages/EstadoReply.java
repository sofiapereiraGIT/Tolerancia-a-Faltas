package Common.Messages;

import java.util.ArrayList;
import java.util.List;

public class EstadoReply extends Message {
    private int serverID;
    private List<Message> messages;

    public EstadoReply(String c, int server, List<Message> m){
        super(-1, c);
        this.serverID = server;
        this.messages = new ArrayList<>();
        this.setMessages(m);
    }

    public int getServerId(){
        return this.serverID;
    }

    public List<Message> getMessages() {
        return messages;

    }

    public void setMessages(List<Message> message){
        this.messages = message;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append("--- EstadoReply ---\n");
        sb.append("To server: ").append(this.serverID).append("\n");

        for(Message m : this.messages) {
            sb.append("Message: ").append(m.toString()).append("\n");
        }

        return sb.toString();
    }
}
