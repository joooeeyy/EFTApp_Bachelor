package util;

public interface Poll {
    boolean isComplete();
    String getCurrentQuestion();
    void handleAnswer(int answerIndex); // Handle the selected answer (0-based index)
    String getFeedback();
    String[] getAnswerChoices(); // Provide answer options for the current question
    double[] getTotalScore();
    String getPollMethod();
}
