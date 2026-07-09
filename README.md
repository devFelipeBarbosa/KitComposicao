# Kit Composição — Preencher Itens por Composição de PA (Centrais de Pedido)

> ⚠️ **EM ANDAMENTO** — homologado na base dev (08/07/2026). Reteste na base teste do
> cliente **iniciado e bloqueado por ambiente**: evento de liberação de alçada dispara
> envio de código por e-mail e a "Conta Padrão" de **Contas SMTP** da réplica está com
> autenticação inválida (credencial de produção não vale na base teste). Pendente:
> ajuste do SMTP (ou da exigência de código na liberação) na réplica → reteste → entrega técnica.

Personalização Sankhya ERP: **Botão de Ação** na aba de Itens das Centrais de Pedido que
permite escolher um **Produto Acabado (PA)** da Composição de Produto (módulo de Produção)
e preencher os itens do pedido com as **matérias-primas (MPs)** da composição — com opção
de listar o próprio PA.

A inserção passa pelo **barramento nativo da Central** (`CACHelper`, o mesmo do serviço
`CACSP.incluirAlterarItemNota` usado pela tela): preço pela regra da TOP, impostos,
totais da nota, flags de estoque, regras da Central e validações rodam exatamente como
na digitação manual.

---

## Versão Sankhya

- **Build:** `4.35b491`
- **Banco:** Oracle (base dev)
- **App server:** WildFly + tinyejb
- **Java:** 8 (toolchain Gradle)

---

## Arquitetura

```
[Central de Pedidos — aba Itens — Botão "Preencher Itens por Composição PA"]
       │  formulário: CODPRODPA*, CODLOCALORIG*, QTDNEG*, LISTAPA
       ▼
[PreencherItensPorComposicaoPA — AcaoRotinaJava (adapter)]
       │
       ▼
[PreencherItensService — caso de uso]
   1. busca MPs da Composição de Produto (TPRLMP ⋈ TPRATV ⋈ TPRPRC, versão mais recente)
   2. gera itens: QTDNEG = qtd digitada × qtd da MP na composição (+ PA se LISTAPA)
   3. VALIDA ESTOQUE ITEM A ITEM (espelho da digitação manual):
      • gate pela TOP (ATUALEST/VALEST/ATUALESTMP + USOPROD do produto)
      • SP nativa Stp_Valida_Estoque40 via ComercialUtils.validaEstoque
      • sem estoque + TOP 'Avisar' → pergunta POR PRODUTO (ctx.confirmarSimNao):
          "Não há estoque suficiente para 'X'. Deseja efetivá-lo?"
          Sim → autoriza efetivação (bypass central.notas.pode.efetivar_<codprod>)
          Não → pula somente aquele item
      • sem estoque + TOP 'Bloquear' → cancela a operação inteira
   4. inclui itens aceitos pelo barramento nativo:
      CACHelper.incluirAlterarItem(nunota, ServiceContext.getCurrent(), <itens XML>, true)
      → preço da TOP, impostos, RESERVA/ATUALESTOQUE/STATUSNOTA, totais TGFCAB,
        regras da Central, liberações — tudo nativo
       │
       ▼
[Mensagem: inseridos + pulados por estoque]
[Refresh: config do botão "Depois de executar, recarregar: o registro pai"
 → recarrega a nota inteira (grade de itens + totais do rodapé)]
```

---

## Estrutura do Projeto

