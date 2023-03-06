Feature: flujo de la app
  @login
  Scenario: Flujo
  # Login b
    Given driver baseUrl + '/login'
    Then waitForUrl(baseUrl + '/login')
    And input('#username', 'b')
    And input('#password', 'aa')
    When submit().click(".form-signin button")
    Then waitForUrl(baseUrl + '/user/')
  # Entrar a grupo
    When submit().click(".card")
    Then waitForUrl(baseUrl + '/group/2')
  # Entrar a gasto
    When submit().click(".card")
    Then waitForUrl(baseUrl + '/group/2/99')
  # Añadir gasto
    #Given driver baseUrl + '/group/2'
    #When submit().click(".button-add-expense button")
    #Then waitForUrl(baseUrl + '/group/2/new')
    # mas cosas
  # Entrar configuración de grupo
    When submit().click(".navbar-brand")
    Then waitForUrl(baseUrl + '/user/')
    When submit().click(".card")
    Then waitForUrl(baseUrl + '/group/2')
    When submit().click("#groupConfigBtn")
    Then waitForUrl(baseUrl + '/group/2/config')
  # Eliminar usuario si eres moderador

  # Acceder a perfil de usuario
    When submit().click("#profile")
    Then waitForUrl(baseUrl + '/user/config')

  # Log out
    When submit().click("#logoutForm a")
    Then waitForUrl(baseUrl + '/login')