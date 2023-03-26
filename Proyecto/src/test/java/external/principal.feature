Feature: flujo de la app
  
  @group
  Scenario: Entrar a grupo
    Given call read('login.feature@login_b')
    And driver baseUrl + '/user/'
    And delay(350)
    When submit().click("#home_groups .card")
    Then waitForUrl(baseUrl + '/group/2')

  @group_config
  Scenario: Configuracion grupo
    Given call read('principal.feature@group')
    And driver baseUrl + '/group/2'
    And delay(350)
    When submit().click("#groupConfigBtn")
    Then waitForUrl(baseUrl + '/group/2/config')

  @group_viewExpense
  Scenario: View expense in group
    Given call read('principal.feature@group')
    And driver baseUrl + '/group/2'
    And delay(350)
    When submit().click(".body div.card")
    Then waitForUrl(baseUrl + '/group/2/99')

  @group_addExpense
  Scenario: Add expense to group
    Given call read('principal.feature@group')
    And driver baseUrl + '/group/2'
    And delay(350)
    When submit().click("#addExpense")
    Then waitForUrl(baseUrl + '/group/2/new')
    And input('#name', 'Karate Expense')
    And input('#desc', 'Auto generated description')
    And input('#date', '01-01-2023')
    And input('#amount', '42')
    # TODO seleccionar paidby y type --> hacer submit
    Then delay(100)

  @profile
  Scenario: View profile
    Given call read('login.feature@login_b')
    And driver baseUrl + '/user/'
    And delay(350)
    When submit().click("#profile")
    Then waitForUrl(baseUrl + '/user/config')