```
src/main/java/br/com/evolution/kitcomposicao/
├── actions/
│   └── PreencherItensPorComposicaoPA.java   Botão de Ação (adapter — ponto de entrada)
├── service/
│   └── PreencherItensService.java           Caso de uso (composição → validação → inclusão)
├── repository/
│   ├── ComposicaoProdutoRepository.java     MPs do PA no processo (TPRLMP⋈TPRATV⋈TPRPRC, maior VERSAO)
│   ├── ProcessoProdutivoRepository.java     TPRPRC: ativo + qtd atividades + vínculo do PA
│   ├── ItemNotaNativoRepository.java        CACHelper rota XML + bypass de efetivação
│   ├── PedidoRepository.java                TGFCAB⋈TGFTOP (regra de estoque da TOP do pedido)
│   └── ProdutoRepository.java               TGFPRO.USOPROD
├── domain/
│   ├── ComposicaoPA.java                    Regra pura: gera itens com qtd multiplicada
│   ├── ProcessoProdutivo.java               Validações do processo escolhido (ativo/vínculo/1 atividade)
│   ├── ItemComposicao.java                  VO (CODPRODMP + QTDMISTURA)
│   ├── ItemPedido.java                      VO (CODPROD + QTDNEG)
│   ├── RegraEstoquePedido.java              Gate de validação (espelho do nativo)
│   └── ResultadoInclusao.java               Incluídos + pulados (mensagem ao usuário)
└── util/
    ├── TLogCatcher.java                     Logger arquivo
    ├── TLogConfiguration.java               ThreadLocal path/fileName
    ├── TLogType.java                        Enum INFO/ERROR
    ├── ParamUtil.java                       Leitura de parâmetros do formulário
    └── RepositorioArquivos.java             Pasta de log no SW Repository (reflection)

src/test/java/.../domain/
├── ComposicaoPATest.java                    Multiplicação de quantidades + LISTAPA
└── RegraEstoquePedidoTest.java              Gate de validação de estoque (5 cenários)

stubs/src/                                    Fontes dos stubs de compilação (ver Decisão 5)
specs/escopo.md                               Escopo original da personalização
form/                                         Prints do cadastro do botão/formulário
tst/                                          Evidências de teste (screenshots)
monitor/                                      Monitores de consulta/processos (evidências)
build.gradle.kts                              Java 8, encoding UTF-8
libs/                                         JARs Sankhya (gitignored) + stubs jar
```

---

## Formulário do Botão de Ação

Cadastro em **Dicionário de Dados → TGFITE (ItemNota) → Ações**, tipo **Rotina Java**:

| Parâmetro | Nome | Tipo | Obrigatório |
|---|---|---|---|
| Produto Acabado | `CODPRODPA` | Pesquisa (Produto) | Sim |
| Local Origem | `CODLOCALORIG` | Pesquisa (Local) | Sim |
| Quantidade | `QTDNEG` | Número decimal | Sim |
| Listar PA? | `LISTAPA` | Verdadeiro/Falso | Não |
| Processo Produtivo | `IDPROC` | Pesquisa (Processo) | Sim |

**Validações do Processo Produtivo** (um PA pode ter 2+ composições — o processo escolhido
resolve a ambiguidade), na ordem:

0. `TPRPRC.ATIVO='S'` — senão: *"O processo produtivo X não está ativo."*
1. PA com MPs vinculadas ao processo — senão: *"O item (PA) não possui vínculo com o
   processo produtivo escolhido."*
2. Processo com **uma única atividade** (TPRATV) — senão bloqueia: *"...Esta busca permite
   apenas processos com uma atividade configurada e que contenham MPs vinculadas."*
3. Composição sempre na **versão mais recente** (`ROW_NUMBER ... ORDER BY TPRPRC.VERSAO DESC`).

> **"Depois de executar, recarregar": `o registro pai (quando existir)`** — obrigatório.
> Recarrega a nota inteira (itens + totais). "Toda a grade" atualiza só a grade de itens
> e deixa os totais do rodapé desatualizados na tela (o banco fica correto).

---

## Decisões Técnicas

### 1. Inclusão pelo barramento nativo (`CACHelper` rota XML) — não JAPE direto

**Antes (v0.x):** `ctx.novaLinha().save()` (JAPE). Item entrava com `VLRUNIT=0` (regra de
preço da TOP não aplicada), sem impostos, e com flags de estoque erradas
(`RESERVA='N'`/`ATUALESTOQUE=-1` numa TOP de reserva) → `ORA-20101: Reserva diferente da
definicao na TOP` na trigger `TRG_UPT_TGFITE` ao confirmar. Corrigir campo a campo seria
whack-a-mole (hoje reserva, amanhã imposto).

**Agora:** `CACHelper.incluirAlterarItem(nunota, ServiceContext.getCurrent(), itensXML, true)`
— a mesma rota do serviço `CACSPBean.incluirAlterarItemNota` que a tela usa (confirmado
por stacktrace no monitor de processos). Com `inicializaProdutos=true`, o pipeline nativo
resolve preço (regra "Usar como preço" da TOP), impostos, unidade padrão, flags de
estoque, totais da TGFCAB, regras da Central e liberações.

Detalhes do XML (contrato de `buildPrePersistState`/`getPrimaryKeyFromXMLElement`):
- `<itens><item>` com `<NUNOTA>`, `<CODPROD>`, `<QTDNEG>`, `<CODLOCALORIG>`
- **`<SEQUENCIA/>` vazia é obrigatória** em item novo: ausente → erro
  `"XML não possui elemento de PK 'SEQUENCIA'"`; vazia → `PKNullElementError` tratado
  internamente (cria VO novo e gera a sequência)
