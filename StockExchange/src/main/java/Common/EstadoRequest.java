package Common;

public class EstadoRequest extends Msg{
    private int id;
    private int nextMsg;

    public EstadoRequest(int add, int n){
        super(-1,"");
        this.id = add;
        this.nextMsg = n;
    }

    public int getId(){
        return this.id;
    }

    public int getNextMsg(){ return this.nextMsg;}

    public String toString(){
        return "Pedido de estado por parte do servidor "+this.id;
    }
}
