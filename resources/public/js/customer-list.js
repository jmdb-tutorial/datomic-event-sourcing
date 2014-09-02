var customerListModule = angular.module('CustomerListModule', []);

var testCustomers = [
    {"id" : "1", "name" : "1", "email" : "xx@xx.1", "address-line-1" : "add1", "town" : "XXXX", "postcode" : "XX00 0XX"},
    {"id" : "1", "name" : "2", "email" : "xx@xx.1", "address-line-1" : "add1", "town" : "XXXX", "postcode" : "XX00 0XX"},
    {"id" : "1", "name" : "3", "email" : "xx@xx.1", "address-line-1" : "add1", "town" : "XXXX", "postcode" : "XX00 0XX"},
    {"id" : "1", "name" : "4", "email" : "xx@xx.1", "address-line-1" : "add1", "town" : "XXXX", "postcode" : "XX00 0XX"}]



function CustomerListController($scope, $http) {
    $scope.customerList = testCustomers;
}

