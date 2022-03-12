import java.rmi.Remote;
import java.rmi.RemoteException;
import javax.crypto.SealedObject;
import java.lang.Object;
import java.util.*;

public interface IAuction extends Remote {
    
    public SealedObject getSpec(int itemId, SealedObject clientReq) throws RemoteException;
    public SealedObject createAuction(int startingPrice, String description, SealedObject reservePrice, String itemTitle, String itemCondition, SealedObject clientReq) throws RemoteException;
    public List<SealedObject> showAllListings() throws RemoteException;
    public int bid(int auctionID, int newBid, String name, String emailAddress, SealedObject clientReq) throws RemoteException;//add email and name to clientreq
    public SealedObject closeAuction(int auctionID, SealedObject clientReq)throws RemoteException;
   
    public boolean verifySignature(byte[] signatureToVerify, int userID)throws RemoteException;
    public byte[] requestSignature(int challenge) throws RemoteException;
    public int requestChallenge() throws RemoteException;

 
}