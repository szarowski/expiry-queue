# Expiry Queue

The Expiry Queue.

## Building and running the service

The project is build in Gradle, use:
```
gradle clean build
gradle bootRun
```

OR in case you want to run it with Gradle Wrapper:

```
./gradlew clean build
./gradlew bootRun
```

## Design overview

The Expiry Queue is build on Java 8+ and Spring Boot 2.1.0.
It stores data in PostgreSQL and uses Hazelcast in-memory data grid to emulate expiry queue.
The Hazelcast is not only a tool for expiry queue but can also server as a solution for scaling
as the queue is implemented as distributed map.

One can add messages into a queue and automatically store them into a postgresql database
by calling `POST /v1/expiry-queue/write` endpoint described in the endpoints section.
The expiry queue calculates a random expiry time between 10 and 60 seconds and stores
into a hazelcast distributed map and into a database.
Automatic expiry is resolved internally in the hazellcast map by calling:
`expiryMap.put(currentTime.toEpochMilli(), dataInternal, expiryInSeconds, TimeUnit.SECONDS);`
in ExpiryQueueServiceImpl.
To retrieve currently queued messages call: `GET /v1/expiry-queue/read`.
To retrieve all history of messages call: `GET /v1/expiry-queue/log`.

When running the application as: `gradle bootRun` the application requires postgres setup 
described in the `Create DB of Expiry Queue in PostgreSQL` section.
The application uses the H2 in-memory database for testing. 
Tests are split into Unit tests and Integration tests based on the suffix 
(ITest means integration test). The code coverage is about 98 percent.

The SQL scripts are located in the resources under the `/db/migration` path
used by the Flyway migration tool.

The application consists of REST Controller, Service handling the business logic
for the Expiry Queue Controller, Repository for `queue_log` table, Transformer
for transforming data and RowMapper for mapping the query data.
Interfaces are documented. 

## Endpoints
```
Add a Message to the Queue and Store in the Database:   POST   /v1/expiry-queue/write
Get Messages from the Expiry Queue:                     GET    /v1/expiry-queue/read
Get Messages Log from the Database:                     GET    /v1/expiry-queue/log
```

### Examples

#### Get all messages:
curl -X GET http://localhost:8080/v1/expiry-queue/log

#### Get messages from Queue:
curl -X GET http://localhost:8080/v1/expiry-queue/read

#### Add a message to the queue and store in the database:
curl -X POST http://localhost:8080/v1/expiry-queue/write -H "Content-Type: application/json" -d '{"message":"Hello World!"}'

## Requirements

Installed Java 8+.
Installed Gradle. 
Compile with `-parameters` flag. Gradle build already does that.

## Create DB of Expiry Queue in PostgreSQL

If you installed PostgreSQL from APK use the following commands to set up a database.
Note that the user for expiry_queue database requires `expiry_queue` password.

```
sudo -i -u postgres psql -c "CREATE USER expiry_queue WITH PASSWORD 'expiry_queue';"
sudo -u postgres createdb -O expiry_queue expiry_queue
```

## Database schema

This is a merge from Flyway migration. Note that the `CREATE DOMAIN` at the beginning unifies timestamps with timezones for the H2 testing purposes.
```
CREATE DOMAIN IF NOT EXISTS TIMESTAMPTZ AS TIMESTAMP WITH TIME ZONE;

CREATE TABLE IF NOT EXISTS queue_log (
    message             VARCHAR(255)    NOT NULL,
    store_time          TIMESTAMPTZ     NOT NULL,
    expiry_time         TIMESTAMPTZ     NOT NULL,
    remaining_seconds   INTEGER         NOT NULL,

    PRIMARY KEY (store_time)
);
```

## Possible Clusterization

Any modern application in production should be able to maximize high availability.
The expiry queue is not different. The most common approach is to create clusters.
We can create a cluster of postgresql instances and hazelcast instances.
My experience in unmanaged clusterization of Postgresql shows that the best idea is to:
1. Load-balance requests by HAProxy and make use of
2. Patroni templates to utilize
3. Hashicorp's Consul distributed configuration store which provides configuration to
4. Multiple Postgresql server instances residing on physically different virtual machines.

Similarly, Consul can be used as distributed configuration store for Hazelcast.
There are projects like `hazelcast-consul-discovery-spi` which provides 
Consul-based discovery strategy for Hazelcast.

As for scaling the actual services, I'd create a docker container and spread the instances
among virtual machines. Again, Consul or etcd can be useful for service discovery.
Load balance of the HTTP(S) traffic to the services is best achievable by nginx http server.

Modern approaches to auto-scale make use of Kubernetes but I don't have experience with this.

As for managed approaches, I'd use AWS Fargate `https://aws.amazon.com/fargate/`.
As managed approach, it should not require so much (physical) attention as unmanaged.
However, I don't have experience with this again, so it's just a hypothetical example.
