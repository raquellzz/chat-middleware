# 🚀 Tracker do Projeto - Middleware Distribuído

## 1. Modelo de Componentes (Nota: 2,0)
*O quão bem o middleware esconde a rede do programador.*
- [x] Criar anotações customizadas (`@RequestMap`, `@Get`, `@Post`, `@Param`, `@Lifecycle`).
- [x] **Modelo Sofisticado:** Preservar a assinatura dos métodos (Injetar parâmetros dinamicamente ao invés de passar um grande JSONObject).

## 2. Basic Remoting Patterns (Nota: 2,0)
*A infraestrutura básica de rede e invocação.*
- [x] **Server Request Handler:** Implementado (A classe que amarra o TCP ao Marshaller).
- [x] **Invoker:** Implementado (O cérebro que executa o método via Reflection).
- [x] **Marshaller:** Implementado (Faz o parse do JSON/HTTP usando Gson).
- [x] **Remote Object:** Implementado (O seu `Storage` e `Validator` limpos de código de rede).
- [x] **Remoting Error:** Implementado (A `RemotingException` que gera os HTTP 404, 400, 500).

## 3. Identification Patterns (Nota: 1,0)
*Como o middleware localiza os objetos.*
- [x] **Lookup:** Implementado (O mapa em memória que liga Rotas a Métodos).
- [ ] **Object Id:** (Identificador único para cada serviço registrado).
- [ ] **Absolute Object Reference (AOR):** (Pacote com IP, Porta e Object Id).

## 4. Lifecycle Management Patterns (Nota: 2,0)
*Como o middleware gerencia a memória sob carga.*
- **Estratégias de Criação (Escolher 2 de 3):**
    - [x] Static Instance (Instância global única).
    - [x] Per-Request Instance (Nova instância a cada requisição).
    - [ ] Client-Dependent Instance (Ignorado - já cumprimos a cota).
- **Estratégias de Recursos (Escolher 2 de 4):**
    - [x] Lazy Acquisition (Cria a instância apenas no momento do primeiro uso).
    - [x] Pooling (Mantém uma fila de instâncias prontas para economizar CPU).
    - [ ] Leasing (Ignorado).
    - [ ] Passivation (Ignorado).

## 5. Extension Patterns (Nota: 3,0)
*Mecanismos para adicionar comportamentos extras sem mexer no núcleo.*
- [ ] **Invocation Interceptor:** (Ganchos executados antes e depois da chamada).
- [ ] **Invocation Context:** (Passagem de metadados ocultos, como cabeçalhos/tokens).
- [ ] **Protocol Plug-In:** (Suportar dois protocolos de rede):
    - [x] TCP (`TCPProtocolPlugIn`).
    - [ ] UDP (`UDPProtocolPlugIn` a ser implementado).

## 6. Arquitetura e Testes Finais (Tolerância a Falhas)
*Requisitos práticos para o dia da apresentação.*
- [ ] **Broker:** O componente que conhece os AORs e distribui as requisições.
- [ ] **JMeter Configurado:** Script disparando requisições HTTP para a plataforma.
- [ ] **Métricas:** Determinar o *Knee Capacity* e o *Usable Capacity*.
- [ ] **Tolerância a Falhas:** Derrubar instâncias via terminal e ver a taxa de erro subir e depois normalizar.