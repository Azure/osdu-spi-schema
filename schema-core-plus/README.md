# Schema Service

schema-core-plus is a Maven multi-module project service.

## Getting Started

These instructions will get you a copy of the project up and running on your local machine for development and testing purposes. See deployment for notes on how to deploy the project on a live system.

### Prerequisites (Infra and access required)

Pre-requisites

* GCloud SDK with java (latest version)
* JDK 25
* Lombok 1.18.40 or later
* [Maven 3.9.16+](https://maven.apache.org/download.cgi)

### Lettuce runtime dependency

`os-core-common` declares `lettuce-core` as **provided**. Without an explicit dependency, startup fails with `ClassNotFoundException: io.lettuce.core.codec.RedisCodec` when creating `gsonCodecFactory`. `schema-core-plus` therefore declares `lettuce-core` (`${lettuce.version}`) so Redis cache codecs resolve at runtime.

### Installation

### Service Configuration

#### Baremetal

[Baremetal service configuration](docs/baremetal/README.md)


### Run Locally

Check that maven is installed:

```bash
$ mvn --version
Apache Maven 3.9.16
Maven home: /usr/share/maven
Java version: 25.0.4
...
```

You may need to configure access to the remote maven repository that holds the OSDU dependencies. This file should live within `~/.mvn/community-maven.settings.xml`:

```bash
$ cat ~/.m2/settings.xml
<?xml version="1.0" encoding="UTF-8"?>
<settings xmlns="http://maven.apache.org/SETTINGS/1.0.0"
          xmlns:xsi="http://www.w3.org/2001/XMLSchema-instance"
          xsi:schemaLocation="http://maven.apache.org/SETTINGS/1.0.0 http://maven.apache.org/xsd/settings-1.0.0.xsd">
    <servers>
        <server>
            <id>community-maven-via-private-token</id>
            <!-- Treat this auth token like a password. Do not share it with anyone, including Microsoft support. -->
            <!-- The generated token expires on or before 11/14/2019 -->
             <configuration>
              <httpHeaders>
                  <property>
                      <name>Private-Token</name>
                      <value>${env.COMMUNITY_MAVEN_TOKEN}</value>
                  </property>
              </httpHeaders>
             </configuration>
        </server>
    </servers>
</settings>
```

Once the above Prerequisite are done, we can follow the below steps to run the service locally,

1. Navigate to the root of the schema project, os-schema. For building the project using command line, run below command :

    ```bash
    mvn clean install
    ```

    This will build the core project as well as all the underlying projects. If we want  to build projects for specific cloud vendor, we can use mvn --projects command. For example, if we want to build only for Google Cloud, we can use below command :

    ```bash
    mvn --projects schema-core,schema-core-plus clean install
    ```

2. Run schema service in command line.  


Download pluggable OSDU Drivers:
```bash
    - mvn dependency:copy -DrepoUrl=$OSM_PACKAGE_REGISTRY_URL -Dartifact="org.opengroup.osdu:os-osm-postgres:$OSM_VERSION:jar:plugin" -Dtransitive=false -DoutputDirectory="./tmp"
    - mvn dependency:copy -DrepoUrl=$OBM_PACKAGE_REGISTRY_URL -Dartifact="org.opengroup.osdu:os-obm-s3:$OBM_VERSION:jar:plugin" -Dtransitive=false -DoutputDirectory="./tmp"
    - mvn dependency:copy -DrepoUrl=$OQM_PACKAGE_REGISRTY_URL -Dartifact="org.opengroup.osdu:os-oqm-rabbitmq:$OQM_VERSION:jar:plugin" -Dtransitive=false -DoutputDirectory="./tmp"
```
Select schema-core-plus to run :
```bash
    java --add-opens java.base/java.lang=ALL-UNNAMED \
         --add-opens java.base/java.lang.reflect=ALL-UNNAMED \
         -Djava.security.egd=file:/dev/./urandom \
         -Dserver.port=${PORT} \
         -Dlog4j.formatMsgNoLookups=true \
         -Dloader.path=plugins/ \
         -Dloader.debug=true \
         -Dloader.main=org.opengroup.osdu.schema.CorePlusSchemaApplication \
         -jar schema-core-plus-0.25.0-SNAPSHOT-spring-boot.jar 
```
   
3. The port and path for the service endpoint can be configured in ```application.properties``` in the provider folder as following. If not specified, then  the web container (ex. Tomcat) default is used:

    ```bash
    server.servlet.contextPath=/api/schema-service/v1/
    server.port=8080
   ```
   
You can access the service APIs by following the service contract in [schema.yaml](docs/api/schema.yaml)

## Testing

#### Baremetal

[Baremetal Testing](docs/baremetal/README.md)

## License

Copyright © Google LLC

Copyright © EPAM Systems

Licensed under the Apache License, Version 2.0 (the "License");
you may not use this file except in compliance with the License.
You may obtain a copy of the License at

[http://www.apache.org/licenses/LICENSE-2.0](http://www.apache.org/licenses/LICENSE-2.0)

Unless required by applicable law or agreed to in writing, software
distributed under the License is distributed on an "AS IS" BASIS,
WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
See the License for the specific language governing permissions and
limitations under the License.
