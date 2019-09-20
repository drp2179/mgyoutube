using System;
using System.Threading.Tasks;
using Microsoft.AspNetCore.Http;

namespace api_dotnet.aspnet
{

    public class ResponseHelper
    {
        public static Task Created(HttpResponse response, string location = null, object data = null, string contentType = null)
        {
            if (contentType != null)
            {
                response.Headers.Add("Content-Type", contentType);
            }
            if (location != null)
            {
                response.Headers.Add("Location", location);
            }
            response.StatusCode = StatusCodes.Status201Created;
            return response.WriteAsync(data == null ? "" : data.ToString());
        }
        public static Task Created(HttpContext context, string location = null, object data = null, string contentType = null)
        {
            return Created(context.Response, location, data, contentType);
        }

        public static Task NoContent(HttpResponse response)
        {
            return Task.Run(() =>
           { response.StatusCode = StatusCodes.Status204NoContent; }
            );
        }
        public static Task NoContent(HttpContext context)
        {
            return NoContent(context.Response);
        }

        public static Task NotFound(HttpResponse response)
        {
            response.StatusCode = StatusCodes.Status404NotFound;
            return response.WriteAsync(""); // this seems wrong
        }
        public static Task NotFound(HttpContext context)
        {
            return NotFound(context.Response);
        }

        public static Task Ok(HttpResponse response, string payload, string contentType)
        {
            if (contentType != null)
            {
                response.Headers.Add("Content-Type", contentType);
            }
            response.StatusCode = StatusCodes.Status200OK;
            return response.WriteAsync(payload);
        }

        public static Task BadRequest(HttpResponse response)
        {
            response.StatusCode = StatusCodes.Status400BadRequest;
            return response.WriteAsync("");
        }
        public static Task BadRequest(HttpContext context)
        {
            return BadRequest(context.Response);
        }

        public static Task Ok(HttpResponse response)
        {
            return Ok(response, "", null);
        }
        public static Task Ok(HttpContext context)
        {
            return Ok(context.Response, "", null);
        }

        public static Task Unauthorized(HttpResponse response)
        {
            response.StatusCode = StatusCodes.Status401Unauthorized;
            return response.WriteAsync(""); // this seems wrong
        }
        public static Task Unauthorized(HttpContext context)
        {
            return Unauthorized(context.Response);
        }
        public static Task InternalServerError(HttpResponse response)
        {
            response.StatusCode = StatusCodes.Status500InternalServerError;
            return response.WriteAsync(""); // this seems wrong
        }
        public static Task InternalServerError(HttpContext context)
        {
            return InternalServerError(context.Response);
        }
    }

}