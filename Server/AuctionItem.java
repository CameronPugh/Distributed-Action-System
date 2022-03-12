import java.io.Serializable;

public class AuctionItem implements Serializable {

    private static final long serialVersionUID = 1190476516911661470L;
    private int itemId;
    private String itemTitle;
    private String itemDescription;
    private String itemCondition;

    public AuctionItem(String t, String d, String c, int id){
        this.itemTitle = t;
        this.itemDescription = d;
        this.itemCondition = c;
        this.itemId = id;
    }
    public int getID(){
        return itemId;
    }
    public String getItemTitle(){
        return itemTitle;
    }
    public String getItemDescription(){
        return itemDescription;
    }
    public String getItemCondition(){
        return itemCondition;
    }
}
