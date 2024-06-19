
import java.security.*;
import java.util.ArrayList;


public class Transaction {

    public String transactionId; // this is also the hash of the transaction.
    public PublicKey sender; // senders address/public key.
    public PublicKey recipient; // Recipients address public key.
    public float value;
    public byte[] signature; // this is to prevent anybody else from spending funds on out wallet;


    public ArrayList<TransactionInput> inputs = new ArrayList<TransactionInput>();
    public ArrayList<TransactionOutput> outputs = new ArrayList<TransactionOutput>();


    private static int sequence = 0; // a rough count of how mant transaction have been generated.

    //constructor:
    public Transaction(PublicKey from,PublicKey to, float value, ArrayList<TransactionInput> inputs ){
        this.sender = from;
        this.recipient = to;
        this.value = value;
        this.inputs = inputs;

    }

    //THis calculates the transaction hash (which will be used as its id)
    private String calculateHash() {
        sequence++; // increase the squence to avoid 2 identical transactions having the same hash
        return StringUtil.applySha256(
                StringUtil.getStringFromKey(sender) +
                        StringUtil.getStringFromKey(recipient) +
                        Float.toString(value) + sequence
        );


    }

    //Sign all the data we dont wish to be tempered with.
    public void generateSignature(PrivateKey privateKey) {
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + Float.toString(value);
        signature = StringUtil.applyECDSASig(privateKey,data);
    }
    //verify the data we signed hasnt been tempered with
    public boolean verifySignature() {
        String data = StringUtil.getStringFromKey(sender) + StringUtil.getStringFromKey(recipient) + Float.toString(value);
        return StringUtil.verifyECDSASig(sender, data, signature);

    }
    public boolean processTransaction() {

        if(verifySignature() == false){
            System.out.println("#Transaction Signature failed to verify");
            return false;
        }
        //gather transaction inputs (Make sure they are unspent):
        for(TransactionInput i : inputs) {
            i.UTXO = NoobChain.UTXOs.get(i.transactionOutputId);
        }
        //check if transaction is valid
        if(getInputsValue() < NoobChain.minimumTransaction) {
            System.out.println("#Transaction Inputs to small: "+ getInputsValue());
            return false;
        }
        //generate transaction outputs:

        float leftOver = getInputsValue() - value; //get value of inputs then the left over change:
        transactionId = calculateHash();
        outputs.add(new TransactionOutput( this.recipient, value, transactionId)); //send value to recipient
        outputs.add(new TransactionOutput( this.sender, leftOver, transactionId)); //send the left over 'change' back to sender

        //add outputs to Unspent list
        for(TransactionOutput o : outputs){
            NoobChain.UTXOs.put(o.id, o);
        }
        //remove transaction inputs from UTXO lists as spent:
        for(TransactionInput i : inputs){
            if(i.UTXO == null) continue; //if transaction cant be found skip it
            NoobChain.UTXOs.remove(i.UTXO.id);
        }
        return true;
    }
//returns sums of inputs(UTXOs) value
        public float getInputsValue(){
        float total = 0;
        for(TransactionInput i : inputs){
            if(i.UTXO == null) continue;//if Transaction cant be found skip it
            total += i.UTXO.value;
        }
        return  total;
    }
    //return sum of outputs:
    public  float getOutsputsValue(){
        float total = 0;
        for(TransactionOutput o : outputs) {
            total += o.value;
        }
        return total;
    }

}

