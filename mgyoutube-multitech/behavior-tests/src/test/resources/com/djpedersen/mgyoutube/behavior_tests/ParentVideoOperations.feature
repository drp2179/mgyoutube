Feature: Parent Video Operations

  Scenario: Story 8: Parent can run a search and see the results
    Given a parent user dad8 with password dad8password
    And the parent user dad8 has logged into the parents page
    When the parent user dad8 submits "learn guitar" as search words
    Then the parent user dad8 can see search results for "learn guitar"

	Scenario: Story 9a: Parent can view a video from a search result
    Given a parent user dad8 with password dad8password
    And the parent user dad8 has logged into the parents page
    And the parent user dad8 has searched for "learn guitar"
    When the parent user dad8 clicks on search result 3
    Then the parent user dad8 can see the youtube player with video 3
    
	Scenario: Story 9b: Parent can view several videos from a search result
    Given a parent user dad8 with password dad8password
    And the parent user dad8 has logged into the parents page
    And the parent user dad8 has searched for "learn guitar"
    When the parent user dad8 clicks on search result 2
    And the parent user dad8 can see the youtube player with video 2
    And the parent user dad8 clicks on search result 4
    And the parent user dad8 can see the youtube player with video 4
    And the parent user dad8 clicks on search result 6
    Then the parent user dad8 can see the youtube player with video 6
    