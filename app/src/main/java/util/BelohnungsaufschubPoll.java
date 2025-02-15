package util;

public class BelohnungsaufschubPoll implements Poll {

    private final String[] questions = {
            "When I see something I want, I usually buy it, whether I can afford it or not.", // -
            "I think it's better to 'live hand to mouth' than to save up for something in the long term.", // -
            "When I go shopping, I find it hard to stick to only what I planned to buy.", // -
            "If you don't try to fulfill your desires immediately, you might miss out on something in life.", // -
            "People who save a lot and have to give up many things because of it are to blame themselves, as they don't get much out of life.", // -
            "When shopping, I am often tempted to buy things spontaneously that catch my eye.", // -
            "In general, I set aside some money for possible emergencies in the future.", // +
            "When I want something, I find it hard to wait for a long time.", // -
            "At the end of the month, I am always short on cash.", // -
            "I always have enough supplies at home for hard times.", // +
            "I always plan everything thoroughly in life before making a decision.", // +
            "When I shop, I often come home with things I didn't really want." // -
    };


    // Polarity: true for positive items, false for negative items
    private final boolean[] itemPolarity = {
            false, false, false, false, false, false, // Negative items (0: immediate)
            true, // Positive item (1: delayed)
            false, // Negative item (0: immediate)
            false, // Negative item (0: immediate)
            true,  // Positive item (1: delayed)
            true,  // Positive item (1: delayed)
            false  // Negative item (0: immediate)


    };

    private final int[] responses; // Store responses (0 = No, 1 = Yes)
    private int currentIndex = 0;

    public BelohnungsaufschubPoll() {
        responses = new int[questions.length]; // Initialize response array
        for (int i = 0; i < responses.length; i++) {
            responses[i] = -1; // Default to unanswered
        }
    }

    @Override
    public boolean isComplete() {
        return currentIndex >= questions.length;
    }

    @Override
    public String getCurrentQuestion() {
        if (isComplete()) {
            return "Belohnungsaufschub Poll Complete! Thank you for your responses.";
        }
        return questions[currentIndex];
    }

    @Override
    public void handleAnswer(int answerIndex) {
        if (isComplete()) return;
        responses[currentIndex] = answerIndex; // Record response (0 = No, 1 = Yes)
        currentIndex++; // Move to the next question
    }

    @Override
    public String getFeedback() {
        // If no answers have been provided yet
        if (currentIndex == 0) {
            return "Please answer the current question.";
        }

        // If all questions have been answered
        if (isComplete()) {
            return "Poll Complete! Thank you for participating.";
        }

        // Provide feedback for the last answered question
        return "You answered: " + (responses[currentIndex - 1] == 0 ? "No" : "Yes");
    }

    @Override
    public String[] getAnswerChoices() {
        return new String[]{"No", "Yes"};
    }

    // Get the total score based on the number of "Yes" responses and their polarity
    public double[] getTotalScore() {
        int score = 0;
        for (int i = 0; i < responses.length; i++) {
            if (responses[i] == 1) {
                // If the item is positive, 1 means delayed (good)
                // If the item is negative, 1 means immediate (bad)
                score += (itemPolarity[i] ? 0 : 1); // Add 1 for positive items
            } else {
                score += (itemPolarity[i] ? 1 : 0);
            }
        }
        return new double[]{score};
    }

    @Override
    public String getPollMethod() {
        return "Belohnungsaufschub";
    }
}
