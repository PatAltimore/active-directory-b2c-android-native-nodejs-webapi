
package com.microsoft.oauth.samples.azureb2c;

import android.content.Context;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.support.v4.app.FragmentActivity;
import android.support.v4.app.FragmentManager;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.Loader;
import android.text.TextUtils;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.Window;
import android.webkit.CookieManager;
import android.webkit.CookieSyncManager;
import android.widget.Button;
import android.widget.TextView;

import com.microsoft.oauth.samples.AsyncResourceLoader;
import com.microsoft.oauth.samples.OAuth;
import com.microsoft.oauth.samples.SamplesActivity;
import com.microsoft.oauth.samples.SamplesConstants;
import com.google.api.client.auth.oauth2.BearerToken;
import com.google.api.client.auth.oauth2.ClientParametersAuthentication;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.http.GenericUrl;
import com.wuman.android.auth.AuthorizationDialogController;
import com.wuman.android.auth.AuthorizationFlow;
import com.wuman.android.auth.DialogFragmentController;
import com.wuman.android.auth.OAuthManager;
import com.wuman.android.auth.oauth2.store.SharedPreferencesCredentialStore;
import com.wuman.oauth.samples.R;

import java.io.IOException;
import java.util.Arrays;
import java.util.logging.Logger;

/* Http imports */
import android.os.StrictMode;
import com.github.kevinsawicki.http.HttpRequest;

import de.keyboardsurfer.android.widget.crouton.Crouton;

public class SimpleOAuth2Activity extends FragmentActivity {

