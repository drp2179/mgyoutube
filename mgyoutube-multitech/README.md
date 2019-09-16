# MG YouTube MultiTech

MG YouTube is a web system for providing content filters for YouTube.  It is divided into two apps, one for the parents and one for the children.
The parent app allows parents to create and maintain allowed searches that a child can use in the childrens app.  Additionally, parents can black list phrases, individual videos and other things as we go along.

Features are added using the following steps (more or less in order):
- write a story in Trello, with Acceptance Criteria
- write one or more Acceptance Tests in Gherkin that detail the expected behavior of the system
- implement the Gherkin steps in Java using Selenium and HttpClient where appropriate
- update YAML specification of REST API as necessary using Swagger Hub
- write one or more API tests using RestAssured
- implement the API in Java using JAX-RS, Jersey and Jetty
- ensure the API tests are green
- implement the UI in Vue.JS
- ensure the behavior tests are green
- reimplement the API in C# using .NET Core and AspNetCore
- ensure the API tests are green
- ensure the behavior tests are green
- reimplement the API in Node.JS using Restify and Typescript
- ensure the API tests are green
- ensure the behavior tests are green
- reimplement the API in Python with Pyramid
- ensure the API tests are green
- ensure the behavior tests are green
- reimplement the API in Go with gorilla/mux
- ensure the API tests are green
- ensure the behavior tests are green
- re-implement the UI in React.JS
- ensure the behavior tests are green
- re-implement the UI in Angular 8
- ensure the behavior tests are green

[Active and Completed Stories](Stories.md)

The idea is that we should be able to demonstate substitutable REST implementations against a specific API spect.  Further, that API should be able to support multiple systems that all demonstate the same UI behaviors.

Yes, we're skipping the Unit Tests here (professional development would always, always, have unit tests for everything, right?) But, we do need to teach what comes after unit tests...

In anticipation of an upcoming interview I decided to jump into the data repos a bit.  Starting with MongoDB, I implemented the repository layers in Java, C# (sync), Node and Go.  
That all working (i.e. the API and Behavior tests all pass) I replaced the C# sync implementation with an async/await verion, and in Java created a SQL version using an embedded H2 db.  
I may abandon the Simple implementations all around given the Mongo and H2 impls...

Finishing work on Story 11 will help me decide...