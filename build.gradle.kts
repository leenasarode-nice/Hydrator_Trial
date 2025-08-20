import org.gradle.api.tasks.testing.logging.TestExceptionFormat

plugins {
    id("application")
    id("io.freefair.lombok") version "6.2.0"
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
    maven {
        url = uri("https://packages.confluent.io/maven/")
    }
    mavenLocal()
    maven {
        url = uri("https://nice-devops-369498121101.d.codeartifact.us-west-2.amazonaws.com/maven/cxone-maven/")
        credentials {
            username = "aws"
            password = project.properties["codeartifactToken"].toString()
        }
    }
}

dependencies {
    testImplementation("org.junit.jupiter:junit-jupiter:5.10.0")
    implementation("redis.clients:jedis:5.1.0")
    implementation("com.mysql:mysql-connector-j:8.3.0")
    implementation("software.amazon.awssdk:secretsmanager:2.26.1")
    implementation("software.amazon.awssdk:rds:2.26.1")
    implementation("software.amazon.jdbc:aws-advanced-jdbc-wrapper:2.3.5")
    testImplementation("org.apache.kafka:kafka-streams-test-utils:3.1.0")
    implementation("org.slf4j:slf4j-api:1.7.36")
    implementation("ch.qos.logback:logback-classic:1.2.10")
    implementation("org.apache.kafka:kafka-streams:3.6.1")
    implementation("io.confluent:kafka-streams-protobuf-serde:7.4.0")
    implementation("com.nice.routing:routing-state-proto:0.27.0")
    implementation("com.google.protobuf:protobuf-java:3.24.3")
    implementation("com.nice.outbound:outbound-protobuf:0.37.0")
    implementation("software.amazon.msk:aws-msk-iam-auth:1.1.5")
    implementation("software.amazon.awssdk:sqs:2.20.5")
    implementation("org.testng:testng:7.7.0")
    implementation("com.aventstack:extentreports:4.0.9")
    implementation("software.amazon.awssdk:kinesis:2.20.5")
    implementation("com.nice:findmatch-avro:0.22.5")
    implementation("com.nice.routing:findmatch-avro-serde:latest.release")
    implementation("com.nice:findmatch-conversion:0.1.0-alpha")
    implementation("com.nice.cxone.suitedata:suite-data-hdd-contracts:0.0.40")
    implementation("com.nice.cxone.suitedata:suite-data-event-contracts:0.9.24")
    implementation("com.nice.outbound:outbound-protobuf:0.37.0")

}

java {
    sourceCompatibility = JavaVersion.VERSION_17
    targetCompatibility = JavaVersion.VERSION_17
}

tasks.test {

    useTestNG {
        useDefaultListeners = true
        suites("src/test/resources/testSuite.xml")
    }

    testLogging {
        showStandardStreams = true
        exceptionFormat = TestExceptionFormat.FULL
        events("started", "passed", "skipped", "failed", "standardOut", "standardError")
    }

    systemProperties = System.getProperties().toMap() as Map<String, Any>
}