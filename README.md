# iw-hotel

export JAVA_HOME=/usr/local/opt/openjdk@17/libexec/openjdk.jdk/Contents/Home
./mvnw spring-boot:run

CREATE DATABASE villadictos_db;
CREATE USER 'villadictos'@'localhost' IDENTIFIED BY 'villadictos123';
GRANT ALL PRIVILEGES ON villadictos_db.* TO 'villadictos'@'localhost';