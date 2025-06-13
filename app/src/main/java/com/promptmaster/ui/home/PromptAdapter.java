package com.promptmaster.ui.home;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.promptmaster.R;
import com.promptmaster.data.Prompt;

import java.util.ArrayList;
import java.util.List;

public class PromptAdapter extends RecyclerView.Adapter<PromptAdapter.PromptViewHolder> {

    private List<Prompt> prompts = new ArrayList<>();

    public void setPrompts(List<Prompt> newPrompts) {
        this.prompts = newPrompts;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public PromptViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.prompt_item, parent, false);
        return new PromptViewHolder(itemView);
    }

    @Override
    public void onBindViewHolder(@NonNull PromptViewHolder holder, int position) {
        Prompt currentPrompt = prompts.get(position);
        holder.titleTextView.setText(currentPrompt.getTitle());
        holder.descriptionTextView.setText(currentPrompt.getDescription());
    }

    @Override
    public int getItemCount() {
        return prompts.size();
    }

    static class PromptViewHolder extends RecyclerView.ViewHolder {
        private final TextView titleTextView;
        private final TextView descriptionTextView;

        public PromptViewHolder(@NonNull View itemView) {
            super(itemView);
            titleTextView = itemView.findViewById(R.id.prompt_title);
            descriptionTextView = itemView.findViewById(R.id.prompt_description);
        }
    }
}
