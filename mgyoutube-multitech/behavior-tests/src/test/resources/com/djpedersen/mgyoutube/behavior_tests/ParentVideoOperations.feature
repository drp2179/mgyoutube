Feature: Parent Video Operations

  Scenario: Story 8: Parent can run a search and see the results
    Given a parent user dad8 with password dad8password
    And a child user child81 with password child61password and dad8 as a parent
    And the parent user dad8 has logged into the parents page
    When the parent user dad8 submits "learn guitar" as search words
    Then the parent user dad8 can see search results for "learn guitar"
