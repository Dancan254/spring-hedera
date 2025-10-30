package com.javaguy.hedera.files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/direct")
@CrossOrigin(origins = "*")
public class DirectHederaController {

    private static final Logger logger = LoggerFactory.getLogger(DirectHederaController.class);
    private final HederaService hederaService;

    public DirectHederaController(HederaService hederaService) {
        this.hederaService = hederaService;
    }

    @GetMapping("/balance/{accountId}")
    public ResponseWrapper checkBalance(@PathVariable String accountId) {
        try {
            logger.info("Direct test: Checking balance for account: {}", accountId);
            BlockchainModels.BalanceQuery request = new BlockchainModels.BalanceQuery(accountId);
            BlockchainModels.OperationResult result = hederaService.getAccountBalance(request);

            return new ResponseWrapper(
                    result.success(),
                    result.success() ? result.message() : result.error(),
                    result.transactionId(),
                    result.data()
            );
        } catch (Exception e) {
            logger.error("Direct balance test failed: {}", e.getMessage(), e);
            return new ResponseWrapper(false, "Exception: " + e.getMessage(), null, null);
        }
    }

    @PostMapping("/token/create")
    public ResponseWrapper createToken(@RequestBody CreateTokenRequest request) {
        try {
            logger.info("Direct test: Creating token {} ({})", request.name(), request.symbol());
            BlockchainModels.TokenCreateRequest tokenRequest = new BlockchainModels.TokenCreateRequest(
                    request.name(),
                    request.symbol(),
                    request.initialSupply(),
                    request.decimals()
            );
            BlockchainModels.OperationResult result = hederaService.createToken(tokenRequest);

            return new ResponseWrapper(
                    result.success(),
                    result.success() ? result.message() : result.error(),
                    result.transactionId(),
                    result.data()
            );
        } catch (Exception e) {
            logger.error("Direct token creation test failed: {}", e.getMessage(), e);
            return new ResponseWrapper(false, "Exception: " + e.getMessage(), null, null);
        }
    }

    @PostMapping("/token/transfer")
    public ResponseWrapper transferTokens(@RequestBody TransferTokenRequest request) {
        try {
            logger.info("Direct test: Transferring {} of token {} to {}",
                    request.amount(), request.tokenId(), request.toAccountId());
            BlockchainModels.TransferRequest transferRequest = new BlockchainModels.TransferRequest(
                    request.tokenId(),
                    request.toAccountId(),
                    request.amount()
            );
            BlockchainModels.OperationResult result = hederaService.transferTokens(transferRequest);

            return new ResponseWrapper(
                    result.success(),
                    result.success() ? result.message() : result.error(),
                    result.transactionId(),
                    result.data()
            );
        } catch (Exception e) {
            logger.error("Direct token transfer test failed: {}", e.getMessage(), e);
            return new ResponseWrapper(false, "Exception: " + e.getMessage(), null, null);
        }
    }

    @PostMapping("/account/create")
    public ResponseWrapper createAccount() {
        try {
            logger.info("Direct test: Creating new Hedera account");
            BlockchainModels.OperationResult result = hederaService.createAccount();

            return new ResponseWrapper(
                    result.success(),
                    result.success() ? result.message() : result.error(),
                    result.transactionId(),
                    result.data()
            );
        } catch (Exception e) {
            logger.error("Direct account creation test failed: {}", e.getMessage(), e);
            return new ResponseWrapper(false, "Exception: " + e.getMessage(), null, null);
        }
    }

    // Test endpoint to verify basic connectivity
    @GetMapping("/health")
    public ResponseWrapper healthCheck() {
        try {
            // Try to check the operator account balance as a health check
            String operatorAccount = "0.0.5800339"; // Your operator account
            BlockchainModels.BalanceQuery request = new BlockchainModels.BalanceQuery(operatorAccount);
            BlockchainModels.OperationResult result = hederaService.getAccountBalance(request);

            return new ResponseWrapper(
                    result.success(),
                    result.success() ? "Hedera connection is healthy" : "Hedera connection failed: " + result.error(),
                    null,
                    result.success() ? result.message() : null
            );
        } catch (Exception e) {
            logger.error("Health check failed: {}", e.getMessage(), e);
            return new ResponseWrapper(false, "Health check failed: " + e.getMessage(), null, null);
        }
    }

    // Request/Response DTOs
    public record CreateTokenRequest(
            String name,
            String symbol,
            int initialSupply,
            Integer decimals
    ) {}

    public record TransferTokenRequest(
            String tokenId,
            String toAccountId,
            long amount
    ) {}

    public record ResponseWrapper(
            boolean success,
            String message,
            String transactionId,
            Object data
    ) {}
}