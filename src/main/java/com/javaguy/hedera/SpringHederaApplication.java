package com.javaguy.hedera;

import com.javaguy.hedera.files.BlockchainTools;
import org.springframework.ai.support.ToolCallbacks;
import org.springframework.ai.tool.ToolCallback;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;

import java.util.List;

@SpringBootApplication
public class SpringHederaApplication {

	public static void main(String[] args) {
		SpringApplication.run(SpringHederaApplication.class, args);
	}

    @Bean
    public List<ToolCallback> tools(BlockchainTools blockchainTools) {
        return List.of(ToolCallbacks.from(blockchainTools));
    }

}
