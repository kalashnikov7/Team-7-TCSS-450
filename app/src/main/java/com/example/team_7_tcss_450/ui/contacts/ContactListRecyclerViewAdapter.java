package com.example.team_7_tcss_450.ui.contacts;

import android.content.res.Resources;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import androidx.annotation.NonNull;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team_7_tcss_450.R;
import com.example.team_7_tcss_450.databinding.FragmentContactCardBinding;
import com.example.team_7_tcss_450.ui.contacts.model.Contact;

import java.util.List;

public class ContactListRecyclerViewAdapter extends RecyclerView.Adapter<ContactListRecyclerViewAdapter.ContactViewHolder> {

    private final List<Contact> mValues;
    private final String mUserEmail;

    public ContactListRecyclerViewAdapter(List<Contact> items, String email)
    {
        mValues = items;
        mUserEmail = email;
    }

    @NonNull
    @Override
    public ContactListRecyclerViewAdapter.ContactViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new ContactListRecyclerViewAdapter.ContactViewHolder(LayoutInflater
                .from(parent.getContext())
                .inflate(R.layout.fragment_contact_card, parent, false));
    }

    @Override
    public void onBindViewHolder(final ContactViewHolder holder, int position) {
        holder.mItem = mValues.get(position);
        Contact item = holder.mItem;
        Resources res = holder.mView.getResources();
        final FragmentContactCardBinding binding = holder.binding;
        final String fullName = item.getFirstName() + " " + item.getLastName();

        binding.textViewContactName.setText(fullName);
        binding.textViewContactUsername.setText(item.getUserName());

        holder.openContact(mValues.get(position));
    }

    @Override
    public int getItemCount() {
        return mValues.size();
    }

    public static class ContactViewHolder extends RecyclerView.ViewHolder {
        public Contact mItem;
        public View mView;
        public FragmentContactCardBinding binding;

        public ContactViewHolder(View view) {
            super(view);
            mView = view;
            binding = FragmentContactCardBinding.bind(view);
        }

        void openContact(final Contact contact) {
            mItem = contact;
            binding.openContact.setOnClickListener(view -> {
                Navigation.findNavController(mView).navigate(
                        ContactListFragmentDirections.actionNavigationContactsToContactFragment(contact)
                );
            });
        }

    }

}