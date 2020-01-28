(function() {
    'use strict';

    angular
        .module('goonaTiraApp')
        .config(stateConfig);

    stateConfig.$inject = ['$stateProvider', '$urlRouterProvider'];

    function stateConfig($stateProvider, $urlRouterProvider) {
        $stateProvider.state('docsearch', {
            parent: 'app',
            url: '/docsearch?q',
            params: {
                q: {
                  value: '',
                  squash: true
                }
              },
            reloadOnSearch: false,
            data: {
                authorities: ['ROLE_USER'],
                pageTitle: 'global.menu.docsearch'
            },
            views: {
                'content@': {
                    templateUrl: 'app/edm/document-search-result.html',
                    controller: 'DocumentSearchController',
                    controllerAs: 'vm'
                }
            },
            resolve: {
                translatePartialLoader: ['$translate', '$translatePartialLoader', function ($translate, $translatePartialLoader) {
                    $translatePartialLoader.addPart('docsearch');
                    return $translate.refresh();
                }]
            }
        });
    }
})();
