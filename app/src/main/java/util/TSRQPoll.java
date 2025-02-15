package util;

public class TSRQPoll implements Poll {

    private final String[] questions;
    private int[] responses; // Store responses (1 to 7 Likert scale)
    private int currentIndex = 0;
    private final int NUM_ITEMS = 15; // Corrected to 15 (TSRQ has 15 questions)
    private String firstReason;

    public TSRQPoll(String[] questions) {
        this.questions = questions;
        this.firstReason = questions[0]; // General question
        currentIndex++; // Skip the first question (general question)

        responses = new int[NUM_ITEMS + 1]; // 16 elements (1 general + 15 TSRQ questions)
        for (int i = 0; i < responses.length; i++) {
            responses[i] = -1; // Default to unanswered
        }
    }

    @Override
    public boolean isComplete() {
        return currentIndex > NUM_ITEMS; // Complete after 15 TSRQ questions
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
            return "Poll Complete! Thank you for participating. The score is: ";
        }

        return "You answered: " + responses[currentIndex - 1];
    }

    @Override
    public String[] getAnswerChoices() {
        return new String[]{"Strongly Disagree", "Disagree", "Slightly Disagree", "Neither", "Slightly Agree", "Agree", "Strongly Agree"};
    }

    @Override
    public double[] getTotalScore() {
        // Define indices for each subscale (adjusted to skip the first element)
        int[] autonomousItems = {1, 3, 6, 8, 11, 13}; // Adjusted to 1-based index (skipping responses[0])
        int[] controlledItems = {2, 4, 7, 9, 12, 14}; // Adjusted to 1-based index
        int[] amotivationItems = {5, 10, 15}; // Adjusted to 1-based index

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