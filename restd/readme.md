This is a REST interface for the BaseX backend.

## Requirements
The [Mathosphere](https://github.com/TU-Berlin/mathosphere) project must be cloned to your repository and its submodules 
must be initialized. The [BaseX](https://github.com/TU-Berlin/mathosphere/tree/master/basex) backend must then be 
installed.

## Run
```
cd /your/path/to/mathosphere/restd
mvn clean install -DskipTests
mvn test
mvn exec:java -Drestx.http.XForwardedSupport=all -Dpassword=mathosphere
```
where mathosphere can be any password.

The data directory (e.g. /tmp) can be specified via
```
mvn exec:java -Drestx.http.XForwardedSupport=all -Dpassword=mathosphere -Dpath=/tmp
```
otherwise the BaseX search engine will use a default dataset.

An example dataset can be found in the [BaseX README](https://github.com/TU-Berlin/mathosphere/tree/master/basex).
