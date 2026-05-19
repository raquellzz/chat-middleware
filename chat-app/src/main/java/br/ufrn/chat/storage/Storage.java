package br.ufrn.chat.storage;

import br.ufrn.middleware.annotations.Get;
import br.ufrn.middleware.annotations.Lifecycle;
import br.ufrn.middleware.annotations.RequestMap;
import br.ufrn.middleware.enums.CreationStrategy;
import br.ufrn.middleware.enums.ResourceStrategy;

@RequestMap(router = "/storage")
@Lifecycle(creation = CreationStrategy.PER_REQUEST, resource = ResourceStrategy.POOLING)
public class Storage {

    // private final ChatDatabase database;

    public Storage() {
        // this.database = new ChatDatabase();
        System.out.println("[STORAGE] Fui criado na memória!");
    }

    @Get(router = "/ping")
    public String ping() {
        return "PONG";
    }
}