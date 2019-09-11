import { readFileSync } from "fs"

export class YouTubeProperties {
    public apiKey: string;
    public applicationName: string;

    constructor() {
        var json = readFileSync("secrets/youtube.json");
        var jsonObject = JSON.parse(json.toString());

        this.apiKey = jsonObject.apiKey;
        this.applicationName = jsonObject.applicationName
    }
}