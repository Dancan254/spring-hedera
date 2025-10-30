package com.javaguy.hedera.files;

import com.hedera.hashgraph.sdk.AccountId;
import com.hedera.hashgraph.sdk.Client;
import com.hedera.hashgraph.sdk.PrivateKey;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.util.concurrent.TimeoutException;

@Configuration
public class HederaConfig {
    @Value("${hedera.network}")
    private String network;

    @Value("${hedera.operator.account-id}")
    private String operatorAccountId;

    @Value("${hedera.operator.private-key}")
    private String operatorPrivateKey;

    @Bean
    public Client client() throws TimeoutException {
        Client client = "testnet".equals(network)
                ? Client.forTestnet()
                : Client.forMainnet();
        client.setOperator(
                AccountId.fromString(operatorAccountId),
                PrivateKey.fromString(operatorPrivateKey)
        );
        return client;
    }
}
