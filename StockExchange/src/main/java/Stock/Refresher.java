package Stock;

import Common.*;
import Common.Messages.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Refresher implements Runnable{
    private Server server;
    private Middleware middlewareS;

    public Refresher(Server s, Middleware sender){
        this.server = s;
        this.middlewareS = sender;
    }

    public void run(){
        while(true) {
            if(this.server.getMessages().size() > this.server.getNextMsg()){
                Message mensagem = this.server.getMessages().get(this.server.getNextMsg());

                if(this.server.isWaiting()){
                    if(mensagem instanceof EstadoReply){
                        EstadoReply estM = (EstadoReply) mensagem;

                        if(estM.getServerId() == this.server.getId()){
                            System.out.println(estM.toString());
                            List<Message> result = estM.getMessages();

                            for(Message m: result){
                                this.server.addMsg(m);
                            }

                            for(int i=0; i<this.server.getNotProcessedMsg().size(); i++){
                                Message tmp = this.server.getNotProcessedMsg().get(i);
                                this.server.addMsg(tmp);
                                this.server.removeNotProcessedMsg(tmp);
                            }

                            this.server.setWaiting(false);
                            this.server.storeState("server"+this.server.getId()+"DB");
                            this.server.writeInTextFile("server"+this.server.getId()+"TXT");
                        }
                    }
                }

                else {
                    Message m = processMsg(mensagem);
                    this.server.setNextMsg(this.server.getNextMsg()+1);
                    this.server.storeState("server"+this.server.getId()+"DB");
                    this.server.writeInTextFile("server"+this.server.getId()+"TXT");

                    if(m != null){
                        middlewareS.sendMessage(this.server.getS().encode(m), m.getClientName());
                    }
                }
            }
        }
    }

    public Message processMsg(Message mensagem){
        Message message = null;

        if(mensagem instanceof ActionsRequest){
            ActionsRequest actionsRequest = (ActionsRequest) mensagem;
            System.out.println(actionsRequest.toString());
            this.server.addClientName(actionsRequest.getClientName());

            String company = actionsRequest.getCompany();
            long actions = this.server.getStock().actions(company);
            ActionsReply actionsReply = new ActionsReply(actionsRequest.getTransactionID(), actionsRequest.getClientName(), this.server.getId(), company, actions);
            System.out.println(actionsReply.toString());

            message = actionsReply;

        } else if(mensagem instanceof BuyRequest){
            BuyRequest buyRequest = (BuyRequest) mensagem;
            System.out.println(buyRequest.toString());
            this.server.addClientName(buyRequest.getClientName());

            String company = buyRequest.getCompany();
            long qt = buyRequest.getActions();
            boolean result = this.server.getStock().buy(company, qt);
            BuyReply buyReply = new BuyReply(buyRequest.getTransactionID(), buyRequest.getClientName(), this.server.getId(), company, result);
            System.out.println(buyReply.toString());

            message = buyReply;

        } else if(mensagem instanceof CompaniesRequest){
            CompaniesRequest companiesRequest = (CompaniesRequest) mensagem;
            System.out.println(companiesRequest.toString());
            this.server.addClientName(companiesRequest.getClientName());

            Map<String, Long> companies = this.server.getStock().getCompanies();
            CompaniesReply companiesReply = new CompaniesReply(companiesRequest.getTransactionID(), companiesRequest.getClientName(), this.server.getId(), companies);
            System.out.println(companiesReply.toString());

            message = companiesReply;

        } else if(mensagem instanceof SellRequest){
            SellRequest sellRequest = (SellRequest) mensagem;
            System.out.println(sellRequest.toString());
            this.server.addClientName(sellRequest.getClientName());

            String company = sellRequest.getCompany();
            long qt = sellRequest.getActions();
            boolean result = this.server.getStock().sell(company, qt);
            SellReply sellReply = new SellReply(sellRequest.getTransactionID(), sellRequest.getClientName(), this.server.getId(), company, result);
            System.out.println(sellReply.toString());

            message = sellReply;

        } else if(mensagem instanceof EstadoRequest){
            EstadoRequest estadoRequest = (EstadoRequest) mensagem;

            if(estadoRequest.getServerID() != this.server.getId()){
                System.out.println(estadoRequest.toString());

                List<Message> result = new ArrayList<>();
                if(this.server.getMessages().size() > estadoRequest.getNextMsg()) {
                    result = this.server.getMessages().subList(estadoRequest.getNextMsg(), this.server.getMessages().size());
                }

                EstadoReply estadoReply = new EstadoReply(estadoRequest.getClientName(), estadoRequest.getServerID(), result);
                System.out.println(estadoReply.toString());

                message = estadoReply;
            }
        } else if(mensagem instanceof MembershipInfoRequest) {
            MembershipInfoRequest mir = (MembershipInfoRequest) mensagem;
            System.out.println(mir.toString());
            this.server.addClientName(mir.getClientName());

            int number = this.server.getLatestMembershipInfo().getMembershipInfo().getMembers().length;
            List<String> names = this.server.getServersNames(this.server.getLatestMembershipInfo());
            MembershipInfoReply mi = new MembershipInfoReply(mir.getTransactionID(), mir.getClientName(), this.server.getId(), number, names);
            message = mi;
        }

        return message;
    }
}
