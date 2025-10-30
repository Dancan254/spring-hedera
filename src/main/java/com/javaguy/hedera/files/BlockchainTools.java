package com.javaguy.hedera.files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.ai.tool.annotation.ToolParam;
import org.springframework.stereotype.Component;

@Component
public class BlockchainTools {

    private static final Logger logger = LoggerFactory.getLogger(BlockchainTools.class);
    private final HederaService hederaService;

    public BlockchainTools(HederaService hederaService) {
        this.hederaService = hederaService;
    }

    @Tool(description = "Create a new token on Hedera network. Requires token name, symbol, initial supply, and optional decimals.")
    public String createToken(
            @ToolParam(description = "The name of the token") String name,
            @ToolParam(description = "The symbol/ticker of the token") String symbol,
            @ToolParam(description = "Initial supply of tokens to create") int initialSupply,
            @ToolParam(description = "Number of decimal places (default 2)", required = false) Integer decimals) {

        try {
            logger.debug("Creating token with name: {}, symbol: {}, supply: {}", name, symbol, initialSupply);
            BlockchainModels.TokenCreateRequest request = new BlockchainModels.TokenCreateRequest(name, symbol, initialSupply, decimals);
            BlockchainModels.OperationResult result = hederaService.createToken(request);

            if (result.success()) {
                return String.format("✅ Token created successfully!\n" +
                                "Message: %s\n" +
                                "Transaction ID: %s\n" +
                                "You can verify this transaction on HashScan: https://hashscan.io/testnet/transaction/%s",
                        result.message(), result.transactionId(), result.transactionId());
            } else {
                logger.error("Token creation failed: {}", result.error());
                return "❌ Failed to create token: " + result.error();
            }
        } catch (Exception e) {
            logger.error("Exception in createToken: {}", e.getMessage(), e);
            return "❌ Error creating token: " + e.getMessage();
        }
    }

    @Tool(description = "Transfer tokens from the operator account to another account. Requires token ID, recipient account ID, and amount.")
    public String transferTokens(
            @ToolParam(description = "The ID of the token to transfer (format: 0.0.XXXXXX)") String tokenId,
            @ToolParam(description = "The account ID to transfer tokens to (format: 0.0.XXXXXX)") String toAccountId,
            @ToolParam(description = "Amount of tokens to transfer") long amount) {

        try {
            logger.debug("Transferring {} tokens {} to account {}", amount, tokenId, toAccountId);
            BlockchainModels.TransferRequest request = new BlockchainModels.TransferRequest(tokenId, toAccountId, amount);
            BlockchainModels.OperationResult result = hederaService.transferTokens(request);

            if (result.success()) {
                return String.format("✅ Transfer completed!\n" +
                                "Message: %s\n" +
                                "Transaction ID: %s\n" +
                                "View on HashScan: https://hashscan.io/testnet/transaction/%s",
                        result.message(), result.transactionId(), result.transactionId());
            } else {
                logger.error("Token transfer failed: {}", result.error());
                return "❌ Transfer failed: " + result.error();
            }
        } catch (Exception e) {
            logger.error("Exception in transferTokens: {}", e.getMessage(), e);
            return "❌ Error transferring tokens: " + e.getMessage();
        }
    }

    @Tool(description = "Check HBAR and token balance for any Hedera account")
    public String checkBalance(
            @ToolParam(description = "The account ID to check balance for (format: 0.0.XXXXXX)") String accountId) {

        try {
            logger.debug("Checking balance for account: {}", accountId);
            BlockchainModels.BalanceQuery request = new BlockchainModels.BalanceQuery(accountId);
            BlockchainModels.OperationResult result = hederaService.getAccountBalance(request);

            if (result.success()) {
                return String.format("✅ Balance retrieved for account %s:\n%s\n" +
                                "View account details: https://hashscan.io/testnet/account/%s",
                        accountId, result.message(), accountId);
            } else {
                logger.error("Balance check failed for account {}: {}", accountId, result.error());
                return String.format("❌ Failed to get balance for account %s: %s", accountId, result.error());
            }
        } catch (Exception e) {
            logger.error("Exception in checkBalance for account {}: {}", accountId, e.getMessage(), e);
            return String.format("❌ Error checking balance for account %s: %s", accountId, e.getMessage());
        }
    }

    @Tool(description = "Create a new Hedera account with a small initial balance")
    public String createAccount() {
        try {
            logger.debug("Creating new Hedera account");
            BlockchainModels.OperationResult result = hederaService.createAccount();

            if (result.success()) {
                if (result.data() instanceof HederaService.AccountInfo accountInfo) {
                    return String.format("✅ New account created!\n" +
                                    "Account ID: %s\n" +
                                    "Transaction ID: %s\n" +
                                    "⚠️ IMPORTANT: Save the private key securely!\n" +
                                    "Private Key: %s\n" +
                                    "View account: https://hashscan.io/testnet/account/%s",
                            accountInfo.accountId(), result.transactionId(),
                            accountInfo.privateKey(), accountInfo.accountId());
                } else {
                    return "✅ " + result.message() + "\nTransaction ID: " + result.transactionId();
                }
            } else {
                logger.error("Account creation failed: {}", result.error());
                return "❌ Failed to create account: " + result.error();
            }
        } catch (Exception e) {
            logger.error("Exception in createAccount: {}", e.getMessage(), e);
            return "❌ Error creating account: " + e.getMessage();
        }
    }

    @Tool(description = "Transfer HBAR (Hedera native currency) from the operator account to another account. Requires recipient account ID and amount.")
    public String transferHbar(
            @ToolParam(description = "The account ID to transfer HBAR to (format: 0.0.XXXXXX)") String toAccountId,
            @ToolParam(description = "Amount of HBAR to transfer") long amount) {
        try {
            logger.debug("Transferring {} HBAR to account {}", amount, toAccountId);
            BlockchainModels.HbarTransferRequest request = new BlockchainModels.HbarTransferRequest(toAccountId, amount);
            BlockchainModels.OperationResult result = hederaService.transferHbar(request);

            if (result.success()) {
                return String.format("✅ HBAR transfer completed!\n" +
                                "Message: %s\n" +
                                "Transaction ID: %s\n" +
                                "View on HashScan: https://hashscan.io/testnet/transaction/%s",
                        result.message(), result.transactionId(), result.transactionId());
            } else {
                logger.error("HBAR transfer failed: {}", result.error());
                return "❌ HBAR transfer failed: " + result.error();
            }
        } catch (Exception e) {
            logger.error("Exception in transferHbar: {}", e.getMessage(), e);
            return "❌ Error transferring HBAR: " + e.getMessage();
        }
    }
}