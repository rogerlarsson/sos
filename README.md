# SOS - Sea of Stuff

This is a prototype of a distributed autonomic personal data storage system.

```
      ___           ___           ___
     /  /\         /  /\         /  /\
    /  /:/_       /  /::\       /  /:/_
   /  /:/ /\     /  /:/\:\     /  /:/ /\
  /  /:/ /::\   /  /:/  \:\   /  /:/ /::\
 /__/:/ /:/\:\ /__/:/ \__\:\ /__/:/ /:/\:\
 \  \:\/:/~/:/ \  \:\ /  /:/ \  \:\/:/~/:/
  \  \::/ /:/   \  \:\  /:/   \  \::/ /:/
   \__\/ /:/     \  \:\/:/     \__\/ /:/
     /__/:/       \  \::/        /__/:/
     \__\/         \__\/         \__\/
```

### Repo Organisation

```
|-- sos
    |-- docs                // Webpage for this project
    |-- sos-core            // The core of the SOS
    |-- sos-rest            // REST interface for the SOS
    |-- sos-rest-jetty      // Jetty server
    |-- sos-filesystem      // File system used for the WebDAV server
    |-- web-ui              // Web UI for the SOS
    |-- sos-app             // Basic application to run a SOS node (with webui and WebDAV)
    |-- sos-web-archive     // Example of an application using the SOS
    |-- git-to-sos          // Utility that converts a git repository into SOS content
    |-- sos-experiments     // Code with configurations files for the experiments
    |-- experiments         // Scripts to analyse experiments results
                            // + datasets and contexts used for the experiments
                            // + Results are written here, under the output (local) or remote (distributed exp) folders
    |-- sos-instrument      // Instrumentation code. Useful to get results for the experiments.
    |-- scripts             // A bunch of useful scripts
    |-- README.md           // This README file
```


## sos-core

The sos-core module contains the code to manage a SOS node and with it:
- create and manage manifests
- manage metadata and contexts
- manage the SOS services (agent, storage, nodeDiscoveryService, dataDiscoveryService, metadataService, etc...)

### SOS Model

The SOS model consists of the following parts:

- Data model
- Metadata model
- Users and Roles
- Context model
- Node model

All the elements of the SOS model can be represented as manifests and they are identied by a unique ID (GUID).

![SOS Model](docs/images/SOS-model.png)

Example of contexts in JSON formats can be found [here](sos-core/src/main/java/uk/ac/standrews/cs/sos/impl/context/README.md).

### SOS Architecture

Each SOS node consists of a collection of services that manage specific aspects of the node itself and its behaviour.

The services are:

- agent
- storage
- data discovery
- node discovery
- metadata
- user and role
- context

Each service, except for the agent one, can be exposed to the outside world by running a REST server.
You should set the service to be exposed in the node configuration:

```json
"services": {
      "context": {
        "exposed": false
      },
      "storage": {
        "exposed": true
      },
      ...
}
```

The SOS_APP (see the **sos-app** module) should be run with the following parameters: `java -jar sos.jar -c CONFIGURATION -j`,
with the `-j` option enabling the Jetty REST server.


## SOS Modules

In this section we provide a brief insight to some of the modules of the SOS project.

### sos-rest

The sos-rest project defines the REST API. This is server-agnostic.
We provide a server implementation on top of the jersey REST API (see the sos-rest-jetty module).


### sos-filesystem

The sos-filesystem is a very basic example of how the SOS model can be mapped to a real world application.

The mapping used is the following:

- file :: version manifest -> atom manifest -> atom data
- directory :: version manifest -> compound manifest


