package Common;

import java.util.ArrayList;
import java.util.List;

public class EstadoReply extends Msg{
    private int serverId;
    private List<Msg> messages;

    public EstadoReply(int server, List<Msg> m){
        super(-1,"");
        this.serverId = server;
        this.messages = new ArrayList<>();
        this.setMessages(m);
    }

    public int getServerId(){
        return this.serverId;
    }

    public List<Msg> getMessages() {
        List<Msg> result = new ArrayList<>();

        for(Msg m: this.messages){
            result.add(m.clone());
        }

        return result;

    }

    public void setMessages(List<Msg> msg){
        this.messages.clear();

        for(Msg m: msg){
            this.messages.add(m.clone());
        }
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append("EstadoReply para o servidor ").append(this.getServerId());

        return sb.toString();
    }
}
