

export interface SearchesModule {
    getSavedSearchesForParent(parentUsername: string): Promise<string[]>;

    addSearchToParent(parentUsername: string, searchPhrase: string): Promise<void>;

    removeSearchFromParent(parentUsername: string, searchPhrase: string): Promise<void>;
}