from apimodel import Video
from modules.VideoModule import VideoModule
from googleapiclient.discovery import build, Resource


class DefaultVideoModuleImpl(VideoModule):
    YOUTUBE_API_SERVICE_NAME = "youtube"
    YOUTUBE_API_VERSION = "v3"
    YOUTUBE_API_MAX_VIDEOS_RETURNED = 50

    def __init__(self, apiKey: str, applicationName: str):
        self.apiKey = apiKey
        self.applicationName = applicationName

    def search(self, searchTerms: str) -> list:
        youtube = build(DefaultVideoModuleImpl.YOUTUBE_API_SERVICE_NAME,
                        DefaultVideoModuleImpl.YOUTUBE_API_VERSION, developerKey=self.apiKey)

        #
        # false positve pylink(no-member) "Instance of 'Resource' has no 'search' member"
        # dont want to disable it in general
        #
        response = youtube.search().list(
            q=searchTerms,
            part='id,snippet',
            maxResults=DefaultVideoModuleImpl.YOUTUBE_API_MAX_VIDEOS_RETURNED,
            type="video",
            safeSearch="strict"
        ).execute()

        # print("search '", searchTerms, "' returned:", response.items)

        videos = list()

        # type(response.items) <class 'builtin_function_or_method'>
        # print("type(response.items)", type(response.items))
        # response.items <built-in method items of dict object at 0x000001E821D51A98>
        # print("response.items", response.items)
        # response.items() dict_items([('kind', 'youtube#searchListResponse'),...
        # print("response.items()", response.items())

        #
        # WHY?!?! if response.items is a dict() why can we not subscript it or call get()
        #
        # TypeError: argument of type 'builtin_function_or_method' is not iterable
        # print("items in response.items", 'items' in response.items)
        # TypeError: 'builtin_function_or_method' object is not subscriptable
        # print("response.items['items']", response.items['items'])
        # AttributeError: 'builtin_function_or_method' object has no attribute 'get'
        # print("response.items.get('items')", response.items.get('items'))

        for responseItemKey, responseItem in response.items():
            # print("responseItemKey", responseItemKey)
            if (responseItemKey == "items"):
                # print("type(responseItem)", type(responseItem))
                # print("len(responseItem)", len(responseItem))
                videoResult = responseItem[0]
                #print("type(videoResult)", type(videoResult))
                #print("videoResult", videoResult)
                for videoResult in responseItem:
                    id = videoResult['id']
                    #print("type(id)", type(id))
                    #print("id", id)
                    snippet = videoResult['snippet']
                    #print("type(snippet)", type(snippet))
                    thumbnails = snippet['thumbnails']
                    #print("type(thumbnails)", type(thumbnails))
                    #print("thumbnails", thumbnails)
                    video = Video.Video()
                    video.videoId = id['videoId']
                    video.title = snippet['title']
                    video.description = snippet['description']
                    video.channelId = snippet['channelId']
                    video.channelTitle = snippet['channelTitle']
                    video.thumbnailUrl = thumbnails['default']['url']
                    video.thumbnailHeight = thumbnails['default']['height']
                    video.thumbnailWidth = thumbnails['default']['width']
                    video.publishedAt = snippet['publishedAt']

                    videos.append(video)

        return videos