    static final Logger LOGGER = Logger.getLogger(SamplesConstants.TAG);

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_INDETERMINATE_PROGRESS);
        super.onCreate(savedInstanceState);

        FragmentManager fm = getSupportFragmentManager();

        if (fm.findFragmentById(android.R.id.content) == null) {
            OAuthFragment list = new OAuthFragment();
            fm.beginTransaction().add(android.R.id.content, list).commit();
        }
    }

    @Override
    protected void onDestroy() {
        Crouton.cancelAllCroutons();
        super.onDestroy();
    }

    private static String validateToken(String url, String accessToken)
    {
        StrictMode.ThreadPolicy policy = new StrictMode.ThreadPolicy.Builder().permitAll().build();
        StrictMode.setThreadPolicy(policy);

        try {
            HttpRequest request = new HttpRequest(url, HttpRequest.METHOD_GET);
            request = request.authorization("Bearer " + accessToken).acceptJson();
            LOGGER.info("Http Response Code: " + request.code());

            if (request.ok()) {
                return request.body();
            } else {
                return "Error calling backend, http code: " + request.code();
            }
        } catch(Exception e) {
            return "Exception in ValidateToken: " + e.toString();
        }
    }

    public static class OAuthFragment extends Fragment implements
            LoaderManager.LoaderCallbacks<AsyncResourceLoader.Result<Credential>> {

        private static final int LOADER_GET_TOKEN = 0;
        private static final int LOADER_DELETE_TOKEN = 1;
        private static final int LOADER_EDIT_PROFILE = 2;

        /* Since this sample was written using an Android emulator, you
         * must use 10.0.2.2 to loopback to the host
         */
        final String SERVICE_URL = "http://10.0.2.2:5000/api/claims";

        private OAuthManager oauth;
        private OAuthManager oauthEditProfile;

        private Button btnLogin;
        private Button btnDeleteToken;
        private Button btnEditProfile;
        private TextView txtDetails;

        @Override
        public void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            setHasOptionsMenu(true);
        }

        @Override
        public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
            super.onCreateOptionsMenu(menu, inflater);
            inflater.inflate(R.menu.delete_cookies_menu, menu);
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                Bundle savedInstanceState) {
            return inflater.inflate(R.layout.oauth_login, container, false);
        }

        @Override
        public void onViewCreated(View view, Bundle savedInstanceState) {
            btnLogin = (Button) view.findViewById(R.id.btnLogin);
            btnDeleteToken = (Button) view.findViewById(R.id.btnDeleteToken);
            btnEditProfile = (Button) view.findViewById(R.id.btnEditProfile);

            txtDetails = (TextView) view.findViewById(R.id.txtDetails);

            btnLogin.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                if (getLoaderManager().getLoader(LOADER_GET_TOKEN) == null) {
                    getLoaderManager().initLoader(LOADER_GET_TOKEN, null,
                            OAuthFragment.this);
                } else {
                    getLoaderManager().restartLoader(LOADER_GET_TOKEN, null,
                            OAuthFragment.this);
                }
                }
            });

            btnDeleteToken.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getLoaderManager().getLoader(LOADER_DELETE_TOKEN) == null) {
                        getLoaderManager().initLoader(LOADER_DELETE_TOKEN, null,
                                OAuthFragment.this);
                    } else {
                        getLoaderManager().restartLoader(LOADER_DELETE_TOKEN, null,
                                OAuthFragment.this);
                    }
                }
            });

            btnEditProfile.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (getLoaderManager().getLoader(LOADER_EDIT_PROFILE) == null) {
                        getLoaderManager().initLoader(LOADER_EDIT_PROFILE, null,
                                OAuthFragment.this);
                    } else {
                        getLoaderManager().restartLoader(LOADER_EDIT_PROFILE, null,
                                OAuthFragment.this);
                    }
                }
            });

        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            boolean fullScreen = getActivity().getSharedPreferences("Preference", 0)
                .getBoolean(SamplesActivity.KEY_AUTH_MODE, true);

            // setup credential storage
            SharedPreferencesCredentialStore credentialStore =
                    new SharedPreferencesCredentialStore(getActivity(),
                            SamplesConstants.CREDENTIALS_STORE_PREF_FILE, OAuth.JSON_FACTORY);

            // setup Authorization Code flow
            AuthorizationFlow flow = new AuthorizationFlow.Builder(
                    BearerToken.authorizationHeaderAccessMethod(),
                    OAuth.HTTP_TRANSPORT,
                    OAuth.JSON_FACTORY,
                    new GenericUrl(Azureb2cConstants.TOKEN_SERVER_URL),
                    new ClientParametersAuthentication(Azureb2cConstants.CLIENT_ID, null),
                    Azureb2cConstants.CLIENT_ID,
                    Azureb2cConstants.AUTHORIZATION_ENDPOINT_URL)
                    .setScopes(Arrays.asList(Azureb2cConstants.CLIENT_ID, "offline_access"))
                    .setCredentialStore(credentialStore)
                    .build();

            // setup UI controller
            AuthorizationDialogController controller =
                    new DialogFragmentController(getFragmentManager(), fullScreen) {
                        @Override
                        public String getRedirectUri() throws IOException {
                            return Azureb2cConstants.REDIRECT_URL;
                        }

                        @Override
                        public boolean isJavascriptEnabledForWebView() {
                            return true;
                        }

                        @Override
                        public boolean disableWebViewCache() {
                            return false;
                        }

                        @Override
                        public boolean removePreviousCookie() {
                            return false;
                        }

                    };
            oauth = new OAuthManager(flow, controller);


            String authEndpointEditProfile = Azureb2cConstants.AUTHORIZATION_ENDPOINT_URL.replace(Azureb2cConstants.POLICY_NAME_SIGNUP_SIGNIN, Azureb2cConstants.POLICY_NAME_EDIT_PROFILE);
            String tokenEndpointEditProfile = Azureb2cConstants.TOKEN_SERVER_URL.replace(Azureb2cConstants.POLICY_NAME_SIGNUP_SIGNIN, Azureb2cConstants.POLICY_NAME_EDIT_PROFILE);
            AuthorizationFlow flowEditProfile = new AuthorizationFlow.Builder(
                    BearerToken.authorizationHeaderAccessMethod(),
                    OAuth.HTTP_TRANSPORT,
                    OAuth.JSON_FACTORY,
                    new GenericUrl(tokenEndpointEditProfile),
                    new ClientParametersAuthentication(Azureb2cConstants.CLIENT_ID, null),
                    Azureb2cConstants.CLIENT_ID,
                    authEndpointEditProfile)
                    .setScopes(Arrays.asList(Azureb2cConstants.CLIENT_ID, "offline_access"))
                    .setCredentialStore(credentialStore)
                    .build();
            oauthEditProfile = new OAuthManager(flowEditProfile, controller);
        }

        @Override
        public Loader<AsyncResourceLoader.Result<Credential>> onCreateLoader(int id, Bundle args) {
            getActivity().setProgressBarIndeterminateVisibility(true);
            btnLogin.setEnabled(false);
            txtDetails.setText("");
            if (id == LOADER_GET_TOKEN) {
                return new GetTokenLoader(getActivity(), oauth);
            } else if (id == LOADER_DELETE_TOKEN) {
                return new DeleteTokenLoader(getActivity(), oauth);
            } else { //if (id == LOADER_DELETE_TOKEN) {
                return new EditProfileLoader(getActivity(), oauthEditProfile);
            }
        }

        @Override
        public void onLoadFinished(Loader<AsyncResourceLoader.Result<Credential>> loader,
                AsyncResourceLoader.Result<Credential> result) {

            txtDetails.setText("");
            int loaderId = loader.getId();
            if (loaderId == LOADER_GET_TOKEN  || loaderId == LOADER_EDIT_PROFILE) {
                getToken(loader, result);
            }
            else if (loaderId == LOADER_DELETE_TOKEN) {
                deleteToken(loader, result);
            }
            getActivity().setProgressBarIndeterminateVisibility(false);
        }

        @Override
        public void onLoaderReset(Loader<AsyncResourceLoader.Result<Credential>> loader) {
            getActivity().setProgressBarIndeterminateVisibility(false);
        }

        @Override
        public void onDestroy() {
            getLoaderManager().destroyLoader(LOADER_GET_TOKEN);
            getLoaderManager().destroyLoader(LOADER_DELETE_TOKEN);
            super.onDestroy();
        }

        private void getToken(Loader<AsyncResourceLoader.Result<Credential>> loader,
                              AsyncResourceLoader.Result<Credential> result){
            if (result.success) {
                /* You have your token, send it to your backend for validation */
                String validatedClaims = validateToken(SERVICE_URL,
                        result.data.getAccessToken());
                txtDetails.setText(String.format(getView().getContext().getString(R.string.validated_claims), validatedClaims));

                /* Now you can use the claims data for your App-specific logic! */
                hasToken(true);
            } else {
                hasToken(false);
            }
        }

        private void deleteToken(Loader<AsyncResourceLoader.Result<Credential>> loader,
                              AsyncResourceLoader.Result<Credential> result){
            hasToken(false);
        }

        private void hasToken(boolean hasToken) {
            if (hasToken){
                btnLogin.setVisibility(View.INVISIBLE);
                btnDeleteToken.setVisibility(View.VISIBLE);
                btnEditProfile.setVisibility(View.VISIBLE);
            }
            else {
                btnLogin.setEnabled(true);

                btnLogin.setVisibility(View.VISIBLE);
                btnDeleteToken.setVisibility(View.INVISIBLE);
                btnEditProfile.setVisibility(View.INVISIBLE);
            }
        }

        private static class GetTokenLoader extends AsyncResourceLoader<Credential> {

            private final OAuthManager oauth;

            public GetTokenLoader(Context context, OAuthManager oauth) {
                super(context);
                this.oauth = oauth;
            }

            @Override
            public Credential loadResourceInBackground() throws Exception {
                Credential credential =
                        oauth.authorizeExplicitly(Azureb2cConstants.USER, null, null).getResult();

                LOGGER.info("Access token: " + credential.getAccessToken());
                LOGGER.info("Refresh token: " + credential.getRefreshToken());
                LOGGER.info("Refresh token expiration: " + credential.getExpiresInSeconds());

                if (credential.getExpiresInSeconds() < 0) {
                    LOGGER.info("Refresh token expired. Deleting creds and re-prompting for creds");
                    oauth.deleteCredential(Azureb2cConstants.USER, null, null).getResult();
                    credential = oauth.authorizeExplicitly(Azureb2cConstants.USER, null, null).getResult();
                }

                return credential;
            }

            @Override
            public void updateErrorStateIfApplicable(AsyncResourceLoader.Result<Credential> result) {
                Credential data = result.data;
                result.success = !TextUtils.isEmpty(data.getAccessToken());
                result.errorMessage = result.success ? null : "error";
            }
        }

        private static class DeleteTokenLoader extends AsyncResourceLoader<Credential> {

            private final OAuthManager oauth;
            private boolean success;

            public DeleteTokenLoader(Context context, OAuthManager oauth) {
                super(context);
                this.oauth = oauth;
            }

            @Override
            @SuppressWarnings("deprecation")
            public Credential loadResourceInBackground() throws Exception {

                /* Wipes the tokens out of the persistent storage */
                success = oauth.deleteCredential(Azureb2cConstants.USER, null, null).getResult();

                /* Clears all the web views session cookies, will clear other apps sessions.
                 * Recommendation is for app to set a persistent flag and then do prompt=force
                 */
                CookieSyncManager cookieSyncMngr=CookieSyncManager.createInstance(getContext());
                cookieSyncMngr.startSync();
                CookieManager cookieManager=CookieManager.getInstance();
                cookieManager.removeAllCookie();
                cookieManager.removeSessionCookie();
                cookieSyncMngr.stopSync();
                cookieSyncMngr.sync();

                LOGGER.info("Token deleted: " + success);
                return null;
            }

            @Override
            public void updateErrorStateIfApplicable(Result<Credential> result) {
                result.success = success;
                result.errorMessage = result.success ? null : "error";
            }
        }

        private static class EditProfileLoader extends AsyncResourceLoader<Credential> {

            private final OAuthManager oauth;

            public EditProfileLoader(Context context, OAuthManager oauth) {
                super(context);
                this.oauth = oauth;
            }

            @Override
            public Credential loadResourceInBackground() throws Exception {
                oauth.deleteCredential(Azureb2cConstants.USER_EDIT_PROFILE, null, null).getResult();
                Credential credential = oauth.authorizeExplicitly(Azureb2cConstants.USER_EDIT_PROFILE, null, null).getResult();

                LOGGER.info("Access token (edit profile): " + credential.getAccessToken());
                LOGGER.info("Refresh token (edit profile): " + credential.getRefreshToken());
                LOGGER.info("Refresh token expiration (edit profile): " + credential.getExpiresInSeconds());

                return credential;
            }

            @Override
            public void updateErrorStateIfApplicable(Result<Credential> result) {
                result.success = true;
                result.errorMessage = null;
            }
        }
    }
}
