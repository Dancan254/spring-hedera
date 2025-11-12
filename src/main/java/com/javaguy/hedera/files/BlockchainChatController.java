package com.javaguy.hedera.files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.client.advisor.MessageChatMemoryAdvisor;
import org.springframework.ai.chat.memory.ChatMemory;
import org.springframework.ai.chat.memory.MessageWindowChatMemory;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/chat")
@CrossOrigin(origins = "*")
public class BlockchainChatController {

    private static final Logger logger = LoggerFactory.getLogger(BlockchainChatController.class);

    private final ChatClient chatClient;
    private final BlockchainTools blockchainTools;
    private final LoanAssistanceService loanAssistanceService;

    public BlockchainChatController(ChatModel chatModel, ChatMemory chatMemory, BlockchainTools blockchainTools, LoanAssistanceService loanAssistanceService) {
        this.blockchainTools = blockchainTools;
        this.loanAssistanceService = loanAssistanceService;
        this.chatClient = ChatClient.builder(chatModel)
                .defaultAdvisors(MessageChatMemoryAdvisor.builder(chatMemory).build())
                .defaultSystem("""
                    You are a Hedera blockchain assistant. You MUST use the available functions for ALL blockchain operations.
                    
                    IMPORTANT: When users ask about blockchain operations, you MUST call the appropriate function:
                    
                    1. For balance checking: ALWAYS call checkBalance function with the account ID
                    2. For token creation: ALWAYS call createToken function
                    3. For token transfers: ALWAYS call transferTokens function
                    4. For account creation: ALWAYS call createAccount function
                    
                    DO NOT provide generic responses. ALWAYS use the functions to get real data.
                    If a function returns an error, please show the user the exact error message.
                    
                    Available functions:
                    - checkBalance: Use this for ANY balance inquiry
                    - createToken: Use this for token creation
                    - transferTokens: Use this for token transfers  
                    - createAccount: Use this for new account creation
                    You are allowed to answer simple conversational questions (like names and greetings) using the conversation history, but avoid complex topics outside the scope of Hedera services.
                    Do not answer any questions of of the scope of the application which is handling the hedera services
                    """)
                .build();
    }

    @PostMapping
    public AIChatResponse chat(@RequestBody AIChatRequest request) {
        try {
            logger.info("Received chat request: {}", request.message());
            String conversationId = "general-chat-session";

            logger.info("Using conversation ID: {}", conversationId);

            String response = chatClient.prompt()
                    .user(request.message())
                    .advisors(a -> a.param(ChatMemory.CONVERSATION_ID, conversationId))
                    .tools(blockchainTools)
                    .call()
                    .content();

            logger.info("Chat response: {}", response);
            return new AIChatResponse(response, true);

        } catch (Exception e) {
            logger.error("Error processing chat request: {}", e.getMessage(), e);
            return new AIChatResponse(
                    "Sorry, I encountered an error: " + e.getMessage(),
                    false
            );
        }
    }

    @PostMapping("/loan/{userId}")
    public ResponseEntity<LoanAssistanceService.ChatResponse> chatAboutLoan(
            @PathVariable String userId,
            @RequestBody AIChatRequest request) {
        try {
            logger.info("Processing loan chat for user: {}", userId);
            var response = loanAssistanceService.handleLoanQuery(userId, request.message());
            return ResponseEntity.ok(response);
        } catch (Exception e) {
            logger.error("Error in loan chat: {}", e.getMessage(), e);
            return ResponseEntity.internalServerError().build();
        }
    }
    public record AIChatRequest(String message) {}
    public record AIChatResponse(String response, boolean success) {}
}