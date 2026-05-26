    package br.ufrn.chat.storage;

    import java.util.ArrayList;
    import java.util.List;
    import java.util.Map;
    import java.util.concurrent.ConcurrentHashMap;
    import java.util.concurrent.CopyOnWriteArrayList;

    import com.google.gson.Gson;

    import br.ufrn.chat.domain.ChatMessage;

    public class ChatDatabase {
        private final Map<String, List<ChatMessage>> salasDeChat = new ConcurrentHashMap<>();
        private final Gson gson = new Gson();

        public void salvarMensagem(String payloadJson) {
            try {
                ChatMessage msg = gson.fromJson(payloadJson, ChatMessage.class);
                
                salasDeChat.computeIfAbsent(msg.sala(), k -> new CopyOnWriteArrayList<>())
                        .add(msg);
                
                System.out.println("[BANCO] Salvo na sala [" + msg.sala() + "] -> " + 
                                msg.remetente() + ": " + msg.texto());
                                
            } catch (Exception e) {
                System.err.println("[BANCO] Erro ao tentar salvar JSON corrompido: " + payloadJson);
            }
        }

        public String listarMensagensDaSala(String sala) {
        if (sala == null || sala.trim().isEmpty()) {
            System.err.println("[BANCO DE DADOS] Erro: Sala consultada é nula.");
            return "[]"; 
        }
        
        List<ChatMessage> mensagensOriginais = salasDeChat.getOrDefault(sala, new CopyOnWriteArrayList<>());
    
        System.out.println("[BANCO DE DADOS] Consulta recebida para a sala: [" + sala + "]");
        System.out.println("[BANCO DE DADOS] Total de mensagens retornadas: " + mensagensOriginais.size());
            
        
        if (mensagensOriginais.isEmpty()) {
            return "[]";
        }
        
        List<ChatMessage> snapshotSeguro = new ArrayList<>(mensagensOriginais);
        
        int limite = 50; 
        List<ChatMessage> ultimasMensagens;
        
        if (snapshotSeguro.size() > limite) {
            ultimasMensagens = snapshotSeguro.subList(snapshotSeguro.size() - limite, snapshotSeguro.size());
        } else {
            ultimasMensagens = snapshotSeguro;
        }
        
        System.out.println("[BANCO DE DADOS] Total de mensagens retornadas: " + ultimasMensagens.size());
        return gson.toJson(ultimasMensagens);
    }
    }