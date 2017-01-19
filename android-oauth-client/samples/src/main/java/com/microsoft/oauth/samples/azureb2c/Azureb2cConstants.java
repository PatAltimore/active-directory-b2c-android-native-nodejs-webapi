
package com.microsoft.oauth.samples.azureb2c;

public class Azureb2cConstants {

    /* TODO: Update the following 3 constants */
    /* Obtained from registering your app at portal.azure.com */
    /* Labeled as Application ID in the Portal */
    public static final String CLIENT_ID = "YOUR CLIENT ID";

    /* Endpoints the app talks to, replace with your config info */
    public static final String AUTHORIZATION_ENDPOINT_URL =
            "https://login.microsoftonline.com/tfp/<Tenant Name e.g. mytenant.onmicrosoft.com>/<My SiSu Policy Name>/oauth2/v2.0/authorize";

    public static final String TOKEN_SERVER_URL =
            "https://login.microsoftonline.com/tfp/<Tenant Name e.g. mytenant.onmicrosoft.com>/<My SiSu Policy Name>/oauth2/v2.0/token";

    /* Does not need to be updated */
    public static final String REDIRECT_URL = "https://login.microsoftonline.com/tfp/oauth2/nativeclient";

    private Azureb2cConstants() {
    }

}
