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
    private int id;
    private List<Message> messages;
    private List<Message> notProcessedMsg;
    private int nextMsg;
    private boolean waiting;
    private List<String> clientNames;
    private List<String> latestMembershipInfo;

    private ReentrantLock lockStock;
    private ReentrantLock lockMessages;
    private ReentrantLock lockNotProcessedMsg;
    private ReentrantLock lockNextMsg;
    private ReentrantLock lockWaiting;
    private ReentrantLock lockClientNames;
    private ReentrantLock lockInfo;

    public Server(int id){
        this.stock = new StockImp();
        this.id = id;
        this.messages = new ArrayList<>();
        this.notProcessedMsg = new ArrayList<>();
        this.nextMsg = 0;
        this.waiting = true;
        this.clientNames = new ArrayList<>();
        this.latestMembershipInfo = new ArrayList<>();

        this.lockStock = new ReentrantLock();
        this.lockMessages = new ReentrantLock();
        this.lockNotProcessedMsg = new ReentrantLock();
        this.lockNextMsg = new ReentrantLock();
        this.lockWaiting = new ReentrantLock();
        this.lockClientNames = new ReentrantLock();
        this.lockInfo = new ReentrantLock();
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

    public Message getMessage(){
        return this.messages.get(this.getNextMsg());
    }

    public List<Message> getNotProcessedMsg(){
        return this.notProcessedMsg;
    }

    public int getNextMsg() {
        return this.nextMsg;
    }

    public void setNextMsg(int nextMsg) {
        this.nextMsg = nextMsg;
    }

    public boolean isWaiting(){
        return this.waiting;
    }

    public void setWaiting(boolean b){
        this.waiting = b;
    }

    public List<String> getClientNames(){
        return this.clientNames;
    }

    public List<String> getLatestMembershipInfo(){
        List<String> result = new ArrayList<>(this.latestMembershipInfo);

        return result;
    }

    public void setLatestMembershipInfo(SpreadMessage sm){
        this.latestMembershipInfo = getServersNames(sm);
    }

    public void addMsg(Message m){
        this.messages.add(m);
    }

    public void addNotProcessedMsg(Message m){
        this.notProcessedMsg.add(m);
    }

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

    public List<String> getServersNames(SpreadMessage msg){
        List<String> allActiveServers = new ArrayList<>();

        for(SpreadGroup g : msg.getMembershipInfo().getMembers()){
            for(int i=1; i<g.toString().length(); i++){
                if (g.toString().charAt(i)=='#'){
                    String serverName = g.toString().substring(1,i);
                    if(serverName.charAt(0)=='s'){
                        allActiveServers.add(serverName);
                    }
                    i=g.toString().length();
                }
            }
        }

        return allActiveServers;
    }

    public ReentrantLock getLockStock() {
        return lockStock;
    }

    public ReentrantLock getLockMessages() {
        return lockMessages;
    }

    public ReentrantLock getLockNotProcessedMsg() {
        return lockNotProcessedMsg;
    }

    public ReentrantLock getLockNextMsg() {
        return lockNextMsg;
    }

    public ReentrantLock getLockWaiting() {
        return lockWaiting;
    }

    public ReentrantLock getLockClientNames() {
        return lockClientNames;
    }

    public ReentrantLock getLockInfo() {
        return lockInfo;
    }

    public void lockAll(){
        this.lockStock.lock();
        this.lockMessages.lock();
        this.lockNotProcessedMsg.lock();
        this.lockNextMsg.lock();
        this.lockWaiting.lock();
        this.lockClientNames.lock();
        this.lockInfo.lock();
    }

    public void unlockAll(){
        this.lockStock.unlock();
        this.lockMessages.unlock();
        this.lockNotProcessedMsg.unlock();
        this.lockNextMsg.unlock();
        this.lockWaiting.unlock();
        this.lockClientNames.unlock();
        this.lockInfo.unlock();
    }

    public String toString(){
        StringBuilder sb = new StringBuilder();

        sb.append("-----SERVIDOR ").append(this.id).append("-----\n\n");
        sb.append(this.stock.toString()).append("\n");

        sb.append("Processed messages\n");
        for(Message m: this.messages){
            sb.append(m.toString());
        }
        sb.append("\n");

        sb.append("Next message: ").append(this.nextMsg).append("\n");

        sb.append("Not processed messages\n");
        for(Message m: this.notProcessedMsg){
            sb.append(m.toString());
        }
        sb.append("\n");

        sb.append("Clients names\n");
        for(String s: this.clientNames){
            sb.append(s).append("\n");
        }

        return sb.toString();
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

    public static void main(final String[] args){
        int id = Integer.parseInt(args[0]);
        Server se = loadState(id, "server"+id+"DB");

        Serializer s = Serializer.builder()
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
                    .withTypes(MembershipInfoRequest.class)
                    .build();

        Middleware middlewareS = new Middleware("sender_s"+args[0], "servergroup");
        Middleware middlewareR = new Middleware("receiv_s"+args[0], "servergroup");

        SpreadMessage spreadMessage = middlewareR.receiveMessage();
        se.setLatestMembershipInfo(spreadMessage);

        System.out.println("\nNew membership message from " + spreadMessage.getMembershipInfo().getGroup());
        for(SpreadGroup g : spreadMessage.getMembershipInfo().getMembers()){
            System.out.println(g.toString());
        }

        if(spreadMessage.getMembershipInfo().getMembers().length == 2){
            se.setWaiting(false);
            System.out.println("No need to wait. I am alone.");
        }
        else {
            EstadoRequest est = new EstadoRequest("servergroup", se.getId(), se.getNextMsg());
            System.out.println(est.toString());
            middlewareS.sendMessage(s.encode(est), "servergroup");
        }

        Thread refresher1 = new Thread(new Refresher(1, se, middlewareS));
        Thread refresher2 = new Thread(new Refresher(2, se, middlewareS));
        refresher1.start();
        refresher2.start();

        boolean guardar = false;

        while(true){
            SpreadMessage msg = middlewareR.receiveMessage();

            if(msg.isRegular()){
                Message m = s.decode(msg.getData());

                se.getLockWaiting().lock();
                if(se.isWaiting()){
                    if(m instanceof EstadoReply && ((EstadoReply) m).getServerId() == se.getId()){
                        se.getLockMessages().lock();
                        se.addMsg(m);
                        se.getLockMessages().unlock();
                    }
                    else{
                        if(!guardar && m instanceof EstadoRequest && ((EstadoRequest) m).getServerID() == se.getId()) guardar = true;
                        if(guardar){
                            se.getLockNotProcessedMsg().lock();
                            se.addNotProcessedMsg(m);
                            se.getLockNotProcessedMsg().unlock();
                        }
                    }
                }
                else{
                    se.getLockMessages().lock();
                    se.addMsg(m);
                    se.getLockMessages().unlock();
                }
                se.getLockWaiting().unlock();

                se.lockAll();
                se.storeState("server"+id+"DB");
                se.writeInTextFile("server"+id+"TXT");
                se.unlockAll();
            }
            else{
                se.getLockInfo().lock();
                se.setLatestMembershipInfo(spreadMessage);
                se.getLockInfo().unlock();

                System.out.println("\nNew membership message from " + spreadMessage.getMembershipInfo().getGroup());
                for(SpreadGroup g : spreadMessage.getMembershipInfo().getMembers()){
                    System.out.println(g.toString());
                }

                int number = spreadMessage.getMembershipInfo().getMembers().length;
                if(number == 2){
                    se.getLockWaiting().lock();
                    if(se.isWaiting()){
                        se.setWaiting(false);
                        System.out.println("Stoped waiting");
                    }
                    se.getLockWaiting().unlock();
                }

                MembershipInfoReply mi = new MembershipInfoReply(-1, "", id, number, se.getServersNames(spreadMessage));

                se.getLockClientNames().lock();
                for(String name: se.getClientNames()){
                    middlewareS.sendMessage(s.encode(mi), name);
                }
                se.getLockClientNames().unlock();
            }
        }
    }
}
