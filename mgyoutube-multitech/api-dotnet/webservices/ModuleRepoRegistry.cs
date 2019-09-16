using System;
using api_dotnet.modules;
using api_dotnet.repos;

namespace api_dotnet.webservices
{
    public class ModuleRepoRegistry
    {
        public static UserModule TheUserModule { get; set; } = null;
        public static VideoModule TheVideoModule { get; set; } = null;
        public static SearchesModule TheSearchesModule { get; set; } = null;

    }
}