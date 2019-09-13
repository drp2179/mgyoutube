import { SearchesDataRepo } from "./searchesdatarepo";


export class SimplisticSearchesDataRepoImpl implements SearchesDataRepo {

    private parentSearches: Map<number, Set<string>> = new Map()

    constructor() {
    }

    getSearchesForParentUser(parentUserId: number): string[] {
        var returnArray: string[] = [];
        var existingSearches = this.parentSearches.get(parentUserId);

        if (existingSearches != null) {
            existingSearches.forEach(s => returnArray.push(s));
        }

        return returnArray;
    }

    addSearchToParentUser(parentUserId: number, searchPhrase: string): void {
        var existingSet = this.parentSearches.get(parentUserId)
        if (existingSet == null) {
            existingSet = new Set();
            this.parentSearches.set(parentUserId, existingSet);
        }

        existingSet.add(searchPhrase);
    }


}