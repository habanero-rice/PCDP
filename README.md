==========
PCDP
==========

PCDP is a shared-memory, pedagogical, parallel programming framework. PCDP's
design, implementation, and APIs emphasize simplicity to make it straightforward
to use for programmers new to parallel programming. PCDP supports task
parallelism, loop parallelism, actor parallelism, bulk synchronization,
point-to-point synchronization, and isolation. PCDP is built on top of the Java
Fork-Join framework, but offers more convenient APIs.

Javadocs for PCDP can be accessed at:

[https://habanero-rice.github.io/PCDP/](https://habanero-rice.github.io/PCDP/)

=============================================
User Installation
=============================================

The simplest way to install PCDP is to add it as a Maven dependency to your
Maven project. You can do so by adding the following lines to your project's
pom.xml:

    <properties>
        <pcdp.version>0.0.4-SNAPSHOT</pcdp.version>
    </properties>

    <repositories>
        <repository>
            <id>pcdp-repo</id>
            <url>https://raw.github.com/habanero-maven/hjlib-maven-repo/mvn-repo-pcdp-${pcdp.version}/</url>
            <snapshots>
                <enabled>true</enabled>
                <updatePolicy>always</updatePolicy>
            </snapshots>
        </repository>
    </repositories>

    <dependencies>
        <dependency>
            <groupId>edu.rice.pcdp</groupId>
            <artifactId>pcdp-core</artifactId>
            <version>${pcdp.version}</version>
        </dependency>
    </dependencies>


=============================================
Developer Installation
=============================================

It is also straightforward to install PCDP from source using Maven:

    $ git clone <this-repo> PCDP
    $ cd PCDP
    $ mvn install

The above steps will place the PCDP JAR under ./target/.
