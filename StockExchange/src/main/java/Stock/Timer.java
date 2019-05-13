package Stock;

public class Timer implements Runnable{
    private Server stockServer;

    public Timer(Server bs){
        this.stockServer = bs;
    }

    public void run(){
        try {
            Thread.sleep(10000);
            this.stockServer.setEstado(true);
            System.out.println("Ninguém para me responder, vou assumir o meu saldo.");
        } catch (InterruptedException e) {
        }
    }
}
