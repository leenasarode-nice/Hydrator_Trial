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
            password = "eyJ2ZXIiOjEsImlzdSI6MTc1NTY3MDEyMywiZW5jIjoiQTEyOEdDTSIsInRhZyI6IlhyQnJHY2gzQ1Q0cHZtZXdTNTRMLWciLCJleHAiOjE3NTU3MTMzMjMsImFsZyI6IkExMjhHQ01LVyIsIml2IjoiR2UwV1Vtak1KWHVtMjEtVCJ9.EeNL3tZI4sPVUU-dTjVksQ.D54MCCBIHyfAqkdd.WKgzFNK3vjEC23bibBzGF2cvSzTVHoqICV5hP9_eJIpp7E6MwYotOkDoIqhtffPTTa-IQFi4Lil6LqbzAdjyhzwzpwIdzQOiAHuv6PCBW4SZqzepKZ5Kfdr9Pt-Yy21TObbn4azahXR2fPrvwuBP8UgNSp-CDyOd5eKQnyqrOGqo0K75HKfsDG7PgVAalO8Lj3cUpVjNrbeFC_7Vm0Pcd4eyLFO3rd8EC654m-Bv9AlCmAcBPRtkYN9aKRuMZiQfosJWwQFFaPTLJPxZwvJ0Gv59cK1TW-LxazebodoIZrgRElvhx-g6ZgxSAmHvxmpu29ELnwvJRl-FsTC7Wnbvhj5szs9hkCeKVXPgvipn12a5-DD_gD1fsw-MA-l8iIBFCvE6PErvLKwVM6Yp3osm3giaEEuB6UhhqByJ5v84_xJOY6__oRgYBtpKQTQnTWgN6SIJPcRHUCjeJdFX1-zU1l_eOWJYzBhX6Br81D7rTU_xrwZheFBPFW7ZwzOl8RCx6BNrTZPEW4mitac5GUer53aX7G_QOmPmT-bhx-R2E65JSE0RYVEEbUzRBWsKn0D1TamKR7QOVC1GYTg7IixVS4YZgOccFmeLfnvJZLI-neykvkJxE8V7bLApySDUutxz2PxmeCOQKqxFNnosGJHvSnlHCs8TWDahIsgW5UtPBywu1ogWPkl6tLGieKThxQUh7rguMriXKTh2tWKlx7DK3D_XhKlPCLjBpvxOLxe6EfNKiHHktm-gxagjZ-sfa-0kI49rmWsk9sRrpOOa5QZ9q2uL5Okysc9S5oSWY1_PDu0VJA64PEgjIC2LUYxkAeAZB26cwvjdgixXxRCVJa02NTfGuQ8OzOytdLYMZMtrqAe0dv9p2AtRNCE-ZHq84EQ0OPqKmi_hYy4vMDP8KPew0Zs6k9CckfBi0EOEzAu0XM6ZsbTGsymbLunDBUZe2Y32KqIteEKr8RhqN9Tq8INMu0xp9RAq29OSa9HY2U5tdIemaFVfGLCchjCoWUW5NBT2f_M-iFA7Y4Q9zD0Ud-ZC3socbmTbIZh7qLxsF_6EtwrnISv8w7xFhH5Bxsp-zPLsUWU8rj7M0Lbcbvzr8efDqK-j3lFcGAeI3XDmRiNcAF3BUNIWc3_ewCx4kqwbum7J7o4HWIezXYZEnkt6wlkU0dvrKAcgpizQEzkycVM22c4GVP7Im1uVKyDG5STY3N81xz04Qxz4bsmnblMnpMTxtY6M5D3dLJeI9XlUiM5Ac035b3gIWyvuLrx-YA.0VZRZ6MBplyfJTNV4HuWoA"
//                project.properties["codeartifactToken"].toString()
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