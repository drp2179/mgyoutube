import { Component, Input } from '@angular/core';
import { DomSanitizer } from '@angular/platform-browser'

@Component({
    selector: 'player-panel',
    template: `
        <div *ngIf="videoId" id="playerwrapper">
            <div style="display:none">{{videoId}}</div>
            <div style="display:none">{{theVideoUrl()}}</div>
            <iframe id="youtubeplayer" frameBorder="0" allowFullScreen="1" allow="accelerometer; autoplay; encrypted-media; gyroscope; picture-in-picture" title="YouTube video player" width="640" height="360" [src]="theVideoUrl()"></iframe>
        </div>
    `
})
export class PlayerPanel {
    @Input() videoId: string = null;
    private _sanitizer: DomSanitizer;

    theVideoUrl() {
        var aVideoUrl = "https://www.youtube.com/embed/" + this.videoId + "?ref=0&amp;enablejsapi=1&amp;widgetid=1&amp;modestbranding=1";
        return this._sanitizer.bypassSecurityTrustResourceUrl(aVideoUrl);
    }

    constructor(sanitizer: DomSanitizer) {
        this._sanitizer = sanitizer;
        this.videoId = null;
    }
}
