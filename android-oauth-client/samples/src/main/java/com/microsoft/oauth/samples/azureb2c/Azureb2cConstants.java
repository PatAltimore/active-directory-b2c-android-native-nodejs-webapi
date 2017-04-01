
package com.microsoft.oauth.samples.azureb2c;

public class Azureb2cConstants {

    /* TODO: Update the following constants */
    public static final String TENANT_ID = "fabrikamb2c.onmicrosoft.com";
    public static final String CLIENT_ID = "33ae356b-b6d1-4fb7-80b9-949349f02b99";
    public static final String POLICY_NAME_SIGNUP_SIGNIN = "B2C_1_SUSI";
    public static final String POLICY_NAME_EDIT_PROFILE = "B2C_1_Edit_Profile";

    /* Does not need to be updated */
    public static final String URL_TEMPLATE = "https://login.microsoftonline.com/tfp/%s/%s/oauth2/v2.0/%s";
    public static final String AUTHORIZATION_ENDPOINT_URL = String.format(URL_TEMPLATE, TENANT_ID, POLICY_NAME_SIGNUP_SIGNIN, "authorize");
    public static final String TOKEN_SERVER_URL = String.format(URL_TEMPLATE, TENANT_ID, POLICY_NAME_SIGNUP_SIGNIN, "token");


    public static final String USER = "azure-ad-b2c";
    public static final String USER_EDIT_PROFILE = USER + "-ep";

    public static final String REDIRECT_URL = "https://login.microsoftonline.com/tfp/oauth2/nativeclient";

    private Azureb2cConstants() {
    }

}
