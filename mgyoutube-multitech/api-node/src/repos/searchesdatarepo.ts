

export interface SearchesDataRepo {

    getSearchesForParentUser(parentUserId: number): string[];

    addSearchToParentUser(parentUserId: number, searchPhrase: string): void;
}