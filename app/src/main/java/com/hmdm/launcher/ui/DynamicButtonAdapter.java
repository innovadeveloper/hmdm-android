package com.hmdm.launcher.ui;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.hmdm.launcher.R;

import java.util.ArrayList;
import java.util.List;

public class DynamicButtonAdapter extends RecyclerView.Adapter<DynamicButtonAdapter.DynamicButtonViewHolder> {

    private List<DynamicButton> dynamicButtons = new ArrayList<>();
    private OnDynamicButtonClickListener listener;

    public interface OnDynamicButtonClickListener {
        void onDynamicButtonClick(DynamicButton button);
    }

    public DynamicButtonAdapter(OnDynamicButtonClickListener listener) {
        this.listener = listener;
    }

    @NonNull
    @Override
    public DynamicButtonViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.item_dynamic_button, parent, false);
        return new DynamicButtonViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull DynamicButtonViewHolder holder, int position) {
        DynamicButton button = dynamicButtons.get(position);
        holder.bind(button, listener);
    }

    @Override
    public int getItemCount() {
        return dynamicButtons.size();
    }

    public void setDynamicButtons(List<DynamicButton> buttons) {
        this.dynamicButtons = buttons != null ? buttons : new ArrayList<>();
        notifyDataSetChanged();
    }

    public void addDynamicButton(DynamicButton button) {
        this.dynamicButtons.add(button);
        notifyItemInserted(dynamicButtons.size() - 1);
    }

    public void removeDynamicButton(int position) {
        if (position >= 0 && position < dynamicButtons.size()) {
            dynamicButtons.remove(position);
            notifyItemRemoved(position);
        }
    }

    public void clearDynamicButtons() {
        dynamicButtons.clear();
        notifyDataSetChanged();
    }

    static class DynamicButtonViewHolder extends RecyclerView.ViewHolder {
        private Button dynamicButton;

        public DynamicButtonViewHolder(@NonNull View itemView) {
            super(itemView);
            dynamicButton = itemView.findViewById(R.id.dynamicButton);
        }

        public void bind(DynamicButton button, OnDynamicButtonClickListener listener) {
            dynamicButton.setText(button.getTitle());
            dynamicButton.setEnabled(button.isEnabled());
            
            if (button.getIconResId() != 0) {
                dynamicButton.setCompoundDrawablesWithIntrinsicBounds(button.getIconResId(), 0, 0, 0);
            } else {
                dynamicButton.setCompoundDrawablesWithIntrinsicBounds(0, 0, 0, 0);
            }

            if(button.isLongClick()){
                dynamicButton.setOnLongClickListener(new View.OnLongClickListener() {
                    @Override
                    public boolean onLongClick(View view) {
                        if (listener != null) {
                            listener.onDynamicButtonClick(button);
                        }
                        return true;
                    }
                });
            }else{
                dynamicButton.setOnClickListener(v -> {
                    if (listener != null) {
                        listener.onDynamicButtonClick(button);
                    }
                });
            }

        }
    }
}