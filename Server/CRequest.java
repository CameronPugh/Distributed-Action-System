import java.io.Serializable;

public class CRequest implements Serializable {
    
    private static final long serialVersionUID = 1190476516911661470L;
    private int clientId;

    public CRequest(int id){
        this.clientId = id;
    }
    public int getClientID(){
        return clientId;
    }
}
