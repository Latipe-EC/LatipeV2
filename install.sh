#!/bin/bash

./terminate.sh
# Set the path to the Java executable
JAVA_HOME=/usr/bin/java
export PATH=$JAVA_HOME/bin:$PATH

# Set the path to the JAR files
java -jar discovery-server/target/discovery-server-1.0.0-SNAPSHOT.jar &
java -jar api-gateway/target/api-gateway-1.0.0-SNAPSHOT.jar &
java -jar auth/target/auth-1.0.0-SNAPSHOT.jar &
java -jar cart/target/cart-1.0.0-SNAPSHOT.jar &
java -jar payment/target/payment-1.0.0-SNAPSHOT.jar &
java -jar product/target/product-1.0.0-SNAPSHOT.jar &
java -jar user/target/user-1.0.0-SNAPSHOT.jar &
java -jar store/target/store-1.0.0-SNAPSHOT.jar &
java -jar search/target/search-1.0.0-SNAPSHOT.jar &
java -jar media/target/media-1.0.0-SNAPSHOT.jar &

# Wait for all processes to finish
wait