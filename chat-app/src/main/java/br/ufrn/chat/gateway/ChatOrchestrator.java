package br.ufrn.chat.gateway;

import br.ufrn.middleware.remoting.MessageProcessor;
import br.ufrn.middleware.remoting.ServiceCluster;
import br.ufrn.middleware.utils.HttpHelper;
import br.ufrn.middleware.utils.HttpHelper.HttpRequestData;

public class ChatOrchestrator implements MessageProcessor {
    
    private final ServiceCluster clusterValidators;
    private final ServiceCluster clusterStorages;

    public ChatOrchestrator(ServiceCluster clusterValidators, ServiceCluster clusterStorages) {
        this.clusterValidators = clusterValidators;
        this.clusterStorages = clusterStorages;
    }

    @Override
    public String processMessage(String rawRequest) {
        HttpRequestData req = HttpHelper.parse(rawRequest);
        
        if (!req.isValid) {
            return HttpHelper.buildResponse(400, "{\"erro\": \"Requisicao invalida\"}");
        }

        try {
            if (req.route.equals("/chat")) {
                System.out.println("[ORQUESTRADOR] Iniciando fluxo (/chat)");
                
                String resValidacao = clusterValidators.sendWithFaultTolerance("POST", "/validator/validar", req.body);
                if (resValidacao.contains("REJEITADA") || resValidacao.contains("erro")) {
                    return HttpHelper.buildResponse(400, resValidacao);
                }

                String resSalvar = clusterStorages.sendWithFaultTolerance("POST", "/storage/salvar", req.body);
                if (resSalvar.contains("\"erro\"")) {
                    return HttpHelper.buildResponse(404, resSalvar);
                }
                return HttpHelper.buildResponse(200, resSalvar);

            } else if (req.route.startsWith("/ler")) {
                System.out.println("[ORQUESTRADOR] Iniciando fluxo (/ler)");
                String parametrosUrl = "";
                if (req.route.contains("?")) {
                    parametrosUrl = req.route.substring(req.route.indexOf("?"));
                }

                String resListar = clusterStorages.sendWithFaultTolerance("GET", "/storage/listar" + parametrosUrl, "");
                
                if (resListar.contains("\"erro\"")) {
                    System.err.println("[GATEWAY ALERTA] Falha na rota: " + req.route + " | Resposta do Cluster: " + resListar);
                    return HttpHelper.buildResponse(404, resListar);
                }
                return HttpHelper.buildResponse(200, resListar);
            }
            
            return HttpHelper.buildResponse(404, "{\"erro\": \"Rota não encontrada\"}");

        } catch (Exception e) {
            return HttpHelper.buildResponse(503, "{\"erro\": \"" + e.getMessage() + "\"}");
        }
    }
}