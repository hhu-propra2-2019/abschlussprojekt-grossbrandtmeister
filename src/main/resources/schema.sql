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
    address     varchar(100),
    url         varchar(100),
    status      varchar(30),
    type        varchar(30)
);

CREATE TABLE student
(
    id    integer primary key auto_increment,
    name  varchar(200),
    email varchar(100)
);

CREATE TABLE student_event
(
    student          integer,
    submittedSummary varchar(100) DEFAULT '',
    event            integer,
/*    submissionTime     time,
    submissionDate     date,
    acceptedSummary    varchar(100),
    usedForCertificate varchar(100),*/
    primary key (student, event)
);