package util;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.CheckBox;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.eftapp.R;

import java.util.List;

import persistance.Cue;

public class CueAdapter extends RecyclerView.Adapter<CueAdapter.MyViewHolder> {
    private List<Cue> cueList;
    private Context context;
    private final OnItemClickListener onItemClickListener;
    private final OnDeleteIconClickListener onDeleteIconClickListener;

    public interface OnItemClickListener {
        void onItemClick(int id);
    }

    public interface OnDeleteIconClickListener {
        void onDeleteIconClick(int id);
    }

    // Constructor to pass in the list of cues
    public CueAdapter(List<Cue> cueList, Context context, OnItemClickListener onItemClickListener, OnDeleteIconClickListener onDeleteIconClickListener) {
        this.cueList = cueList;
        this.context = context;
        this.onItemClickListener = onItemClickListener;
        this.onDeleteIconClickListener = onDeleteIconClickListener;
    }


    @NonNull
    @Override
    public CueAdapter.MyViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        // Inflate the item layout for each row
        View itemView = LayoutInflater.from(context).inflate(R.layout.cue_recycler_view_row, parent, false);
        return new MyViewHolder(itemView, onItemClickListener, onDeleteIconClickListener);
    }

    @Override
    public void onBindViewHolder(@NonNull CueAdapter.MyViewHolder holder, int position) {
        // Set the title and body text from the Cue object
        Cue cue = cueList.get(position);
        holder.titleText.setText(cue.getTitle());
        holder.readCheckbox.setChecked(cue.isRead);

        // Set click listener with the cue ID
        holder.startButton.setOnClickListener(v -> {
            if (onItemClickListener != null) {
                onItemClickListener.onItemClick(cue.getId());
            }
        });

        holder.deleteButton.setOnClickListener(v -> {
            if (onDeleteIconClickListener != null) {
                onDeleteIconClickListener.onDeleteIconClick(cue.getId());
            }
        });

    }

    // Method to update the cue list
    public void updateCueList(List<Cue> newCueList) {
        if (newCueList != null) {
            this.cueList = newCueList;
            notifyDataSetChanged(); // Notify the adapter that the data has changed
        }
    }

    @Override
    public int getItemCount() {
        return cueList.size();
    }


    // ViewHolder to hold the views for each item in the list
    public static class MyViewHolder extends RecyclerView.ViewHolder {

        TextView titleText;
        OnItemClickListener onItemClickListener;
        OnDeleteIconClickListener onDeleteIconClickListener;
        Button startButton;
        CheckBox readCheckbox;
        ImageView deleteButton;

        public MyViewHolder(@NonNull View itemView, OnItemClickListener onItemClickListener, OnDeleteIconClickListener onDeleteIconClickListener) {
            super(itemView);
            titleText = itemView.findViewById(R.id.cueTitle);
            this.startButton = itemView.findViewById(R.id.startButton);
            this.readCheckbox = itemView.findViewById(R.id.readCheckbox);
            this.onItemClickListener = onItemClickListener;
            this.deleteButton = itemView.findViewById(R.id.deleteButton);
            this.onDeleteIconClickListener = onDeleteIconClickListener;
        }
    }
}
