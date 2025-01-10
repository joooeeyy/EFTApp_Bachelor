package util;

public class BelohnungsaufschubPoll implements Poll {

    private final String[] questions = {
            "Wenn ich etwas sehe, was ich gerne haben möchte, kaufe ich es im Allgemeinen, ob ich es mir leisten kann oder nicht.", // -
            "Ich finde es besser, 'von der Hand in den Mund zu leben', als längerfristig auf etwas zu sparen.", // -
            "Wenn ich einkaufe, fällt es mir schwer, nur das zu kaufen, was ich mir vorgenommen habe.", // -
            "Wenn man nicht versucht, seine Wünsche sofort zu erfüllen, kann es sein, dass man im Leben etwas versäumt.", // -
            "Leute, die viel sparen und deshalb auf vieles verzichten müssen, sind selbst schuld, denn sie haben nicht viel vom Leben.", // -
            "Beim Einkaufen bin ich häufig versucht, spontan Dinge zu kaufen, die mir gerade ins Auge stechen.", // -
            "Im Allgemeinen lege ich für mögliche Notfälle in der Zukunft etwas Geld zurück.", // +
            "Wenn ich etwas gerne haben möchte, fällt es mir schwer, längere Zeit darauf zu warten.", // -
            "Gegen Monatsende bin ich immer knapp bei Kasse.", // -
            "Ich habe immer ausreichend Vorräte für Notzeiten zuhause.", // +
            "Ich plane im Leben immer alles gründlich, bevor ich etwas entscheide.", // +
            "Wenn ich einkaufe, komme ich häufig mit Dingen nach Hause, die ich eigentlich gar nicht wollte." // -
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
                score += (itemPolarity[i] ? 1 : 0); // Add 1 for positive items
            }
        }
        return new double[]{score};
    }

    @Override
    public String getPollMethod() {
        return "Belohnungsaufschub";
    }
}
