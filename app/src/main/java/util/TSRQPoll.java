package util;

public class TSRQPoll implements Poll {

    private final String[] questions;

    private int[] responses; // Store responses (1 to 7 Likert scale)
    private int currentIndex = 0;
    private final int NUM_ITEMS = 16;
    private String firstReason;

    public TSRQPoll(String[] questions) {
        this.questions = questions;
        this.firstReason = questions[0];
        currentIndex++; // Leave out the first sentence

        responses = new int[NUM_ITEMS]; // Initialize response array
        for (int i = 0; i < responses.length; i++) {
            responses[i] = -1; // Default to unanswered
        }
    }

    @Override
    public boolean isComplete() {
        return currentIndex >= NUM_ITEMS;
    }

    @Override
    public String getCurrentQuestion() {
        if (isComplete()) {
            return "TSRQ Poll Complete! Thank you for your responses.";
        }
        return firstReason + " " + questions[currentIndex];
    }

    @Override
    public void handleAnswer(int answerIndex) {
        if (isComplete()) return;

        // We assume the answerIndex is from 0 to 6, representing:
        // 0 = Not at all true, 6 = Very true
        responses[currentIndex] = answerIndex + 1; // Convert to scale from 1 to 7
        currentIndex++; // Move to the next question
    }

    @Override
    public String getFeedback() {
        if (currentIndex == 0) {
            return "Please answer the current question.";
        }

        if (isComplete()) {
            return "Poll Complete! Thank you for participating. The score is: " ;
        }

        return "You answered: " + responses[currentIndex - 1];
    }

    @Override
    public String[] getAnswerChoices() {
        return new String[]{"Not at all true", "Slightly true", "Somewhat true", "Moderately true", "Very true", "Extremely true", "Very true"};
    }

    @Override
    public double[] getTotalScore() {
        // Define indices for each subscale
        int[] autonomousItems = {0, 2, 5, 7, 10, 12}; // Adjusting to 0-based index (1 -> 0, 3 -> 2, etc.)
        int[] controlledItems = {1, 3, 6, 8, 11, 13}; // Adjusting to 0-based index
        int[] amotivationItems = {4, 9, 14}; // Adjusting to 0-based index

        // Calculate autonomous motivation mean
        double autonomousSum = 0;
        for (int index : autonomousItems) {
            autonomousSum += responses[index];
        }
        double autonomousMean = autonomousSum / autonomousItems.length;

        // Calculate controlled motivation mean
        double controlledSum = 0;
        for (int index : controlledItems) {
            controlledSum += responses[index];
        }
        double controlledMean = controlledSum / controlledItems.length;

        // Calculate amotivation mean
        double amotivationSum = 0;
        for (int index : amotivationItems) {
            amotivationSum += responses[index];
        }
        double amotivationMean = amotivationSum / amotivationItems.length;

        // Return the scores for all three subscales
        return new double[]{autonomousMean, controlledMean, amotivationMean};
    }

    @Override
    public String getPollMethod() {
        return "TSRQ";
    }
}

