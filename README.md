# Simple bank app

This is simple java app to transfer money from one account to another.
It has rest API and a DB in a local file. it's not a production code, just the way to show you can write simple apps in java
without using Spring or other monster frameworks.

## Start App
Start via maven or just import a project into IDE and start from there:
```
mvn exec:java
```

This will start a local webserver. To access list of available API go to:
http://localhost:8080/api

### Examples of API
* **/api/accounts** - list of all available accounts with their balances and ids
* **/api/account?id=1** - get an information about account with id equals to 1
* **/api/account/add?balance=500** - create a new account with the balance of 500 or with balance=0 if not specified
* **/api/account/send?sender=1&reciever=2&amount=30** -  send 30 from account with id 1 to account with id 2
* **//accounts/clean** - clean all the accounts
## Run tests
```
mvn test
```

## Features
1. Create an account
2. List all accounts
3. Trasfer money from one account to another

## Known issues / to be done
1. Double is used for money - for simplicity. It's a bad practise in production as it's not an "approximate" number. In production
should use a specific type supported by your DB or store integer and fractional parts as separate fields and implement logic for arithmetic.
2. Not much parameters verification and error messaging. If you pass a String instead of Double, the request will be just ignored.
3. No performance/stress testing. Only test for multithreading update.
4. RestAPI is not covered by automated testing Tested from browser console.
You can test this from the browser by running the following JS from console:
```javascript
//create 200 accounts with 100 balance
for(var i =1;i<=200;i++){fetch("http://localhost:8080/api/account/add?balance=100")};
```
```javascript
//transfer 1 from the first account to all others
for(var i =2;i<=200;i++){
    var query = `http://localhost:8080/api/account/send?sender=1&reciever=${i}&amount=1`;
    fetch(query);
};
```

4. .....
