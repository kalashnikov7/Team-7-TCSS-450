package com.example.team_7_tcss_450.ui.signin;

import static com.example.team_7_tcss_450.utils.PasswordValidator.checkExcludeWhiteSpace;
import static com.example.team_7_tcss_450.utils.PasswordValidator.checkPwdLength;
import static com.example.team_7_tcss_450.utils.PasswordValidator.checkPwdSpecialChar;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.lifecycle.ViewModelProvider;
import androidx.navigation.NavController;
import androidx.navigation.Navigation;

import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;

import com.auth0.android.jwt.JWT;
import com.example.team_7_tcss_450.R;
import com.example.team_7_tcss_450.databinding.FragmentSignInBinding;
import com.example.team_7_tcss_450.model.PushyTokenViewModel;
import com.example.team_7_tcss_450.model.UserInfoViewModel;
import com.example.team_7_tcss_450.utils.PasswordValidator;

import org.json.JSONException;
import org.json.JSONObject;

import java.util.Objects;


/**
 * A simple {@link Fragment} subclass.
 */
public class SignInFragment extends Fragment {

    private PushyTokenViewModel mPushyTokenViewModel;
    private UserInfoViewModel mUserViewModel;
    private FragmentSignInBinding binding;
    private SignInViewModel mSignInModel;

    private final PasswordValidator mEmailValidator = checkPwdLength(2)
            .and(checkExcludeWhiteSpace())
            .and(checkPwdSpecialChar("@"));

    private final PasswordValidator mPassWordValidator = checkPwdLength(1)
            .and(checkExcludeWhiteSpace());

    public SignInFragment() {
        // Required empty public constructor
    }

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        mSignInModel = new ViewModelProvider(requireActivity())
                .get(SignInViewModel.class);
        mPushyTokenViewModel = new ViewModelProvider(requireActivity())
                .get(PushyTokenViewModel.class);
    }

    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, ViewGroup container,
                             Bundle savedInstanceState) {
        binding = FragmentSignInBinding.inflate(inflater);
        // Inflate the layout for this fragment
        return binding.getRoot();
    }

    @Override
    public void onViewCreated(@NonNull View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);

        //Local access to the ViewBinding object. No need to create as Instance Var as it is only
        //used here.
        binding = FragmentSignInBinding.bind(requireView());

        //On button click, attempt to sign in
        binding.buttonSignIn.setOnClickListener(this::attemptSignIn);

        //Handle auth response once we get word back from the web service
        mSignInModel.addResponseObserver(getViewLifecycleOwner(), this::observeSignInResponse);

        //On button click, navigate to Registration Fragment
        binding.buttonRegister.setOnClickListener(button -> {
            Navigation.findNavController(requireView()).navigate(
                    SignInFragmentDirections.actionSignInFragmentToRegistrationFragment());
            Log.d("REGISTER", "Register Button Hit!");
        });

        //don't allow sign in until pushy token retrieved
        mPushyTokenViewModel.addTokenObserver(getViewLifecycleOwner(), token ->
                binding.buttonSignIn.setEnabled(!token.isEmpty()));

        mPushyTokenViewModel.addResponseObserver(
                getViewLifecycleOwner(),
                this::observePushyPutResponse
        );

        SignInFragmentArgs args = SignInFragmentArgs.fromBundle(getArguments());
        binding.editTextEmailAddress.setText(args.getEmail().equals("default") ? "" : args.getEmail());
        binding.editTextPassword.setText(args.getPassword().equals("default") ? "" : args.getPassword());
    }

    @Override
    public void onStart() {
        super.onStart();

        SharedPreferences prefs =
                getActivity().getSharedPreferences(
                        getString(R.string.keys_shared_prefs),
                        Context.MODE_PRIVATE);

        if (prefs.contains(getString(R.string.keys_prefs_jwt))) {
            String token = prefs.getString(getString(R.string.keys_prefs_jwt), "");
            JWT jwt = new JWT(token);
            // Check to see if the web token is still valid or not. To make a JWT expire after a
            // longer or shorter time period, change the expiration time when the JWT is
            // created on the web service.
            if(!jwt.isExpired(0)) {
                String email = jwt.getClaim("email").asString();
                navigateToSuccess(email, token);
                return;
            }
        }
    }

    private void attemptSignIn(final View button) {
        validateEmail();
    }

    private void validateEmail() {
        mEmailValidator.processResult(
                mEmailValidator.apply(binding.editTextEmailAddress.getText().toString().trim()),
                this::validatePassword,
                result -> binding.editTextEmailAddress.setError("Please enter a valid Email address."));
    }

    private void validatePassword() {
        mPassWordValidator.processResult(
                mPassWordValidator.apply(binding.editTextPassword.getText().toString()),
                this::verifyAuthWithServer,
                result -> binding.editTextPassword.setError("Please enter a valid Password."));
    }

    private void verifyAuthWithServer() {
        mSignInModel.connect(
                binding.editTextEmailAddress.getText().toString(),
                binding.editTextPassword.getText().toString());
        //This is an Asynchronous call. No statements after should rely on the
        //result of connect().
    }

    public void navigateToSuccess(final String email, final String jwt){
        if (binding.switchSignin.isChecked()) {
            SharedPreferences prefs =
                    getActivity().getSharedPreferences(
                            getString(R.string.keys_shared_prefs),
                            Context.MODE_PRIVATE);
            //Store the credentials in SharedPrefs
            prefs.edit().putString(getString(R.string.keys_prefs_jwt), jwt).apply();
        }
        Navigation.findNavController(requireView()).navigate(
                SignInFragmentDirections
                        .actionSignInFragmentToMainActivity(email, jwt)
        );
        //Remove THIS activity from the Task list. Pops off the backstack
        getActivity().finish();
    }

    /**
     * An observer on the HTTP Response from the web server. This observer should be
     * attached to SignInViewModel.
     *
     * @param response the Response from the server
     */
    private void observeSignInResponse(final JSONObject response) {
        if (response.length() > 0) {
            if (response.has("code")) {
                try {
                    binding.editTextEmailAddress.setError(
                            "Error Authenticating: " +
                                    response.getJSONObject("data").getString("message"));
                } catch (JSONException e) {
                    Log.e("JSON Parse Error", e.getMessage());
                }
            } else {
                try {
                    mUserViewModel = new ViewModelProvider(requireActivity(),
                            new UserInfoViewModel.UserInfoViewModelFactory(
                                    binding.editTextEmailAddress.getText().toString(),
                                    response.getString("token")
                            )).get(UserInfoViewModel.class);

                    sendPushyToken();
                } catch (JSONException e) {
                    Log.e("JSON Parse Error", e.getMessage());
                }
            }
        } else {
            Log.d("JSON Response", "No Response");
        }

    }

    /**
     * Helper to abstract the request to send the pushy token to the web service
     */
    private void sendPushyToken() {
        mPushyTokenViewModel.sendTokenToWebservice(mUserViewModel.getJWT());
    }

    /**
     * An observer on the HTTP Response from the web server. This observer should be
     * attached to PushyTokenViewModel.
     *
     * @param response the Response from the server
     */
    private void observePushyPutResponse(final JSONObject response) {
        if (response.length() > 0) {
            if (response.has("code")) {
                //this error cannot be fixed by the user changing credentials...
                binding.editTextEmailAddress.setError(
                        "Error Authenticating on Push Token. Please contact support");
            } else {
                navigateToSuccess(
                        binding.editTextEmailAddress.getText().toString(),
                        mUserViewModel.getJWT()
                );
            }
        }
    }

}