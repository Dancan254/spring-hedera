package com.javaguy.hedera.files;


import com.hedera.hashgraph.sdk.*;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Service;

@Service
public class HederaService {
    private static final Logger logger = LoggerFactory.getLogger(HederaService.class);
    private final Client client;

    public HederaService(Client client) {
        this.client = client;
    }


    public BlockchainModels.OperationResult createToken(BlockchainModels.TokenCreateRequest request) {
        try{
            logger.info("Creating token: {} ({})", request.name(), request.symbol());
            TokenCreateTransaction transaction = new TokenCreateTransaction()
                    .setTokenName(request.name())
                    .setTokenSymbol(request.symbol())
                    .setInitialSupply(request.initialSupply())
                    .setDecimals(request.decimals() != null ? request.decimals() : 2)
                    .setTreasuryAccountId(client.getOperatorAccountId())
                    .setAdminKey(client.getOperatorPublicKey())
                    .setSupplyKey(client.getOperatorPublicKey())
                    .setFreezeDefault(false);
            TransactionResponse response = transaction.execute(client);
            TransactionReceipt receipt = response.getReceipt(client);

            String tokenId = receipt.tokenId.toString();
            String transactionId = response.transactionId.toString();
            logger.info("Successfully created token: {} with ID: ({})", request.name(), request.symbol());
            return BlockchainModels.OperationResult.success(
                    String.format("Token '%s' created succefully with id: %s", request.name(), tokenId), transactionId
            );
        }catch (Exception e){
            logger.error("Failed to create token: {}", request.name());
            return BlockchainModels.OperationResult.error("Failed to create token: " + e.getMessage());
        }
    }

    public BlockchainModels.OperationResult transferTokens(BlockchainModels.TransferRequest request) {
        try {
            logger.info("Transferring {} of token {} to {}",
                    request.amount(), request.tokenId(), request.toAccountId());

            TokenId tokenId = TokenId.fromString(request.tokenId());
            AccountId toAccount = AccountId.fromString(request.toAccountId());
            AccountId fromAccount = client.getOperatorAccountId();

            TransferTransaction transaction = new TransferTransaction()
                    .addTokenTransfer(tokenId, fromAccount, -request.amount())
                    .addTokenTransfer(tokenId, toAccount, request.amount());

            TransactionResponse response = transaction.execute(client);
            TransactionReceipt receipt = response.getReceipt(client);

            String transactionId = response.transactionId.toString();

            logger.info("Transfer completed successfully: {}", transactionId);

            return BlockchainModels.OperationResult.success(
                    String.format("Successfully transferred %d tokens to %s",
                            request.amount(), request.toAccountId()),
                    transactionId
            );

        } catch (Exception e) {
            logger.error("Failed to transfer tokens: {}", e.getMessage(), e);
            return BlockchainModels.OperationResult.error("Failed to transfer tokens: " + e.getMessage());
        }
    }

    public BlockchainModels.OperationResult getAccountBalance(BlockchainModels.BalanceQuery request) {
        try {
            logger.info("Checking balance for account: {}", request.accountId());

            AccountId accountId = AccountId.fromString(request.accountId());
            AccountBalance balance = new AccountBalanceQuery()
                    .setAccountId(accountId)
                    .execute(client);

            BalanceInfo balanceInfo = new BalanceInfo(
                    balance.hbars.toString(),
                    balance.tokens.toString()
            );

            logger.info("Balance retrieved for {}: {} HBAR", request.accountId(), balance.hbars);

            return BlockchainModels.OperationResult.success(
                    String.format("Account %s has %s HBAR", request.accountId(), balance.hbars),
                    balanceInfo
            );

        } catch (Exception e) {
            logger.error("Failed to get balance: {}", e.getMessage(), e);
            return BlockchainModels.OperationResult.error("Failed to get balance: " + e.getMessage());
        }
    }

    public BlockchainModels.OperationResult createAccount() {
        try {
            logger.info("Creating new Hedera account");

            PrivateKey privateKey = PrivateKey.generateED25519();
            PublicKey publicKey = privateKey.getPublicKey();

            AccountCreateTransaction transaction = new AccountCreateTransaction()
                    .setKey(publicKey)
                    .setInitialBalance(Hbar.fromTinybars(1000)); // Small initial balance

            TransactionResponse response = transaction.execute(client);
            TransactionReceipt receipt = response.getReceipt(client);

            AccountId newAccountId = receipt.accountId;
            String transactionId = response.transactionId.toString();

            AccountInfo accountInfo = new AccountInfo(
                    newAccountId.toString(),
                    publicKey.toString(),
                    privateKey.toString()
            );

            logger.info("Account created successfully: {}", newAccountId);

            return BlockchainModels.OperationResult.success(
                    String.format("Account created successfully: %s", newAccountId),
                    accountInfo
            );

        } catch (Exception e) {
            logger.error("Failed to create account: {}", e.getMessage(), e);
            return BlockchainModels.OperationResult.error("Failed to create account: " + e.getMessage());
        }
    }

    public BlockchainModels.OperationResult transferHbar(BlockchainModels.HbarTransferRequest request) {
        try {
            logger.info("Transferring {} HBAR to account {}", request.amount(), request.toAccountId());

            AccountId toAccount = AccountId.fromString(request.toAccountId());
            AccountId fromAccount = client.getOperatorAccountId();

            TransferTransaction transaction = new TransferTransaction()
                    .addHbarTransfer(fromAccount, Hbar.fromTinybars(-request.amount())) // Deduct from operator
                    .addHbarTransfer(toAccount, Hbar.fromTinybars(request.amount()));   // Add to recipient

            TransactionResponse response = transaction.execute(client);
            TransactionReceipt receipt = response.getReceipt(client);

            String transactionId = response.transactionId.toString();

            logger.info("HBAR transfer completed successfully: {}", transactionId);

            return BlockchainModels.OperationResult.success(
                    String.format("Successfully transferred %d HBAR to %s",
                            request.amount(), request.toAccountId()),
                    transactionId
            );

        } catch (Exception e) {
            logger.error("Failed to transfer HBAR: {}", e.getMessage(), e);
            return BlockchainModels.OperationResult.error("Failed to transfer HBAR: " + e.getMessage());
        }
    }
    // Helper records for response data
    public record BalanceInfo(String hbarBalance, String tokenBalances) {}
    public record AccountInfo(String accountId, String publicKey, String privateKey) {}
}
