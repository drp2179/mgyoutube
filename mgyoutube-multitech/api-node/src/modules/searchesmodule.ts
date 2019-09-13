

export interface SearchesModule {
    getSavedSearchesForParent(parentUsername: string): string[];

    addSearchToParent(parentUsername: string, searchPhrase: string): void;
}