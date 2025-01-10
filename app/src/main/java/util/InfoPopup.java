package util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupWindow;
import android.widget.TextView;

import com.example.eftapp.R;

public class InfoPopup {
    private Context context;

    public InfoPopup(Context context) {
        this.context = context;
    }

    public void showPopup(View anchorView, String message) {
        // Inflate the custom view
        View popupView = LayoutInflater.from(context).inflate(R.layout.popup_info, null);

        // Create the PopupWindow
        PopupWindow popupWindow = new PopupWindow(popupView,
                ViewGroup.LayoutParams.WRAP_CONTENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);

        // Set the message
        TextView messageTextView = popupView.findViewById(R.id.popupText);
        messageTextView.setText(message);

        // Show the popup
        popupWindow.setFocusable(true); // Make it dismissible
        popupWindow.showAsDropDown(anchorView, 0, 0);
    }
}
