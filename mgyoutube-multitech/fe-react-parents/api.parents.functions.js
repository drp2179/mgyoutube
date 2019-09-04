function api_getAllChildrenForParent(parentUsername, successCallback, failureCallback) {
    const url = '/api/parents/' + parentUsername + '/children';

    const httpRequest = new XMLHttpRequest();
    httpRequest.onReadyStateChange = function () {
        console.warn(url, "onReadyStateChange");

        if (this.readyState === XMLHttpRequest.DONE) {
            if (this.status === 200) {
                const children = JSON.parse(this.response)

                if (successCallback) {
                    console.log("should be calling success callback with", children);
                    successCallback(children);
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
        console.warn(url, JSON.stringify(e));
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

    try {
        httpRequest.open("GET", url, true);
        console.log(url, "PRE-GET")
        httpRequest.send();
    }
    catch (err) {
        console.warn(url, err, JSON.stringify(err));
        if (failureCallback) {
            failureCallback();
        }
    }
    finally {
        console.log(url, "POST-GET")
    }

}


function api_addChildToParent(parentUsername, childUsername, childPassword, successCallback, failureCallback) {
    const url = '/api/parents/' + parentUsername + '/children/' + childUsername;

    const httpRequest = new XMLHttpRequest();
    httpRequest.onReadyStateChange = function () {
        console.warn(url, "onReadyStateChange");

        if (this.readyState === XMLHttpRequest.DONE) {
            if (this.status === 200) {
                const newChild = JSON.parse(this.response)

                if (successCallback) {
                    console.log("should be calling success callback");
                    successCallback(newChild);
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
        console.warn(url, JSON.stringify(e));
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
    payloadObj.username = childUsername;
    payloadObj.password = childPassword;
    payloadObj.isParent = false;
    const payload = JSON.stringify(payloadObj);

    try {
        httpRequest.open("PUT", url, true);
        httpRequest.setRequestHeader("Content-Type", "application/json");
        console.log(url, "PRE-PUT")
        httpRequest.send(payload);
    }
    catch (err) {
        console.warn(url, err, JSON.stringify(err));
        if (failureCallback) {
            failureCallback();
        }
    }
    finally {
        console.log(url, "POST-PUT")
    }

}

