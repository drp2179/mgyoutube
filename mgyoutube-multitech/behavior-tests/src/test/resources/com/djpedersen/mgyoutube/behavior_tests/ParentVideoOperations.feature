Feature: Parent Video Operations

  Scenario: Story 8: Parent can run a search and see the results
    Given a parent user dad8 with password dad8password
    And the parent user dad8 has logged into the parents page
    When the parent user dad8 submits "learn guitar" as search words
    Then the parent user dad8 can see search results for "learn guitar"

	Scenario: Story 9a: Parent can view a video from a search result
    Given a parent user dad9a with password dad9apassword
    And the parent user dad9a has logged into the parents page
    And the parent user dad9a has searched for "learn guitar"
    When the parent user dad9a clicks on search result 3
    Then the parent user dad9a can see the youtube player with video 3
    
	Scenario: Story 9b: Parent can view several videos from a search result
    Given a parent user dad9b with password dad9bpassword
    And the parent user dad9b has logged into the parents page
    And the parent user dad9b has searched for "learn guitar"
    When the parent user dad9b clicks on search result 2
    And the parent user dad9b can see the youtube player with video 2
    And the parent user dad9b clicks on search result 4
    And the parent user dad9b can see the youtube player with video 4
    And the parent user dad9b clicks on search result 6
    Then the parent user dad9b can see the youtube player with video 6
    
  #Scenario: Story 10a: Parent can save a search and see it the saved search list
    #Given a parent user dad10a with password dad10apassword
    #And the parent user dad10a has logged into the parents page
    #And the parent user dad10a has searched for "learn flute"
    #When the parent user dad10a saves the "learn flute" search
    #Then the parent user dad10a can see "learn flute" in the saved search list
   #
  #Scenario: Story 10b: Parent can previosly saved searches after logging in
    #Given a parent user dad10b with password dad10bpassword
#		And the parent user dad10b has previously saved a search for "learn clarinet"
#		When the parent user dad10b logs into the parents app
    #Then the parent user dad10b can see "learn clarinet" in the saved search list
    