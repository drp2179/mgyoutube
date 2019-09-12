import { Component, Output, EventEmitter } from '@angular/core';
import { globalState } from "./globalstate"
declare var api_addChildToParent: any;

@Component({
    selector: 'add-children-panel',
    template: `
        <div id="addchildpanel">
            <label for="newchildnamefield">New Child Username</label>
            <input id="newchildnamefield" type="text" [value]="this.theChildUsername" (change)="usernameChangeHandler($event)"/>
            <label for="newchildpasswordfield">New Child Password</label>
            <input id="newchildpasswordfield" type="password" [value]="this.theChildPassword" (change)="passwordChangeHandler($event)"/>
            <button id="addNewChildButton" (click)="addNewChildClickHandler()">Add Child</button>
        </div>
    `
})
export class AddChildrenPanel {

    theChildUsername: string;
    theChildPassword: string;

    @Output() onChildAdded = new EventEmitter<any>();

    constructor() {
        this.theChildUsername = "";
        this.theChildPassword = "";
    }

    addNewChildClickHandler() {
        api_addChildToParent(globalState.loggedInUsername, this.theChildUsername, this.theChildPassword, (newChild) => { this.addNewChildSuccess(newChild); }, () => this.addNewChildFailed());
    }
    usernameChangeHandler(event: any) {
        this.theChildUsername = event.target.value;
    }
    passwordChangeHandler(event: any) {
        this.theChildPassword = event.target.value;
    }

    addNewChildSuccess(newChild: any) {
        this.onChildAdded.emit(newChild);

        this.theChildUsername = null;
        this.theChildPassword = null;
    }

    addNewChildFailed() {
        alert("adding child " + this.theChildUsername + " failed");
    }
}
