package Client;

import Common.*;
import Common.Messages.*;
import io.atomix.utils.serializer.Serializer;
import spread.SpreadGroup;
import spread.SpreadMessage;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;

public class ClientStubThread implements Runnable {
    private final String myGroupName;
    private final Middleware middleware;

    private final Serializer s;

    private final Map<Integer, CompletableFuture<Map<String, Long>>> CFcompanies;
    private final Map<Integer, CompletableFuture<Map<String, Long>>> CFactions;
    private final Map<Integer, CompletableFuture<Map<String, Boolean>>> CFbuy;
    private final Map<Integer, CompletableFuture<Map<String, Boolean>>> CFsell;

    private final ReentrantLock lockCFcompanies;
    private final ReentrantLock lockCFactions;
    private final ReentrantLock lockCFbuy;
    private final ReentrantLock lockCFsell;

    private List<String> allActiveServers;
    private final Map<Integer, List<String>> waitingFromServers;
    private final Map<Integer, List<String>> receivedFromServers = new HashMap<>();
    private final Map<Integer, Message> receivedMessages = new HashMap<>();

    private final ReentrantLock lockAllActiveServers;
    private final ReentrantLock lockWaitingFromServers;
    private final ReentrantLock lockReceivedFromServers = new ReentrantLock();
    private final ReentrantLock lockReceivedMessages = new ReentrantLock();

    ClientStubThread(String port, Map<Integer, CompletableFuture<Map<String, Long>>> CFcompanies, Map<Integer, CompletableFuture<Map<String, Long>>> CFactions,
                     Map<Integer, CompletableFuture<Map<String, Boolean>>> CFbuy, Map<Integer, CompletableFuture<Map<String, Boolean>>> CFsell,
                     ReentrantLock lockCFcompanies, ReentrantLock lockCFactions, ReentrantLock lockCFbuy, ReentrantLock lockCFsell,
                     Map<Integer, List<String>> waitingFromServers, List<String> allActiveServers,
                     ReentrantLock lockWaitingFromServers, ReentrantLock lockAllActiveServers) {
        this.myGroupName = "stubgroup" + port;
        this.middleware = new Middleware("receiver_stub" + port, this.myGroupName);

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

        this.CFcompanies = CFcompanies;
        this.CFactions = CFactions;
        this.CFbuy = CFbuy;
        this.CFsell = CFsell;

        this.lockCFcompanies = lockCFcompanies;
        this.lockCFactions = lockCFactions;
        this.lockCFbuy = lockCFbuy;
        this.lockCFsell = lockCFsell;

        this.waitingFromServers = waitingFromServers;
        this.allActiveServers = allActiveServers;

        this.lockWaitingFromServers = lockWaitingFromServers;
        this.lockAllActiveServers = lockAllActiveServers;
    }

    @Override
    public void run() {
        //First message sent
        byte[] msgBytes = this.s.encode(new MembershipInfoRequest(0, this.myGroupName));
        this.middleware.sendMessage(msgBytes, "servergroup");

        while (true) {
            SpreadMessage spreadMessage = this.middleware.receiveMessage();

            if (spreadMessage.isRegular()) {
                Message msg = s.decode(spreadMessage.getData());
                System.out.println("New message (" + msg.toString() + ") from " + spreadMessage.getSender());

                if (msg instanceof MembershipInfoReply) {
                    MembershipInfoReply reply = (MembershipInfoReply) msg;
                    serversInfo(reply);
                }

                else {
                    //Received from (sender name)
                    SpreadGroup g = spreadMessage.getSender();
                    for(int i=1; i<g.toString().length(); i++) {
                        if (g.toString().charAt(i)=='#'){
                            i=g.toString().length();
                        }
                        else {
                            String serverName = g.toString().substring(1,i+1);
                            this.lockReceivedFromServers.lock();
                            if(!this.receivedFromServers.containsKey(msg.getTransactionID())){
                                this.receivedFromServers.put(msg.getTransactionID(), new ArrayList<>());
                            }
                            this.receivedFromServers.get(msg.getTransactionID()).add(serverName);
                            this.lockReceivedFromServers.unlock();
                        }
                    }

                    //Verify if it already received from everyone
                    this.lockWaitingFromServers.lock();
                    this.lockReceivedFromServers.lock();
                    boolean send = true;
                    System.out.println("Waiting for - Received");
                    for(String s : this.waitingFromServers.get(msg.getTransactionID())) {
                        if (!receivedFromServers.get(msg.getTransactionID()).contains(s)) {
                            send = false;
                            System.out.println(s+" - false");
                        }
                        else {
                            System.out.println(s+" - true");
                        }
                    }
                    this.lockReceivedMessages.lock();
                    if(send==true){
                        System.out.println("I received from everyone! Show message to client.");
                        this.receivedMessages.put(msg.getTransactionID(), msg); //because of the remove
                        answerMsg(msg);
                        this.waitingFromServers.remove(msg.getTransactionID());
                        this.receivedFromServers.remove(msg.getTransactionID());
                        this.receivedMessages.remove(msg.getTransactionID());
                    }
                    else {
                        System.out.println("I did not receive from everyone. Waiting for the others.");
                        this.receivedMessages.put(msg.getTransactionID(), msg);
                    }
                    this.lockReceivedMessages.unlock();
                    this.lockReceivedFromServers.unlock();
                    this.lockWaitingFromServers.unlock();
                }
            } else {
                System.out.println("New membership message from " + spreadMessage.getMembershipInfo().getGroup());

                System.out.println("Current group members:");
                for (SpreadGroup g : spreadMessage.getMembershipInfo().getMembers()) {
                    System.out.println(g.toString());
                }
            }
        }
    }

