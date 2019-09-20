using System;
using api_dotnet.modules;
using api_dotnet.repos;
using api_dotnet.webservices;
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Routing;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;

namespace api_dotnet
{
    public class Startup
    {
        private SupportWebService supportWebService = null;
        private ChildrenWebService childrenWebService = null;
        private VideoWebService videoWebService = null;
        private ParentsWebService parentsWebService = null;

        public Startup(IConfiguration configuration)
        {
            Console.WriteLine("Startup.Startup");
            Configuration = configuration;
        }

        public IConfiguration Configuration { get; }

        // This method gets called by the runtime. Use this method to add services to the container.
        public void ConfigureServices(IServiceCollection services)
        {
            Console.WriteLine("Startup.ConfigureServices");

            YouTubeProperties youTubeProperties = new YouTubeProperties(Configuration);
            ModuleRepoRegistry.TheVideoModule = new DefaultVideoModuleImpl(youTubeProperties.ApiKey, youTubeProperties.ApplicationName);

            DatastoresProperties datastoresProperties = new DatastoresProperties(Configuration);

            //UserDataRepo userDataRepo = new SimplisticUserDataRepoImpl();
            //UserDataRepo userDataRepo = new MongoUserDataRepoImpl(datastoresProperties.MongoConnectionString, datastoresProperties.MongoDatabaseName);
            UserDataRepo userDataRepo = new CouchUserDataRepoImpl(datastoresProperties.CouchConnectionString, datastoresProperties.CouchUsername, datastoresProperties.CouchPassword);
            userDataRepo.RepositoryStartup();

            //SearchesDataRepo searchesDataRepo = new SimplisticSearchesDataRepoImpl();
            //SearchesDataRepo searchesDataRepo = new MongoSearchesDataRepoImpl(datastoresProperties.MongoConnectionString, datastoresProperties.MongoDatabaseName);
            SearchesDataRepo searchesDataRepo = new CouchSearchesDataRepoImpl(datastoresProperties.CouchConnectionString, datastoresProperties.CouchUsername, datastoresProperties.CouchPassword);
            searchesDataRepo.RepositoryStartup();

            ModuleRepoRegistry.TheUserModule = new DefaultUserModuleImpl(userDataRepo);
            ModuleRepoRegistry.TheSearchesModule = new DefaultSearchesModuleImpl(userDataRepo, searchesDataRepo);


            supportWebService = new SupportWebService();
            parentsWebService = new ParentsWebService();
            childrenWebService = new ChildrenWebService();
            videoWebService = new VideoWebService();

            services.AddMvc().SetCompatibilityVersion(CompatibilityVersion.Version_2_2);
        }

        // This method gets called by the runtime. Use this method to configure the HTTP request pipeline.
        public void Configure(IApplicationBuilder app, IHostingEnvironment env)
        {
            Console.WriteLine("Startup.Configure");
            if (env.IsDevelopment())
            {
                app.UseDeveloperExceptionPage();
            }
            else
            {
                // The default HSTS value is 30 days. You may want to change this for production scenarios, see https://aka.ms/aspnetcore-hsts.
                app.UseHsts();
            }

            IRouteBuilder routeBuilder = new RouteBuilder(app);

            supportWebService.SetupRoutes(routeBuilder);
            childrenWebService.SetupRoutes(routeBuilder);
            parentsWebService.SetupRoutes(routeBuilder);
            videoWebService.SetupRoutes(routeBuilder);

            app.UseRouter(routeBuilder.Build());
        }
    }
}
