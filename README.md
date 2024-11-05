# Giphy Picker based on java-getting-started

This is a simple Java app, which can easily be deployed to Heroku.

This application supports the [Getting Started with Java on Heroku](https://devcenter.heroku.com/articles/getting-started-with-java) article - check it out.


## It's Running on Heroku

The app is running on [https://shrouded-hollows-81863.herokuapp.com/](https://shrouded-hollows-81863.herokuapp.com/).


## Running Locally

Make sure you have Java and Maven installed.  Also, install the [Heroku CLI](https://cli.heroku.com/).

```sh
$ git clone https://github.com/dbaronster/giphy.git
$ cd giphy
$ mvn install
$ heroku local:start
```

Your app should now be running on [localhost:5000](http://localhost:5000/).

To enable the database, ensure you have a local `.env` file that reads something like this:

```
JDBC_DATABASE_URL=jdbc:postgresql://localhost:5432/java_database_name
```

## Implementation Notes

I chose to use Heroku for rapid development, but it turned out to be anything but... I haven't been doing much front end development recently, and never a Thymeleaf project. That set me way back, not focusing on the data until the last minute - too late in the process. With Spring security in place, I can't get past the login form. There is a circular dependency on Datasource that is bringing it down. Even beyond that, the form to select a favorite selection does not work (Thymeleaf issue).
With all of these flaws, I hope we still have something to discuss!

## Heroku Documentation

For more information about using Java on Heroku, see these Dev Center articles:

- [Java on Heroku](https://devcenter.heroku.com/categories/java)
