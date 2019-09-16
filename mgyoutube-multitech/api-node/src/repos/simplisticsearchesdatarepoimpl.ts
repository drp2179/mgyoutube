import { SearchesDataRepo } from "./searchesdatarepo";


export class SimplisticSearchesDataRepoImpl implements SearchesDataRepo {

    private parentSearches: Map<string, Set<string>> = new Map()

    constructor() {
    }

    async repositoryStartup(): Promise<void> {
        // nothing to do here
    }

    async getSearchesForParentUser(parentUserId: string): Promise<string[]> {
        var returnArray: string[] = [];
        var existingSearches = this.parentSearches.get(parentUserId);

        if (existingSearches != null) {
            existingSearches.forEach(s => returnArray.push(s));
        }

        return returnArray;
    }

    async addSearchToParentUser(parentUserId: string, searchPhrase: string): Promise<void> {
        var existingSet = this.parentSearches.get(parentUserId)
        if (existingSet == null) {
            existingSet = new Set();
            this.parentSearches.set(parentUserId, existingSet);
        }

        existingSet.add(searchPhrase);
    }
}