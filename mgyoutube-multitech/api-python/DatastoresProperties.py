import json


class DatastoresProperties(object):

    def __init__(self):
        with open('secrets/datastores.json') as json_file:
            data = json.load(json_file)
            self.couchConnectionString = data['couchConnectionString']
            self.couchUsername = data['couchUsername']
            self.couchPassword = data['couchPassword']
            self.mongoConnectionString = data['mongoConnectionString']
            self.mongoDatabaseName = data['mongoDatabaseName']
