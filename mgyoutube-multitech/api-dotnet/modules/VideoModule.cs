using System.Collections.Generic;
using api_dotnet.apimodel;

namespace api_dotnet.modules
{

    interface VideoModule
    {
        List<Video> search(string searchTerms);
    }
}