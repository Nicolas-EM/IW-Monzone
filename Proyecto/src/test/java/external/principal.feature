Feature: flujo de la app
  Background:
  * def delayTime = 1000

  @group
  Scenario: Entrar a grupo
    Given call read('login.feature@login_b')
    And delay(delayTime)
    When click("#group-2 .card")
    And delay(delayTime)
    Then waitForUrl(baseUrl + '/group/2')

   @group_config
   Scenario: Configuracion grupo
     Given call read('principal.feature@group')
     And delay(delayTime)
     When click("#groupConfigBtn")
     And delay(delayTime)
     Then waitForUrl(baseUrl + '/group/2/config')

   @group_viewExpense
   Scenario: View expense in group
     Given call read('principal.feature@group')
     And delay(delayTime)
     When click(".body div.card")
     And delay(delayTime)
     Then waitForUrl(baseUrl + '/group/2/99')

  @group_addExpense
  Scenario: Add expense to group
    Given call read('principal.feature@group')
    And delay(delayTime)
    When click("#addExpense")
    And delay(delayTime)
    Then waitForUrl(baseUrl + '/group/2/new')
    And delay(delayTime)
    And input('#name', 'Karate Expense')
    And input('#desc', 'Auto generated description')
    And input('#dateString', '01-01-2023')
    And input('#amount', '42')
    And select('#paidById', 1)
    And select('#typeId', 1)
    And click("#btn-save")
    And delay(delayTime)
    Then waitForUrl(baseUrl + '/group/2')
    And click("#expensesTable .card") # obtiene el primerExpense
    # Comprobar que los campos son correctos
    # Then match input('#name').value == 'Karate Expense'
    # Then match input('#desc').value == 'Auto generated description'
    # Then match input('#dateString').value == '01-01-2023'
    # Then match input('#amount').value == '42'
    # Then match select('#paidById').value == 'bonito'
    # Then match select('#typeId').value == 'Accommodation'


  @profile
  Scenario: View profile
    Given call read('login.feature@login_b')
    And delay(delayTime)
    When click("#btn-drop")
    And delay(delayTime)
    When click("#profile")
    Then waitForUrl(baseUrl + '/user/config')