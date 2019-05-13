package Client;

import Common.*;
import Common.Messages.*;
import io.atomix.utils.serializer.Serializer;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.locks.ReentrantLock;

public class ClientStub {
    private final String myGroupName;
    private final Middleware middleware;
    private final Serializer s;
    private int transactionID;

    private final Map<Integer, CompletableFuture<Map<String, Long>>> CFcompanies;
    private final Map<Integer, CompletableFuture<Map<String, Long>>> CFactions;
    private final Map<Integer, CompletableFuture<Map<String, Boolean>>> CFbuy;
    private final Map<Integer, CompletableFuture<Map<String, Boolean>>> CFsell;

    private final ReentrantLock lockCFcompanies = new ReentrantLock();
    private final ReentrantLock lockCFactions = new ReentrantLock();
    private final ReentrantLock lockCFbuy = new ReentrantLock();
    private final ReentrantLock lockCFsell = new ReentrantLock();

    ClientStub(String port){
        this.myGroupName = "stubgroup"+port;
        this.middleware = new Middleware("sender_stub"+port, this.myGroupName);

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

        this.transactionID = 0;

        this.CFcompanies = new HashMap<>();
        this.CFactions = new HashMap<>();
        this.CFbuy = new HashMap<>();
        this.CFsell = new HashMap<>();

        new Thread(new ClientStubThread(port, this.CFcompanies, this.CFactions, this.CFbuy, this.CFsell, this.lockCFcompanies, this.lockCFactions, this.lockCFbuy, this.lockCFsell))
                .start();
    }

    public CompletableFuture<Map<String, Long>> getCompanies() {
        this.transactionID++;
        final CompletableFuture<Map<String, Long>> cf = new CompletableFuture<>();

        this.lockCFcompanies.lock();
        this.CFcompanies.put(this.transactionID, cf);
        this.lockCFcompanies.unlock();

        byte[] msg = this.s.encode(new CompaniesRequest(this.transactionID, this.myGroupName));
        this.middleware.sendMessage(msg, "servergroup");

        return cf;
    }

    public CompletableFuture<Map<String, Long>> getActions(String companyName) {
        this.transactionID++;
        final CompletableFuture<Map<String, Long>> cf = new CompletableFuture<>();

        this.lockCFactions.lock();
        this.CFactions.put(this.transactionID, cf);
        this.lockCFactions.unlock();

        byte[] msg = this.s.encode(new ActionsRequest(this.transactionID, this.myGroupName, companyName));
        this.middleware.sendMessage(msg, "servergroup");

        return cf;
    }

    public CompletableFuture<Map<String, Boolean>> buy(String companyName, long actionsQuantity) {
        this.transactionID++;
        final CompletableFuture<Map<String, Boolean>> cf = new CompletableFuture<>();

        this.lockCFbuy.lock();
        this.CFbuy.put(this.transactionID, cf);
        this.lockCFbuy.unlock();

        byte[] msg = this.s.encode(new BuyRequest(this.transactionID, this.myGroupName, companyName, actionsQuantity));
        this.middleware.sendMessage(msg, "servergroup");

        return cf;
    }

    public CompletableFuture<Map<String, Boolean>> sell(String companyName, long actionsQuantity) {
        this.transactionID++;
        final CompletableFuture<Map<String, Boolean>> cf = new CompletableFuture<>();

        this.lockCFsell.lock();
        this.CFsell.put(this.transactionID, cf);
        this.lockCFsell.unlock();

        byte[] msg = this.s.encode(new SellRequest(this.transactionID, this.myGroupName, companyName, actionsQuantity));
        this.middleware.sendMessage(msg, "servergroup");

        return cf;
    }
}
