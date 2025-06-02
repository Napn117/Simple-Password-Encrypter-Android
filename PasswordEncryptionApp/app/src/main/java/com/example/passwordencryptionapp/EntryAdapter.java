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

    /**
     * Constructor for EntryAdapter.
     * This takes a list of PasswordEntry objects and a listener for edit actions.
     * The list represents the password entries that will be displayed in the RecyclerView.
     * The OnEditClickListener allows actions when the edit button is clicked.
     */
    public EntryAdapter(List<PasswordEntry> passwordEntries, OnEditClickListener editClickListener) {
        this.passwordEntries = passwordEntries;
        this.editClickListener = editClickListener;
    }

    /**
     * This method is called when a new ViewHolder needs to be created.
     * It inflates the layout for a single password entry item.
     */
    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_password_entry, parent, false);
        return new ViewHolder(view);
    }

    /**
     * This method binds the data to the ViewHolder.
     * It sets the service name, username, and password text fields, and sets the click listener for the edit button.
     */
    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        PasswordEntry entry = passwordEntries.get(position);
        holder.serviceNameText.setText(entry.getServiceName());
        holder.usernameText.setText(entry.getUsername());
        holder.passwordText.setText(entry.getPassword());
        holder.editButton.setOnClickListener(v -> editClickListener.onEditClick(entry));
    }

    /**
     * This method returns the number of items in the list.
     * It tells the RecyclerView how many password entries need to be displayed.
     */
    @Override
    public int getItemCount() {
        return passwordEntries.size();
    }

    /**
     * ViewHolder class for holding the views of a single password entry.
     * This contains references to the TextViews for service name, username, and password,
     * as well as the button for editing the entry.
     */
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

    /**
     * Interface for handling edit button clicks.
     * This allows the adapter to communicate edit actions to the activity or fragment that is using it.
     */
    public interface OnEditClickListener {
        void onEditClick(PasswordEntry entry);
    }
}