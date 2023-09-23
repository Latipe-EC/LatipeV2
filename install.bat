@echo off

call terminate.bat
:: Set the path to the Java executable
set JAVA_HOME=C:\Program Files\Java\jdk1.8.0_291
set PATH=%JAVA_HOME%\bin;%PATH%

:: Set the path to the JAR files
start java -jar discovery-server/target/discovery-server-1.0.0-SNAPSHOT.jar
start java -jar api-gateway/target/api-gateway-1.0.0-SNAPSHOT.jar
start java -jar auth/target/auth-1.0.0-SNAPSHOT.jar
start java -jar cart/target/cart-1.0.0-SNAPSHOT.jar
start java -jar payment/target/payment-1.0.0-SNAPSHOT.jar
start java -jar product/target/product-1.0.0-SNAPSHOT.jar
start java -jar user/target/user-1.0.0-SNAPSHOT.jar
start java -jar store/target/store-1.0.0-SNAPSHOT.jar
start java -jar search/target/search-1.0.0-SNAPSHOT.jar
start java -jar media/target/media-1.0.0-SNAPSHOT.jar

:: Wait for all processes to finish
timeout /t 5