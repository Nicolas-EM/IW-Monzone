Feature: login en servidor
  Background:
  * def delayTime = 2000

  @group_from_profile
  Scenario: Ir a group desde perfil de usuario
    Given call read('principal.feature@profile')
    And delay(delayTime)
    When click("#group-2")
    Then waitForUrl(baseUrl + '/group/2')
