package br.ufrn.chat.validator;

import br.ufrn.middleware.annotations.Lifecycle;
import br.ufrn.middleware.annotations.Param;
import br.ufrn.middleware.annotations.Post;
import br.ufrn.middleware.annotations.RequestMap;
import br.ufrn.middleware.enums.CreationStrategy;
import br.ufrn.middleware.enums.ResourceStrategy;

@RequestMap(router = "/validator")
@Lifecycle(creation = CreationStrategy.STATIC, resource = ResourceStrategy.LAZY)
public class Validator {

    public Validator() {
        System.out.println("[VALIDATOR] Instância única ativada!");
    }

    @Post(router = "/validar")
    public String validarMensagem(@Param(name = "payload") String corpo) {
        System.out.println("-> Iniciando validação rigorosa do payload: [" + corpo + "]");
        
        // TODO: integrar ChatLogicProcessor futuramente
        
        return "OK";
    }
}