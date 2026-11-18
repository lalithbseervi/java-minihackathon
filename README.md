## Prerequisites
1. Java JDK 17 or greater
2. JavaFX SDK
3. MySQL Server v8.4.0 or greater
4. MySQL Connector v9.2.0

## Database Setup
Next, setup a new database and a table as described in [schema.sql](https://github.com/lalithbseervi/java-minihackathon/blob/main/schema.sql)

In the Java source code, change the following details according to your MySQL configuration:

```java
String url = "jdbc:mysql://localhost:3306/your_db_name";
String user = "your_username";
String password = "your_password";
```

Replace your_db_name, your_username, and your_password with the actual values.

## Run the Application
The project can be run using correctly configured Apache Netbeans.
The Student Registration System window will appear.
