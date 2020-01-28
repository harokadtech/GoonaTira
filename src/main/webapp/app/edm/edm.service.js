(function () {
    'use strict';

    angular
        .module('goonaTiraApp')
        .factory('Category', Category);

    Category.$inject = ['$resource'];
    function Category ($resource) {
        var service = $resource('api/category/:id', {}, {
            'query': {method: 'GET', isArray: true},
            'get': {
                method: 'GET',
                transformResponse: function (data) {
                    data = angular.fromJson(data);
                    return data;
                }
            },
            'save': { method:'POST' },
            'update': { method:'PUT' },
            'delete':{ method:'DELETE'}
        });

        return service;
    } 

})();



