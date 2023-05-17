Feature: login en servidor
  Background:
  * def delayTime = 2000
#
#  Este test funciona, pero no es de buena educación martillear una API externa
#
#  Scenario: login malo en github
#    Given driver 'https://github.com/login'
#    And input('#login_field', 'dummy')
#    And input('#password', 'world')
#    When submit().click("input[name=commit]")
#    Then match html('.flash-error') contains 'Incorrect username or password.'
#

  Scenario: login malo en plantilla
    Given driver baseUrl + '/user/2'
    And input('#username', 'dummy')
    And input('#password', 'world')
    When submit().click("#btn-signin")
    Then match html('.error') contains 'Error en nombre de usuario o contraseña'

  @login_b
  Scenario: login correcto como b
    Given driver baseUrl
    And delay(delayTime)
    And input('#username', 'b')
    And input('#password', 'aa')
    When click("#btn-signin")
    And delay(delayTime)
    Then waitForUrl(baseUrl + '/user')

  @login_a
  Scenario: login correcto como a
    Given driver baseUrl
    And delay(delayTime)
    And input('#username', 'a')
    And input('#password', 'aa')
    When click("#btn-signin")
    And delay(delayTime)
    Then waitForUrl(baseUrl + '/admin')

  Scenario: logout after login
    Given driver baseUrl
    And delay(delayTime)
    And input('#username', 'a')
    And input('#password', 'aa')
    When click("#btn-signin")
    Then waitForUrl(baseUrl + '/admin')
    When click("{button}logout")
    And delay(delayTime)
    Then waitForUrl(baseUrl + '/login')
