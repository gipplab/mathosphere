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

## Deploy
At first you have to set up a server in your local maven settings file  ~/.m2/settings.xml.
For example
```XML
<?xml version="1

.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0
                      http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <servers>
        <server>
            <id>mathosphere</id>
            <username>admin</username>
            <password>admin</password>
        </server>
    </servers>
</settings>
```
The same user password combination needs to be set in your tomcat-user file and must have the
to `manager-script` role.
e.g.
```XML
<user username="admin" password="admin" roles="admin-gui,manager-gui,manager-script" />
```
Afterwards you can start the server.
```
cd /your/path/to/mathosphere/restd
mvn clean install -DskipTests
mvn tomcat7:deploy 
mvn tomcat7:redeploy
```

## Test
Navigate to
http://HOST:PORT(/restd?)/api/@/ui/api-docs/
The default password is mathosphere.

by default url is: 
http://127.0.0.1:10043/api/@/ui/