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
	userDataRepo := repos.NewSimpleUserDataRepoImpl()
	userModule := modules.NewDefaultUserModuleImpl(userDataRepo)

	router := mux.NewRouter().StrictSlash(true)
	router.Use(loggingMiddleware)

	parentsWebService := webservices.NewParentWebService(router, userModule)
	childrenWebService := webservices.NewChildrenWebService(router, userModule)
	supportWebService := webservices.NewSupportWebService(router, userModule)

	// gets rid of the "declared and not used" error
	log.Println(supportWebService)
	log.Println(childrenWebService)
	log.Println(parentsWebService)

	log.Println("api-go listening on port 10000")
	log.Fatal(http.ListenAndServe(":10000", router))
}
