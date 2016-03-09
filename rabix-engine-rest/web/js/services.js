angular.module('taskApp.services',[]).factory('Task',function($resource){
    return $resource('http://localhost:8081/v0/engine/:id',{id:'@_id'},{
        update: {
            method: 'PUT'
        }
    });
}).service('popupService',function($window){
    this.showPopup=function(message){
        return $window.confirm(message);
    }
});
