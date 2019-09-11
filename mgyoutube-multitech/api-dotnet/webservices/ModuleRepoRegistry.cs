using System;
using api_dotnet.modules;
using api_dotnet.repos;

namespace api_dotnet.webservices
{
    class ModuleRepoRegistry
    {
        private static UserModule theUserModule = null;
        private static UserDataRepo theUserDataRepo = null;
        private static VideoModule theVideoModule = null;

        public static UserModule GetUserModule()
        {
            if (theUserModule == null)
            {
                theUserModule = new DefaultUserModuleImpl(GetUserDataRepo());
            }
            return theUserModule;
        }

        public static void SetUserModule(UserModule userModule)
        {
            theUserModule = userModule;
        }

        public static VideoModule GetVideoModule()
        {
            return theVideoModule;
        }

        public static void SetVideoModule(VideoModule videoModule)
        {
            theVideoModule = videoModule;
        }

        public static UserDataRepo GetUserDataRepo()
        {
            return theUserDataRepo;
        }

        public static void SetUserDataRepo(UserDataRepo userDataRepo)
        {
            theUserDataRepo = userDataRepo;
        }
    }
}