- `VLRUNIT` e `CODVOL` omitidos de propósito: o pipeline calcula
- A rota alternativa `modifiedVOs`/`buildPrePersistState(null, "ItemNota", vo)` **não
  serve para item novo** (é para duplicação de nota, exige PK completa)

**Evidência (monitor):** na mesma request do botão: 3 INSERTs TGFITE → `SUM(VLRTOT +
VLRIPI - VLRDESC ...)` → `UPDATE TGFCAB SET BASEICMS=2200, VLRICMS=396, VLRNOTA=5200`.

### 2. Validação de estoque item a item ANTES do barramento

Quando falta estoque, a validação nativa lança `ServiceCanceledException` + client event
`br.com.sankhya.mgecomercial.event.estoque.insuficiente.produto`. Na Central, o "Sim" do
popup seta o flag de sessão `central.notas.pode.efetivar_<codprod>` e re-invoca o serviço.
**Num botão de ação ninguém seta o flag na re-execução → loop infinito** (monitor
registrou 2.439 re-execuções).

Solução — espelho da digitação manual, sem deixar o evento nascer:
- Pré-validação por item com a **mesma SP nativa** (`Stp_Valida_Estoque40` via
  `ComercialUtils.validaEstoque`); `getValEst()`: `'I'` inativo, `'N'` não valida,
  `'A'` avisa/pergunta, outro = bloqueia
- Pergunta **por produto** via `ctx.confirmarSimNao` (mecanismo nativo de botões:
  1ª execução lança a pergunta e faz rollback; a re-execução volta com a resposta) —
  itens ordenados por `CODPROD` para os índices das perguntas serem determinísticos
- **Sim** → seta o mesmo flag que o "Sim" da tela (`central.notas.pode.efetivar_<codprod>`)
- **Não** → pula somente aquele item (comportamento do manual)
- TOP configurada para **bloquear** → cancela a operação inteira com erro claro

### 3. Gate de validação — espelho do nativo (`RegraEstoquePedido`)

Extraído por decompilação de `EstoqueHelpper.getAtualizacaoEstoque` +
`KitIndependenteHelper.validaEstoqueComp` (build 4.35b491):

```
RESERVA      = TOP.ATUALEST=='R' ? 'S' : 'N'
ATUALESTOQUE = 'B'→-1 | 'R'→1 | 'N'→0 | 'E'→1 | usoProd 'M' sem reserva → TOP.ATUALESTMP
valida se    : usoProd != 'S'  &&  (ATUALESTOQUE == -1  ||  (reserva && TOP.VALEST='S'))
```

TOP do pedido lida na **versão do cabeçalho**: `TGFCAB ⋈ TGFTOP (CODTIPOPER + DHALTER = DHTIPOPER)`.

### 4. Pergunta pendente não é erro

`confirmarSimNao` lança `IllegalStateException` com payload `__TITULO__/__TEXTO__` na 1ª
execução (protocolo da plataforma). O catch da action detecta e **repassa sem logar**.

### 5. Stubs de compilação (JARs do servidor ausentes localmente)

Classes como `ServiceContext` (ws), `JapeSession` e `DynamicVO` (mge-jape.jar) não estão
na biblioteca local. `libs/sankhya-compile-stubs.jar` traz stubs mínimos **com descriptors
exatos extraídos do bytecode** (`javap -c` no mgecom-model — nunca chutar assinatura).
Fontes em `stubs/src/`. Em runtime valem as classes reais do servidor. JDOM (rota XML)
vem do Maven Central (`org.jdom:jdom:1.1.3`, mesma major do servidor).

### 6. Clean Architecture

Domínio puro e testável (`ComposicaoPA`, `RegraEstoquePedido` — 8 testes unitários), caso
de uso orquestrando, adapter fino no botão, infra isolada nos repositories.

### 7. Logging — padrão `TLogCatcher`

`<SW Repository>/personalizacao/evolution/log<YYYY-MM-DD>-PreencherItensPorComposicaoPA.txt`.

---

## Tabelas Envolvidas

| Tabela | Papel |
|---|---|
| `TGFCAB` | Pedido (cabeçalho) — totais/impostos atualizados pelo pipeline nativo |
| `TGFITE` | Itens inseridos (MPs + PA opcional) |
| `TGFTOP` | Regras da TOP: preço, ATUALEST/VALEST/STATUSBAIXAEST/ATUALESTMP |
| `TGFPRO` | Cadastro do produto (USOPROD, unidade padrão, decimais) |
| `TPRLMP` / `TPRATV` / `TPRPRC` | Composição de Produto (MPs por PA, versão mais recente) |
| `TGFEST` | Estoque (via SP nativa `Stp_Valida_Estoque40`) |

