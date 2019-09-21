import json


class DatastoresProperties(object):

    def __init__(self):
        with open('secrets/datastores.json') as json_file:
            data = json.load(json_file)
            self.couchConnectionstring = data['couchConnectionstring']
            self.couchUsername = data['couchUsername']
            self.couchPassword = data['couchPassword']
