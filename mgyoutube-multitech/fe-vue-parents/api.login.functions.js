function api_login(username, password, successCallback, failureCallback) {

    const httpRequest = new XMLHttpRequest();
    httpRequest.onReadyStateChange = function () {
        console.warn("/api/parents/auth onReadyStateChange");

        if (this.readyState === XMLHttpRequest.DONE) {
            if (this.status === 200) {
                const loggedInUser = JSON.parse(this.response)

                if (successCallback) {
                    console.log("should be calling success callback with", loggedInUser);
                    successCallback(loggedInUser);
                }
                else {
                    console.log("status(" + this.status + ") == 200 and success callback not defined");
                }
            }
            else if (failureCallback) {
                console.log("should be calling failure callback");
                failureCallback();
            }
            else {
                console.log("status(" + this.status + ") != 200 and failure callback not defined");
            }
        }
    }
    httpRequest.onError = (e) => {
        console.warn("/api/parents/auth onError " + JSON.stringify(e));
        if (failureCallback) {
            failureCallback();
        }
    }
    httpRequest.onloadend = httpRequest.onReadyStateChange;
    // httpRequest.onloadend = (e) => {
    //     console.warn("/api/auth onloadend e: " + JSON.stringify(e));
    //     console.warn("/api/auth onloadend httpRequest: " + JSON.stringify(httpRequest));
    //     console.warn("/api/auth onloadend httpRequest.readyState: " + httpRequest.readyState)
    //     console.warn("/api/auth onloadend httpRequest.status: " + httpRequest.status)
    // }

    const payloadObj = {}
    payloadObj.username = username;
    payloadObj.password = password;
    const payload = JSON.stringify(payloadObj);

    try {
        httpRequest.open("POST", '/api/parents/auth', true);
        httpRequest.setRequestHeader("Content-Type", "application/json");
        console.log("/api/auth PRE-POST")
        httpRequest.send(payload);
    }
    catch (err) {
        console.warn("/api/parents/auth Send Catch " + JSON.stringify(err));
        if (failureCallback) {
            failureCallback();
        }
    }
    finally {
        console.log("/api/parents/auth POST-POST")
    }

    // if (!username || !password) {
    //     console.warn("no username or password")
    // } else if (username == "dad" && password === "dadpassword" && successCallback) {
    //     successCallback();
    // } else if (failureCallback) {
    //     failureCallback();
    // }
}

function api_logout(successCallback) {
    if (successCallback) {
        successCallback();
    }
}
