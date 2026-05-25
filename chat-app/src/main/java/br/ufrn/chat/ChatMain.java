package br.ufrn.chat;

import br.ufrn.chat.storage.Storage;
import br.ufrn.chat.validator.Validator;
import br.ufrn.middleware.extension.LoggerInterceptor;
import br.ufrn.middleware.identification.AOR;
import br.ufrn.middleware.identification.Lookup;
import br.ufrn.middleware.identification.ObjectId;
import br.ufrn.middleware.invocation.Invoker;
import br.ufrn.middleware.invocation.Marshaller;
import br.ufrn.middleware.lifecycle.LifecycleManager;
import br.ufrn.middleware.remoting.ServerRequestHandler;
import br.ufrn.middleware.remoting.UDPProtocolPlugIn;
import br.ufrn.middleware.remoting.TCPProtocolPlugIn;

//NAO ESTA SENDO USADO, MAS DEIXEI COMO EXEMPLO DE NÓ QUE RODA TUDO JUNTO (VALIDATOR + STORAGE) PARA TESTES RÁPIDOS
public class ChatMain {
    public static void main(String[] args) {
        System.out.println("=== Iniciando Nó de Storage (Componente B) ===");

        if (args.length < 2) {
            System.out.println("Uso incorreto. Exemplo: java -jar app.jar STORAGE_1 8081 TCP");
            return;
        }

        String nomeNo = args[0]; // Ex: STORAGE_1
        int minhaPorta = Integer.parseInt(args[1]); // Ex: 8081
        // String protocolo = args[2]; // Ex: TCP ou UDP
        String protocolo = "TCP"; // Por enquanto, fixamos o protocolo para UDP

        System.out.println("=== Iniciando Nó: " + nomeNo + " na porta " + minhaPorta + " com protocolo " + protocolo + " ===");

        ObjectId meuId = new ObjectId(nomeNo);
        AOR minhaAor = new AOR("127.0.0.1", minhaPorta, protocolo, meuId);
        System.out.println("[SISTEMA] Minha Identidade na Rede: " + minhaAor.toString());

        LifecycleManager lifecycleManager = new LifecycleManager();
        Lookup lookup = new Lookup();
        
        lifecycleManager.registerClass(Storage.class);
        lookup.registerService(Storage.class);
        
        lifecycleManager.registerClass(Validator.class);
        lookup.registerService(Validator.class);
        
        Invoker invoker = new Invoker(lookup, lifecycleManager);

        invoker.addInterceptor(new LoggerInterceptor());
        System.out.println("[SISTEMA] Interceptador de Log e Desempenho ativado.");

        Marshaller marshaller = new Marshaller(invoker);
        
        ServerRequestHandler server = new ServerRequestHandler(new TCPProtocolPlugIn(), marshaller);
        server.start(minhaPorta);
    }
}