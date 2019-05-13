package Client;

import io.atomix.utils.net.Address;

import java.util.Map;
import java.util.Random;

public class Main {
    public static void main(final String[] args) {
        final Address cliAddress = Address.from("localhost:"+args[0]);
        final ClientStub stub = new ClientStub(args[0]);

        Client client = new Client();
        client = client.loadState("clientDB-"+args[0]);

        System.out.println(client.toString());
        if(client.getCompanys().size()==0){
            System.out.println("You don't have any actions.");
        }

        final int numOps = 5; // Total number of Buy + Sell
        final Random random = new Random();
        int randomOperation, randomCompany;
        Map<String, Long> companies;

        //todo mandar companysrequest
        //todo mandar actionsrequest

        for (int i = 0; i < numOps; i++) {
            randomOperation = random.nextInt(2);
            companies = client.getCompanys();

            if(randomOperation==0 && companies.size()==0){
                randomOperation=1;
            }

            switch (randomOperation) {
                case 0:
                    randomCompany = random.nextInt(companies.size());

                    System.out.println("Sending sell request");
                    /* todo
                    bankStub.saldo().thenAccept((saldoBank) -> {
                        System.out.println("Saldo response: "+saldoBank);
                        if(saldoBank != saldo.get())
                            System.out.println("ERROR. Something went wrong. ATUALIZAR SALDO");
                        saldo.set(saldoBank);
                    });*/
                    break;

                case 1:
                    //para clientes concorrentes o Mov reply poderia dizer tb o saldo antigo e atual
                    //para o client conseguir ver o que aconteceu qd faz o Movimento

                    final int qtd = random.nextInt(20)-10;
                    /* todo
                    System.out.println("Sending mov request with qtd = "+qtd);
                    bankStub.mov(qtd).thenAccept((validation) -> {
                        System.out.println("Mov response: "+validation);
                        if(validation==true) {
                            saldo.addAndGet(qtd);
                            System.out.println("Saldo atualizado. Saldo = "+saldo);
                        }
                        else
                            System.out.println("Quantidade insuficiente");
                    });*/
                    break;

                default:
                    System.out.println("Invalid operation.");
                    System.exit(1);
                    break;
            }
        }
    }
}
