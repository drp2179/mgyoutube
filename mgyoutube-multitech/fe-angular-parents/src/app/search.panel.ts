import { Component, Output, EventEmitter } from '@angular/core';
declare var api_getVideoSearchResults: any;

@Component({
    selector: 'search-panel',
    template: `
        <div id="searchpanel">
            <player-panel [videoId]="theVideoId"></player-panel>
            <div id="searchtermspanel">
                <h3>Search Terms</h3>
                <label htmlFor="searchterms">Search for:</label>
                <input id="searchterms" type="text" (change)="searchTermsChangeHandler($event)" [value]="this.theSearchTerms" />
                <button id="submitSearchTermsButton" (click)="onSearchHandler()">Search</button>
            </div>
            <div id="searchresultspanel">
                <div *ngFor="let video of this.theVideos; let i = index">
                    <div key={i} id="search-result-{{i}}">
                        <div id="search-result-{{i}}-video-{{video.videoId}}"><a id="search-result-{{i}}-watch" (click)="watchVideo(video.videoId)" href="#"><img src="{{video.thumbnailUrl}}"/>{{video.title}}</a></div> 
                        <div>{{video.description}}</div>
                    </div>
                </div>
            </div>
        </div>
    `
})
export class SearchPanel {

    theVideoId: string;
    theSearchTerms: string;
    theVideos = [];

    constructor() {
        this.theVideoId = "";
        this.theSearchTerms = "";
    }

    onSearchHandler(event: any) {
        api_getVideoSearchResults(this.theSearchTerms, (videos: any) => this.searchSuccess(videos), () => this.searchFailed());
    }

    searchTermsChangeHandler(event: any) {
        this.theSearchTerms = event.target.value;
    }

    searchSuccess(videos: any) {
        this.theSearchTerms = null;
        this.theVideos = videos;
    }

    searchFailed() {
        alert("searching for  '" + this.theSearchTerms + "' failed");
    }

    watchVideo(event: any) {
        console.log("watchVideo, " + event);
        this.theVideoId = event;
    }
}
