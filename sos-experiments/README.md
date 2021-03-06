# sos-experiment
This module contains the code to run the experiment on the SOS.

## Experiments

See [Experiments.md](Experiments.md)

## Experiment Setup

An experiment is defined by a Java class and by a bunch of experiment configuration files.
The experiment class must implement that Experiment interface, which consists of five main methods.

- setupIteration
- runIteration
- finishIteration
- cleanup

Each method defines a particular phase of the experiment.
If you are writing a new experiment and extend the BaseExperiment class, then these methods will be execute as in the order above.


## Running an experiment using the ChicShock utility

The ChicShock utility can be used to distribute a SOS application to multiple nodes and run the experiment from a remote node.

The ChicShock utility has two types of methods:

- the **chic** methods distribute SOS application to remote nodes, via SSH
- the **shock** methods start the remote SOS instances


## Running an experiment locally

The experiment can also be run locally, by running its main method.


## Experiment Configuration

Each experiment needs two types of configuration files:

**configuration.json**: This file contains information on how to distribute
the application for the experiment PLUS generic information about the actual experiment

**node configurations**: These are the configuration files for the nodes to be run.
It is suggested to call the node configuration files as `node_{NODE_ID}.json`

The node where the experiment is run is configured inside of the `configuration.json` file as follows:

```json
"experimentnode": {
      "id": 0,
      "remote": false,
      "configurationfile": "node_0.json"
    },
```


If the remote option is **true**, then the **ChicShock** utility will distribute it to the specified node. The configuration will then change as follows:

```json
"experimentnode": {
      "id": 0,
      "remote": true,
      "configurationfile": "node_0.json",
      "ssh" : {
                "type" : 0,
                "host" : "cs-wifi-056.cs.st-andrews.ac.uk",
                "user" : "lorna",
                "known_hosts": "/Users/sic2/.ssh/known_hosts",
                "password" : "PASSWORD"
              }
    },
```


Note that you can specify two types of SSH connection configurations for a node.

### SSH Type 0

SSH Type 0 connections should be used when no passphrase is required. I use
this type of connection with an SSH enabled Mini-MAC.

The user should have connected to the host via SSH at least once before being able to use this configuration.

```json
"ssh" : {
        "type" : 0,
        "host" : "cs-wifi-056.cs.st-andrews.ac.uk",
        "user" : "lorna",
        "known_hosts": "/Users/sic2/.ssh/known_hosts",
        "password" : "ENCRYPTED PASSWORD"
      }
```

### SSH Type 1

SSH Type 1 connection should be used when private key with passphrase are required.

The user should have connected to the host via SSH at least once before being able to use this configuration.

```json
"ssh" : {
        "type" : 1,
        "host" : "hogun-10.cluster",
        "user" : "sic2",
        "known_hosts": "/Users/sic2/.ssh/known_hosts",
        "config": "/Users/sic2/.ssh/config",
        "privatekeypath": "/Users/sic2/.ssh/id_rsa",
        "passphrase" : "ENCRYPTED PASSPHRASE"
    }
```

### Passwords and Passphrases

As you might have noticed, when specifying the SSH configuration for a node,
you also need to provide the password/passphrase for the SSH connection.

The passwords and passphrases in the configuration files must be encrypted using a symmetric key stored in `.key`

The `KeyGenerator` utility can be used to generate such key and to generate the encrypted passkeys.

#### Generating a symmetric key

```
$ run the main in uk.ac.standrews.cs.sos.experiments.KeyGenerator
$ K to generate key and E to encrypt password/passphrase
$ K
$ cat .key
$ Tcyaw+WWIDRp9HB66UbgbQ==
```

The `.key` file is added to the `.gitignore` list and **MUST NEVER** be committed to the repository.

#### Generating the encrypted passkey

```
$ run the main in uk.ac.standrews.cs.sos.experiments.KeyGenerator
$ K to generate key and E to encrypt password/passphrase
$ E
$ Input the password/passphrase
$ ThisIsAWeakPassword
$ Encrypted key is: a6B2FS3Lu7Gff8HJ3cS/nov3LDsE5b1yniBQs+Gb9eMU2/cIrvs5wWxeFI7U9GbQrCTaIpsS+lbQ+Ks3yVrJtA==
```

