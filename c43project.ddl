USE final_project;

DROP TABLE IF EXISTS Listing, Address, Amenities, Users, Renter, Hosts, Located, Provides, Owns, Reserved;

create table Users
(
    sin        integer primary key not null,
    password   varchar(50),
    occupation varchar(20) DEFAULT 'No Occupation',
    firstName  varchar(20)         not null,
    lastName   varchar(20)         not null,
    dob        DATE                not null,
    CHECK ( sin > 0 ),
    CHECK ( dob < '2004-08-02' )
);

create table Hosts
(
    hostID integer primary key not null references Users (sin)
);

create table Renter
(
    renterID    integer primary key not null references Users (sin),
    ccNumber    varchar(20)         not null,
    expiryMonth integer             not null,
    expiryYear  integer             not null,
    cvc         integer             not null,
    CHECK ( cvc < 1000 and cvc > 99 ),
    CHECK ( expiryMonth > 0 and expiryMonth <= 12 ),
    CHECK ( expiryYear > 2022 or (expiryYear = 2022 and expiryMonth > 8))
#     CHECK ( ccNumber > 999999999999999 and ccNumber <= 9999999999999999 )
);

create table Listing
(
    listID      integer     not null auto_increment primary key,
    listingType varchar(15) not null,
    longitude   double      not null,
    latitude    double      not null,
    price       double      not null,
    CHECK ( latitude >= -90.0 and latitude <= 90.0 ),
    CHECK ( longitude >= -180.0 and longitude <= 180.0 )
);


create table Address
(
    addressID  integer primary key auto_increment,
    streetNo   integer     not null,
    streetName varchar(30) not null,
    city       varchar(30) not null,
    province   varchar(30) not null,
    country    varchar(30) not null,
    postalCode varchar(6)  not null,
    unitNo     integer,
    CHECK ( streetNo > 0 and unitNo >= 0 )
);

create table Amenities
(
    amenityID           integer not null primary key auto_increment,
--     list of booleans with amenities
    wifi                boolean not null DEFAULT FALSE,
    washer              boolean not null DEFAULT FALSE,
    ac                  boolean not null DEFAULT FALSE,
    heating             boolean not null DEFAULT FALSE,
    tv                  boolean not null DEFAULT FALSE,
    iron                boolean not null DEFAULT FALSE,
    kitchen             boolean not null DEFAULT FALSE,
    dryer               boolean not null DEFAULT FALSE,
    workspace           boolean not null DEFAULT FALSE,
    hairDryer           boolean not null DEFAULT FALSE,
    pool                boolean not null DEFAULT FALSE,
    parking             boolean not null DEFAULT FALSE,
    crib                boolean not null DEFAULT FALSE,
    grill               boolean not null DEFAULT FALSE,
    indoorFireplace     boolean not null DEFAULT FALSE,
    hotTub              boolean not null DEFAULT FALSE,
    evCharger           boolean not null DEFAULT FALSE,
    gym                 boolean not null DEFAULT FALSE,
    breakfast           boolean not null DEFAULT FALSE,
    smoking             boolean not null DEFAULT FALSE,
    beachfront          boolean not null DEFAULT FALSE,
    waterfront          boolean not null DEFAULT FALSE,
    smokeAlarm          boolean not null DEFAULT FALSE,
    carbonMonoxideAlarm boolean not null DEFAULT FALSE
);



create table Reserved
(
    reservationID   integer not null auto_increment primary key,
    hostID          integer not null,
    renterID        integer not null,
    listID          integer not null,
    startDate       DATE    not null,
    endDate         DATE    not null,
    statusAvailable boolean not null default false,
#     primary key (hostID, renterID, listID, startDate, endDate),
    foreign key (hostID) references Hosts (hostID),
    foreign key (renterID) references Renter (renterID),
    foreign key (listID) references Listing (listID),
    price           double  not null,
    hostCancelled boolean default false,
    hostReview      varchar(200),
    hostScore       integer,
    renterReview    varchar(200),
    renterScore     integer,
    CHECK ( hostScore <= 5 and hostScore >= 1 ),
    CHECK ( renterScore <= 5 and renterScore >= 1 )
);

create table Owns
(
    listID integer not null primary key references Listing (listID),
    hostID integer not null references Hosts (hostID)
);

create table Located
(
    listID    integer not null references Listing (listID),
    addressID integer not null references Address (addressID),
    primary key (listID, addressID)
);

create table Provides
(
    amenityID integer not null references Amenities (amenityID),
    listID    integer not null references Listing (listID)
#     primary key (listID, amenityID)
);

INSERT INTO Users VALUES (100,'password1','Lawyer','user1','userLast','2000-01-01'),(101,'password2','Accountant','user2','user2Last','1993-12-13'),(103,'password3','Student','user3','user3Last','1969-06-09'),(104,'password4','Janitor','user4','user4Last','1998-02-03');
INSERT INTO Hosts VALUES (100),(101),(103),(104);
INSERT INTO Renter VALUES (100,'1234567890123456',9,2048,345),(101,'56738920113456789',9,2023,124),(103,'8765432109567890',4,2024,432),(104,'7205730183750394',1,2023,903);

