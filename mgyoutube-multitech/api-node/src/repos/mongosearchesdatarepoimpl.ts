import { SearchesDataRepo } from "./searchesdatarepo";
import { MongoClient, Db } from "mongodb";


const SEARCHES_COLLECTION_NAME = "searches";
const SEARCHES_COLLECTION_PARENT_FIELDNAME = "parentUserId";
const SEARCHES_COLLECTION_SEARCH_PHRASE_FIELDNAME = "phrase";


export class MongoSearchesDataRepoImpl implements SearchesDataRepo {
    private client: MongoClient;
    private database: Db;
    private connectionString: string;
    private databaseName: string;

    constructor(connectionString: string, databaseName: string) {
        this.connectionString = connectionString;
        this.databaseName = databaseName;
    }

    async connectDb() {
        await MongoClient.connect(this.connectionString, { useNewUrlParser: true, useUnifiedTopology: true }).then(client => {
            this.client = client;
            this.database = this.client.db(this.databaseName);
        }).catch(err => {
            throw new Error(err);
        });
    }

    async repositoryStartup(): Promise<void> {
        await this.connectDb();

        const searchesCollection = this.database.collection(SEARCHES_COLLECTION_NAME);
        if (searchesCollection != null) {
            const numDocs = await searchesCollection.countDocuments();
            if (numDocs > 0) {
                await this.database.dropCollection(SEARCHES_COLLECTION_NAME);
            }
        }
    }

    async getSearchesForParentUser(parentUserId: string): Promise<string[]> {
        var searches: string[] = [];

        const filterDoc: any = {}
        filterDoc[SEARCHES_COLLECTION_PARENT_FIELDNAME] = parentUserId;

        const searchesCollection = this.database.collection(SEARCHES_COLLECTION_NAME);
        await searchesCollection.find(filterDoc).forEach(d => {
            searches.push(d[SEARCHES_COLLECTION_SEARCH_PHRASE_FIELDNAME]);
        });

        return searches;
    }

    async addSearchToParentUser(parentUserId: string, searchPhrase: string): Promise<void> {
        const document: any = {}
        document[SEARCHES_COLLECTION_PARENT_FIELDNAME] = parentUserId;
        document[SEARCHES_COLLECTION_SEARCH_PHRASE_FIELDNAME] = searchPhrase;

        const searchesCollection = this.database.collection(SEARCHES_COLLECTION_NAME);
        await searchesCollection.insertOne(document);
    }
}