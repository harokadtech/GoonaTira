angular.module('goonaTiraApp')
    .controller('DocumentSearchController', ['$scope', '$location', '$state', '$http',  '$sce', 'Category',
                    function($scope, $location, $state, $http, $sce, Category) {
	        var vm = this;
	        vm.error = null;

            vm.searchedPattern = $state.params.q || "";

            vm.categories = Category.query();
            vm.autocompleteDocumentList = [];
            vm.topTerms = [];
            vm.aggregations = {
                date: [],
                fileExtension: []
            };

      /* vm.searchTextChanged = function() {
        $state.go('.', {search: vm.searchText});
      };*/
/*
              $scope.$on('$locationChangeSuccess', function() {
                if($state.params.q)
                vm.searchText = $state.params.q;
              });*/
            /*// prevent re-loading when searching
            var lastRoute = $route.current;
            $scope.$on('$locationChangeSuccess', function(event) {
                if ($route.current.params.q) {
                    $route.current = lastRoute;
                }
            });*/

            vm.findCategoryById = function(categoryId) {
                return vm.categories.filter(function(category) {
                    return category.id === categoryId;
                })[0];
            };

            vm.getTrustedHtmlContent = function(htmlString) {
                return $sce.trustAsHtml(htmlString);
            };

            vm.getCustomTrustedHtmlContent = function(htmlString) {
            	//var htmlContent = htmlString ? htmlString.replace(/\n+/g, "\n"):htmlString;
                return $sce.trustAsHtml(htmlString);
            };

            vm.linkToDocument = function(edmDocument) {
                // web link
                if (edmDocument.nodePath.indexOf("http") === 0) {
                    return edmDocument.nodePath;
                }
                // network link
                if (edmDocument.nodePath.indexOf("//") === 0) {
                    return edmDocument.nodePath.replace(/\//g, "\\"); // windows style
                }
                // local file
                return "/api/document/files?docId=" + edmDocument.id;
            };

            vm.getDocumentNodeIcon = function(node) {
                switch (node.fileExtension.toLowerCase()) {
                    case "pdf":
                        return "pdf";
                    case "html":
                        return "html";
                    case "png":
                    case "jpg":
                    case "jpeg":
                    case "gif":
                        return "image";
                    case "doc":
                    case "docx":
                        return "word";
                    case "xls":
                    case "xlsx":
                        return "excel";
                    case "ppt":
                    case "pptx":
                        return "power-point";
                    case "txt":
                        return "text";
                }
                return "unknown"; // default icon
            };

            /**
             * Submit search, according to the content of `vm.searchedPattern`
             */
            vm.submitSearch = function() {
                if (vm.searchedPattern && vm.searchedPattern.trim().length !== 0) {
                    $location.path('/docsearch').search({
                        'q': vm.searchedPattern
                    });
                }
                console.debug("Search : " + vm.searchedPattern);
                vm._sendSearchRequest();
            };

            /**
             * Refresh auto-complete document list
             */
            vm._refreshAutocompleteDocumentList = function() {
                $http.get('/api/document/suggest/?q=' + vm.searchedPattern).success(function(response, status, headers, config) {
                    vm.autocompleteDocumentList = response;
                });
            };

            /**
             * Add some word in the search word and submit search
             */
            vm.addWordAndSubmitSearch = function(word) {
                var requestPrefix = "";
                if (vm.searchedPattern && vm.searchedPattern.trim().length !== 0) {
                    requestPrefix = vm.searchedPattern.trim() + ' AND ';
                }
                vm.searchedPattern = requestPrefix + word;
                vm.submitSearch();
            };

            /**
             * Automatically called when the search pattern is updated in front view
             */
            vm.searchPatternHaveBeenUpdated = function() {
                vm._refreshAutocompleteDocumentList();
                vm.submitSearch();
            };

            vm.extractAggregationInfoFromDateAgg = function(key) {
                computeValue = function(monthToRemove) {
                    var fromDate = moment().subtract(monthToRemove, 'months').startOf("month").format('YYYY-MM-DD');
                    var toDate = moment().endOf("month").format('YYYY-MM-DD'); // can be removed for '*' ?
                    return "(date:[" + fromDate + " TO " + toDate + "])";
                };

                var candidates = {
                    last_year: {
                        label: "Durant l'année passée",
                        value: computeValue(12)
                    },
                    last_6_months: {
                        label: "Durant ces 6 derniers mois",
                        value: computeValue(6)
                    },
                    last_2_months: {
                        label: "Durant ces 2 derniers mois",
                        value: computeValue(2)
                    },
                    last_month: {
                        label: "Durant ce mois",
                        value: computeValue(1)
                    }
                };

                return candidates[key];
            };

            /**
             * Get query filter as string
             * For example : " AND (date:[2015-09-01 TO 2015-09-30])"
             */
            vm._getQueryFilters = function() {
                if (vm.searchedPattern.trim().length === 0) {
                    return;
                }
                console.log("aggregation updated");

                // file extension
                var aggregateFileExtensionFilter = vm.aggregations.fileExtension
                    .filter(function isChecked(e) {
                        return e.isChecked && e.key;
                    })
                    .map(function formatedQuery(e) {
                        return "fileExtension:" + e.key;
                    })
                    .join(" OR ");
                if (aggregateFileExtensionFilter) {
                    aggregateFileExtensionFilter = " AND (" + aggregateFileExtensionFilter + ")";
                }
                console.debug("aggregateFileExtensionFilter = " + aggregateFileExtensionFilter);

                // date
                var aggregateDateFilter = "";
                if (vm.dateAggregationFilter) {
                    aggregateDateFilter = " AND " + vm.dateAggregationFilter;
                }
                console.debug("aggregateDateFilter = " + aggregateDateFilter);

                // category
                var categoryFilter = vm.categories
                    .filter(function isChecked(e) {
                        return e.isChecked;
                    })
                    .map(function formatedQuery(e) {
                        return "categoryId:" + e.id;
                    })
                    .join(" OR ");
                if (categoryFilter) {
                    categoryFilter = " AND (" + categoryFilter + ")";
                }
                console.debug("categoryFilter = " + categoryFilter);

                // final filter
                var finalFilter = aggregateFileExtensionFilter + aggregateDateFilter + categoryFilter;
                console.info("final filter : " + finalFilter);
                return finalFilter;
            };

            vm.crawlFiles = function() {
            	// &sourceName=Dev4AfricaDocs&categoryName=Dev4Africa
                $http.get('/api/crawl/filesystem?path=GOONA_TIRA').success(function(response, status, headers, config) {
                    vm.crawlResult = response;
                });
            };
            vm.crawlFiles2 = function() {
            	// &sourceName=Dev4AfricaDocs&categoryName=Dev4Africa
                $http.get('/api/crawl/filesystem?path=D:/devWin/searchdocuments/VisaDocs').success(function(response, status, headers, config) {
                    vm.crawlResult = response;
                });
            };

            vm.submitSearchWithFilters = function() {
                var filters = vm._getQueryFilters();

                // submit request with all filters
                $http.get('/api/document?q=' + vm.searchedPattern + filters).success(function(response, status, headers, config) {
                    vm.searchResults = response;
                });
            };

            vm._sendSearchRequest = function() {

                console.info("initilizing scope values (top terms, ...)");

                if (vm.searchedPattern && vm.searchedPattern.trim().length !== 0) {
                    vm.submitSearchWithFilters();
                } else {
                    vm.searchResults = null;
                }
/*
                $http.get('/api/document/top_terms?q=' + (vm.searchedPattern || '')).success(function(response, status, headers, config) {
                    vm.topTerms = response;
                });*/

                $http.get('/api/document/aggregations?q=' + (vm.searchedPattern || '')).success(function(response, status, headers, config) {
                    vm.aggregations = response;
                });
            };
           vm._sendSearchRequest();

          /* $http.get('/api/crawl/filesystem?path=D:/devWin/TiraManager/searchdocuments/Dev4Africa&sourceName=Dev4AfricaDocs&categoryName=Dev4Africa').success(function(response, status, headers, config) {
                    vm.crawlResult = response;
                                // initialization
            vm._sendSearchRequest();
                });*/

        }
    ]);
