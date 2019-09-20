import { readFileSync } from "fs"

export class DatastoresProperties {
    public couchConnectionString: string;
    public couchUsername: string;
    public couchPassword: string;
    public mongoConnectionString: string;
    public mongoDatabaseName: string;

    constructor() {
        var json = readFileSync("secrets/datastores.json");
        var jsonObject = JSON.parse(json.toString());

        this.couchConnectionString = jsonObject.couchConnectionString;
        this.couchUsername = jsonObject.couchUsername;
        this.couchPassword = jsonObject.couchPassword;
        this.mongoConnectionString = jsonObject.mongoConnectionString;
        this.mongoDatabaseName = jsonObject.mongoDatabaseName;
    }
}