# Concert Booking Web Service

## Description

The concert web booking service is for a small venue, with a capacity of 374 seats. The venue’s seats
are classified into three price bands, A, B and C. A concert may run on multiple dates, and each
concert has at least one performer. Additionally, a performer may feature in multiple concerts.
The service allows clients to retrieve information about concerts and performers, to make
and enquire about reservations, to register and authenticate users, and to download performer
images. On making a reservation, the service randomly selects seats in the required price band (if
available) and holds them for a short period. If the reservation is confirmed within this period,
the seats are booked. Without any confirmation, the service frees the seats so that they can be
reserved and booked by others. To make and confirm reservations, clients must be authenticated
and the associated user must have a credit card registered with the service.
In addition, the service allows clients to subscribe to news reports concerning concerts
and performers. Once subscribed, clients will be notified of relevant news items, e.g. scheduling of
particular concerts, notifications for when tickets are going on sale, news stories about performers
etc. The service satisfies the scalability quality attribute requirement. As it is expected that
the service will experience high load when concert tickets go on sale.

## Getting Started

### Prerequisites

You will need Java 1.8 to run the Java application  
Download `jdk1.8` from the [Oracle website](https://www.oracle.com/technetwork/java/javase/downloads/jdk8-downloads-2133151.html)

The project requires Maven. Maven command line tools can be installed, or using an IDE that includes Maven integration such as Eclipse or IntelliJ.  

### Installing

Clone the repository to your local machine with the following command in terminal:

```
git clone https://github.com/dylanfu/concert-booking-web-service.git
```

## Running the tests

First `mvn build` the project.  
Then run ConcertServiceTest to run the JUnit tests located in softeng325-concert-client/src/test
