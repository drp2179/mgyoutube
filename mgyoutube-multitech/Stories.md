# MG YouTube Stories

## Story 1: Anyone can access the front page
As a unclassified user, when I access the MGYouTubeV3 website, I can see links for the children and parents areas.

Acceptance Criteria
- un-authenticated users can access the front page
- a children's link exists
- a parent's link exists


## Story 2: Anyone can move from the front page to the children's page
As an unauthenticated user I can access the children's page

Acceptance Criteria
- can see the childrens page
- auth area show "Login" since not auth'd

## Story 3: Anyone can move from the front page to the parent's page
As an unauthenticated user I can access the parent's page

Acceptance Criteria
- can see the parents page
- auth area show "Login" since not auth'd


## Story 4: Parent can log in - Mock
As a parent user, I can log into the parents app so that I can do parenty things

Acceptance Criteria:
- api endpoint /api/auth exists
- POST to /api/auth { 'user' : 'dad', 'password': 'dadpassword'} returns 200 and the dad user object
- post of any other creds fails return 401
- login area displays "Logout"


## Story 5: Parent can add a child
As a parent user, I can add a child user, so that they may us the childrens app and be constrained by my viewing configuration

Acceptance Criteria
- a child user is created
- the child user can log into the childrens app
- the parent can see the child user in their list of children

## Story 6: Parent can see all of their associated children when they access the parents page
As a parent, when I log into the parents app, I should be able to see all of my existing children so I can determine which child I would like to configure.

Acceptance Criteria:
- any existing children for a parent are displayed

## Story 8: Parent can run a search and see the results
As a logged in parent, I can run a search and see the search results so that I can assess if this is a good search for my child.

Acceptance Criteria:
- can enter search terms and submit
- can see the results

## Story 9: Parent can watch a video from a search result
As a logged in parent, after I run a search I can watch any of the videos in the search results to review specific videos.

Acceptance Criteria:
- logged in parent can click on each search result video search result and get a youtube player to watch the video.

## Story 10: Parent can save search terms
As a parent, I can save a search phrase, so that my children can use that search

Acceptance Criteria
- authenticated parent, after issuing a search, can save that search
- authenticated parent can see the saved search in a saved search list after saving
- authenticated parent can see the previous saved searches immediately after login
- API supports getting saved searches
  - GET /parent/{parent}/searches
- API support adding to saved searches
  - PUT /parent{parent/searches/{search phrase}
