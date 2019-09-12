Feature: Basic Parent Operations

  Scenario: Story 4a: Parents can login to parents app - mock
    Given a parent user dad with password dadpassword
    When the parent user dad accesses the parents page
    And the parent user dad logs into the parents app
    Then the parent user dad can see the parents page
    And the parent user dad should be able to logout

  Scenario: Story 4b: Non parents cannot login to parents app- mock
    Given a parent user dad4b with password dad4bpassword
    Given a child user child4b with password child4bpassword and dad4b as a parent
    When the child user child4b accesses the parents page
    And the child user child4b logs into the parents app
    Then the child user child4b can see the parents page
    And the child user child4b should be able to login

  Scenario: Story 5: A Parent can add a child
    Given a parent user dad5 with password dadpassword
    And the parent user dad5 has logged into the parents page
    And user child5 currently does not exist
    When the parent user dad5 adds child user child5 with password child5password
    Then the parent user dad5 can see child5 listed in their children section
    And the user child5 should be exist

	Scenario: Story 6: A Parent with 2 children can see both
    Given a parent user dad6 with password dad6password
    And a child user child61 with password child61password and dad6 as a parent
    And a child user child62 with password child62password and dad6 as a parent
    And the parent user dad6 has logged into the parents page
    Then the parent user dad6 can see child61 listed in their children section
    And the parent user dad6 can see child62 listed in their children section
    
#	Scenario: Story 7: A Parent can remove a child
    #Given a parent user dad7 with password dad7password
    #And a child user child71 with password child71password and dad7 as a parent
    #And a child user child72 with password child62password and dad7 as a parent
    #And the parent user dad7 has logged into the parents page
    #When the parent user dad7 removes the child user child71
    #Then the user dad7 can see child72 listed in their children section
    #And the user dad7 cannot see child71 listed in their children section
    
    
    