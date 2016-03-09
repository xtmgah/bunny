angular.module('taskApp',['ui.router','ngResource','taskApp.controllers','taskApp.services','taskApp.directives']);

angular.module('taskApp').config(function($stateProvider,$httpProvider){
    $stateProvider.state('tasks',{
        url:'/tasks',
        templateUrl:'partials/tasks.html',
        controller:'TaskListController'
    }).state('newTask',{
        url:'/tasks/new',
        templateUrl:'partials/task-add.html',
        controller:'TaskCreateController'
    });
}).run(function($state){
   $state.go('tasks');
});