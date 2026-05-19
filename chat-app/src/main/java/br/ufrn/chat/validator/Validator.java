package br.ufrn.chat.validator;

import br.ufrn.middleware.annotations.Param;
import br.ufrn.middleware.annotations.Post;
import br.ufrn.middleware.annotations.RequestMap;

@RequestMap(router = "/validator")
public class Validator {

    @Post(router = "/validar")
    public String validarMensagem(@Param(name = "payload") String corpoMensagem) {
        System.out.println("-> Iniciando validação do payload: [" + corpoMensagem + "]");
        
        
        return "OK";
    }
}