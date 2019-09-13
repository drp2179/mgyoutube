import { UserDataRepo } from '../repos/userdatarepo';
import { UserModule } from '../modules/usermodule';
import { DefaultUserModuleImpl } from '../modules/defaultusermoduleimpl';
import { VideoModule } from '../modules/videomodule';
import { SearchesModule } from '../modules/searchesmodule';


export class ModuleRepoRegistry {
    private static theUserModule: UserModule | null = null;
    private static theUserDataRepo: UserDataRepo | null = null;
    private static theVideoModule: VideoModule;
    private static theSearchesModule: SearchesModule;

    public static getUserModule(): UserModule {
        if (ModuleRepoRegistry.theUserModule == null) {
            var udr = ModuleRepoRegistry.getUserDataRepo()
            ModuleRepoRegistry.theUserModule = new DefaultUserModuleImpl(udr as UserDataRepo);
        }
        return ModuleRepoRegistry.theUserModule;
    }

    public static setUserModule(userModule: UserModule) {
        ModuleRepoRegistry.theUserModule = userModule;
    }

    public static getUserDataRepo(): UserDataRepo | null {
        return ModuleRepoRegistry.theUserDataRepo;
    }

    public static setUserDataRepo(userDataRepo: UserDataRepo) {
        ModuleRepoRegistry.theUserDataRepo = userDataRepo;

        if (ModuleRepoRegistry.theUserModule != null) {
            ModuleRepoRegistry.theUserModule.setUserDataRepo(userDataRepo);
        }
    }

    public static setVideosModule(videoModule: VideoModule) {
        ModuleRepoRegistry.theVideoModule = videoModule;
    }

    public static getVideosModule(): VideoModule {
        return ModuleRepoRegistry.theVideoModule;
    }

    public static setSearchesModule(searchesModule: SearchesModule) {
        ModuleRepoRegistry.theSearchesModule = searchesModule;
    }

    public static getSearchesModule(): SearchesModule {
        return ModuleRepoRegistry.theSearchesModule;
    }


}