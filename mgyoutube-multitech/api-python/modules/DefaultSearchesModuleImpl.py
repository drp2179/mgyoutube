from apimodel import Video
from modules.SearchesModule import SearchesModule
from modules.UserNotFoundException import UserNotFoundException
from repos.UserDataRepo import UserDataRepo
from repos.SearchesDataRepo import SearchesDataRepo


class DefaultSearchesModuleImpl(SearchesModule):
    def __init__(self, aUserDataRepo: UserDataRepo, aSearchesDataRepo: SearchesDataRepo):
        self.userDataRepo = aUserDataRepo
        self.searchesDataRepo = aSearchesDataRepo

    def getSavedSearchesForParent(self,  parentUsername: str) -> list():
        parentUser = self.userDataRepo.getUserByUsername(parentUsername)

        if (parentUser is None):
            raise UserNotFoundException(parentUsername)

        searches = self.searchesDataRepo.getSearchesForParentUser(
            parentUser.userId)

        print("getSavedSearchesForParent for ", parentUsername,
              " returning searches.size ", len(searches))
        return searches

    def addSearchToParent(self,  parentUsername: str, searchPhrase: str):
        parentUser = self.userDataRepo.getUserByUsername(parentUsername)

        if (parentUser is None):
            raise UserNotFoundException(parentUsername)

        self.searchesDataRepo.addSearchToParentUser(
            parentUser.userId, searchPhrase)
