// Authors:
// Shane Oatman https://github.com/shoatman
// Sunil Bandla https://github.com/sunilbandla
// Daniel Dobalian https://github.com/danieldobalian

var express = require("express");
var morgan = require("morgan");
var passport = require("passport");
var BearerStrategy = require('passport-azure-ad').BearerStrategy;

var options = {
    // TODO: Update the first 3 variables
    identityMetadata: "https://login.microsoftonline.com/<Tenant Name e.g. mytenant.onmicrosoft.com>/v2.0/.well-known/openid-configuration/", // Update with your tenant name
    policyName: '<My SiSu Policy Name>', // Replace with your Policy Name 
    clientID: "<Client/App ID>", // Replace with your Client/App ID
    isB2C: true,
    validateIssuer: false,
    loggingLevel: 'info',
    passReqToCallback: false
};

var bearerStrategy = new BearerStrategy(options,
    function (token, done) {
        // Send user info using the second argument
        done(null, {}, token);
    }
);

var app = express();
app.use(morgan('dev'));

app.use(passport.initialize());
passport.use(bearerStrategy);

app.use(function (req, res, next) {
    res.header("Access-Control-Allow-Origin", "*");
    res.header("Access-Control-Allow-Headers", "Authorization, Origin, X-Requested-With, Content-Type, Accept");
    next();
});

app.get("/api/claims",
    passport.authenticate('oauth-bearer', {session: false}),
    function (req, res) {
        var claims = req.authInfo;
        console.log('User info: ', req.user);
        console.log('Validated claims: ', claims);
        var claimsList = Object.keys(claims)
            .reduce(function (previous, key) {
                return previous.concat({
                    type: key,
                    value: claims[key]
                });
            }, []);
        res.status(200).json(claimsList);
    }
);

app.listen(5000, function () {
    console.log("Listening on port 5000");
});