    private void serversInfo(MembershipInfoReply reply) {
        //Atualizar allActiveServers
        this.lockAllActiveServers.lock();
            this.allActiveServers = reply.getAllActiveServers();
            List<String> auxAllActiveServers = new ArrayList<>(this.allActiveServers);
            System.out.println("Current active servers: "+auxAllActiveServers.toString());
        this.lockAllActiveServers.unlock();

        //Atualizar os servidores por quais se espera X msg
        this.lockWaitingFromServers.lock();
            Map<Integer, List<String>> auxMap = new HashMap<>();
            for (Map.Entry<Integer, List<String>> entry : this.waitingFromServers.entrySet()){
                List<String> auxList = new ArrayList<>(entry.getValue());
                auxMap.put(entry.getKey(), auxList);
            }

            for (Map.Entry<Integer, List<String>> entry : auxMap.entrySet()){
                System.out.println("Message "+entry.getKey()+" is waiting for:");
                for(String s : entry.getValue()){
                    System.out.print(s+" ");
                    if(!auxAllActiveServers.contains(s))
                        System.out.print("(removed) ");
                        this.waitingFromServers.get(entry.getKey()).remove(s);
                }
            }

            //Verificar se já se pode responder a X msg
            List<Integer> removedKeys = new ArrayList<>();
            this.lockReceivedFromServers.lock();
            this.lockReceivedMessages.lock();
                for (Map.Entry<Integer, List<String>> entry : this.waitingFromServers.entrySet()){
                    System.out.println("--- Message "+entry.getKey()+" ---");
                    boolean send = true;
                    if(!this.receivedFromServers.containsKey(entry.getKey())){
                        send = false;
                        System.out.println("Haven't received from anyone yet.");
                    }
                    else {
                        System.out.println("Waiting for - Received");
                        for(String s : entry.getValue()) {
                            if(!this.receivedFromServers.get(entry.getKey()).contains(s)){
                                send=false;
                                System.out.println(s+" - false");
                            }
                            else {
                                System.out.println(s+" - true");
                            }
                        }
                    }
                    if(send==true){
                        System.out.println("I received from everyone! Show message to client.");
                        answerMsg(this.receivedMessages.get(entry.getKey()));
                        removedKeys.add(entry.getKey());
                    }
                    else {
                        System.out.println("I did not receive from everyone.");
                    }
                }

                System.out.print("Processed messages: ");
                for(Integer i : removedKeys){
                    System.out.print(i+" ");
                    this.waitingFromServers.remove(i);
                    this.receivedFromServers.remove(i);
                    this.receivedMessages.remove(i);
                }
            this.lockReceivedMessages.unlock();
            this.lockReceivedFromServers.unlock();
        this.lockWaitingFromServers.unlock();
    }

    private void answerMsg(Message msg) {
        if (msg instanceof CompaniesReply) {
            CompaniesReply reply = (CompaniesReply) msg;

            this.lockCFcompanies.lock();
            this.CFcompanies.get(reply.getTransactionID()).complete(reply.getCompanies());
            this.lockCFcompanies.unlock();

        } else if (msg instanceof ActionsReply) {
            ActionsReply reply = (ActionsReply) msg;

            this.lockCFactions.lock();
            Map<String, Long> map = new HashMap<>();
            map.put(reply.getCompany(), reply.getActions());
            this.CFactions.get(reply.getTransactionID()).complete(map);
            this.lockCFactions.unlock();

        } else if (msg instanceof BuyReply) {
            BuyReply reply = (BuyReply) msg;

            this.lockCFbuy.lock();
            Map<String, Boolean> map = new HashMap<>();
            map.put(reply.getCompany(), reply.getResult());
            this.CFbuy.get(reply.getTransactionID()).complete(map);
            this.lockCFbuy.unlock();

        } else if (msg instanceof SellReply) {
            SellReply reply = (SellReply) msg;

            this.lockCFsell.lock();
            Map<String, Boolean> map = new HashMap<>();
            map.put(reply.getCompany(), reply.getResult());
            this.CFsell.get(reply.getTransactionID()).complete(map);
            this.lockCFsell.unlock();
        }
    }
}
