package br.ufrn.chat.storage;

import br.ufrn.middleware.extension.LoggerInterceptor;
import br.ufrn.middleware.identification.AOR;
import br.ufrn.middleware.identification.Lookup;
import br.ufrn.middleware.identification.ObjectId;
import br.ufrn.middleware.invocation.Invoker;
import br.ufrn.middleware.invocation.Marshaller;
import br.ufrn.middleware.lifecycle.LifecycleManager;
import br.ufrn.middleware.remoting.ProtocolPlugIn;
import br.ufrn.middleware.remoting.ServerRequestHandler;

public class StorageMain {
    public static void main(String[] args) {
        if (args.length < 2) {
            System.out.println("Uso: java -cp app.jar br.ufrn.chat.StorageMain <ID_STORAGE> <PORTA>");
            return;
        }

        String nomeNo = args[0];
        int minhaPorta = Integer.parseInt(args[1]);
        String protocolo = args.length >= 3 ? args[2] : "TCP";
        System.out.println(protocolo);

        System.out.println("=== Iniciando " + nomeNo + " (Banco de Dados) na porta " + minhaPorta + " ===");

        AOR minhaAor = new AOR("127.0.0.1", minhaPorta, protocolo, new ObjectId(nomeNo));
        
        LifecycleManager lifecycleManager = new LifecycleManager();
        Lookup lookup = new Lookup();
        
        lifecycleManager.registerClass(Storage.class);
        lookup.registerService(Storage.class);
        
        Invoker invoker = new Invoker(lookup, lifecycleManager);
        invoker.addInterceptor(new LoggerInterceptor());
        Marshaller marshaller = new Marshaller(invoker);

        ProtocolPlugIn pluginDeRede;
        
        if (protocolo.equals("UDP")) {
            pluginDeRede = new br.ufrn.middleware.remoting.UDPProtocolPlugIn();
        } else {
            pluginDeRede = new br.ufrn.middleware.remoting.TCPProtocolPlugIn();
        }
        
        ServerRequestHandler server = new ServerRequestHandler(pluginDeRede, marshaller);
        server.start(minhaPorta);
    }
}