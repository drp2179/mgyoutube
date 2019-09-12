import { Component, Output, Input, EventEmitter } from '@angular/core';
declare var api_login: any;

@Component({
    selector: 'player-panel',
    template: `
        <div id="searchpanel">
            {{videoId}}
        </div>
    `
})
export class PlayerPanel {

    @Input() videoId: string;

    constructor() {
        this.videoId = null;
    }

}
