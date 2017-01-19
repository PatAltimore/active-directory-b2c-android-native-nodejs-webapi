---
services: active-directory-b2c
platforms: android, node.js
author: dadobali
---

# Azure AD B2C Android Sample

This sample demonstrates how to use Azure AD B2C with a prominent 3rd party Android OAuth2.0 library [android-oauth-client](https://github.com/wuman/android-oauth-client). There's 3 parts of this sample you'll have to setup to get this running: do some configurations in our Azure portal and define your experience, configure/run the Android app, and finally configure/run the Node.js Web API. 

## Steps to Run

To use Azure AD B2C, you'll first need to create an Azure AD B2C tenant, register your application, and create some sign in and sign up experiences.  

-To create an Azure AD B2C tenant checkout [these steps](https://docs.microsoft.com/en-us/azure/active-directory-b2c/active-directory-b2c-get-started).
-Register your app, checkout [these steps](https://docs.microsoft.com/en-us/azure/active-directory-b2c/active-directory-b2c-app-registration).  
-You can now define your [custom sign in and sign up experience](https://docs.microsoft.com/en-us/azure/active-directory-b2c/active-directory-b2c-reference-policies).  In Azure AD B2C, you define the experience your end users will encounter by creating `policies`.  For this sample, you'll want to create a single combined Sign In/Sign up policy. 
-Clone the code.
```git clone https://github.com/Azure-Samples/active-directory-b2c-android-native-nodejs-webapi.git```
### Setting up the Android App

1. In Android Studio, open an existing project and open the `android-oauth-client`.  You will likely get a few errors and need to install some additional tools in Android Studio. Follow the prompts and let Android Studio update the local data. 

2. Inside `/samples/java/.../azureb2c/Azureb2cConstants.java`, you'll see a few variables to set including:
	-CLIENT_ID which can be found in the Azure Portal B2C under the label Application ID.
	-Replace `<Your Policy Name>` inside the `AUTHORIZATION_ENDPOINT_URL` and `TOKEN_SERVER_URL` with the name of the sign in/sign up policy you created.
	-Replace `<Your Tenant Name>` inside the `AUTHORIZATION_ENDPOINT_URL` and `TOKEN_SERVER_URL` with the name of your Azure AD B2C tenant you created. 
	
To checkout where the OAuth 2.0 flows are happening, see `/samples/java/.../azureb2c/SimpleOAuth2Activity.java`. 

3. Go ahead and try the app.  You'll be able to see your custom experience, sign up for an account, and sign in to an existing account. The app will immediately crash after you sign in unless you complete the Node.JS steps!

> [!NOTE]
> This sample uses the Android embeded Webview meaning tokens are stored in both persistent storage and in session cookies.  When the sample performs a logout, it will remove all session cookies as Android doesn't allow specific session cookies to be removed. 
> 
>

### Setting up the Node.js Web API

1. You should've already cloned the app before, go ahead and open the `token-validator-service-nodejs/index.js` with any text editor. Make sure you've (installed Node)[https://nodejs.org/en/download/]. 

2. Replace the following fields:
	-`clientID` with the same Client/App ID as you used in the Android code. 
	-`policyName` with the same name as before.
	-`<Your Tenant Name>` inside the identityMetadata variable with the name of your Azure AD B2C tenant you created.

3. Run the following command: 
```npm install && npm update```

4. Run the Web API!
```node index.js```

### You're Done!

> [!NOTE]
> This sample uses a 3rd party library (see Note below) that does not refresh Access Tokens.  We have added the logic to check if the Access Token is expired, and indicated with a comment where refresh logic should be applied. 
> 
>

Now run both the Android app and Node web API at the same time.  You'll see after you sign in, a request gets sent to the web API to validate the access token, and all the validated claims (fields inside the token) are sent back.  Finally, all the claims are dumped into the Android UI. Congrats!

## Next Steps

Customize your user experience further by supporting other identity providers.  Checkout the docs belows to learn how to add additional providers: 
-[Microsoft](https://docs.microsoft.com/en-us/azure/active-directory-b2c/active-directory-b2c-setup-msa-app)
-[Facebook](https://docs.microsoft.com/en-us/azure/active-directory-b2c/active-directory-b2c-setup-fb-app)
-[Google](https://docs.microsoft.com/en-us/azure/active-directory-b2c/active-directory-b2c-setup-goog-app)
-[Amazon](https://docs.microsoft.com/en-us/azure/active-directory-b2c/active-directory-b2c-setup-amzn-app)
-[LinkedIn](https://docs.microsoft.com/en-us/azure/active-directory-b2c/active-directory-b2c-setup-li-app)


## Questions & Issues

Please file any questions or problems with the sample as a github issue in this repo.  You can also post on Stackoverflow with the tag `azure-ad-b2c`. For OAuth2.0 library issues, please see note below. 

## Acknowledgements

> [!NOTE]
> Microsoft has tested the android-oauth-client library in basic Azure AD B2C scenarios and confirmed that it works with our service and thus considers it Microsoft Compatible.  Microsoft does not provide fixes for this library and has not done a full review of the library.  Issues and feature requests should be directed to the library's open source project linked below. 
>
>

[android-oauth-client](https://github.com/wuman/android-oauth-client):  The Android library and sample was adapted from this open-source oAuth client library. 
[Crouton](https://github.com/keyboardsurfer/Crouton)
[Google Play Services](https://developers.google.com/android/guides/overview)
[Passport.js](http://passportjs.org/)