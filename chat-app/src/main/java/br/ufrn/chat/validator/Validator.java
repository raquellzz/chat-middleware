package br.ufrn.chat.validator;

import com.google.gson.Gson;

import br.ufrn.chat.domain.ChatMessage; // A classe de erro que criamos
import br.ufrn.middleware.annotations.Get;
import br.ufrn.middleware.annotations.Lifecycle;
import br.ufrn.middleware.annotations.Param;
import br.ufrn.middleware.annotations.Post;
import br.ufrn.middleware.annotations.RequestMap;
import br.ufrn.middleware.enums.CreationStrategy;
import br.ufrn.middleware.exceptions.RemotingException;

@RequestMap(router = "/validator")
@Lifecycle(creation = CreationStrategy.STATIC)
public class Validator {

    private final Gson gson = new Gson();

    @Post(router = "/validar")
    public String validarMensagem(@Param(name = "payload") String corpo) {
        try {
            ChatMessage chatMsg = gson.fromJson(corpo, ChatMessage.class);

            if (chatMsg.remetente() == null || chatMsg.remetente().trim().isEmpty()) {
                throw new RemotingException(400, "O remetente não pode ser vazio.");
            } 
            if (chatMsg.texto() == null || chatMsg.texto().length() > 255) {
                throw new RemotingException(400, "O texto ultrapassa o limite de 255 caracteres.");
            } 
            if (chatMsg.texto().toLowerCase().contains("palavrao")) {
                throw new RemotingException(403, "A mensagem contém conteúdo impróprio.");
            }

            return "Validação APROVADA";

        } catch (RemotingException e) {
            throw e;
        } catch (Exception e) {
            throw new RemotingException(400, "Formato JSON inválido.");
        }
    }

    @Get(router = "/ping")
    public String ping() { return "PONG"; }

}