
package com.microsoft.oauth.samples.azureb2c;

public class Azureb2cConstants {

    /* TODO: Update the following 3 constants */
    public static final String TENANT_ID = "fabrikamb2c.onmicrosoft.com";
    public static final String CLIENT_ID = "33ae356b-b6d1-4fb7-80b9-949349f02b99";
    public static final String POLICY_NAME = "B2C_1_SUSI";

    /* Does not need to be updated */
    public static final String BASE_URL = "https://login.microsoftonline.com/tfp/" + TENANT_ID + "/" + POLICY_NAME + "/oauth2/v2.0/";
    public static final String AUTHORIZATION_ENDPOINT_URL = BASE_URL + "authorize";
    public static final String TOKEN_SERVER_URL = BASE_URL + "token";

    public static final String REDIRECT_URL = "https://login.microsoftonline.com/tfp/oauth2/nativeclient";

    private Azureb2cConstants() {
    }

}
