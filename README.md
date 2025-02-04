# DNetTickets
DNetTickets is an application developed for the Distributed Systems course in the Master's program in Informatics. It simulates the functionality of the NMBS website for purchasing train tickets.

## Project Overview
This project is divided into two parts:
- Fundamentals – Learning core distributed system concepts such as: Java RMI, OpenAPI, REST
- DNetTickets Application – Building a microservice-based distributed system, covering:
  - Java Spring Boot for backend development
  - Pub/Sub for indirect communication
  - NoSQL Firestore database
  - Role-based access control with Firebase
  - Deployment with Google Cloud Engine and autoscaling 

This project provides hands-on experience with designing, developing, and deploying distributed systems in a cloud environment.

## Run
- Run emulator: in assignment2: firebase emulators:start --project demo-distributed-systems-kul
- Run backend: In IntelliJ, you can open the Maven tool in the sidebar and double-click on the spring-boot:run goal under Plugins.
- Open app:  http://localhost:8080
- Open emulator: http://localhost:8081/
### Assign roles
- login met admin user
- ![image](https://github.com/juleje/DistributedSystems/assets/146711917/c94d84ab-3fe4-4cd3-a2eb-b4c29af9f437)
- ![image](https://github.com/juleje/DistributedSystems/assets/146711917/2c9222e7-c8b2-4d1e-940e-e8cd0bbe254c)
- Custom claims (optional) add {"roles":"manager"}
- Remember to save
### Make Collection
- ![image](https://github.com/juleje/DistributedSystems/assets/146711917/41245bb5-d431-4f18-8b06-554495de50cf)
- start collection: bookings
- verwijder het automatisch aangemaakt document

## login
normal user: 
- username: test@mail.be
- password: usertest

admin user :
- username: admin@mail.be
- password: admintest

