from modules import UserModule, DefaultUserModuleImpl, VideoModule
from repos import UserDataRepo


class ModuleRepoRegistry(object):

    theUserModule: UserModule = None
    theUserDataRepo: UserDataRepo = None
    theVideoModule: VideoModule = None

    def __init__(self):
        pass

    @classmethod
    def getUserModule(cls) -> UserModule:
        if (cls.theUserModule is None):
            cls.theUserModule = DefaultUserModuleImpl.DefaultUserModuleImpl(
                cls.getUserDataRepo())
        return cls.theUserModule

    @classmethod
    def setUserModule(cls, userModule: UserModule):
        cls.theUserModule = userModule

    @classmethod
    def getUserDataRepo(cls) -> UserDataRepo:
        return cls.theUserDataRepo

    @classmethod
    def setUserDataRepo(cls, userDataRepo: UserDataRepo):
        cls.theUserDataRepo = userDataRepo

    @classmethod
    def getVideoModule(cls) -> VideoModule:
        return cls.theVideoModule

    @classmethod
    def setVideoModule(cls, videoModule: VideoModule):
        cls.theVideoModule = videoModule
