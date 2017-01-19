
package com.microsoft.oauth.samples.azureb2c;

import android.content.Context;
import android.os.Build;
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
import android.view.MenuItem;
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
import de.keyboardsurfer.android.widget.crouton.Style;

public class SimpleOAuth2Activity extends FragmentActivity {

    static final Logger LOGGER = Logger.getLogger(SamplesConstants.TAG);

    /* If token expires in <=60, refresh it */
    /* This default follows the oauth libraries default config */
    static final Long NEAR_EXPIRATION_SECONDS = 60l;

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
                LOGGER.info("Error calling backend, http code: " + request.code());
                return null;
            }
        } catch(Exception e) {
            LOGGER.info("Exception in ValidateToken: " + e.toString());
            return null;
        }
    }

    public static class OAuthFragment extends Fragment implements
            LoaderManager.LoaderCallbacks<AsyncResourceLoader.Result<Credential>> {

        private static final int LOADER_GET_TOKEN = 0;
        private static final int LOADER_DELETE_TOKEN = 1;

        /* Since this sample was written using an Android emulator, you
         * must use 10.0.2.2 to loopback to the host
         */
        final String SERVICE_URL = "http://10.0.2.2:5000/api/claims";

        private OAuthManager oauth;

        private Button button;
        private TextView message;

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
            button = (Button) view.findViewById(android.R.id.button1);
            setButtonText(R.string.get_token);
            message = (TextView) view.findViewById(android.R.id.text1);
            button.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    if (v.getTag().equals(R.string.get_token)) {
                        if (getLoaderManager().getLoader(LOADER_GET_TOKEN) == null) {
                            getLoaderManager().initLoader(LOADER_GET_TOKEN, null,
                                    OAuthFragment.this);
                        } else {
                            getLoaderManager().restartLoader(LOADER_GET_TOKEN, null,
                                    OAuthFragment.this);
                        }
                    } else {
                        if (getLoaderManager().getLoader(LOADER_DELETE_TOKEN) == null) {
                            getLoaderManager().initLoader(LOADER_DELETE_TOKEN, null,
                                    OAuthFragment.this);
                        } else {
                            getLoaderManager().restartLoader(LOADER_DELETE_TOKEN, null,
                                    OAuthFragment.this);
                        }
                    }
                }
            });
        }

        @Override
        public void onActivityCreated(Bundle savedInstanceState) {
            super.onActivityCreated(savedInstanceState);
            boolean fullScreen = getActivity().getSharedPreferences("Preference", 0)
                .getBoolean(SamplesActivity.KEY_AUTH_MODE, false);

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
        }

        @Override
        public Loader<AsyncResourceLoader.Result<Credential>> onCreateLoader(int id, Bundle args) {
            getActivity().setProgressBarIndeterminateVisibility(true);
            button.setEnabled(false);
            message.setText("");
            if (id == LOADER_GET_TOKEN) {
                return new GetTokenLoader(getActivity(), oauth);
            } else {
                return new DeleteTokenLoader(getActivity(), oauth);
            }
        }

        @Override
        public void onLoadFinished(Loader<AsyncResourceLoader.Result<Credential>> loader,
                AsyncResourceLoader.Result<Credential> result) {
            if (loader.getId() == LOADER_GET_TOKEN) {
            } else {
                message.setText("");
            }
            if (result.success) {
                if (loader.getId() == LOADER_GET_TOKEN) {
                    setButtonText(R.string.delete_token);

                    if (result.data.getExpiresInSeconds() > NEAR_EXPIRATION_SECONDS) {

                        /* You have your token, send it to your backend for validation */

                        String validatedClaims = validateToken(SERVICE_URL,
                                result.data.getAccessToken());
                        message.setText("Validated Claims:\n\n" + validatedClaims);

                        /* Now you can use the claims data for your App-specific logic! */

                    } else {

                        /* Use the Refresh Token to get new Access Token and then clear storage. */
                        message.setText("Your Access Token is Expired.");
                    }


                } else {
                    setButtonText(R.string.get_token);
                }
            } else {
                setButtonText(R.string.get_token);
                Crouton.makeText(getActivity(), result.errorMessage, Style.ALERT).show();
            }
            getActivity().setProgressBarIndeterminateVisibility(false);
            button.setEnabled(true);
        }

        @Override
        public void onLoaderReset(Loader<AsyncResourceLoader.Result<Credential>> loader) {
            message.setText("");
            getActivity().setProgressBarIndeterminateVisibility(false);
            button.setEnabled(true);
        }

        @Override
        public void onDestroy() {
            getLoaderManager().destroyLoader(LOADER_GET_TOKEN);
            getLoaderManager().destroyLoader(LOADER_DELETE_TOKEN);
            super.onDestroy();
        }

        private void setButtonText(int action) {
            button.setText(action);
            button.setTag(action);
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
                        oauth.authorizeExplicitly(getContext().getString(R.string.token_azureb2c),
                                null, null).getResult();
                LOGGER.info("Access token: " + credential.getAccessToken());
                LOGGER.info("Refresh Token: " + credential.getRefreshToken());
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
                success = oauth.deleteCredential(getContext().getString(R.string.token_azureb2c),
                        null, null).getResult();

                /* Clears all the webviews session cookies, will clear other apps sessions.
                 * Recommendation is for app to set a persistent flag and then do prompt=force
                 */
                CookieSyncManager cookieSyncMngr=CookieSyncManager.createInstance(getContext());
                cookieSyncMngr.startSync();
                CookieManager cookieManager=CookieManager.getInstance();
                cookieManager.removeAllCookie();
                cookieManager.removeSessionCookie();
                cookieSyncMngr.stopSync();
                cookieSyncMngr.sync();

                LOGGER.info("token deleted: " + success);
                return null;
            }

            @Override
            public void updateErrorStateIfApplicable(Result<Credential> result) {
                result.success = success;
                result.errorMessage = result.success ? null : "error";
            }
        }
    }
}
