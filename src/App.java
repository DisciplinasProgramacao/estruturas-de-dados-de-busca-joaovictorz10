import java.nio.charset.Charset;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardOpenOption;
import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;
import java.util.function.Function;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class App {

	/** Nome do arquivo de dados. O arquivo deve estar localizado na raiz do projeto */
    static String nomeArquivoDados;
    
    /** Scanner para leitura de dados do teclado */
    static Scanner teclado;

    /** Quantidade de produtos cadastrados atualmente na lista */
    static int quantosProdutos = 0;

    static AVL<String, Produto> produtosBalanceadosPorNome;
    
    static AVL<Integer, Produto> produtosBalanceadosPorId;
    
    static TabelaHash<Produto, Lista<Pedido>> pedidosPorProduto;
    // ... variáveis existentes ...
static AVL<Integer, Fornecedor> fornecedoresPorId; // Árvore AVL para fornecedores
static TabelaHash<Produto, Lista<Fornecedor>> fornecedoresDoProduto; // Hash associando Produto -> Lista de Fornecedores
    static void limparTela() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    /** Gera um efeito de pausa na CLI. Espera por um enter para continuar */
    static void pausa() {
        System.out.println("Digite enter para continuar...");
        teclado.nextLine();
    }

    /** Cabeçalho principal da CLI do sistema */
    static void cabecalho() {
        System.out.println("AEDs II COMÉRCIO DE COISINHAS");
        System.out.println("=============================");
    }
   
    static <T extends Number> T lerOpcao(String mensagem, Class<T> classe) {
        
    	T valor;
        
    	System.out.println(mensagem);
    	try {
            valor = classe.getConstructor(String.class).newInstance(teclado.nextLine());
        } catch (InstantiationException | IllegalAccessException | IllegalArgumentException 
        		| InvocationTargetException | NoSuchMethodException | SecurityException e) {
            return null;
        }
        return valor;
    }
    
    /** Imprime o menu principal, lê a opção do usuário e a retorna (int).
     * Perceba que poderia haver uma melhor modularização com a criação de uma classe Menu.
     * @return Um inteiro com a opção do usuário.
    */
static int menu() {
    cabecalho();
    System.out.println("1 - Procurar produto, por id");
    System.out.println("2 - Gravar, em arquivo, pedidos de um produto");
    System.out.println("3 - Relatório de um Fornecedor (na tela)"); 
    System.out.println("4 - Gravar, em arquivo, fornecedores de um produto"); 
    System.out.println("0 - Sair");
    System.out.print("Digite sua opção: ");
    try {
        return Integer.parseInt(teclado.nextLine());
    } catch (NumberFormatException e) {
        return -1;
    }
}
    
    /**
     * Lê os dados de um arquivo-texto e retorna uma árvore de produtos. Arquivo-texto no formato
     * N (quantidade de produtos) <br/>
     * tipo;descrição;preçoDeCusto;margemDeLucro;[dataDeValidade] <br/>
     * Deve haver uma linha para cada um dos produtos. Retorna uma árvore vazia em caso de problemas com o arquivo.
     * @param nomeArquivoDados Nome do arquivo de dados a ser aberto.
     * @return Uma árvore com os produtos carregados, ou vazia em caso de problemas de leitura.
     */
    static <K> AVL<K, Produto> lerProdutos(String nomeArquivoDados, Function<Produto, K> extratorDeChave) {
    Scanner arquivo = null;
    AVL<K, Produto> produtosCadastrados = new AVL<>();

    try {
        arquivo = new Scanner(new File(nomeArquivoDados), Charset.forName("UTF-8"));

        // Proteção: verifica se o arquivo tem conteúdo antes de ler
        if (!arquivo.hasNextLine()) {
            return produtosCadastrados; // Retorna árvore vazia se arquivo vazio
        }

        // Lê a quantidade (primeira linha) e remove espaços em branco
        String linhaQtd = arquivo.nextLine().trim();
        if (linhaQtd.isEmpty()) return produtosCadastrados;
        
        int numProdutos = Integer.parseInt(linhaQtd);
        quantosProdutos = numProdutos;

        for (int i = 0; i < numProdutos; i++) {
            // Proteção: verifica se realmente existe uma próxima linha antes de ler
            if (arquivo.hasNextLine()) {
                String linha = arquivo.nextLine();
                // Pula linhas vazias que possam ter ficado no arquivo
                while (linha.trim().isEmpty() && arquivo.hasNextLine()) {
                    linha = arquivo.nextLine();
                }
                
                if (!linha.trim().isEmpty()) {
                    Produto produto = Produto.criarDoTexto(linha);
                    K chave = extratorDeChave.apply(produto);
                    produtosCadastrados.inserir(chave, produto);
                }
            }
        }
        
    } catch (IOException | NumberFormatException e) {
        System.out.println("Erro ao ler o arquivo: " + e.getMessage());
        produtosCadastrados = null;
    } finally {
        if (arquivo != null) {
            arquivo.close();
        }
    }
    
    return produtosCadastrados;
}
    
    static <K> Produto localizarProduto(ABB<K, Produto> produtosCadastrados, K procurado) {
    	
    	Produto produto;
    	
    	cabecalho();
    	System.out.println("Localizando um produto...");
    	
    	try {
    		produto = produtosCadastrados.pesquisar(procurado);
    	} catch (NoSuchElementException excecao) {
    		produto = null;
    	}
    	
    	System.out.println("Número de comparações realizadas: " + produtosCadastrados.getComparacoes());
    	System.out.println("Tempo de processamento da pesquisa: " + produtosCadastrados.getTempo() + " ms");
        
    	return produto;
    	
    }
    
    /** Localiza um produto na árvore de produtos organizados por id, a partir do código de produto informado pelo usuário, e o retorna. 
     *  Em caso de não encontrar o produto, retorna null */
    static Produto localizarProdutoID(ABB<Integer, Produto> produtosCadastrados) {
        
        int idProduto = lerOpcao("Digite o identificador do produto desejado: ", Integer.class);
        
        return localizarProduto(produtosCadastrados, idProduto);
    }
    
    /** Localiza um produto na árvore de produtos organizados por nome, a partir do nome de produto informado pelo usuário, e o retorna. 
     *  A busca não é sensível ao caso. Em caso de não encontrar o produto, retorna null */
    static Produto localizarProdutoNome(ABB<String, Produto> produtosCadastrados) {
        
    	String descricao;
    	
    	System.out.println("Digite o nome ou a descrição do produto desejado:");
        descricao = teclado.nextLine();
        
        return localizarProduto(produtosCadastrados, descricao);
    }
    
    private static void mostrarProduto(Produto produto) {
    	
        cabecalho();
        String mensagem = "Dados inválidos para o produto!";
        
        if (produto != null){
            mensagem = String.format("Dados do produto:\n%s", produto);
        }
        
        System.out.println(mensagem);
    }
    
    private static Lista<Pedido> gerarPedidos(int quantidade) {
        Lista<Pedido> pedidos = new Lista<>();
        Random sorteio = new Random(42);
        int produtosPorPedido;
        int formaDePagamento;
        
        for (int i = 0; i < quantidade; i++) {
            formaDePagamento = sorteio.nextInt(2) + 1;
            Pedido pedido = new Pedido(LocalDate.now(), formaDePagamento);
            
            produtosPorPedido = sorteio.nextInt(8) + 1; // 1 a 8 itens por pedido
            
            for (int j = 0; j < produtosPorPedido; j++) {
         
                int range = (quantosProdutos > 0) ? quantosProdutos : 1; 
                int id = sorteio.nextInt(range) + 10_000;
                
              
                try {
                    Produto produto = produtosBalanceadosPorId.pesquisar(id);
                    pedido.incluirProduto(produto);
                    inserirNaTabela(produto, pedido);
                } catch (NoSuchElementException e) {
                 
                }
            }
            pedidos.inserirFinal(pedido);
        }
        return pedidos;
    }

private static void inserirNaTabela(Produto produto, Pedido pedido) {
    Lista<Pedido> lista = null;

 
    try {
        lista = pedidosPorProduto.pesquisar(produto);
    } catch (NoSuchElementException e) {
       
        lista = null;
    }

    if (lista == null) {
        lista = new Lista<>();
        pedidosPorProduto.inserir(produto, lista);
    }

    // Insere o pedido na lista
    lista.inserirFinal(pedido);
}

static AVL<Integer, Fornecedor> lerFornecedores(String nomeArquivo) {
    AVL<Integer, Fornecedor> arvoreFornecedores = new AVL<>(Integer::compare); // Comparador de Inteiros para o ID
    Scanner arquivo = null;

    try {
        arquivo = new Scanner(new File(nomeArquivo), Charset.forName("UTF-8"));
        
        if (!arquivo.hasNextLine()) return arvoreFornecedores;

        int qtdFornecedores = Integer.parseInt(arquivo.nextLine().trim());
        Random sorteio = new Random(123); // Seed fixa para reprodutibilidade ou sem seed para aleatório total

        for (int i = 0; i < qtdFornecedores; i++) {
            String nome = arquivo.nextLine();
            try {
                Fornecedor novoFornecedor = new Fornecedor(nome);
                
                // Sorteia até 6 produtos para este fornecedor [cite: 46]
                int qtdProdutos = sorteio.nextInt(6) + 1; 
                
                for (int j = 0; j < qtdProdutos; j++) {
                    int idProd = sorteio.nextInt(quantosProdutos) + 10_000; 
                    
                    try {
                        Produto prod = produtosBalanceadosPorId.pesquisar(idProd);
                        
                        novoFornecedor.adicionarProduto(prod);
                        
                        Lista<Fornecedor> listaFornecedores = fornecedoresDoProduto.pesquisar(prod);
                        if (listaFornecedores == null) {
                            listaFornecedores = new Lista<>();
                            fornecedoresDoProduto.inserir(prod, listaFornecedores);
                        }
                        listaFornecedores.inserirFinal(novoFornecedor);
                        
                    } catch (NoSuchElementException e) {
                       
                    }
                }
                
                arvoreFornecedores.inserir(novoFornecedor.getDocumento(), novoFornecedor);
                
            } catch (IllegalArgumentException e) {
                System.out.println("Erro ao criar fornecedor '" + nome + "': " + e.getMessage());
            }
        }
    } catch (IOException e) {
        System.out.println("Erro ao ler arquivo de fornecedores: " + e.getMessage());
    } finally {
        if (arquivo != null) arquivo.close();
    }
    
    return arvoreFornecedores;
}
/**
 * Localiza um fornecedor pelo ID e imprime seus dados (Tarefa 4) [cite: 51]
 */
static void relatorioDeFornecedor() {
    int id = lerOpcao("Digite o documento (ID) do fornecedor: ", Integer.class);
    
    try {
        Fornecedor f = fornecedoresPorId.pesquisar(id);
        cabecalho();
        System.out.println("RELATÓRIO DE FORNECEDOR");
        System.out.println(f.toString());
    } catch (NoSuchElementException e) {
        System.out.println("Fornecedor não encontrado com o documento " + id);
    }
}

/**
 * Gera arquivo com a lista de fornecedores de um determinado produto (Tarefa 4) [cite: 52]
 */
static void relatorioFornecedoresDoProduto() {
 
    Produto produto = localizarProdutoID(produtosBalanceadosPorId);
    
    if (produto == null) {
        System.out.println("Produto não encontrado.");
        return;
    }

    Lista<Fornecedor> lista = null;
    try {
        lista = fornecedoresDoProduto.pesquisar(produto);
    } catch (Exception e) {
        lista = null;
    }

    if (lista == null) {
        System.out.println("Nenhum fornecedor encontrado para este produto.");
        return;
    }

    String nomeArquivo = "Fornecedores_Produto_" + produto.hashCode() + ".txt";

    try (BufferedWriter bw = Files.newBufferedWriter(
            Path.of(nomeArquivo),
            Charset.forName("UTF-8"),
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING)) {

        bw.write("RELATÓRIO DE FORNECEDORES DO PRODUTO");
        bw.newLine();
        bw.write("Produto: " + produto.toString());
        bw.newLine();
        bw.write("==================================================");
        bw.newLine();

        try {
            Celula<Fornecedor> atual = lista.getPrimeiro();
            while (atual != null) {
                Fornecedor f = atual.getItem();
                bw.write("Fornecedor: " + f.getNome() + " | Doc: " + f.getDocumento());
                bw.newLine();
                atual = atual.getProximo();
            }
        } catch (Exception e) {
            bw.write("Erro ao iterar lista de fornecedores.");
        }

        System.out.println("Relatório gerado: " + nomeArquivo);

    } catch (IOException e) {
        System.out.println("Erro ao gravar arquivo: " + e.getMessage());
    }
}

    
    
static void pedidosDoProduto() {
    Produto produto = localizarProdutoID(produtosBalanceadosPorId);
    if (produto == null) {
        System.out.println("Produto não encontrado.");
        return;
    }

    Lista<Pedido> pedidosDoProduto = null;
    try {
        pedidosDoProduto = pedidosPorProduto.pesquisar(produto);
    } catch (Exception e) {
    
        pedidosDoProduto = null;
    }

    if (pedidosDoProduto == null) {
        System.out.println("Esse produto não possui pedidos.");
        return;
    }

  
    String nomeBase = produto.toString().replaceAll("[^a-zA-Z0-9_\\-]", "_");
    String nomeArquivo = "RelatorioProduto_" + nomeBase + ".txt";

   
    try (BufferedWriter bw = Files.newBufferedWriter(
            Path.of(nomeArquivo),
            Charset.forName("UTF-8"),
            StandardOpenOption.CREATE,
            StandardOpenOption.TRUNCATE_EXISTING)) {

        bw.write("RELATÓRIO DE PEDIDOS DO PRODUTO");
        bw.newLine();
        bw.write("Produto: " + produto.toString());
        bw.newLine();
        bw.write("Total de pedidos registrados nesta tabela: ");
        // se Lista tiver getTamanho()
        try {
            bw.write(String.valueOf(pedidosDoProduto.getTamanho()));
        } catch (Throwable t) {
           
        }
        bw.newLine();
        bw.newLine();


        try {
            Celula<Pedido> atual = pedidosDoProduto.getPrimeiro();
            int contador = 0;
            while (atual != null) {
                contador++;
                Pedido p = atual.getItem();
                bw.write("Pedido #" + contador);
                bw.newLine();
               
                bw.write(p.toString());
                bw.newLine();
                bw.write("--------------------------------------------------");
                bw.newLine();
                atual = atual.getProximo();
            }
        } catch (NoSuchMethodError | NoSuchElementException | NullPointerException e) {
           
            try {
                int tamanho = pedidosDoProduto.getTamanho();
                for (int i = 0; i < tamanho; i++) {
                    Pedido p = pedidosDoProduto.pegar(i);
                    bw.write("Pedido #" + (i+1));
                    bw.newLine();
                    bw.write(p.toString());
                    bw.newLine();
                    bw.write("--------------------------------------------------");
                    bw.newLine();
                }
            } catch (Throwable t) {
             
                bw.write("Não foi possível iterar os pedidos: " + t.getMessage());
                bw.newLine();
            }
        }

        System.out.println("Relatório gerado com sucesso em: " + nomeArquivo);

    } catch (IOException e) {
        System.out.println("Erro ao criar/grav ar o arquivo: " + e.getMessage());
    }
}
    
	public static void main(String[] args) {
    teclado = new Scanner(System.in, Charset.forName("UTF-8"));
    nomeArquivoDados = "produtos.txt";
    
    // Tenta carregar os produtos
    produtosBalanceadosPorId = lerProdutos(nomeArquivoDados, Produto::hashCode);
    
    // --- CORREÇÃO DE SEGURANÇA AQUI ---
    if (produtosBalanceadosPorId == null || produtosBalanceadosPorId.vazia()) {
        System.out.println("ERRO CRÍTICO: Não foi possível ler o arquivo 'produtos.txt' ou ele está vazio.");
        System.out.println("Verifique se o arquivo está na pasta raiz do projeto: " + System.getProperty("user.dir"));
        return; // Encerra o programa para evitar o NullPointerException
    }
    // ----------------------------------

    // Agora é seguro criar a segunda árvore, pois sabemos que a primeira não é nula
    try {
        produtosBalanceadosPorNome = new AVL<>(produtosBalanceadosPorId, produto -> produto.toString(), String::compareTo);
    } catch (Exception e) {
        System.out.println("Erro ao indexar produtos por nome: " + e.getMessage());
        produtosBalanceadosPorNome = new AVL<>(String::compareTo); // Inicia vazia para não quebrar
    }

    pedidosPorProduto = new TabelaHash<>((int)(quantosProdutos * 1.25));
    
    
    fornecedoresDoProduto = new TabelaHash<>((int)(quantosProdutos * 1.5));
    
   
    System.out.println("Carregando fornecedores...");
    fornecedoresPorId = lerFornecedores("fornecedores.txt");
    


    gerarPedidos(25_000); 
   
    int opcao = -1;
  
    do {
        opcao = menu();
        switch (opcao) {
            case 1 -> mostrarProduto(localizarProdutoID(produtosBalanceadosPorId));
            case 2 -> pedidosDoProduto();
            case 3 -> relatorioDeFornecedor(); // Novo
            case 4 -> relatorioFornecedoresDoProduto(); // Novo
        }
        if (opcao != 0) pausa();
    } while(opcao != 0);       

    teclado.close();    
}
}