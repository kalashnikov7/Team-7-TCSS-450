package com.example.team_7_tcss_450.ui.contacts;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.widget.Button;
import android.widget.EditText;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.view.MenuHost;
import androidx.core.view.MenuProvider;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.Lifecycle;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.Navigation;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.team_7_tcss_450.R;
import com.example.team_7_tcss_450.databinding.FragmentContactListBinding;
import com.example.team_7_tcss_450.model.UserInfoViewModel;
import com.example.team_7_tcss_450.ui.contacts.model.ContactListViewModel;

import java.util.Objects;

public class ContactListFragment extends Fragment {

    private ContactListViewModel mContactListModel;
    private UserInfoViewModel mUserModel;
    private FragmentContactListBinding binding;


    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ContactListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("fragment_made", "OnCreate Called in ContactListFragment");
        super.onCreate(savedInstanceState);

        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        mUserModel = provider.get(UserInfoViewModel.class);

        mContactListModel = new ViewModelProvider(requireActivity()).get(ContactListViewModel.class);

        if (mContactListModel.isEmptyContactsList() && !mContactListModel.getContactsStatus()) {
            mContactListModel.connectGetContactList(mUserModel.getJWT(),mUserModel.getEmail());}
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentContactListBinding.inflate(inflater);

        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    @SuppressLint("NotifyDataSetChanged")
    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FragmentContactListBinding binding = FragmentContactListBinding.bind(requireView());

        // Set the adapter
        Context context = view.getContext();

        binding.contactList.setLayoutManager(new LinearLayoutManager(context));

        final RecyclerView rv = binding.contactList;
        rv.setAdapter(new ContactListRecyclerViewAdapter(
                mContactListModel.getContactList(),
                mUserModel.getEmail()));

        mContactListModel.addContactListObserver(getViewLifecycleOwner(), (contactsList) -> {
            Objects.requireNonNull(rv.getAdapter()).notifyDataSetChanged();
        });

        // Add "add new contact" icon to top menu bar
        MenuHost menuHost = requireActivity();
        // Add menu items without using the Fragment Menu APIs
        // Note how we can tie the MenuProvider to the viewLifecycleOwner
        // and an optional Lifecycle.State (here, RESUMED) to indicate when
        // the menu should be visible
        menuHost.addMenuProvider(new MenuProvider() {
            @Override
            public void onCreateMenu(@NonNull Menu menu, @NonNull MenuInflater menuInflater) {
                menuInflater.inflate(R.menu.contacts_action_menu, menu);
            }
            // Handle the menu selection
            @SuppressLint("NonConstantResourceId")
            @Override
            public boolean onMenuItemSelected(@NonNull MenuItem menuItem) {
                switch (menuItem.getItemId()) {
                    case R.id.action_contact_send_invite: {
                        showAddContactDialog();
                        return true;
                    }
                    case R.id.navigation_invite_contacts: {
                        navigateToInvites();
                        return true;
                    }
                    case R.id.action_contact_requests: {
                        navigateToRequests();
                        return true;
                    }
                    default:
                        return false;
                }
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    private void navigateToRequests() {
        //On button click, navigate to Second Home
        Navigation.findNavController(requireView()).navigate(
                ContactListFragmentDirections
                        .actionNavigationContactsToNavigationRequestContacts());
    }

    private void navigateToInvites() {
        Navigation.findNavController(requireView()).navigate(R.id.navigation_invite_contacts);
    }


    public void showAddContactDialog() {
        final Dialog dialog = new Dialog(getActivity());
        dialog.requestWindowFeature(Window.FEATURE_NO_TITLE);
        dialog.setCancelable(true);
        dialog.setContentView(R.layout.fragment_add_contact_dialog);
        // Try to play with this later instead of using dialog.findViewById
        /* final FragmentAddChatDialogBinding dialogBinding =
                FragmentAddChatDialogBinding.bind(requireView());*/

        final EditText contactEditText = dialog.findViewById(R.id.edit_contact_email);

        Button confirmButton = dialog.findViewById(R.id.confirm_add_contact);
        Button cancelButton = dialog.findViewById(R.id.cancel_add_contact);

        confirmButton.setOnClickListener(view -> {
            final String contactEmail = contactEditText.getText().toString();
            if (contactEmail.length() == 0)
                contactEditText.setError("Empty Title");
            else {
                mContactListModel.connectAddContact(mUserModel.getJWT(), mUserModel.getEmail(),contactEmail);
                dialog.dismiss();
            }
            Log.d("CHAT PREVIEWS", "DIALOG CONFIRM BUTTON CLICKED");
        });

        cancelButton.setOnClickListener(view -> {
            dialog.cancel();
        });

        dialog.show();
    }

}
