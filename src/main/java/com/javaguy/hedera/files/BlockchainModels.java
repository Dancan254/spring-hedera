package com.javaguy.hedera.files;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Positive;

public class BlockchainModels {

    public record TokenCreateRequest(
            @NotBlank String name,
            @NotBlank String symbol,
            @Positive int initialSupply,
            Integer decimals
    ) {}

    public record TransferRequest(
            @NotBlank String tokenId,
            @NotBlank String toAccountId,
            @Positive long amount
    ) {}

    public record HbarTransferRequest(
            @NotBlank String toAccountId,
            @Positive long amount
    ) {}

    public record BalanceQuery(
            @NotBlank String accountId
    ) {}

    public record OperationResult(
            boolean success,
            String message,
            String transactionId,
            Object data,
            String error
    ) {
        public static OperationResult success(String message, String transactionId) {
            return new OperationResult(true, message, transactionId, null, null);
        }

        public static OperationResult success(String message, Object data) {
            return new OperationResult(true, message, null, data, null);
        }

        public static OperationResult error(String error) {
            return new OperationResult(false, null, null, null, error);
        }
    }
}
