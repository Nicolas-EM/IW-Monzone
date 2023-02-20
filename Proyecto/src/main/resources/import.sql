-- insert admin (username a, password aa)
INSERT INTO IWUser (id, enabled, roles, username, password)
VALUES (1, TRUE, 'ADMIN,USER', 'a',
    '{bcrypt}$2a$10$2BpNTbrsarbHjNsUWgzfNubJqBRf.0Vz9924nRSHBqlbPKerkgX.W');
INSERT INTO IWUser (id, enabled, roles, username, password)
VALUES (2, TRUE, 'USER', 'b',
    '{bcrypt}$2a$10$2BpNTbrsarbHjNsUWgzfNubJqBRf.0Vz9924nRSHBqlbPKerkgX.W');
INSERT INTO IWUser (id, enabled, roles, username, password)
VALUES (3, TRUE, 'ADMIN,USER', 'Nico',
    '{bcrypt}$2a$10$2BpNTbrsarbHjNsUWgzfNubJqBRf.0Vz9924nRSHBqlbPKerkgX.W');

-- Groups
INSERT INTO IWGroup (id, description, title)
VALUES (1, 'G1 Desc', 'G1 Title');
INSERT INTO IWGroup (id, description, title)
VALUES (2, 'G2 Desc', 'G2 Title');

-- Membership
INSERT INTO IWMember (GROUP_ID, USER_ID, ROLE)
VALUES (1, 2, 0);
INSERT INTO IWMember (GROUP_ID, USER_ID, ROLE)
VALUES (2, 2, 0);

-- start id numbering from a value that is larger than any assigned above
ALTER SEQUENCE "PUBLIC"."GEN" RESTART WITH 1024;
