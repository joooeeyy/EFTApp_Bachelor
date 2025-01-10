package util;

public class AdjustingAmountPoll implements Poll {

    private double immediateAmount = 50.0; // Start with $50 immediate
    private final double delayedAmount = 100.0; // Fixed delayed reward
    private final double adjustmentFactor = 0.5; // Adjustment halves each time
    private final double threshold = 0.01; // Stopping rule threshold
    private int delayInMonths = 1; // Initial delay
    private double adjustmentStep = 25.0; // Initial adjustment step size
    private boolean isComplete = false;

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

        adjustmentStep *= adjustmentFactor; // Halve the adjustment step
        if (adjustmentStep < threshold) {
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
        return new double[]{adjustmentStep};
    }

    @Override
    public String getPollMethod() {
        return "Delayed Discounting";
    }
}

