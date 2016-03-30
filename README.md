[![Build Status](https://img.shields.io/travis/essobedo/application-manager/master.svg)](https://travis-ci.org/essobedo/application-manager)
[![License](https://img.shields.io/badge/license-LGPLv2.1-green.svg)](http://www.gnu.org/licenses/old-licenses/lgpl-2.1.en.html)
[![Javadoc](https://javadoc-emblem.rhcloud.com/doc/com.github.essobedo/application-manager/badge.svg?color=blue&prefix=v)](http://www.javadoc.io/doc/com.github.essobedo/application-manager)
[![Maven Central](https://img.shields.io/maven-central/v/com.github.essobedo/application-manager.svg)](https://maven-badges.herokuapp.com/maven-central/com.github.essobedo/application-manager)

## What is it for?

The main purpose of this project is to be able to dynamically upgrade a guest application. The guest application can be both a Java FX application or a non Java FX application.

## How to build it?

This project relies on *maven*, so you will need to install maven 3 with a JDK 8, then simply launch the famous
command *mvn clean install* and that's it!

To avoid signing the artifacts you can launch *mvn clean install -Pfast*.
To check the quality of the code, you can launch *mvn clean install -Pcheck*.

## How to launch it?

In case of a non Java FX application simply execute the command *java -jar application-manager-${version}.jar*.

In case of a Java FX application, you will need to specify the following parameters to the java packager:

|   Parameter   |                         Value                       |
| ------------- | --------------------------------------------------- |
|    *appclass* | com.github.essobedo.appma.core.Launcher             |
|    *srcfiles* | The full path of application-manager-${version}.jar |
|    *srcfiles* | appma.properties                                    |

See below an example of configuration of the java packager with maven:

```xml
<plugin>
    <groupId>org.codehaus.mojo</groupId>
    <artifactId>exec-maven-plugin</artifactId>
    <executions>
        <execution>
            <id>package-jar</id>
            <phase>package</phase>
            <goals>
                <goal>exec</goal>
            </goals>
            <configuration>
                <executable>
                    ${env.JAVA_HOME}/bin/javapackager
                </executable>
                <arguments>
                    <argument>-deploy</argument>
                    <argument>-native</argument>
                    <argument>-appclass</argument>
                    <argument>com.github.essobedo.appma.core.Launcher</argument>
                    <argument>-srcfiles</argument>
                    <argument>${settings.localRepository}/com/github/essobedo/application-manager/${version.appma}/application-manager-${version.appma}.jar</argument>
                    <argument>-srcfiles</argument>
                    <argument>appma.properties</argument>
                    <argument>-srcfiles</argument>
                    <argument>${project.build.directory}/${artifactId}-jar-with-dependencies.jar</argument>
                    <argument>-outdir</argument>
                    <argument>./target</argument>
                    <argument>-outfile</argument>
                    <argument>${project.artifactId}-app</argument>
                    <argument>-name</argument>
                    <argument>${artifactId}</argument>
                    <argument>-title</argument>
                    <argument>Hello World Project</argument>
                    <argument>-vendor</argument>
                    <argument>essobedo</argument>
                    <argument>-Bcopyright=essobedo</argument>
                </arguments>
            </configuration>
        </execution>
    </executions>
</plugin>
```

## How does it work?

### Build the ClassLoader

The application manager checks first for a file called by default *appma.properties* in the same directory as the file *application-manager-${version}.jar*.
If the file can be found, it will then get the value of the key *classpath*, in order to build the ClassLoader required to launch the guest application.

See below an example of content of this file:

```
classpath=my-application.jar;my-dependency.jar;my-root-directory-containing-dependency-classes
```

The name of the file could be modified thanks to the System parameter *essobedo.appma.core.config*.

In case the application manager cannot find the configuration file, it will check if there are jar files in the directory if so they will be used to create
the ClassLoader otherwise it will use the directory as root directory of your application assuming that there is no jar file only class files that have been
deployed directly in the directory.

### Launch the guest application

Once the ClassLoader created, the application manager relies on Service Provider Interface aka SPI to detect the application to launch. Indeed it will
look for an implementation of the interface [Manageable](https://github.com/essobedo/application-manager/blob/master/src/main/java/com/github/essobedo/appma/spi/Manageable.java)
that accepts the arguments that have been provided to the application manager. Once it finds an application that matches, it will create an instance of it then initialize it to
launch it.

### Check for update and upgrade the guest application

An implementation of Manageable can interact with the application manager in order to indicate that the application must exit but also to check for an update or to
upgrade the application to the latest version. Behind the scene, the application manager relies also on SPI to identify the implementation of the interface
[VersionManager](https://github.com/essobedo/application-manager/blob/master/src/main/java/com/github/essobedo/appma/spi/VersionManager.java) that knows how to
check for update or upgrade this particular Manageable implementation.

### Define your implementations

To define your Manageable implementation and your VersionManager implementation, you will have to create the files in *META-INF/services* called respectively
*com.github.essobedo.appma.spi.Manageable* and *com.github.essobedo.appma.spi.VersionManager* in which you will have to put the full qualified name of your
implementations.
