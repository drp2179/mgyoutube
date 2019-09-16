Feature: Basic Site Operations

  Scenario: Story 1: Anyone can access the front page
    Given a user none with no password
    When the user none accesses the home page
    Then the user none can see a link to the childrens page
    And the user none can see a link to the parents page
    
  Scenario: Story 2: Anyone can access the childrens page
    Given a user none with no password
    When the user none accesses the chilrens page
    Then the user none can see the childrens page
    And the user none should be able to login

	Scenario: Story 3: Anyone can access the parents page
    Given a user none with no password
    When the user none accesses the parents page
    Then the user none can see the parents page
    And the user none should be able to login
    
    