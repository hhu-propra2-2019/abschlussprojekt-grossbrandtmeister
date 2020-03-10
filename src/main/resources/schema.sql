DROP TABLE IF EXISTS event;
DROP TABLE IF EXISTS student;
DROP TABLE IF EXISTS student_event;

CREATE TABLE event
(
    id          integer primary key auto_increment,
    title       varchar(255),
    description text,
    price       double,
    time        time,
    date        date,
    address     varchar(200),
    url         text,
    status      varchar(30),
    type        varchar(30)
);

CREATE TABLE student
(
    id    integer primary key auto_increment,
    login varchar(200) unique,
    name  varchar(200),
    email varchar(100)
);

CREATE TABLE student_event
(
    student              integer,
    event                integer,
    submitted_summary    boolean,
    url                  text,
    time_submission      time,
    date_submission      date,
    accepted             boolean,
    used_for_certificate boolean,
    primary key (student, event)
);