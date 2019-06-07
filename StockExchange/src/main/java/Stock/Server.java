package Stock;

import Common.*;

import Common.Messages.*;
import Common.Messages.MembershipInfoReply;
import io.atomix.utils.serializer.Serializer;
import spread.*;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.locks.ReentrantLock;

public class Server implements Serializable {
    private StockImp stock;
    private Serializer s;
    private int id;
    private List<Message> messages;
    private List<Message> notProcessedMsg;
    private int nextMsg;
    private boolean waiting;
    private List<String> clientNames;
    private SpreadMessage latestMembershipInfo;

    private ReentrantLock lockStock;
    private ReentrantLock lockMessages;
    private ReentrantLock lockNotProcessedMsg;
    private ReentrantLock lockNextMsg;
    private ReentrantLock lockWaiting;
    private ReentrantLock lockClientNames;
    private ReentrantLock lockInfo;

    public Server(int id){
        this.stock = new StockImp();
        this.s = Serializer.builder()
                .withTypes(ActionsReply.class)
                .withTypes(ActionsRequest.class)
                .withTypes(BuyReply.class)
                .withTypes(BuyRequest.class)
                .withTypes(CompaniesReply.class)
                .withTypes(CompaniesRequest.class)
                .withTypes(SellReply.class)
                .withTypes(SellRequest.class)
                .withTypes(EstadoReply.class)
                .withTypes(EstadoRequest.class)
                .withTypes(MembershipInfoReply.class)
                .build();
        this.id = id;
        this.messages = new ArrayList<>();
        this.notProcessedMsg = new ArrayList<>();
        this.nextMsg = 0;
        this.waiting = true;
        this.clientNames = new ArrayList<>();
        this.latestMembershipInfo = new SpreadMessage();

        this.lockStock = new ReentrantLock();
        this.lockMessages = new ReentrantLock();
        this.lockNotProcessedMsg = new ReentrantLock();
        this.lockNextMsg = new ReentrantLock();
        this.lockWaiting = new ReentrantLock();
        this.lockClientNames = new ReentrantLock();
        this.lockInfo = new ReentrantLock();
    }

    public StockImp getStock() {
        this.lockStock.lock();
        StockImp result = this.stock;
        this.lockStock.unlock();

        return result;
    }

    public int getId() {
        return id;
    }

    public List<Message> getMessages() {
        this.lockMessages.lock();
        List<Message> result = this.messages;
        this.lockMessages.unlock();

        return result;
    }

    public List<Message> getNotProcessedMsg(){
        this.lockNotProcessedMsg.lock();
        List<Message> result = this.notProcessedMsg;
        this.lockNotProcessedMsg.unlock();

        return result;
    }

    public int getNextMsg() {
        this.lockNextMsg.lock();
        int result = this.nextMsg;
        this.lockNextMsg.unlock();

        return result;
    }

    public void setNextMsg(int nextMsg) {
        this.lockNextMsg.lock();
        this.nextMsg = nextMsg;
        this.lockNextMsg.unlock();
    }

    public Serializer getS() {
        return this.s;
    }

    public boolean isWaiting(){
        this.lockWaiting.lock();
        boolean result = this.waiting;
        this.lockWaiting.unlock();

        return result;
    }

    public void setWaiting(boolean b){
        this.lockWaiting.lock();
        this.waiting = b;
        this.lockWaiting.unlock();
    }

    public List<String> getClientNames(){
        this.lockClientNames.lock();
        List<String> result = this.clientNames;
        this.lockClientNames.unlock();

        return result;
    }

    public SpreadMessage getLatestMembershipInfo(){
        this.lockInfo.lock();
        SpreadMessage result = this.latestMembershipInfo;
        this.lockInfo.unlock();

        return result;
    }

    public void setLatestMembershipInfo(SpreadMessage sm){
        this.lockInfo.lock();
        this.latestMembershipInfo = sm;
        this.lockInfo.unlock();
    }

    public void addMsg(Message m){
        this.lockMessages.lock();
        this.messages.add(m);
        this.lockMessages.unlock();
    }

    public void addNotProcessedMsg(Message m){
        this.lockNotProcessedMsg.lock();
        this.notProcessedMsg.add(m);
        this.lockNotProcessedMsg.unlock();
    }

    public void removeNotProcessedMsg(Message m){
        this.lockNotProcessedMsg.lock();
        for(int i=0; i<this.notProcessedMsg.size(); i++){
            Message tmp = this.notProcessedMsg.get(i);
            if(tmp.getTransactionID() == m.getTransactionID() && tmp.getClientName() == m.getClientName()){
                this.notProcessedMsg.remove(i);
                break;
            }
        }
        this.lockNotProcessedMsg.unlock();
    }

    public void addClientName(String name){
        this.lockClientNames.lock();
        if(!this.clientNames.contains(name)) this.clientNames.add(name);
        this.lockClientNames.unlock();
    }

