import java.security.*;
import java.security.spec.ECGenParameterSpec;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

public class Wallet {
    public PrivateKey privateKey;
    public PublicKey publicKey;

    // Only UTXOs owned by this wallet.
    public HashMap<String, TransactionOutput> UTXOs = new HashMap<String, TransactionOutput>();

    public Wallet() {
        generateKeyPair();
    }

    // Generates a key pair (public and private keys) for the wallet.
    public void generateKeyPair() {
        try {
            KeyPairGenerator keyGen = KeyPairGenerator.getInstance("ECDSA", "BC");
            SecureRandom random = SecureRandom.getInstance("SHA1PRNG");
            ECGenParameterSpec ecSpec = new ECGenParameterSpec("prime192v1");
            // Initialize the key generator and generate a KeyPair.
            keyGen.initialize(ecSpec, random);
            KeyPair keyPair = keyGen.generateKeyPair();
            // Set the public and private keys from the KeyPair.
            privateKey = keyPair.getPrivate();
            publicKey = keyPair.getPublic();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    // Returns the balance and stores the UTXOs owned by this wallet in this.UTXOs.
    public float getBalance() {
        float total = 0;
        for (Map.Entry<String, TransactionOutput> item : NoobChain.UTXOs.entrySet()) {
            TransactionOutput UTXO = item.getValue();
            if (UTXO.isMine(publicKey)) { // If output belongs to me (if coins belong to me).
                UTXOs.put(UTXO.id, UTXO); // Add to our list of unspent transactions.
                total += UTXO.value;
            }
        }
        return total;
    }

    // Generates and returns a new transaction from this wallet.
    public Transaction sendFunds(PublicKey _recipient, float value) {
        if (getBalance() < value) { // Gather balance and check funds.
            System.out.println("#Not enough Funds to send transaction. Transaction Discarded.");
            return null;
        }

        // List of inputs for the transaction.
        ArrayList<TransactionInput> inputs = new ArrayList<>();

        float total = 0;
        for (Map.Entry<String, TransactionOutput> item : UTXOs.entrySet()) {
            TransactionOutput UTXO = item.getValue();
            total += UTXO.value;
            inputs.add(new TransactionInput(UTXO.id));
            if (total > value) break;
        }

        Transaction newTransaction = new Transaction(publicKey, _recipient, value, inputs);
        newTransaction.generateSignature(privateKey);

        // Remove the used inputs from our list of UTXOs.
        for (TransactionInput input : inputs) {
            UTXOs.remove(input.transactionOutputId);
        }

        return newTransaction;
    }
}
