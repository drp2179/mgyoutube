import { SearchesModule } from "./searchesmodule"
import { UserDataRepo } from "../repos/userdatarepo";
import { SearchesDataRepo } from "../repos/searchesdatarepo";
import { UserNotFoundException } from "./usernotfoundexception";

export class DefaultSearchesModuleImpl implements SearchesModule {
    private userDataRepo: UserDataRepo;
    private searchesDataRepo: SearchesDataRepo;

    constructor(aUserDataRepo: UserDataRepo, aSearchesDataRepo: SearchesDataRepo) {
        this.setUserDataRepo(aUserDataRepo);
        this.setSearchesDataRepo(aSearchesDataRepo);
    }

    setUserDataRepo(udr: UserDataRepo): void {
        this.userDataRepo = udr;
    }
    setSearchesDataRepo(sdr: SearchesDataRepo) {
        this.searchesDataRepo = sdr;
    }

    async getSavedSearchesForParent(parentUsername: string): Promise<string[]> {
        const parentUser = await this.userDataRepo.getUserByUsername(parentUsername);

        if (parentUser == null) {
            throw new UserNotFoundException(parentUsername);
        }

        const searches = await this.searchesDataRepo.getSearchesForParentUser(parentUser.userId);

        console.log(
            "getSavedSearchesForParent for ", parentUsername, " returning searches.size ", searches.length);

        return searches;
    }

    async addSearchToParent(parentUsername: string, searchPhrase: string): Promise<void> {
        const parentUser = await this.userDataRepo.getUserByUsername(parentUsername);

        if (parentUser == null) {
            throw new UserNotFoundException(parentUsername);
        }

        await this.searchesDataRepo.addSearchToParentUser(parentUser.userId, searchPhrase);
    }

    async removeSearchFromParent(parentUsername: string, searchPhrase: string): Promise<void> {
        const parentUser = await this.userDataRepo.getUserByUsername(parentUsername);

        if (parentUser == null) {
            throw new UserNotFoundException(parentUsername);
        }

        await this.searchesDataRepo.removeSearchFromParentUser(parentUser.userId, searchPhrase);
    }


}