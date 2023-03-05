-- insert admin (username a, password aa)
INSERT INTO IWUser (id, enabled, name, roles, username, password)
VALUES (1, true, 'admin', 'ADMIN,USER', 'a',
    '{bcrypt}$2a$10$2BpNTbrsarbHjNsUWgzfNubJqBRf.0Vz9924nRSHBqlbPKerkgX.W'),
    (2, true, 'bonito', 'USER', 'b',
    '{bcrypt}$2a$10$2BpNTbrsarbHjNsUWgzfNubJqBRf.0Vz9924nRSHBqlbPKerkgX.W'),
    (3, true, 'Nicoooooo', 'ADMIN,USER', 'Nico',
    '{bcrypt}$2a$10$2BpNTbrsarbHjNsUWgzfNubJqBRf.0Vz9924nRSHBqlbPKerkgX.W');

-- Generate 10 random users
INSERT INTO IWUser (id, enabled, name, roles, username, password)
SELECT t.id + 3, true, CONCAT('User ', t.id), 'USER', CONCAT('user', t.id), '{bcrypt}$2a$10$2BpNTbrsarbHjNsUWgzfNubJqBRf.0Vz9924nRSHBqlbPKerkgX.W'
FROM (
  SELECT 1 AS id UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
) AS t;

-- Generate 3 random groups
INSERT INTO IWGroup (ID, enabled, CURRENCY, DESC, NAME, NUM_MEMBERS, TOT_BUDGET)
SELECT t.ID, true, FLOOR(RAND() * 2), CONCAT('Group ', t.ID, ' description'), CONCAT('Group ', t.ID, ' name'), FLOOR(RAND() * 9) + 1, 0
FROM (
  SELECT 1 AS ID UNION SELECT 2 UNION SELECT 3
) AS t;

-- Generate random group memberships for each user
INSERT INTO IWMember (GROUP_ID, USER_ID, enabled, BUDGET, ROLE, balance)
SELECT
    FLOOR(RAND() * 3) + 1, -- choose a random group ID between 1 and 5
    IWUser.id,
    true,
    FLOOR(RAND() * 1000) + 1, -- choose a random budget between 1 and 1000
    0, -- set role to 0
    0
FROM
    IWUser;
-- Generate random group memberships for each empty group
/*
INSERT INTO IWMember (GROUP_ID, USER_ID, BUDGET, ROLE)
SELECT
    g.ID,
    FLOOR(RAND() * 13) + 1, -- choose a random user ID between 1 and 13
    FLOOR(RAND() * g.TOT_BUDGET) + 1, -- choose a random budget between 1 and the group's total budget
    0 -- set role to 0
FROM
    IWGroup g
WHERE
    g.ID NOT IN (
        SELECT GROUP_ID FROM IWMember
    );
*/

-- Expenses Type
INSERT INTO IWTYPE (ID, NAME)
VALUES 
  (1, 'Food'),
  (2, 'Transportation'),
  (3, 'Entertainment'),
  (4, 'Housing'),
  (5, 'Shopping');

-- Generate 10 random expenses
INSERT INTO IWExpense (ID, enabled, AMOUNT, DATE, DESC, NAME, PICTURE, PAID_BY_ID, TYPE_ID)
SELECT t.ID, true, FLOOR(RAND() * 100), DATEADD('DAY', -FLOOR(RAND() * 30), '2023-03-04 00:00:00'), CONCAT('Expense ', t.ID, ' description'), CONCAT('Expense ', t.ID, ' name'), CONCAT('/picture', t.ID),
  (SELECT id FROM IWUser ORDER BY RAND() LIMIT 1), FLOOR(RAND() * 5) + 1
FROM (
  SELECT 1 AS ID UNION SELECT 2 UNION SELECT 3 UNION SELECT 4 UNION SELECT 5 UNION SELECT 6 UNION SELECT 7 UNION SELECT 8 UNION SELECT 9 UNION SELECT 10
) AS t;

-- Generate random owns relations
INSERT INTO IWOwns (USER_ID, EXPENSE_ID, GROUP_ID, enabled)
SELECT USER_ID, ID, GROUP_ID, true
FROM IWExpense e
INNER JOIN IWMember m ON e.PAID_BY_ID = m.USER_ID;


-- start id numbering from a value that is larger than any assigned above
ALTER SEQUENCE "PUBLIC"."GEN" RESTART WITH 1024;