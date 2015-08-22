package com.score.senz.ui;

import android.app.Activity;
import android.graphics.Typeface;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.EditText;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;

import com.score.senz.R;
import com.score.senz.exceptions.InvalidInputFieldsException;
import com.score.senz.exceptions.InvalidPhoneNoException;
import com.score.senz.pojos.User;
import com.score.senz.utils.ActivityUtils;
import com.score.senz.utils.PhoneBookUtils;

/**
 * Activity class that handles user registrations
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class RegistrationActivity extends Activity implements View.OnClickListener {

    private static final String TAG = RegistrationActivity.class.getName();

    // registration deal with User object
    private User registeringUser;

    // UI fields
    private EditText editTextPhoneNo;
    private TextView countryCodeText;
    private TextView textViewHeaderText;
    private TextView textViewSignUpText;
    private RelativeLayout signUpButton;

    /**
     * {@inheritDoc}
     */
    public void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.registration_layout);

        initUi();
    }

    /**
     * Initialize UI components,
     * Set country code text
     * set custom font for UI fields
     */
    private void initUi() {
        Typeface typefaceThin = Typeface.createFromAsset(getAssets(), "fonts/vegur_2.otf");

        editTextPhoneNo = (EditText) findViewById(R.id.registration_phone_no);
        signUpButton = (RelativeLayout) findViewById(R.id.registration_sign_up_button);
        textViewHeaderText = (TextView) findViewById(R.id.registration_header_text);
        textViewSignUpText = (TextView) findViewById(R.id.registration_sign_up_text);
        signUpButton.setOnClickListener(RegistrationActivity.this);

        String countryCode = PhoneBookUtils.getCountryCode(this);
        countryCodeText = (TextView) findViewById(R.id.country_code);
        if (!countryCode.isEmpty())
            countryCodeText.setText(countryCode);

        textViewHeaderText.setTypeface(typefaceThin, Typeface.BOLD);
        textViewSignUpText.setTypeface(typefaceThin, Typeface.BOLD);
        editTextPhoneNo.setTypeface(typefaceThin, Typeface.NORMAL);
    }

    /**
     * Initialize user object
     */
    private void initRegisteringUser() {
        String countryCode = countryCodeText.getText().toString().trim();
        String phoneNo = editTextPhoneNo.getText().toString().trim();
        String internationalPhoneNo = countryCode + phoneNo.substring(phoneNo.length() - 9);

        registeringUser = new User("0", internationalPhoneNo, "");
        registeringUser.setUsername("Me");
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onClick(View v) {
        if (v == signUpButton) {
            signUp();
        }
    }

    /**
     * Sign-up button action,
     * Need to connect web socket and send PUT query to register
     * the user
     */
    private void signUp() {
        initRegisteringUser();

        try {
            ActivityUtils.isValidRegistrationFields(registeringUser);

            ActivityUtils.hideSoftKeyboard(this);
            ActivityUtils.showProgressDialog(this, "Registering...");
            registerUser();
        } catch (InvalidInputFieldsException e) {
            Log.e(TAG, e.toString());
            Toast.makeText(this, "Invalid input fields", Toast.LENGTH_LONG).show();
        } catch (InvalidPhoneNoException e) {
            Log.e(TAG, e.toString());
            Toast.makeText(this, "Phone no should contains 10 digits", Toast.LENGTH_LONG).show();
        }
    }

    /**
     * Create user via sending PUT query to server,
     * need to send the query via the web socket
     */
    private void registerUser() {
        // send public key to SenZ server(via senz message)
    }

    /**
     * {@inheritDoc}
     */
    @Override
    public void onBackPressed() {
        super.onBackPressed();

        Log.d("TAG", "OnBackPressed: go back");
        this.overridePendingTransition(R.anim.stay_in, R.anim.bottom_out);
    }
}