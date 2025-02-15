package util;

public class AdjustingAmountPoll implements Poll {

    private double immediateAmount; // Start with half of the delayed reward
    private final double delayedAmount; // Fixed delayed reward ($100)
    private final double adjustmentFactor = 0.5; // Adjustment halves each time
    private final double threshold = 0.01; // Smaller threshold for finer precision
    private final double minAdjustmentStep = 0.10; // Minimum adjustment step
    private int delayInMonths = 1; // Delay in months (e.g., 1 month)
    private double adjustmentStep; // Initial adjustment step size
    private boolean isComplete = false;
    private int maxIterations = 20; // Increased maximum iterations
    private int currentIteration = 0;

    // Constructor to set the delayed reward amount
    public AdjustingAmountPoll() {
        this.delayedAmount = 100.0; // Fixed delayed reward ($100)
        this.immediateAmount = delayedAmount / 2; // Start with $50
        this.adjustmentStep = 10.0; // Initial step size is $10
    }

    @Override
    public boolean isComplete() {
        return isComplete;
    }

    @Override
    public String getCurrentQuestion() {
        if (isComplete) {
            return "Task Complete! Indifference Point: $" + String.format("%.2f", immediateAmount);
        }
        return "Would you prefer $" + String.format("%.2f", immediateAmount)
                + " now or $" + String.format("%.2f", delayedAmount)
                + " in " + delayInMonths + " months?";
    }

    @Override
    public void handleAnswer(int answerIndex) {
        if (isComplete) return;

        if (answerIndex == 0) { // Immediate reward chosen
            immediateAmount -= adjustmentStep;
        } else { // Delayed reward chosen
            immediateAmount += adjustmentStep;
        }

        // Ensure immediateAmount stays within bounds (0 <= immediateAmount <= delayedAmount)
        immediateAmount = Math.max(0, Math.min(immediateAmount, delayedAmount));

        // Halve the adjustment step, but don't let it go below the minimum step size
        adjustmentStep = Math.max(adjustmentStep * adjustmentFactor, minAdjustmentStep);

        currentIteration++;

        // Stop if the adjustment step is below the threshold or max iterations reached
        if (adjustmentStep <= threshold || currentIteration >= maxIterations) {
            isComplete = true;
        }
    }

    @Override
    public String getFeedback() {
        if (isComplete) {
            return "Indifference Point Found: $" + String.format("%.2f", immediateAmount);
        }
        return "Adjusting Amount Task: $" + String.format("%.2f", immediateAmount);
    }

    @Override
    public String[] getAnswerChoices() {
        return new String[]{
                "$" + String.format("%.2f", immediateAmount) + " now",
                "$" + String.format("%.2f", delayedAmount) + " in " + delayInMonths + " months"
        };
    }

    @Override
    public double[] getTotalScore() {
        return new double[]{calculateDiscountRate()};
    }

    @Override
    public String getPollMethod() {
        return "Delayed Discounting";
    }

    // New method to calculate the discount rate
    public double calculateDiscountRate() {
        if (!isComplete) {
            throw new IllegalStateException("Task is not complete. Cannot calculate discount rate.");
        }
        double delayInYears = delayInMonths / 12.0; // Convert delay to years
        return (delayedAmount - immediateAmount) / (immediateAmount * delayInYears);
    }
}