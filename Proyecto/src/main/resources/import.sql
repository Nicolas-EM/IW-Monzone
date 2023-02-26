-- insert admin (username a, password aa)
INSERT INTO IWUser (id, name, roles, username, password)
VALUES (1, 'admin', 'ADMIN,USER', 'a',
    '{bcrypt}$2a$10$2BpNTbrsarbHjNsUWgzfNubJqBRf.0Vz9924nRSHBqlbPKerkgX.W');
INSERT INTO IWUser (id, name, roles, username, password)
VALUES (2, 'bonito', 'USER', 'b',
    '{bcrypt}$2a$10$2BpNTbrsarbHjNsUWgzfNubJqBRf.0Vz9924nRSHBqlbPKerkgX.W');
INSERT INTO IWUser (id, name, roles, username, password)
VALUES (3, 'Nicoooooo', 'ADMIN,USER', 'Nico',
    '{bcrypt}$2a$10$2BpNTbrsarbHjNsUWgzfNubJqBRf.0Vz9924nRSHBqlbPKerkgX.W');

-- Groups

INSERT INTO IWGroup (ID, CURRENCY, DESC, NAME, NUM_MEMBERS, TOT_BUDGET)
VALUES (1, 0, 'G1 Desc', 'G1 Name', 0, 0);
INSERT INTO IWGroup (ID, CURRENCY, DESC, NAME, NUM_MEMBERS, TOT_BUDGET)
VALUES (2, 1, 'G2 Desc', 'G2 Name', 0, 0);

-- Membership
INSERT INTO IWMember (GROUP_ID, USER_ID, BUDGET, ROLE)
VALUES (1, 2, 100, 0);
INSERT INTO IWMember (GROUP_ID, USER_ID, BUDGET, ROLE)
VALUES (1, 3, 999, 0);
INSERT INTO IWMember (GROUP_ID, USER_ID, BUDGET, ROLE)
VALUES (2, 2, 4, 0);

-- start id numbering from a value that is larger than any assigned above
ALTER SEQUENCE "PUBLIC"."GEN" RESTART WITH 1024;