The sos-filesystem is used in the sos-app. Here, the filesystem is passed to a WebDAV server (https://github.com/stacs-srg/WebDAV-server) and the WebUI project.
The WebDAV server exposes the sos-fs to the OS as well as to any other application that wishes to interact with the SOS.


## Applications

### WebDAV

This is a WebDAV server running on top of the SOS. The content provided by the WebDAV uses the structure defined by the *sos-filesystem*.

### Web archive

This is a very very simple web crawler that added web content to the SOS. 
Plus this application includes a tiny server that mocks "the internet" and provides what is crawled through the browser.

### web-ui

The web-ui exposes the sos-filesystem, similarly to the WebDAV server.
However, here we are not constrained by the WebDAV protocol, thus we are able to demonstrate additional features of the SOS.

### git-to-sos

WIP

###  DNS over SOS

WIP

## Running a SOS node via the SOS-APP

### Packaging

```bash
$ mvn package # or `mvn package -DskipTests` to skip the tests during the packaging process
$ mv target/app-1.0-SNAPSHOT.jar sos.jar
$ java -jar sos.jar -c configuration.conf ARGS
```

### Running multiple nodes

You can bootstrap multiple nodes using the `experiments.sh` bash script (see script folder)

You can also find other useful bash scripts in the script folder.


## Experiments

The code for the experiments is found under `sos-experiments/`. Read the relevant [README](sos-experiments/README.md) for more info.

The experiments results are located under `experiments/output`.


## SOS Internals

When a SOS node is instantiated, the following directory structure is created.

```
|-- sos
    |-- context         // Contains
    |-- data            // Atoms (clear and protected)
    |-- keys            // Keys for Users and Roles
                        // Users have Private Key and Certificate for Digital Signature (.key, .crt)
                        // Roles have Private/Public Asymmetric keys for data protection (.pem, _pub.pem)
                        //       + Digital Signature keys/cert as for the User
    |-- manifests       // Manifests for Atoms, Compounds, Versions and Metadata in JSON
    |-- usro            // Manifests for Users and Roles in JSON
    |-- node            // Indices, caches, dbs for the node
```


## SOS Node Configuration

The configuration of a SOS node is specified using a simple JSON structure.

### Example

```json
{
  "settings": {
    "services": {
      "storage": {
        "exposed": true,
        "canPersist": true,
        "maxReplication": 3
      },
      "cms": {
        "exposed": false,
        "automatic": true,
        "predicateThread": {
          "initialDelay": 10,
          "period": 20

        },
        "policiesThread": {
          "initialDelay": 15,
          "period": 20
        },
        "checkPoliciesThread": {
          "initialDelay": 100,
          "period": 60
        },
        "getdataThread": {
          "initialDelay": 60,
          "period": 60
        },
        "spawnThread": {
          "initialDelay": 90,
          "period": 120
        }
      },
      "mds": {
        "exposed": true,
        "maxReplication": 3
      },
      "rms": {
        "exposed": false
      },
      "nds": {
        "exposed": false,
        "startupRegistration": true,
        "bootstrap" : true,
        "ping": true
      },
      "mms": {
        "exposed": false
      }
    },
    "database": {
      "filename": "node.db"
    },
    "rest": {
      "port": 8080
    },
    "webDAV": {
      "port": 8081
    },
    "webAPP": {
      "port": 8082
    },
    "keys": {
      "location": "~/sos/keys/"
    },
    "store": {
      "type": "local",
      "location": "~/sos/"
    },
    "global": {
      "ssl_trust_store" : "PATH TO THE JAVA SECURITY CACERTS",
      "tasks": {
        "thread": {
          "ps": 4
        }
      },
      "nodeMaintainer": {
        "enabled": true,
        "maxSize": 1048576,
        "thread": {
          "ps": 1,
          "initialDelay": 60,
          "period": 60
        }
      }
    },
    "bootstrapNodes": [
      {
      "guid" : "SHA256_16_bb077f9420219e99bf776a7a116334405a81d2627bd4f87288259607f05d1615",
      "hostname" : "138.251.207.87",
      "port" : 8080,
      "certificate" : "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAKZOnoFAxsx4BiXBKzeJISOv5q5XTSpPZRCmYGg+59VctY1xeYS7NEkEmbk/Sa8y5chrZttN5CggdBJBIFGgMU0CAwEAAQ=="
      }
    ]
  }
}
```


### Finding the Java cacerts path

The cacerts file is a collection of trusted certificate authority (CA) certificates.
Sun Microsystems™ includes a cacerts file with its SSL support in the Java™ Secure Socket Extension (JSSE) tool kit and JDK 1.4.x.
It contains certificate references for well-known Certificate authorities, such as VeriSign™.

The cacerts file is needed to allow the node to make HTTPS (HTTP with SSL) requests.

#### Linux

`$(readlink -f /usr/bin/java | sed "s:bin/java::")lib/security/cacerts`

#### MacOSX

`$(/usr/libexec/java_home)/jre/lib/security/cacerts`



## More stuff

### Headless Tika

The `sos-core` modules used the Apache Tika utility to extract useful metadata from the atoms.
The Tika utility may make some calls to the `java.awt` package, so if you want to run a sos node in headless more, you will have
to specify the following parameter: `-Djava.awt.headless=true`.

### Logging

The logs are automatically written under the `logs/` folder.

The SOS application uses the log4j logger and changing the logs configuration is as straightforward
as providing a new `log4j.properties` file. To explicitly instruct the SOS application to use a non-default properties file, you must
add this parameter when running the java app:

`-Dlog4j.configuration=file:/path/to/log4j.properties`

**Note** that the property value must be a valid URL.


#### Example of a log4j.properties for both file and stdout

```
log4j.rootLogger=file,console
log4j.appender.console=org.apache.log4j.ConsoleAppender
log4j.appender.file=org.apache.log4j.RollingFileAppender

# http://stackoverflow.com/a/4953207/2467938
# -Dlogfile.name={logfile}
log4j.appender.file.File=${logfile.name}
log4j.appender.file.ImmediateFlush=true
log4j.appender.file.Threshold=debug
log4j.appender.file.MaxBackupIndex=10
log4j.appender.file.MaxFileSize=10MB

log4j.appender.file.layout=org.apache.log4j.PatternLayout
log4j.appender.file.layout.ConversionPattern=%d{dd-MM-yyyy HH:mm:ss} [ %-5p ] -  %c %x ( %-4r [%t] ) ==> %m%n

log4j.appender.console.layout=org.apache.log4j.PatternLayout
log4j.appender.console.layout.ConversionPattern=%d{dd-MM-yyyy HH:mm:ss} [ %-5p ] -  %c %x ( %-4r [%t] ) ==> %m%n
```


## Useful tools and commands

- Online JSON Linter (and more) - https://jsoncompare.com/#!/simple/
- Online hex dump inspector - https://hexed.it/
- File to SHA values - https://md5file.com/calculator
- Hash Online - https://quickhash.com
- File leak detector - http://file-leak-detector.kohsuke.org/

- cloc - https://github.com/AlDanial/cloc
    - `cloc . --exclude-dir=datasets,contexts,plots,processed,remote,usro,configuration,target,logs,output`


- List of opened resources: `lsof -p pid`
- File leak detector (https://github.com/kohsuke/file-leak-detector)
    - Build the sos-app jar first with `mvn package -DskipTests`
    - `java -javaagent:third-party/file-leak-detector/file-leak-detector-1.10-jar-with-dependencies.jar=http=19999,strong -Djava.awt.headless=true -jar sos-app/target/sos-app.jar -c example_config.json -j`

## Contributors

This work is developed by Simone Ivan Conte ([@sic2](https://github.com/sic2)) as part of his PhD thesis.

Simone is supervised by Prof. Alan Dearle and Dr. Graham Kirby from the University of St Andrews.
