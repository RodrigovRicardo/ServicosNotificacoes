# Serviço de Notificações

## Convenções do projeto

- Classes em `PascalCase`; métodos e variáveis em `camelCase`.
- Packages em minúsculas, organizados por responsabilidade.
- Indentação de quatro espaços nos ficheiros Java e HTML.
- Templates com nomes em minúsculas e palavras separadas por hífen.
- Ligações e destinos de formulários definidos com atributos Thymeleaf.
- Singular para uma notificação; plural para listas e coleções.

## Endereços

| Método | Endereço | Operação |
| --- | --- | --- |
| GET | `/` ou `/notificacao/nova` | Mostrar o formulário |
| POST | `/notificacao` | Submeter a notificação |
| GET | `/notificacao` | Listar as notificações |
| GET | `/notificacao/{id}` | Consultar uma notificação |

## Simular falhas de envio

No IntelliJ, ativa o perfil `simulacao` na configuração de execução. Neste perfil nenhum email é enviado: o sistema apenas simula o resultado e guarda o histórico na base de dados configurada.

Em `application-simulacao.properties`, define `notificacao.simulacao.falhas-antes-sucesso`:

| Valor | Resultado com o limite atual de três tentativas |
| --- | --- |
| `0` | Sucesso à primeira tentativa |
| `1` | Uma falha seguida de sucesso |
| `2` | Duas falhas seguidas de sucesso |
| `3` ou mais | Três falhas e paragem por atingir o limite |

Reinicia a aplicação após mudar a configuração e cria uma nova notificação para cada cenário. Alterar o perfil não apaga o histórico nem reinicia a contagem de notificações anteriores.

Sem o perfil `simulacao`, a aplicação utiliza o envio SMTP real. O processamento continua a ser feito em segundo plano.

## Executar os testes

Na pasta do projeto, executa `./mvnw test` ou usa a opção de executar os testes no IntelliJ.

- Testes unitários: JUnit e Mockito, com dependências simuladas.
- Testes Web: `NotificacaoControllerTest`, com MockMvc e serviços simulados.
- Integração: `ProcessamentoNotificacoesIntegracaoTest`, com serviços e repositórios reais sobre H2 em memória.

Os testes de contexto e integração ativam os perfis `teste` e `simulacao`. A configuração `application-teste.properties` substitui a ligação ao MySQL e as credenciais SMTP por valores de teste. O agendamento automático fica desligado; os testes chamam o processamento explicitamente, sem esperas nem emails reais.

H2 é uma dependência apenas de teste. Os testes de integração validam o mapeamento JPA e a persistência, mas não substituem a confirmação final no MySQL utilizado pela aplicação.

Os slides usam `@MockBean`; no Spring Boot 4 deste projeto utiliza-se `@MockitoBean` para substituir os serviços nos testes Web. A configuração do Surefire inclui o agente Mockito para compatibilidade com JDKs recentes.


## DTO de submissão

O formulário é recebido como `NotificacaoDTO`, com destinatarios, assunto e conteudo. O serviço converte esses dados numa nova entidade e define PENDENTE. ID e estado não fazem parte do DTO. As páginas e os pedidos Postman mantêm os mesmos campos e endereços.

Origem: exemplo `ClienteDTO` de `JPA.pptx`, diapositivos 20 e 21, com classe simples, construtores e conversão manual. O exemplo do professor é de saída; aqui a estrutura foi adaptada à entrada do formulário. Não foi acrescentada uma biblioteca de mapeamento.
