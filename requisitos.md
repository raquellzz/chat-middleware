# Requisitos do trabalho

1. Trabalho individual.

2. A plataforma de middleware desenvolvida deve possuir um Modelo de Componente
capaz de definir um objeto por meio de anotações e viabilizar a execução dos métodos
desse objeto remoto por meio de invocações de requisições HTTP.

3. Cada aluno deve implementar os seguintes padrões na sua respectiva plataforma:
- [ ] Broker
- [ ] Basic Remoting Patterns (2,0)
    - [ ] Server Request Handler
    - [x] Invoker
    - [x] Marshaller,
    - [ ] Remote Object
    - [ ] Remoting Error
- [ ] Identification Patterns (1,0)
    - [x] Lookup
    - [ ] Object Id,
    - [ ] Absolute Object Reference
- [ ] Lifecycle Management [2 de 3] (2,0)
    - [ ] Static Instance
    - [ ] PerRequest Instance
    - [ ] Client-Dependent Instance
- [ ] Lifecycle Management [2 de 4] (2,0)
    - [ ] Lazy Acquisition
    - [ ] Pooling
    - [ ] Leasing
    - [ ] Passivation
- [ ] Extension Patterns (3,0)
    - [ ] Invocation Interceptor
    - [ ] Invocation Context
    - [ ] Protocol Plug-In deve suportar dois protocolos:
        - [ ] UDP
        - [x] TCP

4. [] Modelo de componentes (2,0)

    O Modelo de componentes se refere a como seu objeto remoto é descrito.
Abaixo temos dois exemplos de anotações que descrevem um método remoto:
O primeiro é exemplo é mais simples, pois tudo é passado via JSONObjects. No
segundo, é descrito um modelo mais sofisticado onde a assinatura do método
é preservada.

5. Testes de Carga com JMeter
o Deve-se utilizar a ferramenta JMeter para realizar testes de carga da aplicação
desenvolvida utilizando a Plataforma de Middleware proposta.
o No dia da apresentação do projeto, todos os testes devem estar preparados e
configurados antecipadamente.

6. Deve-se investigar a capacidade do sistema, e conhecer seu Knee capacity e Usable Capacity