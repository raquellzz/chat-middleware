# Terminal 1:
java -jar chat-app/target/chat-app-1.0-SNAPSHOT-jar-with-dependencies.jar

# Terminal 2:
java -cp chat-app/target/chat-app-1.0-SNAPSHOT-jar-with-dependencies.jar br.ufrn.chat.ChatMain STORAGE_1 8081

# Terminal 3:
java -cp chat-app/target/chat-app-1.0-SNAPSHOT-jar-with-dependencies.jar br.ufrn.chat.ChatMain STORAGE_2 8082


Terminal 1: 

java -cp chat-app/target/chat-app-1.0-SNAPSHOT-jar-with-dependencies.jar br.ufrn.chat.gateway.GatewayMain UDP

Terminal 2: 

java -cp chat-app/target/chat-app-1.0-SNAPSHOT-jar-with-dependencies.jar br.ufrn.chat.storage.StorageMain STORAGE_1 8081 UDP

Terminal 3: 

java -cp chat-app/target/chat-app-1.0-SNAPSHOT-jar-with-dependencies.jar br.ufrn.chat.storage.StorageMain STORAGE_2 8082 UDP

Terminal 4: 

java -cp chat-app/target/chat-app-1.0-SNAPSHOT-jar-with-dependencies.jar br.ufrn.chat.validator.ValidatorMain VALIDATOR_1 8083 UDP

Terminal 5: 

java -cp chat-app/target/chat-app-1.0-SNAPSHOT-jar-with-dependencies.jar br.ufrn.chat.validator.ValidatorMain VALIDATOR_2 8084 UDP