---

## JARs do Classpath (compileOnly)

- `SankhyaW-extensions.jar` — `AcaoRotinaJava`, `ContextoAcao`, `QueryExecutor`, `Registro`
- `mge-modelcore-4.35b491.jar` — `AuthenticationInfo`, proxyconnect (`IPrecoUnitarioInfo`, `IResultadoValidaEstoque`)
- `mgecom-model-4.35b491.jar` — `CACHelper`, `ComercialUtils.validaEstoque`
- `sankhya-compile-stubs.jar` — stubs (fontes em `stubs/src/`; versionado, gerado localmente)
- `org.jdom:jdom:1.1.3` — Maven Central (rota XML)

JARs Sankhya não são versionados (ver `.gitignore`). Copiados da biblioteca interna `4.35b491`.

---

## Setup (homologação / produção)

1. **Build**:
   ```bash
   ./gradlew clean build
   # build/libs/KitComposicaoPA-1.0.jar
   ```
2. Subir o JAR no **Módulo Java** (Dicionário de Dados) do ambiente.
3. Cadastrar o **Botão de Ação** na instância `ItemNota` (seção "Formulário do Botão" acima),
   classe `br.com.evolution.kitcomposicao.actions.PreencherItensPorComposicaoPA`.
4. Configurar **"Depois de executar, recarregar: o registro pai (quando existir)"**.

---

## Verificação (Smoke Test)

1. **Caminho feliz** — PA com composição, estoque OK: itens entram com preço da TOP,
   impostos e totais no rodapé (sem redigitar nada); confirmar a nota.
2. **Estoque insuficiente — Sim em todos**: pergunta por produto; itens entram; totais OK;
   nota confirma (reserva conforme TOP).
3. **Estoque insuficiente — misto Sim/Não**: só os aceitos entram; mensagem informa
   pulados.
4. **Estoque insuficiente — Não em todos**: erro claro, nada inserido.
5. **PA sem composição**: mensagem "Item (PA) não encontrado. Por gentileza, providenciar
   o cadastro da Composição de Produto!".
6. Log em `personalizacao/evolution/` no Repositório de Arquivos.

Evidências dos testes em `tst/` e monitores em `monitor/`.

---

## Próximos Passos

- **Desbloquear ambiente da réplica**: erro *"Houve erro no processo de envio de código de
  liberação! ... Verifique a configuração da 'Conta Padrão' do cadastro 'Contas SMTP' ...
  Envio de e-mail exige autenticação de usuário"* — corrigir credenciais da Conta Padrão
  em Contas SMTP (ou apontar para SMTP de teste), ou rever a exigência de envio de código
  no evento de liberação usado na base teste. A base do cliente tem **liberação de alçada**
  ativa (a dev não tinha) — cenário novo a validar no reteste.
- **Reteste na base teste do cliente**:
  - Conferir build Sankhya do cliente (dev = `4.35b491`; classes decompiladas e stubs
    assumem esse build)
  - Validar convivência com personalizações/regras da Central existentes do cliente
  - Conferir parâmetros de precificação (preço por local/controle) e validação de estoque
    das TOPs do cliente
- **Entrega técnica** (formulário Squad Tech) após homologação com o cliente

---

## Histórico de Mudanças Relevantes

- **v0.1** — inserção via JAPE (`ctx.novaLinha`): preço zero, reserva errada (ORA-20101)
- **v0.2** — preço via `CentralItemNota.inicializaProduto` (preço OK; demais campos ainda fora)
- **v1.0** — **inclusão 100% nativa** via `CACHelper` rota XML + validação de
  estoque item a item com pergunta por produto + refresh "registro pai". Homologada em
  base dev: preço, impostos, totais, reserva, confirmação de nota e os 4 cenários de estoque.
- **v1.1 (atual)** — **seleção do Processo Produtivo (`IDPROC`)** no formulário: um PA pode
  ter 2+ composições; validações de processo ativo, vínculo do PA e atividade única.
  Homologada em base dev (08/07/2026).

---

## Autoria
- **Felipe Barbosa** — CONCEITO EMPRESARIAL (BP RECIFE)
- Cláudia Fu-Wax (Assistente Claude Code)
