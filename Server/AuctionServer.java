import java.rmi.RemoteException;
import java.rmi.registry.LocateRegistry;
import java.rmi.registry.Registry;
import java.rmi.server.UnicastRemoteObject;
import javax.crypto.SealedObject;
import javax.crypto.KeyGenerator;
import javax.crypto.Cipher;
import javax.crypto.*;
import java.io.ObjectInputStream;  // Import the File class
import java.io.ObjectOutputStream; 
import java.io.FileOutputStream;
import java.io.IOException;
import java.lang.Object;
import java.util.*;
import java.io.File;
import java.security.KeyPair;
import java.security.KeyPairGenerator;
import java.security.PrivateKey;
import java.security.PublicKey;
import java.security.SecureRandom;
import java.security.Signature; 
import java.io.FileInputStream;

/*
Asymmetric cryptography offers better security because it uses two different keys — 
a public key which only gets used to encrypt messages, making it safe for anyone to have,
 and a private key to decrypt messages that never needs to be shared. Since the private key
  never needs to be shared, 
it helps ensure only the intended recipient can decrypt encoded messages and creates a 
tamper-proof digital signature. 
*/
public class AuctionServer extends UnicastRemoteObject implements IAuction
{
    private static final long serialVersionUID = 1L;
    private List<AuctionItem> AuctionList;
    private List<AuctionListing> AuctionListingsList;
    private static SecretKey aesKey = null;
    private int newAuctionID = 0;
    private int challengeValue = 0;

    // Signing Algorithm
    private static final String SIGNING_ALGORITHM = "SHA256withRSA";
    private static final String RSA = "RSA";
    

    protected AuctionServer(List<AuctionItem> list, List<AuctionListing> listA) throws RemoteException{
        super();
        this.AuctionList = list;
        this.AuctionListingsList = listA;
    }
    public int requestChallenge() throws RemoteException
    {
        Random rand = new Random();
        challengeValue = rand.nextInt(9999999);
        return challengeValue;
    }
    public byte[] requestSignature(int challenge) throws RemoteException
    {
        try{
            Signature signature = Signature.getInstance(SIGNING_ALGORITHM);

            File myObj = new File("/Users/cameronpugh/Documents/311/SCC_311_CW/Stage2/ServerKeys.txt");
            FileInputStream fos = new FileInputStream(myObj);
            ObjectInputStream oin = new ObjectInputStream(fos);
            KeyPair key = (KeyPair)oin.readObject();
            oin.close();
            
            signature.initSign(key.getPrivate());
            signature.update((Integer.toString(challenge)).getBytes());
            return signature.sign();
        }
        catch(Exception e){      
        }
        return null;    
    }
    public static void GenerateServerKeyPair() throws Exception
    {
        SecureRandom secureRandom = new SecureRandom();
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA);
        keyPairGenerator.initialize(2048, secureRandom);
        KeyPair k = keyPairGenerator.generateKeyPair();

