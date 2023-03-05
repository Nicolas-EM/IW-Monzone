Feature: flujo de la app
  @login
  Scenario: Flujo
  # Login
    Given driver baseUrl + '/login'
    And input('#username', 'b')
    And input('#password', 'aa')
    When submit().click(".form-signin button")
    Then waitForUrl(baseUrl + '/user/')
    Given driver baseUrl + '/user/'
  # Entrar a grupo
    When submit().click(".card")
    Then waitForUrl(baseUrl + '/group/2')
  # Entrar a gasto
    When submit().click(".card")
    Then waitForUrl(baseUrl + '/group/2/1')
  # Añadir gasto
    #Given driver baseUrl + '/group/2'
    #When submit().click(".button-add-expense button")
    #Then waitForUrl(baseUrl + '/group/2/new')
    # mas cosas
  # Entrar configuración de grupo
    Given driver baseUrl + '/group/2'
    When submit().click(".button_config button")
    Then waitForUrl(baseUrl + '/group/2/config')
  # Eliminar usuario si eres moderador (?)

  # Acceder a perfil de usuario
    # Given driver baseUrl + '/user/' #siempre sale el nav
    When submit().click("#profile a")
    Then waitForUrl(baseUrl + '/user/config')

  # Log out
    When submit().click("#logoutForm a")
    Then waitForUrl(baseUrl + '/login')