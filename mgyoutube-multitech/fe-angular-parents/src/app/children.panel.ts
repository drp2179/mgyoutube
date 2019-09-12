import { Component, Output, EventEmitter } from '@angular/core';
import { globalState } from "./globalstate"
declare var api_getAllChildrenForParent: any;

@Component({
    selector: 'children-panel',
    template: `
    <div id="childrenpanel">
        <div *ngFor="let child of this.children; let i = index">
            <div><a (click)="selectChild(i)" href="#">{{child.username}}</a></div>
        </div>
        <add-children-panel (onChildAdded)="childAddedHandler($event)"></add-children-panel>
    </div>
`
})
export class ChildrenPanel {
    children = [];

    constructor() {
    }

    childAddedHandler(event: any) {
        api_getAllChildrenForParent(globalState.loggedInUsername, (children: any) => this.getAllChildrenSuccess(children), () => this.getAllChildrenFailed())
    }

    getAllChildrenSuccess(children: any) {
        this.children = children;
    }
    getAllChildrenFailed() {
        alert("getting all children for " + globalState.loggedInUsername + " failed");
    }

    selectChild(childIndex: number) {
        console.log("selectChild", childIndex);
        console.log("selectChild", this.children[childIndex]);
    }

    ngOnInit() {
        console.log("childrenpanel.ngOnInit");
        api_getAllChildrenForParent(globalState.loggedInUsername, (children: any) => this.getAllChildrenSuccess(children), () => this.getAllChildrenFailed())
    }

}
