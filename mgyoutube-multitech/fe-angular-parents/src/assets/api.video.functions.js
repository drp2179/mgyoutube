function api_getVideoSearchResults(searchTerms, successCallback, failureCallback) {
    const url = '/api/videos?search=' + encodeURIComponent(searchTerms);

    const httpRequest = new XMLHttpRequest();
    httpRequest.onReadyStateChange = function () {
        console.warn(url, "onReadyStateChange");

        if (this.readyState === XMLHttpRequest.DONE) {
            if (this.status === 200) {
                const videos = JSON.parse(this.response)

                if (successCallback) {
                    console.log("should be calling success callback with", videos);
                    successCallback(videos);
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

