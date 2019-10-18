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


function api_saveSearch(parentUsername, searchPhrase, successCallback, failureCallback) {
    const url = '/api/parents/' + parentUsername + '/searches/' + searchPhrase;

    const httpRequest = new XMLHttpRequest();
    httpRequest.onReadyStateChange = function () {
        console.warn(url, "onReadyStateChange");

        if (this.readyState === XMLHttpRequest.DONE) {
            if (this.status === 201) {
                if (successCallback) {
                    console.log("should be calling success callback with");
                    successCallback();
                }
                else {
                    console.log("status(" + this.status + ") == 201 and success callback not defined");
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

    try {
        httpRequest.open("PUT", url, true);
        console.log(url, "PRE-PUT")
        httpRequest.send();
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


function api_getSavedSearchesForParent(parentUsername, successCallback, failureCallback) {
    const url = '/api/parents/' + parentUsername + '/searches';

    const httpRequest = new XMLHttpRequest();
    httpRequest.onReadyStateChange = function () {
        console.warn(url, "onReadyStateChange");

        if (this.readyState === XMLHttpRequest.DONE) {
            if (this.status === 200) {
                const searches = JSON.parse(this.response)

                if (successCallback) {
                    console.log("should be calling success callback with", searches);
                    successCallback(searches);
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
                console.log("status(" + this.status + ") != 201 and failure callback not defined");
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

function api_deleteSavedSearch(parentUsername, searchPhrase, successCallback, failureCallback) {
    const url = '/api/parents/' + parentUsername + '/searches/' + searchPhrase;

    const httpRequest = new XMLHttpRequest();
    httpRequest.onReadyStateChange = function () {
        console.warn(url, "onReadyStateChange");

        if (this.readyState === XMLHttpRequest.DONE) {
            if (this.status === 204) {
                if (successCallback) {
                    console.log("should be calling success callback");
                    successCallback();
                }
                else {
                    console.log("status(" + this.status + ") == 204 and success callback not defined");
                }
            }
            else if (failureCallback) {
                console.log("should be calling failure callback");
                failureCallback();
            }
            else {
                console.log("status(" + this.status + ") != 204 and failure callback not defined");
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

    try {
        httpRequest.open("DELETE", url, true);
        console.log(url, "PRE-DELETE")
        httpRequest.send();
    }
    catch (err) {
        console.warn(url, err, JSON.stringify(err));
        if (failureCallback) {
            failureCallback();
        }
    }
    finally {
        console.log(url, "POST-DELETE")
    }
}
