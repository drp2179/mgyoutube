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

    getSavedSearchesForParent(parentUsername: string): string[] {
        var parentUser = this.userDataRepo.getUserByUsername(parentUsername);

        if (parentUser == null) {
            throw new UserNotFoundException(parentUsername);
        }

        var searches = this.searchesDataRepo.getSearchesForParentUser(parentUser.userId);

        console.log(
            "getSavedSearchesForParent for ", parentUsername, " returning searches.size ", searches.length);

        return searches;
    }

    addSearchToParent(parentUsername: string, searchPhrase: string): void {
        var parentUser = this.userDataRepo.getUserByUsername(parentUsername);

        if (parentUser == null) {
            throw new UserNotFoundException(parentUsername);
        }

        this.searchesDataRepo.addSearchToParentUser(parentUser.userId, searchPhrase);
    }


}