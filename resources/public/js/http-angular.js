
function logPOSTRequest(url, data) {
    var stringData = JSON.stringify(data, undefined, 2);

    var request = "POST " + url + " HTTP/1.1\n"
            + "Content-Type: application/json\n"
            + "Content-Length: " + stringData.length + "\n"
            + stringData + "\n";

    console.log(request);

}

function logResponse(response) {
    var stringResponse = "HTTP/1.1 200 Ok\n"
            + JSON.stringify(response, undefined, 2);

    console.log(stringResponse);
}

function POST($http, url, data, successCallback) {
    logPOSTRequest(url, data);
    $http.post(url, data).success(function (response) {
        //logResponse(response);
        successCallback(response);
    });
}

function GET($http, url, successCallback) {
    $http.get(url).success(function (response) {
        successCallback(response);
    });
}
