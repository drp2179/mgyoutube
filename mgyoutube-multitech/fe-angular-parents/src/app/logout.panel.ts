import { Component, Output, EventEmitter } from '@angular/core';
declare var api_login: any;

@Component({
    selector: 'logout-panel',
    template: `
        <div id="auth"><button id="logoutButton" (click)="logoutClickHandler()">Logout</button></div>
    `
})
export class LogoutPanel {
    @Output() loggedOut = new EventEmitter<any>();

    constructor() {
    }

    logoutClickHandler() {
        this.loggedOut.emit();
    }
}