INSERT INTO Listing VALUES (1,'Apartment',34,35,1000),(2,'Apartment',34,35.1,1500),(3,'Full house',34.15,35,2000),(4,'Room',34.15,35.1,1800),(5,'Room',23,-80,200),(6,'Apartment',54,45,9000),(7,'Full house',82,85,328),(8,'Full house',73,79.1,894),(9,'Apartment',46,87,94372),(10,'Full house',46.1,87,40000),(11,'Apartment',63,89,325),(12,'Full house',63,73,987),(13,'Room',63.2,73,1800);

INSERT INTO Address VALUES (1,30,'Wolf St','Toronto','Ontario','Canada','M3A2P6',504),(2,30,'Wolf St','Toronto','Ontario','Canada','M3A2P6',704),(3,20,'Dog St','Toronto','Ontario','Canada','M3A2P5',NULL),(4,90,'Rutherford Dr','Toronto','Ontario','Canada','M3A2P7',234),(5,100,'Charming Ave','Newark','New Jersey','USA','35192',69),(6,12345,'street dr','New York','New York State','USA','987654',49),(7,832,'random St','Los Angels','some state','USA','765872',NULL),(8,72,'apple st','Mac City','California','USA','467389',NULL),(9,1035,'Ritchie St','Ajax','Ontario','Canada','L1S5R6',NULL),(10,1024,'Ritchie St','Ajax','Ontario','Canada','L1S5R5',NULL),(11,78,'Allard Ave','Milton','Ontario','Canada','K3H7J8',NULL),(12,76,'Instagram St','Poodle','Ontario','Canada','A5S8I0',NULL),(13,295,'Kansas','Poodle','Ontario','Canada','A5S8I1',NULL);

INSERT INTO Located VALUES (1,1),(2,2),(3,3),(4,4),(5,5),(6,6),(7,7),(8,8),(9,9),(10,10),(11,11),(12,12),(13,13);

INSERT INTO Amenities VALUES (1,1,0,1,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0),(2,0,0,1,0,0,1,0,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0),(3,0,0,1,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0),(4,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1),(5,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0,0),(6,0,0,0,0,0,0,1,1,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,0),(7,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0),(8,0,0,0,1,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,1),(9,0,0,1,0,0,0,1,0,0,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0),(10,0,0,0,1,0,0,0,0,1,1,1,1,1,1,0,0,0,0,0,0,0,0,0,0),(11,0,0,0,0,0,1,0,0,0,0,0,0,0,0,0,1,0,0,1,0,0,0,0,1),(12,1,1,0,0,0,0,1,1,1,0,0,0,0,0,0,0,0,0,0,0,0,0,1,0),(13,0,0,0,0,0,0,1,0,1,0,0,0,1,0,0,0,0,0,0,0,1,0,0,1);

INSERT INTO Provides VALUES (1,1),(2,2),(3,3),(4,4),(5,5),(6,6),(7,7),(8,8),(9,9),(10,10),(11,11),(12,12),(13,13);

INSERT INTO Owns VALUES (1,100),(2,100),(3,100),(4,104),(5,104),(6,104),(7,104),(8,103),(9,103),(10,103),(11,103),(12,101),(13,101);


INSERT INTO Reserved VALUES
(1,100,101,1,'2023-02-02','2031-02-08',0,1000,0,NULL,NULL,NULL,NULL),
(2,100,101,1,'2022-03-08','2022-04-12',0,1000,0,NULL,NULL,'The man drove the car down the road.',3),
(3,104,100,5,'2019-06-19','2019-07-24',0,200,0,NULL,NULL,NULL,NULL),
(4,104,101,5,'2020-06-19','2020-07-24',0,200,0,NULL,NULL,NULL,NULL),
(5,104,100,5,'2021-06-19','2021-07-24',0,200,0,NULL,NULL,NULL,NULL),
(6,104,103,5,'2022-06-19','2022-07-24',0,200,0,NULL,NULL,NULL,NULL),
(7,103,100,11,'2022-06-19','2022-07-24',0,325,0,NULL,NULL,NULL,NULL),
(8,103,101,11,'2021-06-19','2021-07-24',0,325,0,NULL,NULL,NULL,NULL),
(9,103,103,11,'2020-06-19','2020-07-24',0,325,0,NULL,NULL,NULL,NULL),
(10,103,104,10,'2020-06-19','2020-07-24',0,40000,0,NULL,NULL,NULL,NULL),
(11,101,100,13,'2020-06-19','2020-07-24',0,1800,0,NULL,NULL,NULL,NULL),
(12,104,100,4,'2020-06-19','2020-07-24',0,1800,0,NULL,NULL,NULL,NULL),
(13,104,103,4,'2019-06-19','2019-07-24',0,1800,0,NULL,NULL,NULL,NULL),
(14,101,103,12,'2019-06-19','2019-07-24',0,1800,0,NULL,NULL,NULL,NULL);








