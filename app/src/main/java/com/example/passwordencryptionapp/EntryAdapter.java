package com.example.passwordencryptionapp;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

public class EntryAdapter extends RecyclerView.Adapter<EntryAdapter.ViewHolder> {

    private final List<PasswordEntry> passwordEntries;
    private final OnEditClickListener editClickListener;

    public EntryAdapter(List<PasswordEntry> passwordEntries, OnEditClickListener editClickListener) {
        this.passwordEntries = passwordEntries;
        this.editClickListener = editClickListener;
    }


    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_password_entry, parent, false);
        return new ViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PasswordEntry entry = passwordEntries.get(position);
        holder.serviceNameText.setText(entry.getServiceName());
        holder.usernameText.setText(entry.getUsername());
        holder.passwordText.setText(entry.getPassword());

        // Set up edit button click listener
        holder.editButton.setOnClickListener(v -> editClickListener.onEditClick(entry));
    }

    @Override
    public int getItemCount() {
        return passwordEntries.size();
    }

    public static class ViewHolder extends RecyclerView.ViewHolder {
        TextView serviceNameText, usernameText, passwordText;
        Button editButton;

        public ViewHolder(@NonNull View itemView) {
            super(itemView);
            serviceNameText = itemView.findViewById(R.id.serviceNameText);
            usernameText = itemView.findViewById(R.id.usernameText);
            passwordText = itemView.findViewById(R.id.passwordText);
            editButton = itemView.findViewById(R.id.editButton);
        }
    }

    public interface OnEditClickListener {
        void onEditClick(PasswordEntry entry);
    }
}
