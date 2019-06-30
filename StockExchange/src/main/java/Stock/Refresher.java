package Stock;

import Common.*;
import Common.Messages.*;
import io.atomix.utils.serializer.Serializer;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Refresher implements Runnable{
    private int id;
    private Server server;
    private Middleware middlewareS;
    private Serializer s;

    public Refresher(int id, Server s, Middleware sender){
        this.id = id;
        this.server = s;
        this.middlewareS = sender;

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
                .withTypes(MembershipInfoRequest.class)
                .withTypes(ArrayList.class)
                .build();
    }

    public void run(){
        while(true) {
            this.server.getLockMessages().lock();
            this.server.getLockNextMsg().lock();

            if(this.server.getMessages().size() > this.server.getNextMsg()){
                Message mensagem = this.server.getMessage();
                this.server.setNextMsg(this.server.getNextMsg()+1);

                this.server.getLockNextMsg().unlock();
                this.server.getLockMessages().unlock();

                this.server.getLockWaiting().lock();

                if(this.server.isWaiting()){
                    if(mensagem instanceof EstadoReply){
                        EstadoReply estM = (EstadoReply) mensagem;

                        if(estM.getServerId() == this.server.getId()){
                            System.out.println("Refresher "+this.id+": "+estM.toString());
                            List<Message> result = estM.getMessages();

                            this.server.getLockMessages().lock();
                            for(Message m: result){
                                this.server.addMsg(m);
                                this.server.setNextMsg(this.server.getNextMsg()+1);
                                this.processMsg(m);
                            }

                            this.server.getLockNotProcessedMsg().lock();
                            for(int i=0; i<this.server.getNotProcessedMsg().size(); i++){
                                Message tmp = new ArrayList<>(this.server.getNotProcessedMsg()).get(i);
                                this.server.addMsg(tmp);
                            }

                            this.server.removeNotProcessedMsg();

                            this.server.setWaiting(false);

                            this.server.getLockNextMsg().lock();
                            this.server.getLockInfo().lock();
                            this.server.getLockStock().lock();
                            this.server.getLockClientNames().lock();

                            this.server.storeState("server"+this.server.getId()+"DB");
                            this.server.writeInTextFile("server"+this.server.getId()+"TXT");

                            this.server.unlockAll();
                        }
                    }
                }

                else {
                    this.server.getLockWaiting().unlock();

                    Message m = processMsg(mensagem);

                    this.server.lockAll();
                    this.server.storeState("server"+this.server.getId()+"DB");
                    this.server.writeInTextFile("server"+this.server.getId()+"TXT");
                    this.server.unlockAll();

                    if(m != null){
                        middlewareS.sendMessage(this.s.encode(m), m.getClientName());
                    }
                }
            }
            else{
                this.server.getLockNextMsg().unlock();
                this.server.getLockMessages().unlock();
            }
        }
    }

    public Message processMsg(Message mensagem){
        Message message = null;

        if(mensagem instanceof ActionsRequest){
            ActionsRequest actionsRequest = (ActionsRequest) mensagem;
            System.out.println("Refresher "+this.id+": "+actionsRequest.toString());

            this.server.getLockClientNames().lock();
            this.server.addClientName(actionsRequest.getClientName());
            this.server.getLockClientNames().unlock();

            String company = actionsRequest.getCompany();

            this.server.getLockStock().lock();
            long actions = this.server.getStock().actions(company);
            this.server.getLockStock().unlock();

            ActionsReply actionsReply = new ActionsReply(actionsRequest.getTransactionID(), actionsRequest.getClientName(), this.server.getId(), company, actions);
            System.out.println("Refresher "+this.id+": "+actionsReply.toString());

            message = actionsReply;

        } else if(mensagem instanceof BuyRequest){
            BuyRequest buyRequest = (BuyRequest) mensagem;
            System.out.println("Refresher "+this.id+": "+buyRequest.toString());

            this.server.getLockClientNames().lock();
            this.server.addClientName(buyRequest.getClientName());
            this.server.getLockClientNames().unlock();

            String company = buyRequest.getCompany();
            long qt = buyRequest.getActions();

            this.server.getLockStock().lock();
            boolean result = this.server.getStock().buy(company, qt);
            this.server.getLockStock().unlock();

            BuyReply buyReply = new BuyReply(buyRequest.getTransactionID(), buyRequest.getClientName(), this.server.getId(), company, result);
            System.out.println("Refresher "+this.id+": "+buyReply.toString());

            message = buyReply;

        } else if(mensagem instanceof CompaniesRequest){
            CompaniesRequest companiesRequest = (CompaniesRequest) mensagem;
            System.out.println("Refresher "+this.id+": "+companiesRequest.toString());

            this.server.getLockClientNames().lock();
            this.server.addClientName(companiesRequest.getClientName());
            this.server.getLockClientNames().unlock();

            this.server.getLockStock().lock();
            Map<String, Long> companies = this.server.getStock().getCompanies();
            this.server.getLockStock().unlock();

            CompaniesReply companiesReply = new CompaniesReply(companiesRequest.getTransactionID(), companiesRequest.getClientName(), this.server.getId(), companies);
            System.out.println("Refresher "+this.id+": "+companiesReply.toString());

            message = companiesReply;

        } else if(mensagem instanceof SellRequest){
            SellRequest sellRequest = (SellRequest) mensagem;
            System.out.println("Refresher "+this.id+": "+sellRequest.toString());

            this.server.getLockClientNames().lock();
            this.server.addClientName(sellRequest.getClientName());
            this.server.getLockClientNames().unlock();

            String company = sellRequest.getCompany();
            long qt = sellRequest.getActions();

            this.server.getLockStock().lock();
            boolean result = this.server.getStock().sell(company, qt);
            this.server.getLockStock().unlock();

            SellReply sellReply = new SellReply(sellRequest.getTransactionID(), sellRequest.getClientName(), this.server.getId(), company, result);
            System.out.println("Refresher "+this.id+": "+sellReply.toString());

            message = sellReply;

        } else if(mensagem instanceof EstadoRequest){
            EstadoRequest estadoRequest = (EstadoRequest) mensagem;

            if(estadoRequest.getServerID() != this.server.getId()){
                System.out.println("Refresher "+this.id+": "+estadoRequest.toString());

                List<Message> result = new ArrayList<>();

                this.server.getLockMessages().lock();
                if(this.server.getMessages().size() > estadoRequest.getNextMsg()) {
                    boolean stop = false;

                    for(int i=estadoRequest.getNextMsg(); i<this.server.getMessages().size() && !stop; i++){
                        Message m = this.server.getMessages().get(i);
                        if(m instanceof EstadoRequest && ((EstadoRequest) m).getServerID() == estadoRequest.getServerID()){
                            result.add(m);
                            stop = true;
                        }
                        else result.add(m);
                    }
                }
                this.server.getLockMessages().unlock();

                EstadoReply estadoReply = new EstadoReply(estadoRequest.getClientName(), estadoRequest.getServerID(), result);
                System.out.println("Refresher "+this.id+": "+estadoReply.toString());

                message = estadoReply;
            }

        } else if(mensagem instanceof MembershipInfoRequest) {
            MembershipInfoRequest mir = (MembershipInfoRequest) mensagem;
            System.out.println("Refresher "+this.id+": "+mir.toString());

            this.server.getLockClientNames().lock();
            this.server.addClientName(mir.getClientName());
            this.server.getLockClientNames().unlock();

            this.server.getLockInfo().lock();
            int number = this.server.getLatestMembershipInfo().size();
            List<String> names = this.server.getLatestMembershipInfo();
            this.server.getLockInfo().unlock();

            MembershipInfoReply mi = new MembershipInfoReply(mir.getTransactionID(), mir.getClientName(), this.server.getId(), number, names);
            System.out.println("Refresher "+this.id+": "+mi.toString());

            message = mi;
        }

        return message;
    }
}