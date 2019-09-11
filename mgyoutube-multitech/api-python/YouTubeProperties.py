import json


class YouTubeProperties(object):

    def __init__(self):
        with open('secrets/youtube.json') as json_file:
            data = json.load(json_file)
            self.apiKey = data['apiKey']
            self.applicationName = data['applicationName']
