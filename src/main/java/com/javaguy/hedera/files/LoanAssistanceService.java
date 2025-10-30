package com.javaguy.hedera.files;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.ai.chat.client.ChatClient;
import org.springframework.ai.chat.model.ChatModel;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Service
public class LoanAssistanceService {
    private static final Logger logger = LoggerFactory.getLogger(LoanAssistanceService.class);

    private final Map<String, List<ChatMessage>> conversationHistory = new ConcurrentHashMap<>();
    private final Map<String, UserLoanData> userLoanData = new ConcurrentHashMap<>();
    private final ChatClient chat;

    public LoanAssistanceService(ChatModel chatModel) {
        this.chat = ChatClient.builder(chatModel)
                .defaultSystem("""
                        You are a helpful DeFi lending assistant specializing in Hedera-based loans.
                        You help users understand:
                        - Loan terms and conditions
                        - Collateral requirements
                        - APY rates and calculations
                        - Repayment schedules
                        - Risk management
                        
                        Always be clear, concise, and helpful. Use simple language to explain complex DeFi concepts.
                        When users ask about specific loans, reference their loan data if available.
                        Provide actionable advice and warnings about risks when appropriate.
                        """)
                .build();
    }

    //loan assistance
    public ChatResponse handleLoanQuery(String userId, String query) {
        logger.info("Processing loan query for user: {} ", userId);

        //conversation history
        List<ChatMessage> history = conversationHistory.computeIfAbsent(userId, k -> List.of());

        UserLoanData loanData = userLoanData.get(userId);

        StringBuilder context = new StringBuilder();
        if (loanData != null) {
            context.append("User's current loans:\n");
            for (Loan loan : loanData.activeLoans()) {
                context.append(String.format("""
                                - Loan ID: %s
                                  Amount: $%f
                                  Collateral: %f %s
                                  APY: %.2f%%
                                  Due: %s
                                  Status: %s
                                """,
                        loan.loanId(),
                        loan.amount(),
                        loan.collateralAmount(),
                        loan.collateralToken(),
                        loan.apy(),
                        loan.dueDate(),
                        loan.status()
                ));
            }
        }

        StringBuilder conversationContext = new StringBuilder();
        for(ChatMessage msg : history.subList(Math.max(0, history.size() - 5), history.size())){
            conversationContext.append(msg.role()).append(": ").append(msg.content()).append("\n");
        }

        String prompt = String.format("""
                User context:
                %s
                
                Recent conversation:
                %s
                
                current query: %s
                
                Provide a helpful, personalized response. If discussing specific loans, references the user's loan data if available.
                Be conversational but professional.
                """, context.toString(), conversationContext.toString(), query);

        String response = chat.prompt()
                .user(prompt)
                .call()
                .content();
        logger.info("Chat response: {}", response);
        history.add(new ChatMessage("user", query));
        history.add(new ChatMessage("assistant", response));

        if (history.size() > 20){
            history.subList(0, history.size() - 20).clear();
        }
        return new ChatResponse(response, detectIntent(query) );
    }
    private String detectIntent(String query) {
        String lowerQuery = query.toLowerCase();
        if (lowerQuery.contains("collateral") || lowerQuery.contains("ltv")) {
            return "COLLATERAL_INQUIRY";
        } else if (lowerQuery.contains("apy") || lowerQuery.contains("rate") || lowerQuery.contains("interest")) {
            return "RATE_INQUIRY";
        } else if (lowerQuery.contains("repay") || lowerQuery.contains("pay back")) {
            return "REPAYMENT_INQUIRY";
        } else if (lowerQuery.contains("risk") || lowerQuery.contains("safe")) {
            return "RISK_INQUIRY";
        }
        return "GENERAL_INQUIRY";
    }


    public record ChatMessage(String role, String content) {}
    public record ChatResponse(String message, String intent) {}
    public record UserLoanData(
            List<Loan> activeLoans,
            double totalBorrowed,
            double averageAPY,
            List<String> collateralTypes,
            String riskProfile
    ) {}

    public record Loan(
            String loanId,
            double amount,
            double collateralAmount,
            String collateralToken,
            double collateralValue,
            double apy,
            String dueDate,
            String status
    ) {}
}

