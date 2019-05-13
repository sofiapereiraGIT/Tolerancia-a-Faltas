package Client;

import Common.*;
import io.atomix.cluster.messaging.ManagedMessagingService;
import io.atomix.utils.net.Address;
import io.atomix.utils.serializer.Serializer;
import spread.SpreadConnection;
import spread.SpreadException;
import spread.SpreadGroup;
import spread.SpreadMessage;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;

public class ClientStub {

    private final SpreadConnection connection;
    private SpreadGroup group;
    private final Serializer s;

    private int transactionID;
    //private final Map<Integer, CompletableFuture<Long>> CFactions;
    //private final Map<Integer, CompletableFuture<Boolean>> CF;

    public ClientStub(String port){
        this.connection = new SpreadConnection();

        try {
            connection.connect(InetAddress.getByName("localhost"), 0, "stub"+port, false, false);
        } catch (SpreadException | UnknownHostException e) {
            e.printStackTrace();
        }

        try {
            group = new SpreadGroup();
            group.join(connection, "stubgroup"+port);
        } catch (SpreadException e) {
            e.printStackTrace();
        }

        this.s = Serializer.builder()
                .withTypes(ActionsReply.class)
                .withTypes(ActionsRequest.class)
                .withTypes(BuyReply.class)
                .withTypes(BuyRequest.class)
                .withTypes(CompanysReply.class)
                .withTypes(CompanysRequest.class)
                .withTypes(SellReply.class)
                .withTypes(SellRequest.class)
                .build();

        this.transactionID = 0;

        //todo
        //this.CFsaldo = new HashMap<>();
        //this.CFmov = new HashMap<>();
    }








    //todo







    public CompletableFuture<Long> saldo() {
        this.requestID++;
        final CompletableFuture<Long> cf = new CompletableFuture<>();
        this.CFsaldo.put(this.requestID,cf);
        this.msgServers.put(this.requestID, this.groupServersAtual);

        /**Multicasting to group**/
        //fazer para cada msg e no fim meter a null (Garbage Collector larga mais facilmente o objeto)
        SpreadMessage msg = new SpreadMessage();
        msg.setData(this.s.encode(new SaldoRequest(this.ms.address(), this.requestID))); //data em array de bytes
        msg.addGroup("servergroup"); //para onde manda
        msg.setReliable(); //delivery method (outros: unreliable, reliable, fifo, causal, agreed, and safe)

        try {
            this.connection.multicast(msg);
        } catch (SpreadException e) {
            e.printStackTrace();
        }

        msg = null;

        return cf;
    }

    public CompletableFuture<Boolean> mov(final long qtd) {
        this.requestID++;
        final CompletableFuture<Boolean> cf = new CompletableFuture<>();
        this.CFmov.put(this.requestID,cf);
        this.msgServers.put(this.requestID, this.groupServersAtual);

        /**Multicasting to group**/
        //fazer para cada msg e no fim meter a null (Garbage Collector larga mais facilmente o objeto)
        SpreadMessage msg = new SpreadMessage();
        msg.setData(this.s.encode(new MovRequest(this.ms.address(), this.requestID, qtd))); //data em array de bytes
        msg.addGroup("servergroup"); //para onde manda
        msg.setReliable(); //delivery method (outros: unreliable, reliable, fifo, causal, agreed, and safe)

        try {
            this.connection.multicast(msg);
        } catch (SpreadException e) {
            e.printStackTrace();
        }

        msg = null;

        return cf;
    }

    public void handleSaldo(final Address origin, final byte[] bytes) {
        final SaldoReply reply = this.s.decode(bytes);

        // verificar se é possivel aceitar a Msg
        if(reply.getMsg_id() <= msg_accept[0]){
            System.out.println("Recebi de "+origin+" a "+reply.toString());

            if(reply.getMsg_id() == msg_accept[0]) {
                this.CFsaldo.get(reply.getMsg_id()).complete(reply.getSaldo());
                msg_accept[0]++;

                System.out.println("Verificar lista de espera (tamanho = " + msgEspera.size() +"; msg a aceitar: "+ msg_accept[0] + ")");
                verificarListaEspera();
                System.out.println("Não dá para aceitar mais (tamanho = " + msgEspera.size() +"; msg a aceitar: "+ msg_accept[0] + ")");
            }
        }
        else {
            System.out.println("Colocada em espera: "+origin+" "+reply.toString());
            msgEspera.add(reply);
        }
    }

    public void handleMov(final Address origin, final byte[] bytes) {
        final MovReply reply = this.s.decode(bytes);

        // verificar se é possivel aceitar a Msg
        if(reply.getMsg_id() <= msg_accept[0]){
            System.out.println("Recebi de "+origin+" a "+reply.toString());

            if(reply.getMsg_id() == msg_accept[0]) {
                this.CFmov.get(reply.getMsg_id()).complete(reply.getBool());
                msg_accept[0]++;

                System.out.println("Verificar lista de espera (tamanho = " + msgEspera.size() +"; msg a aceitar: "+ msg_accept[0] + ")");
                verificarListaEspera();
                System.out.println("Não dá para aceitar mais (tamanho = " + msgEspera.size() +"; msg a aceitar: "+ msg_accept[0] + ")");
            }
        }
        else {
            System.out.println("Colocada em espera: "+origin+" "+reply.toString());
            msgEspera.add(reply);
        }
    }
}
