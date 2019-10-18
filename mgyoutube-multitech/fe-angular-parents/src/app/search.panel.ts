import { Component } from '@angular/core';
import { globalState } from "./globalstate"
declare var api_getVideoSearchResults: any;
declare var api_saveSearch: any;
declare var api_getSavedSearchesForParent: any;
declare var api_deleteSavedSearch: any;

@Component({
    selector: 'search-panel',
    template: `
        <div id="searchpanel">
            <div id="savedsearchespanel">
                <div *ngFor="let phrase of this.theSavedSearches; let i = index">
                    <div><a (click)="onSavedSearchClicked(i)" href="#">{{phrase}}</a></div>
                </div>
            </div>
            <player-panel [videoId]="theVideoId"></player-panel>
            <div id="searchtermspanel">
                <h3>Search Terms</h3>
                <label htmlFor="searchterms">Search for:</label>
                <input id="searchterms" type="text" (change)="searchTermsChangeHandler($event)" [value]="this.theSearchTerms" />
                <button id="submitSearchTermsButton" (click)="onSearchHandler()">Search</button>
            </div>
            <div id="searchresultspanel">
                <div><button id="submitSaveSearchButton" (click)="onSaveSearchHandler()">Save Search</button></div>
                <div><button *ngIf="showingSavedSearchResults" id="deleteSavedSearchButton" (click)="onDeleteSavedSearchHandler()">Delete Saved Search</button></div>
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
    theSavedSearches = [];
    showingSavedSearchResults: boolean;

    constructor() {
        this.theVideoId = "";
        this.theSearchTerms = "";
        this.showingSavedSearchResults = false;
    }

    onSaveSearchHandler(event: any) {
        if (this.theSearchTerms) {
            api_saveSearch(globalState.loggedInUsername, this.theSearchTerms, () => this.saveSearchSuccess(), () => this.saveSearchFailed());
        }
        else {
            alert("no search phrase to save")
        }
    }

    saveSearchSuccess() {
        api_getSavedSearchesForParent(globalState.loggedInUsername, (savedSearches: any) => this.getSavedSearchesSuccess(savedSearches), () => this.getSavedSearchesFailed());
    }
    saveSearchFailed() {
        alert("saving search for  '" + this.theSearchTerms + "' failed");
    }

    getSavedSearchesSuccess(savedSearches: any) {
        this.theSavedSearches = savedSearches;
    }
    getSavedSearchesFailed() {
        alert("retrieving saved searches failed");
    }

    onSearchHandler(event: any) {
        api_getVideoSearchResults(this.theSearchTerms, (videos: any) => this.searchSuccess(videos), () => this.searchFailed());
    }

    searchTermsChangeHandler(event: any) {
        this.theSearchTerms = event.target.value;
    }

    searchSuccess(videos: any) {
        this.theVideos = videos;
    }

    searchFailed() {
        alert("searching for  '" + this.theSearchTerms + "' failed");
    }

    watchVideo(event: any) {
        console.log("watchVideo, " + event);
        this.theVideoId = event;
    }

    onSavedSearchClicked(index: number) {
        console.log("onSavedSearchClicked, index: " + index)
        var searchPhrase = this.theSavedSearches[index];
        console.log("onSavedSearchClicked, searchPhrase: " + searchPhrase)
        api_getVideoSearchResults(searchPhrase, (videos) => this.searchSuccess(videos), () => this.searchFailed());
        this.theSearchTerms = searchPhrase;
        this.showingSavedSearchResults = true;
    }

    onDeleteSavedSearchHandler(event: any) {
        if (this.theSearchTerms) {
            api_deleteSavedSearch(globalState.loggedInUsername, this.theSearchTerms, () => this.deleteSavedSearchSuccess(), () => this.deleteSavedSearchFailed());
        }
        else {
            alert("no search phrase to delete")
        }
    }

    deleteSavedSearchSuccess() {
        api_getSavedSearchesForParent(globalState.loggedInUsername, (savedSearches) => this.getSavedSearchesSuccess(savedSearches), () => this.getSavedSearchesFailed());
        this.showingSavedSearchResults = false;
        this.theSearchTerms = undefined;
        this.theVideos = []
    }

    deleteSavedSearchFailed() {
        alert("deleting saved searches failed");
        this.showingSavedSearchResults = false;
    }

    ngOnInit() {
        console.log("searchpanel.ngOnInit");
        api_getSavedSearchesForParent(globalState.loggedInUsername, (savedSearches: any) => this.getSavedSearchesSuccess(savedSearches), () => this.getSavedSearchesFailed());
    }

}
