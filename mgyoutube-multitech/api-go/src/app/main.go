package main

import (
	"log"
	"modules"
	"net/http"
	"repos"
	"webservices"

	"github.com/gorilla/mux"
)

func loggingMiddleware(next http.Handler) http.Handler {
	return http.HandlerFunc(func(w http.ResponseWriter, r *http.Request) {
		// log.Println("[http pre handler] ------------------------------------------------")
		// log.Println("[http pre handler]", r.Method, r.RequestURI)
		next.ServeHTTP(w, r)
		// log.Println("[http post handler]", r.Method, r.RequestURI, w)
	})
}

func main() {
	youTubeProperties, errReadYouTubeProperties := ReadYouTubeProperties()
	if errReadYouTubeProperties != nil {
		log.Fatalln("Failed to read the youtube.json", errReadYouTubeProperties)
	}

	videoModule := modules.NewDefaultVideoModuleImpl(youTubeProperties.APIKey, youTubeProperties.ApplicationName)

	datastoreProperties, errReadDatastoreProperties := ReadDatastoresProperties()
	if errReadDatastoreProperties != nil {
		log.Fatalln("Failed to read the datastore.json", errReadDatastoreProperties)
	}

	//searchesDataRepo := repos.NewSimplisticSearchesDataRepoImpl()
	//searchesDataRepo := repos.NewMongoSearchesDataRepoImpl(mongoConnectionString, mongoDatabaseName)
	searchesDataRepo := repos.NewCouchSearchesDataRepoImpl(datastoreProperties.CouchConnectionString, datastoreProperties.CouchUsername, datastoreProperties.CouchPassword)
	searchRepoStartupError := searchesDataRepo.RepositoryStartup()
	if searchRepoStartupError != nil {
		log.Panicln("error while initializing searches data repo", searchesDataRepo, searchRepoStartupError)
	}

	//userDataRepo := repos.NewSimpleUserDataRepoImpl()
	//userDataRepo := repos.NewMongoUserDataRepoImpl(mongoConnectionString, mongoDatabaseName)
	userDataRepo := repos.NewCouchUserDataRepoImpl(datastoreProperties.CouchConnectionString, datastoreProperties.CouchUsername, datastoreProperties.CouchPassword)
	userRepoSetupError := userDataRepo.RepositoryStartup()
	if userRepoSetupError != nil {
		log.Panicln("error while initializing user data repo", userDataRepo, userRepoSetupError)
	}

	userModule := modules.NewDefaultUserModuleImpl(userDataRepo)
	searchesModule := modules.NewDefaultSearchesModuleImpl(userDataRepo, searchesDataRepo)

	router := mux.NewRouter().StrictSlash(true)
	router.Use(loggingMiddleware)

	parentsWebService := webservices.NewParentWebService(router, userModule, searchesModule)
	childrenWebService := webservices.NewChildrenWebService(router, userModule)
	supportWebService := webservices.NewSupportWebService(router, userModule)
	videoWebService := webservices.NewVideoWebService(router, videoModule)

	// gets rid of the "declared and not used" error
	log.Println(supportWebService)
	log.Println(childrenWebService)
	log.Println(parentsWebService)
	log.Println(videoWebService)

	log.Println("api-go listening on port 10000")
	log.Fatal(http.ListenAndServe(":10000", router))
}
