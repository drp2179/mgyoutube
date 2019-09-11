import { Video } from '../apimodel/video';


export interface VideoModule {

    //: Array<Video>
    search(searchTerms: string): Promise<Array<Video>>;
}
