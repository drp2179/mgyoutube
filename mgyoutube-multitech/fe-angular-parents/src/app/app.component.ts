import { Component } from '@angular/core';
import { globalState } from './globalstate'

@Component({
  selector: 'app-root',
  template: `
    <login-panel *ngIf="!this.userIsAuthenticated" (loggedIn)="loginSuccessful($event)"></login-panel>
    <logout-panel *ngIf="this.userIsAuthenticated" (loggedOut)="logoutSuccessful()"></logout-panel>
  `
})
export class AppComponent {
  userIsAuthenticated: boolean;

  constructor() {
    this.userIsAuthenticated = false;
  }

  loginSuccessful(loggedInUser: any) {
    console.log("AppComponent.loginSuccessful", loggedInUser);
    this.userIsAuthenticated = true;
    globalState.loggedInUsername = loggedInUser.username;
  }

  logoutSuccessful() {
    console.log("AppComponent.logoutSuccessful");
    this.userIsAuthenticated = false;
    globalState.loggedInUsername = null;
  }
}
