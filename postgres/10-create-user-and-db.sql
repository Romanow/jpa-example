-- file: 10-create-user-and-db.sql
CREATE DATABASE example;
CREATE ROLE program WITH PASSWORD 'test';
GRANT ALL PRIVILEGES ON DATABASE example TO program;
ALTER ROLE program WITH LOGIN;