        File myObj = new File("/Users/cameronpugh/Documents/311/SCC_311_CW/Stage2/ServerKeys.txt");
        FileOutputStream fos = new FileOutputStream(myObj);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(k);
        oos.close();

    }
    public static void Generate_RSA_KeyPair(int userID)throws Exception
    {
        SecureRandom secureRandom = new SecureRandom();
        KeyPairGenerator keyPairGenerator = KeyPairGenerator.getInstance(RSA);
        keyPairGenerator.initialize(2048, secureRandom);
        KeyPair k = keyPairGenerator.generateKeyPair();

        File myObj = new File("/Users/cameronpugh/Documents/311/SCC_311_CW/Stage2/AuthenticationKeys"+ (Integer.toString(userID)) +".txt");
        FileOutputStream fos = new FileOutputStream(myObj);
        ObjectOutputStream oos = new ObjectOutputStream(fos);
        oos.writeObject(k);
        oos.close();
    }
    public boolean verifySignature(byte[] signatureToVerify, int userID)throws RemoteException
    {
        try{
            Signature signature = Signature.getInstance(SIGNING_ALGORITHM);

            File myObj = new File("/Users/cameronpugh/Documents/311/SCC_311_CW/Stage2/AuthenticationKeys"+ (Integer.toString(userID)) +".txt");
            FileInputStream fos = new FileInputStream(myObj);
            ObjectInputStream oin = new ObjectInputStream(fos);
            KeyPair key = (KeyPair)oin.readObject();
            oin.close();

            signature.initVerify(key.getPublic());
            signature.update((Integer.toString(challengeValue)).getBytes());
            return signature.verify(signatureToVerify);

        }catch (Exception e){
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return false;
    }
    /*
        on client - createDigitialSignature from accessing the keypair from accosiated ID
        Send using method from client to server
        Server verifies info.
    */
    public static void generateKeyAndWrite()
    {
       try{
            // Generate key
            KeyGenerator kgen = KeyGenerator.getInstance("AES");
            kgen.init(128);
            aesKey = kgen.generateKey();
        
            File myObj = new File("/Users/cameronpugh/Documents/311/SCC_311_CW/Stage2/key.txt");
            FileOutputStream fos = new FileOutputStream(myObj);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(aesKey);
            oos.close();
            
        }catch (Exception e){
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
    }
    public SealedObject getSpec(int itemId, SealedObject clientReq) throws RemoteException
    {
        //System.out.println("sealedObject-" + clientReq);//TEST LINE

        try{
            CRequest temp = (CRequest)clientReq.getObject(aesKey);
            System.out.println("client request handled for client " + temp.getClientID());
        }catch(Exception e){
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        for (AuctionItem item : AuctionList)
        {
            if (item.getID() == itemId){

                try{
                    Cipher cipher = Cipher.getInstance("AES");
                    cipher.init(Cipher.ENCRYPT_MODE, aesKey);
                    SealedObject sealedObject = new SealedObject(item, cipher);
                    //System.out.println("sealedObject(1)-" + sealedObject);//TEST LINE
                    return sealedObject;
                }catch(Exception e){
                    System.out.println("An error occurred.");
                     e.printStackTrace();
                }
            }
        }
        return null;

    }
    public SealedObject createAuction(int startingPrice, String description, SealedObject reservePrice, String itemTitle, String itemCondition, SealedObject clientReq) throws RemoteException
    {
        CRequest cID = null;
        int newItemID = 0;
        CreateListingRequest newReservePrice = null;

        try{
            cID = (CRequest)clientReq.getObject(aesKey);
            //System.out.println("client request handled for client " + temp.getClientID());
        }catch(Exception e){
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        try{
            newReservePrice = (CreateListingRequest)reservePrice.getObject(aesKey);
            //System.out.println("client request handled for client " + temp.getClientID());
        }catch(Exception e){
            System.out.println("An error occurred.");
            e.printStackTrace();
        }


        if(AuctionList.size() != 0){
            AuctionItem temp = AuctionList.get(AuctionList.size() -1);
            newItemID = (temp.getID()+1);
            newAuctionID = newAuctionID +1;
        }
        else{
            newItemID = 1;
            newAuctionID = 1;
        }
        
        AuctionList.add(new AuctionItem(itemTitle, description, itemCondition,newItemID));
        AuctionListingsList.add(new AuctionListing(newAuctionID, startingPrice, newReservePrice.getReservePrice(), cID.getClientID(),AuctionList.get(AuctionList.size()-1) ));

        try{
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);
            AuctionListing x = new AuctionListing(newAuctionID);//creates empty auctinlisting just containing auctionid
            SealedObject sealedObject = new SealedObject(x, cipher);
            //System.out.println("sealedObject(1)-" + sealedObject);//TEST LINE
            return sealedObject;
        }catch(Exception e){
            System.out.println("An error occurred.");
             e.printStackTrace();
        }
        return null;
    }
    public List<SealedObject> showAllListings() throws RemoteException
    {
        try
        {
            List<SealedObject> list = new ArrayList<>();
            
            Cipher cipher = Cipher.getInstance("AES");
            cipher.init(Cipher.ENCRYPT_MODE, aesKey);

            for (AuctionListing item : AuctionListingsList)
            {
                Listing temp = new Listing(item.getAuctionID(),item.getStartingPrice(),item.getClientID(),item.getAuctionItem());
                SealedObject sealedObject = new SealedObject(temp, cipher);
                list.add(sealedObject);
            }
            return list;

        }
        catch(Exception e)
        {
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        return null;
    }
    public int bid(int auctionID, int newBid, String name, String emailAddress, SealedObject c) throws RemoteException
    {
        CRequest cID = null;
        try{
            cID = (CRequest)c.getObject(aesKey);
            //System.out.println("client request handled for client " + temp.getClientID());
        }catch(Exception e){
            System.out.println("An error occurred.");
            e.printStackTrace();
        }

        for (AuctionListing item : AuctionListingsList)
        {
            if(item.getAuctionID() == auctionID)
            {
                if(newBid>item.getStartingPrice())
                {
                    item.setBiddersID(cID.getClientID());
                    item.setName(name);
                    item.setEmail(emailAddress);
                    item.setStartingBid(newBid);
                    System.out.println("Processed bid");
                    return 1;
                }
                else if(newBid<=item.getStartingPrice())
                {
                    //dont accecpt bid
                    return 0;
                }
                
            }
        }
        return 0;
    }
    public SealedObject closeAuction(int auctionID, SealedObject clientReq)throws RemoteException
    {
        CRequest cID = null;
        try{
            cID = (CRequest)clientReq.getObject(aesKey);
            //System.out.println("client request handled for client " + temp.getClientID());
        }catch(Exception e){
            System.out.println("An error occurred.");
            e.printStackTrace();
        }
        
        for (AuctionListing item : AuctionListingsList)
        {
            if(item.getAuctionID() == auctionID)
            {
                if(item.getClientID()!=cID.getClientID())
                {
                    try{
                        Cipher cipher = Cipher.getInstance("AES");
                        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
                        AuctionListing x = new AuctionListing(-1);//creates empty auctinlisting just containing auctionid
                        SealedObject sealedObject = new SealedObject(x, cipher);
                        return sealedObject;
                    }catch(Exception e){
                        System.out.println("An error occurred.");
                        e.printStackTrace();
                    }
                }
                if(item.getStartingPrice() >= item.getReservePrice())
                {
                    String email = item.getBuyersEmail();
                    String name = item.getBuyersName();

                    try{
                        Cipher cipher = Cipher.getInstance("AES");
                        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
                        AuctionListing x = new AuctionListing(auctionID);//creates empty auctinlisting just containing auctionid
                        x.setName(name);
                        x.setEmail(email);
                        SealedObject sealedObject = new SealedObject(x, cipher);
                        //System.out.println("sealedObject(1)-" + sealedObject);//TEST LINE
                        AuctionListingsList.remove(AuctionListingsList.indexOf(item));
                        return sealedObject;
                    }catch(Exception e){
                        System.out.println("An error occurred.");
                         e.printStackTrace();
                    }

                }
                else if(item.getStartingPrice() < item.getReservePrice())
                {
                    try{
                        Cipher cipher = Cipher.getInstance("AES");
                        cipher.init(Cipher.ENCRYPT_MODE, aesKey);
                        AuctionListing x = new AuctionListing(0);//creates empty auctinlisting just containing auctionid
                        SealedObject sealedObject = new SealedObject(x, cipher);
                        AuctionListingsList.remove(AuctionListingsList.indexOf(item));
                        return sealedObject;
                    }catch(Exception e){
                        System.out.println("An error occurred.");
                        e.printStackTrace();
                    }
                }
            }
        }
        
        //return empty object
        return null;
    }

    private static List<AuctionItem> initializeList() {
        List<AuctionItem> list = new ArrayList<>();
        return list;
    }
    private static List<AuctionListing> initializeAuctionList(){
        List<AuctionListing> list = new ArrayList<>();
        return list;
    }

    public static void main(String[] args) {
        try {
            generateKeyAndWrite();
            GenerateServerKeyPair();
            Generate_RSA_KeyPair(1);
            Generate_RSA_KeyPair(2);
            Generate_RSA_KeyPair(3);
            Generate_RSA_KeyPair(4);
            String name = "myserver";
            Registry registry = LocateRegistry.getRegistry();
            registry.rebind(name, new AuctionServer(initializeList(),initializeAuctionList()));
            System.out.println("Server ready");
        }catch (Exception e){
            System.err.println("Exception:");
            e.printStackTrace();
        }
    }
    
}
