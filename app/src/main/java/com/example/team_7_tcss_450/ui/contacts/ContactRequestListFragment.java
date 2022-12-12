package com.example.team_7_tcss_450.ui.contacts;

import android.annotation.SuppressLint;
import android.app.Dialog;
import android.content.Context;
import android.os.Bundle;

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

import com.example.team_7_tcss_450.R;
import com.example.team_7_tcss_450.databinding.FragmentContactRequestListBinding;
import com.example.team_7_tcss_450.model.UserInfoViewModel;
import com.example.team_7_tcss_450.ui.contacts.model.ContactListViewModel;

/**
 * A simple {@link Fragment} subclass.
 */
public class ContactRequestListFragment extends Fragment {

    private ContactListViewModel mContactListModel;
    private UserInfoViewModel mUserModel;
    private FragmentContactRequestListBinding binding;

    /**
     * Mandatory empty constructor for the fragment manager to instantiate the
     * fragment (e.g. upon screen orientation changes).
     */
    public ContactRequestListFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(Bundle savedInstanceState) {
        Log.d("fragment_made", "OnCreate Called in ContactSentListFragment");
        super.onCreate(savedInstanceState);

        ViewModelProvider provider = new ViewModelProvider(requireActivity());
        mUserModel = provider.get(UserInfoViewModel.class);

        mContactListModel = new ViewModelProvider(requireActivity()).get(ContactListViewModel.class);
    }

    @Override
    public View onCreateView(LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentContactRequestListBinding.inflate(inflater);
        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        FragmentContactRequestListBinding binding = FragmentContactRequestListBinding.bind(requireView());

        // Set the adapter
        Context context = view.getContext();

        binding.contactSentList.setLayoutManager(new LinearLayoutManager(context));

        final RecyclerView rv = binding.contactSentList;
        mContactListModel.addContactListObserver(getViewLifecycleOwner(), (contactsList) -> {
            // while we do observe the contact list from ContactListViewModel,
            // we currently just spawn a list of generated contacts from ContactGenerator.
            // -- replace generated placeholder contacts with real contacts list
//            binding.contactList.setAdapter(new ContactListRecyclerViewAdapter(ContactGenerator.getContactList()));

            // TODO: fix bug when contactList is Empty, there is endless GET calls (only resolved when there is at least one verified contact)
            rv.setAdapter(new ContactListRecyclerViewAdapter(
                    contactsList,
                    mUserModel.getEmail()));
            if (contactsList.isEmpty() &&  !mContactListModel.getRequestStatus()) {
                mContactListModel.connectGetContactRequestList(mUserModel.getJWT(), mUserModel.getEmail());}
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
                    case R.id.action_contact_invites: {
                        navigateToInvites();
                        return true;
                    }
                    case R.id.action_contact_requests: {
                        // Already in request fragment: do nothing
                        return true;
                    }
                    default:
                        return false;
                }
            }
        }, getViewLifecycleOwner(), Lifecycle.State.RESUMED);
    }

    private void navigateToInvites() {
        //On button click, navigate to Second Home
        Navigation.findNavController(requireView()).navigate(
                ContactRequestListFragmentDirections
                        .actionNavigationRequestContactsToNavigationInviteContacts());
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
                mContactListModel.connectAddContact(contactEmail, mUserModel.getJWT());
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