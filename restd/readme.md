== run ==
```
cd restd
mvn clean install
mvn test
mvn exec:java -Drestx.http.XForwardedSupport=all -Dpassword=mathosphere
```
where mathosphere can be any password.

The data directory (e.g. /tmp) can be specified via
```
mvn exec:java -Drestx.http.XForwardedSupport=all -Dpassword=mathosphere -Dpath=/tmp
```
otherwise mathosphere uses a default testset.