# DistributedSystems
Repo for lab sessions for distributed systems of the master Informatica

## Students
Jules Verbessen
Gert-Jan Gillis

## Run
- Run emulator: in assignment2: firebase emulators:start --project demo-distributed-systems-kul
- Run backend: In IntelliJ, you can open the Maven tool in the sidebar and double-click on the spring-boot:run goal under Plugins.
- Open app:  http://localhost:8080
- Open emulator: http://localhost:8081/
### Assign roles
- ![image](https://github.com/juleje/DistributedSystems/assets/146711917/c94d84ab-3fe4-4cd3-a2eb-b4c29af9f437)
- ![image](https://github.com/juleje/DistributedSystems/assets/146711917/2c9222e7-c8b2-4d1e-940e-e8cd0bbe254c)
- Custom claims (optional) add {"roles":"manager"}
- Remember to save
### Make Collection
- ![image](https://github.com/juleje/DistributedSystems/assets/146711917/41245bb5-d431-4f18-8b06-554495de50cf)
- start collection: bookings
- geef de collection de velden: id (string), time (timestamp), customer (string)
- geef de velden een waarden zodat er een test booking is
- geef het booking document een nieuwe collection tickets
- geef de collection tickets de velden trainCompany (string), trainId (string), seatId (string), ticketId (string), customer (string), bookingReference (string)
- geef  de velden een waarden zodat er een test ticket is

## login
normal user: 
- username: test@mail.be
- password: usertest

admin user (not made yet):
- username: admin@mail.be
- password: admintest

## Questions
- best customer is with most bookings or most tickets?
- getBestCustomerS is the most? all of them sorted? of one or more with the same amount?
- authenticatie: user inloggen maar bestaat nog ni -> automatisch sign up. Maar geeft iedereen toegang. wilt dat enkel toegang geeft aan bestaande users?
