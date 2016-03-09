angular.module('taskApp.controllers',[]).controller('TaskListController',function($scope,$state,popupService,$window,$timeout,Task,$q){
	$scope.tasks=Task.query();

    $scope.deleteTask=function(task){
        if(popupService.showPopup('Really stop this?')){
            task.$delete(function(){
                $window.location.href='';
            });
        }
    }

    var poll = function() {
    	var promises = $scope.tasks.map(function(task){
    		var defer = $q.defer();
    		console.log('getting task status')
    		Task.get({ id: task.id }, function(data) {
    			console.log('returning task status')
    			if (task.completed != data.completed) {
    				task.completed = data.completed;
        		}
    			defer.resolve(data);
    		}, function(err) {
    			defer.reject(err);
    		});
    		return defer.promise;
    	})
    	
    	$q.all(promises).then(function() {
    		$timeout(function() {
    			console.log('all promises resolved');
    			poll();
    		}, 1000);
    	});
    }; 
    poll();
}).controller('TaskCreateController',function($scope,$state,$stateParams,Task){

    $scope.task=new Task();
    $scope.task.type = "DRAFT2";

    $scope.addTask=function(){
        $scope.task.$save(function(){
            $state.go('tasks');
        });
    }

});
