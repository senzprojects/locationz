package com.score.senzors.utils;

import android.content.Context;
import android.content.SharedPreferences;
import com.score.senzors.R;
import com.score.senzors.exceptions.NoUserException;
import com.score.senzors.pojos.User;

/**
 * Utility class to deal with Share Preferences
 *
 * @author erangaeb@gmail.com (eranga herath)
 */
public class PreferenceUtils {

    /**
     * Save user credentials in shared preference
     * @param context application context
     * @param user logged-in user
     */
    public static void saveUser(Context context, User user) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor =  preferences.edit();

        //keys should be constants as well, or derived from a constant prefix in a loop.
        editor.putString("id", user.getId());
        editor.putString("phoneNo", user.getPhoneNo());
        editor.putString("username", user.getUsername());
        editor.putString("password", user.getPassword());
        editor.commit();
    }

    /**
     * Get user details from shared preference
     * @param context application context
     * @return user object
     */
    public static User getUser(Context context) throws NoUserException {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        String id = preferences.getString("id", "0");
        String phoneNo = preferences.getString("phoneNo", "");
        String username = preferences.getString("username", "");
        String password = preferences.getString("password", "");

        if(phoneNo.isEmpty() || password.isEmpty())
            throw new NoUserException();

        User user = new User(id, phoneNo, password);
        user.setUsername(username);
        return user;
    }

    /**
     * Save public/private keys in shared preference,
     * @param context application context
     * @param key public/private keys(encoded key string)
     * @param keyType public_key, private_key, server_key
     */
    public static void saveRsaKey(Context context, String key, String keyType) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor =  preferences.edit();
        editor.putString(keyType, key);
        editor.commit();
    }

    /**
     * Get saved RSA key string from shared preference
     * @param context application context
     * @param keyType public_key, private_key, server_key
     * @return key string
     */
    public static String getRsaKey(Context context, String keyType) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return preferences.getString(keyType, "");
    }

    /**
     * Save session key in shared preference
     * @param context application context
     * @param sessionKey session key
     */
    public static void saveSessionKey(Context context, String sessionKey) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        SharedPreferences.Editor editor =  preferences.edit();
        editor.putString("session_key", sessionKey);
        editor.commit();
    }

    /**
     * Read session key form shared preference
     * @param context application context
     * @return session key
     */
    public static String getSessionKey(Context context) {
        SharedPreferences preferences = context.getSharedPreferences(context.getString(R.string.preference_file_key), Context.MODE_PRIVATE);
        return preferences.getString("session_key", "");
    }

}
