

export interface SearchesDataRepo {

    repositoryStartup(): Promise<void>;

    getSearchesForParentUser(parentUserId: string): Promise<string[]>;

    addSearchToParentUser(parentUserId: string, searchPhrase: string): Promise<void>;
}