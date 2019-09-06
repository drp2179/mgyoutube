using System;
using System.Collections.Generic;
using System.Linq;
using System.Threading.Tasks;
using api_dotnet.webservices;
using Microsoft.AspNetCore.Builder;
using Microsoft.AspNetCore.Hosting;
using Microsoft.AspNetCore.Http;
using Microsoft.AspNetCore.HttpsPolicy;
using Microsoft.AspNetCore.Mvc;
using Microsoft.AspNetCore.Routing;
using Microsoft.Extensions.Configuration;
using Microsoft.Extensions.DependencyInjection;
using Microsoft.Extensions.Logging;
using Microsoft.Extensions.Options;

namespace api_dotnet
{
    public class Startup
    {
        private SupportWebService supportWebService = null;
        private ChildrenWebService childrenWebService = null;
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
            supportWebService = new SupportWebService();
            parentsWebService = new ParentsWebService();
            childrenWebService = new ChildrenWebService();

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

            app.UseRouter(routeBuilder.Build());
        }

        private Task handler(HttpContext context)
        {
            Console.WriteLine("In handler" + ", Method: " + context.Request.Method + " " + context.Request.Path);
            context.Response.Headers.Add("Content-Type", "application/json");
            context.Response.StatusCode = 200;
            return context.Response.WriteAsync("{'hello':'out there'}");
        }
    }
}
