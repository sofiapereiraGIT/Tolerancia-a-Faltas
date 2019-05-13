package Common;

import spread.SpreadConnection;
import spread.SpreadException;
import spread.SpreadGroup;
import spread.SpreadMessage;
import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.UnknownHostException;

public class Middleware {

    private SpreadConnection connection;

    public Middleware(String connectionName, String groupName){
        this.connection = new SpreadConnection();

        try {
            this.connection.connect(InetAddress.getByName("localhost"), 0, connectionName, false, true);
        } catch (SpreadException | UnknownHostException e) {
            e.printStackTrace();
        }

        try {
            SpreadGroup group = new SpreadGroup();
            group.join(this.connection, groupName);
        } catch (SpreadException e) {
            e.printStackTrace();
        }
    }

    public void sendMessage(byte[] msgBytes, String groupName){
        SpreadMessage spreadMessage = new SpreadMessage();
        spreadMessage.setData(msgBytes);
        spreadMessage.addGroup(groupName);
        spreadMessage.setReliable();

        try {
            this.connection.multicast(spreadMessage);
        } catch (SpreadException e) {
            e.printStackTrace();
        }

        spreadMessage = null;
    }

    public SpreadMessage receiveMessage(){
        try {
            return this.connection.receive();
        } catch (SpreadException | InterruptedIOException e) {
            e.printStackTrace();
            return null;
        }
    }
}
