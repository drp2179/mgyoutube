using System;
using api_dotnet.modules;
using api_dotnet.repos;

namespace api_dotnet.webservices
{
    public class ModuleRepoRegistry
    {
        public static UserModule TheUserModule { get; set; } = null;
        public static VideoModule TheVideoModule { get; set; } = null;
        // public static UserDataRepo TheUserDataRepo { get; set; } = null;
        // public static SearchesDataRepo TheSearchesDataRepo { get; set; } = null;
        public static SearchesModule TheSearchesModule { get; set; } = null;

        // public static UserModule GetUserModule()
        // {
        //     if (TheUserModule == null)
        //     {
        //         TheUserModule = new DefaultUserModuleImpl(GetUserDataRepo());
        //     }
        //     return TheUserModule;
        // }

        // public static void SetUserModule(UserModule userModule)
        // {
        //     theUserModule = userModule;
        // }

        // public static VideoModule GetVideoModule()
        // {
        //     return theVideoModule;
        // }

        // public static void SetVideoModule(VideoModule videoModule)
        // {
        //     theVideoModule = videoModule;
        // }

        // public static UserDataRepo GetUserDataRepo()
        // {
        //     return theUserDataRepo;
        // }

        // public static void SetUserDataRepo(UserDataRepo userDataRepo)
        // {
        //     theUserDataRepo = userDataRepo;
        // }
    }
}