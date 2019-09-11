import { Video } from '../apimodel/video';
import { VideoModule } from './videomodule';
import { google, youtube_v3 } from 'googleapis'


export class DefaultVideoModuleImpl implements VideoModule {
    private apiKey: string;
    private applicationName: string

    constructor(apiKey: string, applicationName: string) {
        this.apiKey = apiKey;
        this.applicationName = applicationName;
    }

    // setUserDataRepo(udr: UserDataRepo): void {
    //     this.userDataRepo = udr;
    // }

    private getYouTube(): youtube_v3.Youtube {
        var youtube = google.youtube({
            version: 'v3',
            auth: this.apiKey
        });
        return youtube;
    }

    public async search(searchTerms: string): Promise<Array<Video>> {

        var youtube = this.getYouTube();

        var searchResults = await youtube.search.list({
            part: 'id,snippet',
            q: searchTerms,
            safeSearch: 'strict',
            type: 'video',
            maxResults: 50
        });

        var videos: Array<Video> = [];

        if (searchResults.status == 200) {
            if (searchResults.data.items) {
                // the "for (var searchResult in searchResults.data.items)" loop isn't typing correctly so doing it the old fashion way
                for (var i = 0; i < searchResults.data.items.length; i++) {
                    var searchResult = searchResults.data.items[0];
                    var video = new Video();
                    if (searchResult.snippet) {
                        if (searchResult.id) {
                            video.videoId = searchResult.id.videoId
                        }
                        video.title = searchResult.snippet.title;
                        video.description = searchResult.snippet.description;
                        video.channelTitle = searchResult.snippet.channelTitle
                        video.channelId = searchResult.snippet.channelId
                        video.publishedAt = searchResult.snippet.publishedAt
                        if (searchResult.snippet.thumbnails && searchResult.snippet.thumbnails.default) {
                            video.thumbnailUrl = searchResult.snippet.thumbnails.default.url
                            video.thumbnailWidth = searchResult.snippet.thumbnails.default.width
                            video.thumbnailHeight = searchResult.snippet.thumbnails.default.height
                        }
                    }
                    videos.push(video);
                }
            }
            else {
                console.warn("searchResults status indicates failure: ", searchResults.status)
            }
        }

        return videos;
    }
}
