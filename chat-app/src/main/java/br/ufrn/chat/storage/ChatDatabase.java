    package br.ufrn.chat.storage;

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
            List<ChatMessage> mensagens = salasDeChat.getOrDefault(sala, new CopyOnWriteArrayList<>());
            if (!mensagens.isEmpty()) {
                String jsonCompleto = gson.toJson(mensagens);
                int tamanhoRealBytes = jsonCompleto.getBytes().length;
                
                System.out.println("[BANCO] Status da Sala [" + sala + "]: " 
                        + mensagens.size() + " mensagens armazenadas. " 
                        + "Peso total: " + tamanhoRealBytes + " bytes.");
            }

            int limite = 50; 
            List<ChatMessage> ultimasMensagens;
            
            if (mensagens.size() > limite) {
                ultimasMensagens = mensagens.subList(mensagens.size() - limite, mensagens.size());
            } else {
                ultimasMensagens = mensagens;
            }
            
            return gson.toJson(ultimasMensagens);
        }
    }