package br.ufrn.chat;

import br.ufrn.chat.storage.Storage;
import br.ufrn.middleware.extension.LoggerInterceptor;
import br.ufrn.middleware.identification.AOR;
import br.ufrn.middleware.identification.Lookup;
import br.ufrn.middleware.identification.ObjectId;
import br.ufrn.middleware.invocation.Invoker;
import br.ufrn.middleware.invocation.Marshaller;
import br.ufrn.middleware.lifecycle.LifecycleManager;
import br.ufrn.middleware.remoting.ServerRequestHandler;
import br.ufrn.middleware.remoting.TCPProtocolPlugIn;

public class ChatMain {
    public static void main(String[] args) {
        System.out.println("=== Iniciando Nó de Storage (Componente B) ===");

        ObjectId meuId = new ObjectId("STORAGE_NODE_1");
        int minhaPorta = 8080;
        String meuIp = "127.0.0.1";
        
        AOR minhaAor = new AOR(meuIp, minhaPorta, "TCP", meuId);
        System.out.println("[SISTEMA] Minha Identidade na Rede: " + minhaAor.toString());

        LifecycleManager lifecycleManager = new LifecycleManager();
        Lookup lookup = new Lookup();
        
        lifecycleManager.registerClass(Storage.class);
        lookup.registerService(Storage.class);
        
        Invoker invoker = new Invoker(lookup, lifecycleManager);

        invoker.addInterceptor(new LoggerInterceptor());
        System.out.println("[SISTEMA] Interceptador de Log e Desempenho ativado.");

        Marshaller marshaller = new Marshaller(invoker);
        
        ServerRequestHandler server = new ServerRequestHandler(new TCPProtocolPlugIn(), marshaller);
        server.start(minhaPorta);
    }
}