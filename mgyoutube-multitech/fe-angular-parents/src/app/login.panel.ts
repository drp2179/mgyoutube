import { Component, Output, EventEmitter } from '@angular/core';
declare var api_login: any;

@Component({
    selector: 'login-panel',
    template: `
    <div id="auth">
        <input name="username" type="text" [value]="this.theUsername" (change)="usernameChangeHandler($event)" autocomplete="your username"/>
        <input name="password" type="password"  [value]="this.thePassword" (change)="passwordChangeHandler($event)" autocomplete="your password"/>
        <button id="loginButton" (click)="loginClickHandler()">Login</button></div>
    `
})
export class LoginPanel {
    theUsername: string;
    thePassword: string;

    @Output() loggedIn = new EventEmitter<any>();

    constructor() {
        this.theUsername = "";
        this.thePassword = "";
    }

    usernameChangeHandler(event: any) {
        this.theUsername = event.target.value;
    }
    passwordChangeHandler(event: any) {
        this.thePassword = event.target.value;
    }

    loginSuccessful(loggedInUser: any) {
        //console.log("loginSuccessful", loggedInUser)
        this.loggedIn.emit(loggedInUser);
    }

    loginFailed() {
        alert("login failed");
    }

    loginClickHandler() {
        api_login(this.theUsername, this.thePassword, (loggedInUser: any) => this.loginSuccessful(loggedInUser), this.loginFailed);
    }
}