### Example

In the **Experiment_Scale_1** we bootstrap one local SOS node (running the experiment) and one remote SOS node.
Thus, we need three configuration files: one for describing the experiment and two for the configuration files of the two SOS nodes.

```
|-- configurations
    |-- scale_1
        |-- configuration.json  // The configuration of the experiment
        |-- node_0.json         // The configuration for the local node
        |-- node_1.json         // The configuration for one of the remote nodes, as specified in configuration.json
```

## Generating node configurations

The `NodeGenerator` utility generates:

- The node certificate and private key
- The node GUID (based on the certificate)

The generate info is stored at the path: `sos-experiments/src/main/resources/generated_nodes/` and partially printed on the terminal.


**Remember** to add the node to the boostrap nodes of the main experiment node configuration. Example (`node_exp.json`):

```json
{
  "settings": {
    "bootstrapNodes": [
          {
            "guid" : "SHA256_16_aed7bbf1e6ef5c8d22162c096ab069b8d2056696be262551951660aac6d836ef",
            "hostname" : "192.168.1.2",
            "port" : 8080,
            "certificate" : "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBALifpBJBNiW9dzEMsm5maYC12C6UP6tKcJr0nB4UJwXj6+elfyye7KErjyXzbP9WoDDNPWNDQKBA/T4hiZgUl7sCAwEAAQ=="
          },
          {
            "guid" : "SHA256_16_14cdbb3b1154681751681ecf7f0a627cdfb858cb928a6d045befede3099fc2b4",
            "hostname" : "192.168.1.3",
            "port" : 8080,
            "certificate" : "MFwwDQYJKoZIhvcNAQEBBQADSwAwSAJBAK3GdXcbp3jOdc+LSgqj6MMDcz7GdmS4ROUM2qjKksrUFEevqcUlPN4D9oyxvd41pchSyGgvrk2eOQnO+NC+NhcCAwEAAQ=="
          }
        ]
  }
}
```

## Generating Users and Roles

The `USROGenerator` utility can be used to generate users/roles to be used for experiments.

## Instrumentation

Experiments are instrumented using the sos-instrument utility.

It is possible to control what to instrument via the experiment configuration file. All the stats are *false* by default.

```json
"stats": {
  "experiment": true, // to enable instrument calls inside sos-instrument
  "predicate": true,
  "predicate_remote": false,
  "policies": false,
  "checkPolicies": false,
  "checkPolicies": false,
  "io": false,
  "guid_data": false,
  "guid_manifest": false,
  "ping": false
}
```


## ExperimentalFramework

The ExperimentalFramework is to be used to run all the experiments where SOS nodes are remote.


## Useful CLI commands

**Killing all sos jar applications**

`for pid in $(ps -ef | grep "sos.jar" | awk '{print $2}'); do kill -9 $pid; done`


**Copy TextDatasetGenerator jar file to sif node**

- Build the TextDatasetGenerator jar first (`mvn package -DskipTests)
- Then run: `scp sos-experiments/target/text-dataset-generator.jar sif-12.cluster:~/`
- Create missing path for dictionary: `ssh sif-12.cluster 'mkdir -p ~/third-party/english-words'`
- Then run the following to transfer the dictionary: `scp third-party/english-words/words.txt sif-12.cluster:~/third-party/english-words/`

The hostname `sif-12.cluster` can be substituted with any other hostname of your choice.


## JVM Monitoring notes


* -Dcom.sun.management.jmxremote option (on jar executable) + jconsole run aside
* JMXTRANS (+ Graphite to visualize data)
* Ganglia - https://github.com/ganglia
* jvm-mon - https://github.com/ajermakovics/jvm-mon
* jmxterm - http://wiki.cyclopsgroup.org/jmxterm/


You can write a program to connect to your VM using the Attach API which would then query the MBeans (see https://stackoverflow.com/a/1195461/2467938).

Or you can expose the platform MBeanServer over RMI and query the MBeans that way.

See the java.lang.management package for more info


## cronjobs

https://crontab.guru/
