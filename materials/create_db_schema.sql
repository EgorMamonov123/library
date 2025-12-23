--
-- PostgreSQL database dump
--

-- Dumped from database version 16.9
-- Dumped by pg_dump version 16.9

SET statement_timeout = 0;
SET lock_timeout = 0;
SET idle_in_transaction_session_timeout = 0;
SET client_encoding = 'UTF8';
SET standard_conforming_strings = on;
SELECT pg_catalog.set_config('search_path', '', false);
SET check_function_bodies = false;
SET xmloption = content;
SET client_min_messages = warning;
SET row_security = off;

--
-- Name: main; Type: SCHEMA; Schema: -; Owner: postgres
--

CREATE SCHEMA main;


ALTER SCHEMA main OWNER TO postgres;

SET default_tablespace = '';

SET default_table_access_method = heap;

--
-- Name: reservations; Type: TABLE; Schema: main; Owner: postgres
--

CREATE TABLE main.reservations (
   id integer NOT NULL,
   guest integer NOT NULL,
   room integer NOT NULL,
   from_date date NOT NULL,
   to_date date NOT NULL,
   created_at timestamp without time zone DEFAULT CURRENT_TIMESTAMP,
   CONSTRAINT reservations_check CHECK ((to_date > from_date))
);


ALTER TABLE main.reservations OWNER TO postgres;

--
-- Name: reservations_id_seq; Type: SEQUENCE; Schema: main; Owner: postgres
--

CREATE SEQUENCE main.reservations_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE main.reservations_id_seq OWNER TO postgres;

--
-- Name: reservations_id_seq; Type: SEQUENCE OWNED BY; Schema: main; Owner: postgres
--

ALTER SEQUENCE main.reservations_id_seq OWNED BY main.reservations.id;


--
-- Name: rooms; Type: TABLE; Schema: main; Owner: postgres
--

CREATE TABLE main.rooms (
    number integer NOT NULL,
    type character varying(20) NOT NULL
);


ALTER TABLE main.rooms OWNER TO postgres;

--
-- Name: users; Type: TABLE; Schema: main; Owner: postgres
--

CREATE TABLE main.users (
    id integer NOT NULL,
    name character varying(100) NOT NULL,
    role character varying(20)
);


ALTER TABLE main.users OWNER TO postgres;

--
-- Name: users_id_seq; Type: SEQUENCE; Schema: main; Owner: postgres
--

CREATE SEQUENCE main.users_id_seq
    AS integer
    START WITH 1
    INCREMENT BY 1
    NO MINVALUE
    NO MAXVALUE
    CACHE 1;


ALTER SEQUENCE main.users_id_seq OWNER TO postgres;

--
-- Name: users_id_seq; Type: SEQUENCE OWNED BY; Schema: main; Owner: postgres
--

ALTER SEQUENCE main.users_id_seq OWNED BY main.users.id;


--
-- Name: reservations id; Type: DEFAULT; Schema: main; Owner: postgres
--

ALTER TABLE ONLY main.reservations ALTER COLUMN id SET DEFAULT nextval('main.reservations_id_seq'::regclass);


--
-- Name: users id; Type: DEFAULT; Schema: main; Owner: postgres
--

ALTER TABLE ONLY main.users ALTER COLUMN id SET DEFAULT nextval('main.users_id_seq'::regclass);


--
-- Name: reservations reservations_pkey; Type: CONSTRAINT; Schema: main; Owner: postgres
--

ALTER TABLE ONLY main.reservations
    ADD CONSTRAINT reservations_pkey PRIMARY KEY (id);


--
-- Name: rooms rooms_pkey; Type: CONSTRAINT; Schema: main; Owner: postgres
--

ALTER TABLE ONLY main.rooms
    ADD CONSTRAINT rooms_pkey PRIMARY KEY (number);


--
-- Name: users users_pkey; Type: CONSTRAINT; Schema: main; Owner: postgres
--

ALTER TABLE ONLY main.users
    ADD CONSTRAINT users_pkey PRIMARY KEY (id);


--
-- Name: reservations reservations_guest_fkey; Type: FK CONSTRAINT; Schema: main; Owner: postgres
--

ALTER TABLE ONLY main.reservations
    ADD CONSTRAINT reservations_guest_fkey FOREIGN KEY (guest) REFERENCES main.users(id) ON DELETE CASCADE;


--
-- Name: reservations reservations_room_fkey; Type: FK CONSTRAINT; Schema: main; Owner: postgres
--

ALTER TABLE ONLY main.reservations
    ADD CONSTRAINT reservations_room_fkey FOREIGN KEY (room) REFERENCES main.rooms(number) ON DELETE CASCADE;


--
-- PostgreSQL database dump complete
--
