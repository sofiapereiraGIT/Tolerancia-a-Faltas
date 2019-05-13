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

    ClientStubThread(String port, Map<Integer, CompletableFuture<Map<String, Long>>> CFcompanies, Map<Integer, CompletableFuture<Map<String, Long>>> CFactions, Map<Integer, CompletableFuture<Map<String, Boolean>>> CFbuy, Map<Integer, CompletableFuture<Map<String, Boolean>>> CFsell, ReentrantLock lockCFcompanies, ReentrantLock lockCFactions, ReentrantLock lockCFbuy, ReentrantLock lockCFsell) {
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
                .withTypes(MembershipInfo.class)
                .build();

        this.CFcompanies = CFcompanies;
        this.CFactions = CFactions;
        this.CFbuy = CFbuy;
        this.CFsell = CFsell;

        this.lockCFcompanies = lockCFcompanies;
        this.lockCFactions = lockCFactions;
        this.lockCFbuy = lockCFbuy;
        this.lockCFsell = lockCFsell;
    }

    @Override
    public void run() {
        Map<Integer, List<String>> waitingFromServers = new HashMap<>();
        Map<Integer, Integer> receivedFromServers = new HashMap<>();
        List<String> allActiveServers = new ArrayList<>();

        //todo send membershipInfoRequest id=0
        /* espera de 4 servidores
        - 1 morre -> espero por 3
        - 1 entra -> espero por 4
        - 1 morre -> depende se está ou não*/

        while (true) {
            SpreadMessage spreadMessage = this.middleware.receiveMessage();

            if (spreadMessage.isRegular()) {
                Message msg = s.decode(spreadMessage.getData());
                System.out.println("New message (" + msg.toString() + ") from " + spreadMessage.getSender());

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

                } else  if (msg instanceof MembershipInfoReply) {
                    MembershipInfoReply reply = (MembershipInfoReply) msg;

                    allActiveServers = reply.getAllActiveServers();

                    Map<Integer, List<String>> auxMap = new HashMap<>();
                    for (Map.Entry<Integer, List<String>> entry : waitingFromServers.entrySet()){
                        List<String> auxList = new ArrayList<>(entry.getValue());
                        auxMap.put(entry.getKey(), auxList);
                    }

                    for (Map.Entry<Integer, List<String>> entry : auxMap.entrySet()){
                        for(String s : entry.getValue()){
                            if(!allActiveServers.contains(s))
                                waitingFromServers.get(entry.getKey()).remove(s);
                        }
                    }

                    //todo ver se pode fazer completes
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
}
