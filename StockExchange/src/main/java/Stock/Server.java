package Stock;

import Common.*;

import Common.Messages.*;
import Common.Messages.MembershipInfo;
import io.atomix.utils.serializer.Serializer;
import spread.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;

public class Server implements Serializable {
    private StockImp stock;
    private Serializer s;
    private int id;
    private List<Message> messages;
    private List<Message> notProcessedMsg;
    private int nextMsg;
    private boolean waiting;
    private List<String> clientNames;

    public Server(int id){
        this.stock = new StockImp();
        this.s = Serializer.builder()
                .withTypes(ActionsRequest.class)
                .withTypes(ActionsReply.class)
                .withTypes(BuyRequest.class)
                .withTypes(BuyReply.class)
                .withTypes(CompaniesRequest.class)
                .withTypes(CompaniesReply.class)
                .withTypes(SellRequest.class)
                .withTypes(SellReply.class)
                .withTypes(EstadoRequest.class)
                .withTypes(EstadoReply.class)
                .withTypes(MembershipInfo.class)
                .build();
        this.id = id;
        this.messages = new ArrayList<>();
        this.notProcessedMsg = new ArrayList<>();
        this.nextMsg = 0;
        this.waiting = true;
        this.clientNames = new ArrayList<>();
    }

    public StockImp getStock() {
        return this.stock;
    }

    public int getId() {
        return id;
    }

    public List<Message> getMessages() {
        return this.messages;
    }

    public List<Message> getNotProcessedMsg(){ return this.notProcessedMsg;}

    public int getNextMsg() {
        return this.nextMsg;
    }

    public void setNextMsg(int nextMsg) {
        this.nextMsg = nextMsg;
    }

    public Serializer getS() {
        return this.s;
    }

    public boolean isWaiting(){
        return this.waiting;
    }

    public void setWaiting(boolean b){
        this.waiting = b;
    }

    public List<String> getClientNames(){ return this.clientNames;}

    public void addMsg(Message m){
        this.messages.add(m);
    }

    public void addNotProcessedMsg(Message m){ this.notProcessedMsg.add(m); }

    public void removeNotProcessedMsg(Message m){
        for(int i=0; i<this.notProcessedMsg.size(); i++){
            Message tmp = this.notProcessedMsg.get(i);
            if(tmp.getTransactionID() == m.getTransactionID() && tmp.getClientName() == m.getClientName()){
                this.notProcessedMsg.remove(i);
                break;
            }
        }
    }

    public void addClientName(String name){
        if(!this.clientNames.contains(name)) this.clientNames.add(name);
    }

    synchronized void writeInTextFile(String fileName) {
        try {
            PrintWriter fich = new PrintWriter(fileName);
            fich.println(this.toString());
            fich.flush();
            fich.close();
        } catch (IOException e) {
            System.out.println("Error saving state in text file.");
        }
    }

    synchronized void storeState(String fileName) {
        try {
            FileOutputStream fos = new FileOutputStream(fileName);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this);
            oos.flush();
            oos.close();
            fos.close();
        } catch (IOException e) {
            System.out.println("Error saving state.");
        }
    }

    synchronized private static Server loadState(int id, String fileName) {
        Server server = new Server(id);

        try {
            FileInputStream fis = new FileInputStream(fileName);
            ObjectInputStream ois = new ObjectInputStream(fis);
            server = (Server) ois.readObject();
            ois.close();
            fis.close();
        } catch (IOException | ClassNotFoundException e) {
            System.out.println("Could not find previous state.");
        }

        return server;
    }

    //todo : faltam os synchronized ou sockets
    public static void main(final String[] args){
        int id = Integer.parseInt(args[0]);
        Server se = loadState(id, "server"+id+"DB");
        Middleware middlewareS = new Middleware("sender_server"+args[0], "servergroup");
        Middleware middlewareR = new Middleware("receiver_server"+args[0], "servergroup");

        SpreadMessage spreadMessage = middlewareR.receiveMessage();

        System.out.println("\nNew membership message from " + spreadMessage.getMembershipInfo().getGroup());
        for(SpreadGroup g : spreadMessage.getMembershipInfo().getMembers()){
            System.out.println(g.toString());
        }

        if(spreadMessage.getMembershipInfo().getMembers().length == 1){
            se.setWaiting(false);
            System.out.println("No need to wait. I am alone.");
        }
        else {
            EstadoRequest est = new EstadoRequest(se.getId(), se.getNextMsg());
            System.out.println(est.toString());
            middlewareS.sendMessage(se.getS().encode(est), "servergroup");
        }

        Thread refresher = new Thread(new Refresher(se, middlewareS));
        refresher.start();

        while(true){
            SpreadMessage msg = middlewareR.receiveMessage();

            if(msg.isRegular()){
                Message m = se.getS().decode(msg.getData());

                if(se.isWaiting()){
                    se.addNotProcessedMsg(m);
                }
                else{
                    se.addMsg(m);
                }

                se.storeState("server"+id+"DB");
                se.writeInTextFile("server"+id+"TXT");
            }
            else{
                System.out.println("\nNew membership message from " + spreadMessage.getMembershipInfo().getGroup());
                for(SpreadGroup g : spreadMessage.getMembershipInfo().getMembers()){
                    System.out.println(g.toString());
                }

                int number = spreadMessage.getMembershipInfo().getMembers().length;
                if(number == 1){
                    if(se.isWaiting()){
                        se.setWaiting(false);
                        System.out.println("Stoped waiting");
                    }
                }

                MembershipInfo mi = new MembershipInfo(id, number);
                for(String name: se.getClientNames()){
                    middlewareS.sendMessage(se.getS().encode(mi), name);
                }
            }
        }
    }
}
