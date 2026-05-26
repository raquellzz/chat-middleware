package br.ufrn.chat.storage;

import br.ufrn.middleware.annotations.Get;
import br.ufrn.middleware.annotations.Lifecycle;
import br.ufrn.middleware.annotations.Param;
import br.ufrn.middleware.annotations.Post;
import br.ufrn.middleware.annotations.RequestMap;
import br.ufrn.middleware.enums.CreationStrategy;
import br.ufrn.middleware.enums.ResourceStrategy;

@RequestMap(router = "/storage")
@Lifecycle(creation = CreationStrategy.PER_REQUEST, resource = ResourceStrategy.POOLING)
public class Storage {

    private static final ChatDatabase database = new ChatDatabase();
    private static final GenerationState generationState = new GenerationState(database);

    @Post(router = "/salvar")
    public String salvar(@Param(name = "geracao") int geracao, @Param(name = "payload") String payloadJson) {
        
        boolean sucesso = generationState.processMessage(geracao, payloadJson);
        
        if (sucesso) {
            return "Mensagem processada com sucesso na geração " + geracao;
        } else {
            throw new RuntimeException("Conflito de Geração. Rejeitado.");
        }
    }

    @Get(router = "/listar")
    public String listar(@Param(name = "sala") String sala) {
        return database.listarMensagensDaSala(sala);
    }

    @Get(router = "/ping")
    public String ping() { return "PONG"; }
}