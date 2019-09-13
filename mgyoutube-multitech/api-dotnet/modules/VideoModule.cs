using System.Collections.Generic;
using api_dotnet.apimodel;

namespace api_dotnet.modules
{

    public interface VideoModule
    {
        List<Video> search(string searchTerms);
    }
}