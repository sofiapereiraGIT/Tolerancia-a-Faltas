package Common;

import java.util.ArrayList;
import java.util.List;

public class EstadoReply extends Message {
    private int serverId;
    private List<Message> messages;

    public EstadoReply(int server, List<Message> m){
        super(-1,"");
        this.serverId = server;
        this.messages = new ArrayList<>();
        this.setMessages(m);
    }

    public int getServerId(){
        return this.serverId;
    }

    public List<Message> getMessages() {
        return messages;

    }

    public void setMessages(List<Message> message){
        this.messages = message;
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append("EstadoReply para o servidor ").append(this.getServerId());

        return sb.toString();
    }
}