    public List<String> getServersNames(SpreadMessage msg){
        List<String> allActiveServers = new ArrayList<>();

        for(SpreadGroup g : msg.getMembershipInfo().getMembers()){
            for(int i=1; i<g.toString().length(); i++){
                if (g.toString().charAt(i)=='#'){
                    i=g.toString().length();
                }
                else {
                    String serverName = g.toString().substring(1,i+1);
                    if(serverName.charAt(0)=='s'){
                        allActiveServers.add(serverName);
                    }
                }
            }
        }

        return allActiveServers;
    }

    synchronized void writeInTextFile(String fileName) {
        try {
            this.lockClientNames.lock();
            this.lockNotProcessedMsg.lock();
            this.lockMessages.lock();
            this.lockInfo.lock();
            this.lockWaiting.lock();
            this.lockNextMsg.lock();
            this.lockStock.lock();

            PrintWriter fich = new PrintWriter(fileName);
            fich.println(this.toString());
            fich.flush();
            fich.close();

            this.lockStock.unlock();
            this.lockNextMsg.unlock();
            this.lockWaiting.unlock();
            this.lockInfo.unlock();
            this.lockMessages.unlock();
            this.lockNotProcessedMsg.unlock();
            this.lockClientNames.unlock();

        } catch (IOException e) {

            this.lockStock.unlock();
            this.lockNextMsg.unlock();
            this.lockWaiting.unlock();
            this.lockInfo.unlock();
            this.lockMessages.unlock();
            this.lockNotProcessedMsg.unlock();
            this.lockClientNames.unlock();

            System.out.println("Error saving state in text file.");
        }
    }

    synchronized void storeState(String fileName) {
        try {
            this.lockClientNames.lock();
            this.lockNotProcessedMsg.lock();
            this.lockMessages.lock();
            this.lockInfo.lock();
            this.lockWaiting.lock();
            this.lockNextMsg.lock();
            this.lockStock.lock();

            FileOutputStream fos = new FileOutputStream(fileName);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(this);
            oos.flush();
            oos.close();
            fos.close();

            this.lockStock.unlock();
            this.lockNextMsg.unlock();
            this.lockWaiting.unlock();
            this.lockInfo.unlock();
            this.lockMessages.unlock();
            this.lockNotProcessedMsg.unlock();
            this.lockClientNames.unlock();

        } catch (IOException e) {

            this.lockStock.unlock();
            this.lockNextMsg.unlock();
            this.lockWaiting.unlock();
            this.lockInfo.unlock();
            this.lockMessages.unlock();
            this.lockNotProcessedMsg.unlock();
            this.lockClientNames.unlock();

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

    public static void main(final String[] args){
        int id = Integer.parseInt(args[0]);
        Server se = loadState(id, "server"+id+"DB");
        Middleware middlewareS = new Middleware("sender_server"+args[0], "servergroup");
        Middleware middlewareR = new Middleware("receiver_server"+args[0], "servergroup");

        SpreadMessage spreadMessage = middlewareR.receiveMessage();
        se.setLatestMembershipInfo(spreadMessage);

        System.out.println("\nNew membership message from " + spreadMessage.getMembershipInfo().getGroup());
        for(SpreadGroup g : spreadMessage.getMembershipInfo().getMembers()){
            System.out.println(g.toString());
        }

        if(spreadMessage.getMembershipInfo().getMembers().length == 1){
            se.setWaiting(false);
            System.out.println("No need to wait. I am alone.");
        }
        else {
            EstadoRequest est = new EstadoRequest("servergroup", se.getId(), se.getNextMsg());
            System.out.println(est.toString());
            middlewareS.sendMessage(se.getS().encode(est), "servergroup");
        }

        Thread refresher1 = new Thread(new Refresher(se, middlewareS));
        Thread refresher2 = new Thread(new Refresher(se, middlewareS));
        refresher1.start();
        refresher2.start();

        boolean guardar = false;

        while(true){
            SpreadMessage msg = middlewareR.receiveMessage();

            if(msg.isRegular()){
                Message m = se.getS().decode(msg.getData());

                if(se.isWaiting()){
                    if(m instanceof EstadoReply && ((EstadoReply) m).getServerId() == se.getId()) se.addMsg(m);
                    else{
                        if(!guardar && m instanceof EstadoRequest && ((EstadoRequest) m).getServerID() == se.getId()) guardar = true;
                        if(guardar) se.addNotProcessedMsg(m);
                    }
                }
                else{
                    se.addMsg(m);
                }

                se.storeState("server"+id+"DB");
                se.writeInTextFile("server"+id+"TXT");
            }
            else{
                se.setLatestMembershipInfo(spreadMessage);

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

                MembershipInfoReply mi = new MembershipInfoReply(-1, "", id, number, se.getServersNames(spreadMessage));
                for(String name: se.getClientNames()){
                    middlewareS.sendMessage(se.getS().encode(mi), name);
                }
            }
        }
    }
}
