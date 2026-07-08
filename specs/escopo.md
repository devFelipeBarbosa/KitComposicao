## Botão de Ação - Kit Composição (centrais de pedido)

Está personalização tem como objetivo criar um botão de ação na TGFITE para que o usuário possa escolher o produto acabado (PA) 
de uma composição de produto do ERP Sankhya, módulo de Produção, e preencher os itens da tela com as matérias-primas da composição deste
PA, com opção de listar o próprio PA.

### Requisitos

**Fluxo Idealizado:**
1. **Botão de ação**: Clicar no botão de ação nas centrais de pedido;
2. **Formulário**: Preencher os campos obrigatórios: PA, Local, Quantidade, e se é para listar o PA;
3. **Resultado**: É esperada que os itens de MP desse PA sejam preenchidos na aba de Itens das Centrais de Pedido (INSERT);
4. **Feedback**: Mensagem de sucesso ou erro.

---

### Regras de Itens e Composição
* **Divergência de Itens**: Caso o PA não tenha cadastro na tela Composição de Produto, retornar mensagem de erro: "Item (PA) não encontrado. Por gentileza,
providenciar o cadastro da Composição de Produto!";
* **Quantidade**: multiplicar a quantidade digitada do PA buscado pelo valor das quantidades das Composiçôes de Produto (MPs). Essa multiplicação
corresponderá a TGFITE.QTDNEG dos itens.
* **Itens**: usar o CODLOCALORIG do local digitado no formulário da personalização;
* **Regras TOP**: usar definições de usar como preço que a regra da TOP pede.
---

### Condições de Sucesso
* **Sucesso**: mensagem de sucesso informando ao usuário que os itens foram inseridos com sucesso.
* **Erro**: não existindo o PA em algum processo e não havendo MPs cadastrados para esse PA, retornar mensagem de erro.
* **Variáveis**: todas as variáveis do formulário são obrigatórias, com exceção da flag de listar o PA.