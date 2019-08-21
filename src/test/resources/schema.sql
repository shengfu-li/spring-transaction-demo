drop table BOOKINGS if exists;
create table BOOKINGS(ID serial, NAME varchar(15) NOT NULL);

drop table FOO if exists;
create table FOO(ID serial, NAME varchar(15) NOT NULL, AGE INT);

-- DROP TABLE customer if exists ;
-- CREATE TABLE customer(
--   id integer not null auto_increment,
--   name varchar(255) not null,
--   email varchar (255) not null,
--   primary key (id)
-- );