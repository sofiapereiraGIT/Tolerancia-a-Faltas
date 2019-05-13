package Stock;

import Common.*;

import io.atomix.utils.serializer.Serializer;
import spread.*;

import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.Map;

public class Server{
    private StockImp stock;
    private Serializer s;
    private int id;
    private boolean estado;

    public Server(int id){
        this.stock = new StockImp();
        this.s = Serializer.builder()
                .withTypes(ActionsRequest.class)
                .withTypes(ActionsReply.class)
                .withTypes(BuyRequest.class)
                .withTypes(BuyReply.class)
                .withTypes(CompanysRequest.class)
                .withTypes(CompanysReply.class)
                .withTypes(SellRequest.class)
                .withTypes(SellReply.class)
                .build();
        this.id = id;
        this.estado = false;
    }

    public void setEstado(boolean b){
        this.estado = b;
    }

    //todo : fazer o serializable
    public static void main(final String[] args){
        Server bs = new Server(Integer.parseInt(args[0]));
        ArrayList<Msg> mensagens = new ArrayList<>();

        try {
            SpreadConnection connection = new SpreadConnection();
            connection.connect(InetAddress.getByName("localhost"), 0, args[1], false, true);

            SpreadGroup group = new SpreadGroup();
            group.join(connection, "servergroup");

            EstadoRequest est = new EstadoRequest(bs.id);
            System.out.println(est.toString());
            SpreadMessage msgE = new SpreadMessage();
            msgE.setData(bs.s.encode(est));
            msgE.addGroup("servergroup");
            msgE.setReliable();
            connection.multicast(msgE);

            Thread tr = new Thread(new Timer(bs));
            tr.start();

            while(true) {
                SpreadMessage message = connection.receive();
                Msg mensagem = bs.s.decode(message.getData());

                if(!bs.estado){
                    if(mensagem instanceof EstadoReply){
                        EstadoReply estM = (EstadoReply) mensagem;
                        System.out.println(estM.toString());

                        if(estM.getServerId() == bs.id){
                            bs.stock.setCompanys(estM.getCompanys());
                            bs.estado = true;
                            tr.interrupt();

                            for(int i=0; i<mensagens.size(); i++){
                                SpreadMessage msg = bs.processMsg(mensagens.get(i));
                                if(msg != null){
                                    msg.setReliable();
                                    connection.multicast(msg);
                                }
                                mensagens.remove(i);
                            }
                        }
                    }
                    else{
                        mensagens.add(mensagem);
                    }
                }

                else {
                    SpreadMessage msg = bs.processMsg(mensagem);
                    if (msg != null) {
                        msg.setReliable();
                        connection.multicast(msg);
                    }
                }
            }
        } catch (SpreadException | UnknownHostException | InterruptedIOException e) {
            e.printStackTrace();
        }
    }

    public SpreadMessage processMsg(Msg mensagem){
        SpreadMessage msg = null;

        if(mensagem instanceof ActionsRequest){
            ActionsRequest actionsRequest = (ActionsRequest) mensagem;
            System.out.println(actionsRequest.toString());

            String company = actionsRequest.getCompany();
            long actions = this.stock.actions(company);
            ActionsReply actionsReply = new ActionsReply(actionsRequest.getTransactionID(), actionsRequest.getClientName(), this.id, company, actions);
            System.out.println(actionsReply.toString());

            msg = new SpreadMessage();
            msg.setData(this.s.encode(actionsReply));
            msg.addGroup(actionsRequest.getClientName());

        } else if(mensagem instanceof BuyRequest){
            BuyRequest buyRequest = (BuyRequest) mensagem;
            System.out.println(buyRequest.toString());
            String company = buyRequest.getCompany();
            long qt = buyRequest.getActions();

            boolean result = this.stock.buy(company, qt);
            BuyReply buyReply = new BuyReply(buyRequest.getTransactionID(), buyRequest.getClientName(), this.id, company, result);
            System.out.println(buyReply.toString());

            msg = new SpreadMessage();
            msg.setData(this.s.encode(buyReply));
            msg.addGroup(buyRequest.getClientName());

        } else if(mensagem instanceof CompanysRequest){
            CompanysRequest companysRequest = (CompanysRequest) mensagem;
            System.out.println(companysRequest.toString());

            Map<String, Long> companys = this.stock.getCompanys();
            CompanysReply companysReply = new CompanysReply(companysRequest.getTransactionID(), companysRequest.getClientName(), this.id, companys);
            System.out.println(companysReply.toString());

            msg = new SpreadMessage();
            msg.setData(this.s.encode(companysReply));
            msg.addGroup(companysRequest.getClientName());

        } else if(mensagem instanceof SellRequest){
            SellRequest sellRequest = (SellRequest) mensagem;
            System.out.println(sellRequest.toString());
            String company = sellRequest.getCompany();
            long qt = sellRequest.getActions();

            boolean result = this.stock.sell(company, qt);
            SellReply sellReply = new SellReply(sellRequest.getTransactionID(), sellRequest.getClientName(), this.id, company, result);
            System.out.println(sellReply.toString());

            msg = new SpreadMessage();
            msg.setData(this.s.encode(sellReply));
            msg.addGroup(sellRequest.getClientName());

        } else if(mensagem instanceof EstadoRequest){
            EstadoRequest estadoRequest = (EstadoRequest) mensagem;

            if(estadoRequest.getId() != this.id){
                System.out.println(estadoRequest.toString());

                EstadoReply estadoReply = new EstadoReply(estadoRequest.getId(), this.stock.getCompanys());
                System.out.println(estadoReply.toString());

                msg = new SpreadMessage();
                msg.setData(this.s.encode(estadoReply));
                msg.addGroup("servergroup");
            }

        }

        return msg;
    }
}
