-- Clear all data
TRUNCATE TABLE main.reservations CASCADE;
TRUNCATE TABLE main.rooms CASCADE;
TRUNCATE TABLE main.users CASCADE;

-- Reset sequences
ALTER SEQUENCE main.reservations_id_seq RESTART WITH 1;
ALTER SEQUENCE main.users_id_seq RESTART WITH 1;
