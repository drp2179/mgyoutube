import { ModuleRepoRegistry } from './modulereporegistry';
import { Request, Response, Server } from 'restify'
import { BadRequestError } from 'restify-errors'
import { UseNext } from './whatsnext'

export class VideoWebService {

    public static setupRouter(server: Server) {
        // DRP: restify can't seem to use class methods for handlers here... they lose the "this" pointer reference
        server.get('/api/videos', async (req, res, next) => {
            return UseNext.handleAsyncRestifyCall(await VideoWebService.searchVideos(req, res), next);
        });
    }

    private static async searchVideos(req: Request, res: Response): Promise<UseNext> {

        var searchTerms: string = req.query["search"]
        console.log("videosSearch: searchTerms='" + searchTerms + "'");

        if (!searchTerms) {
            console.warn("search term are blank failing as 400");
            const err400 = new BadRequestError();
            return new UseNext(err400);
        }
        else {
            // TODO: need to sanitize payload input before using
            var sanitizedSearchTerms = searchTerms;

            var videos = await ModuleRepoRegistry.getVideosModule().search(searchTerms);

            console.log("videosSearch: searchTerms=" + sanitizedSearchTerms + " returning OK, " + videos);
            res.setHeader("content-type", "application/json")
            res.send(videos)

            return UseNext.Nothing;
        }
    }
}