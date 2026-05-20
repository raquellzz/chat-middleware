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

    // private final ChatDatabase database;

    public Storage() {
        System.out.println("[STORAGE] Instância alocada no Pool!");
    }

    @Get(router = "/ping")
    public String ping() {
        return "PONG";
    }

    @Post(router = "/salvar")
    public String salvar(@Param(name = "geracao") int geracaoDaMensagem, @Param(name = "payload") String payloadJson) {
        
        System.out.println("[STORAGE] Pedido de salvamento recebido -> Geração: " + geracaoDaMensagem);
        System.out.println("[STORAGE] Payload: " + payloadJson);
        
        // No futuro: boolean sucesso = generationState.processMessage(geracaoDaMensagem, payloadJson);
        // Se sucesso == false, você fará: throw new RuntimeException("Conflito de Geração");
        
        return "Mensagem salva com sucesso";
    }

    @Get(router = "/listar")
    public String listar(@Param(name = "sala") String salaSolicitada) {
        System.out.println("[STORAGE] Buscando histórico da sala: " + salaSolicitada);
        
        // No futuro: return database.listarMensagensDaSala(salaSolicitada);
        return "[ { \"msg\": \"Dados simulados da sala " + salaSolicitada + "\" } ]";
    }
}