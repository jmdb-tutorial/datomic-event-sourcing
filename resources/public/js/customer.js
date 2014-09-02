var customerModule = angular.module('CustomerModule', []);

function getParameterByName(name) {
    name = name.replace(/[\[]/, "\\[").replace(/[\]]/, "\\]");
    var regex = new RegExp("[\\?&]" + name + "=([^&#]*)"),
            results = regex.exec(location.search);
    return results == null ? "" : decodeURIComponent(results[1].replace(/\+/g, " "));
}

var customerId = getParameterByName("id");
var testCustomer = {"id" : customerId, "name" : customerId, "email" : "xx@xx.1", "address-line-1" : "add1", "town" : "XXXX", "postcode" : "XX00 0XX"};
var testHistory = [
    {"type" : "create-customer", "user-id" : "fooox", "timestamp" : "1", "changes" : "a-->b"},
    {"type" : "change-address", "user-id" : "barr", "timestamp" : "2", "changes" : "c-->d"}
]



function CustomerController($scope, $http) {
    $scope.customer = testCustomer;
    $scope.history= testHistory;
}

