package Stock;

import Common.*;

import io.atomix.utils.serializer.Serializer;
import spread.*;

import java.io.InterruptedIOException;
import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Server{
    private StockImp stock;
    private Serializer s;
    private int id;
    private boolean estado;
    private List<Msg> messages;
    private int nextMsg;

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
        this.messages = new ArrayList<>();
        this.nextMsg = 0;
    }

    public void setEstado(boolean b){
        this.estado = b;
    }

    public static void main(final String[] args){
        Server se = new Server(Integer.parseInt(args[0]));
        ArrayList<Msg> mensagens = new ArrayList<>();

        try {

            SpreadGroup group = new SpreadGroup();
            group.join(connection, "servergroup");

            EstadoRequest est = new EstadoRequest(se.id, se.nextMsg);
            System.out.println(est.toString());
            SpreadMessage msgE = new SpreadMessage();
            msgE.setData(se.s.encode(est));
            msgE.addGroup("servergroup");
            msgE.setReliable();
            connection.multicast(msgE);

            Thread tr = new Thread(new Timer(se));
            tr.start();

            while(true) {
                SpreadMessage message = connection.receive();
                Msg mensagem = se.s.decode(message.getData());

                if(!se.estado){
                    if(mensagem instanceof EstadoReply){
                        EstadoReply estM = (EstadoReply) mensagem;
                        System.out.println(estM.toString());

                        if(estM.getServerId() == se.id){
                            List<Msg> result = estM.getMessages();

                            for(Msg m: result){
                                se.processMsg(m);
                                se.nextMsg++;
                            }

                            se.estado = true;
                            tr.interrupt();

                            for(int i=0; i<mensagens.size(); i++){
                                Msg m = se.processMsg(mensagens.get(i));

                                SpreadMessage msg = new SpreadMessage();
                                msg.setData(se.s.encode(m));
                                msg.addGroup(m.getClientName());

                                if(msg != null){
                                    msg.setReliable();
                                    msg.setAgreed();
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
                    Msg m = se.processMsg(mensagem);

                    SpreadMessage msg = new SpreadMessage();
                    msg.setData(se.s.encode(m));
                    msg.addGroup(m.getClientName());

                    if (msg != null) {
                        msg.setReliable();
                        msg.setAgreed();
                        connection.multicast(msg);
                        se.nextMsg++;
                    }
                }
            }
        } catch (SpreadException | UnknownHostException | InterruptedIOException e) {
            e.printStackTrace();
        }
    }

    public Msg processMsg(Msg mensagem){
        Msg msg = null;

        if(mensagem instanceof ActionsRequest){
            ActionsRequest actionsRequest = (ActionsRequest) mensagem;
            System.out.println(actionsRequest.toString());

            String company = actionsRequest.getCompany();
            long actions = this.stock.actions(company);
            ActionsReply actionsReply = new ActionsReply(actionsRequest.getTransactionID(), actionsRequest.getClientName(), this.id, company, actions);
            System.out.println(actionsReply.toString());

            msg = actionsReply;

        } else if(mensagem instanceof BuyRequest){
            BuyRequest buyRequest = (BuyRequest) mensagem;
            System.out.println(buyRequest.toString());
            String company = buyRequest.getCompany();
            long qt = buyRequest.getActions();

            boolean result = this.stock.buy(company, qt);
            BuyReply buyReply = new BuyReply(buyRequest.getTransactionID(), buyRequest.getClientName(), this.id, company, result);
            System.out.println(buyReply.toString());

            msg = buyReply;

        } else if(mensagem instanceof CompanysRequest){
            CompanysRequest companysRequest = (CompanysRequest) mensagem;
            System.out.println(companysRequest.toString());

            Map<String, Long> companys = this.stock.getCompanys();
            CompanysReply companysReply = new CompanysReply(companysRequest.getTransactionID(), companysRequest.getClientName(), this.id, companys);
            System.out.println(companysReply.toString());

           msg = companysReply;

        } else if(mensagem instanceof SellRequest){
            SellRequest sellRequest = (SellRequest) mensagem;
            System.out.println(sellRequest.toString());
            String company = sellRequest.getCompany();
            long qt = sellRequest.getActions();

            boolean result = this.stock.sell(company, qt);
            SellReply sellReply = new SellReply(sellRequest.getTransactionID(), sellRequest.getClientName(), this.id, company, result);
            System.out.println(sellReply.toString());

            msg = sellReply;

        } else if(mensagem instanceof EstadoRequest){
            EstadoRequest estadoRequest = (EstadoRequest) mensagem;

            if(estadoRequest.getId() != this.id){
                System.out.println(estadoRequest.toString());

                List<Msg> result = this.messages.subList(estadoRequest.getNextMsg(), this.messages.size());

                EstadoReply estadoReply = new EstadoReply(estadoRequest.getId(), result);
                System.out.println(estadoReply.toString());

                msg = estadoReply;
            }
        }

        return msg;
    }
}
