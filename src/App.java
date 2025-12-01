import java.nio.charset.Charset;
import java.time.LocalDate;
import java.util.NoSuchElementException;
import java.util.Random;
import java.util.Scanner;
import java.util.function.Function;
import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class App {

    static String nomeArquivoDados;
    static Scanner teclado;
    static int quantosProdutos = 0;
    static AVL<String, Produto> produtosBalanceadosPorNome;
    static AVL<Integer, Produto> produtosBalanceadosPorId;
    static TabelaHash<Produto, Lista<Pedido>> pedidosPorProduto;
    
    static void limparTela() {
        System.out.print("\033[H\033[2J");
        System.out.flush();
    }

    static void pausa() {
        System.out.println("Digite enter para continuar...");
        teclado.nextLine();
    }

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
    
    static int menu() {
        cabecalho();
        System.out.println("1 - Procurar produto, por id");
        System.out.println("2 - Gravar, em arquivo, pedidos de um produto");
        System.out.println("0 - Sair");
        System.out.print("Digite sua opção: ");
        try {
            return Integer.parseInt(teclado.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    static <K> AVL<K, Produto> lerProdutos(String nomeArquivoDados, Function<Produto, K> extratorDeChave) {
        Scanner arquivo = null;
        int numProdutos;
        String linha;
        Produto produto;
        AVL<K, Produto> produtosCadastrados;
        K chave;
        
        try {
            arquivo = new Scanner(new File(nomeArquivoDados), Charset.forName("UTF-8"));
            numProdutos = Integer.parseInt(arquivo.nextLine());
            produtosCadastrados = new AVL<K, Produto>();
            
            for (int i = 0; i < numProdutos; i++) {
                linha = arquivo.nextLine();
                produto = Produto.criarDoTexto(linha);
                chave = extratorDeChave.apply(produto);
                produtosCadastrados.inserir(chave, produto);
            }
            quantosProdutos = numProdutos;
            
        } catch (IOException excecaoArquivo) {
            produtosCadastrados = null;
        } finally {
            if (arquivo != null) arquivo.close();
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
    
    static Produto localizarProdutoID(ABB<Integer, Produto> produtosCadastrados) {
        Integer idProduto = lerOpcao("Digite o identificador do produto desejado: ", Integer.class);
        if (idProduto == null) return null;
        return localizarProduto(produtosCadastrados, idProduto);
    }
    
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
    
    private static void inserirNaTabela(Produto produto, Pedido pedido) {
        Lista<Pedido> pedidosDoProduto;
        try {
            pedidosDoProduto = pedidosPorProduto.pesquisar(produto);
        } catch (NoSuchElementException excecao) {
            pedidosDoProduto = new Lista<>();
            pedidosPorProduto.inserir(produto, pedidosDoProduto);
        }
        pedidosDoProduto.inserirFinal(pedido);
    }

    private static Lista<Pedido> gerarPedidos(int quantidade) {
        Lista<Pedido> pedidos = new Lista<>();
        Random sorteio = new Random(42);
        int quantProdutos;
        int formaDePagamento;
        for (int i = 0; i < quantidade; i++) {
            formaDePagamento = sorteio.nextInt(2) + 1;
            Pedido pedido = new Pedido(LocalDate.now(), formaDePagamento);
            quantProdutos = sorteio.nextInt(8) + 1;
            for (int j = 0; j < quantProdutos; j++) {
                int id = sorteio.nextInt(7750) + 10_000;
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
    
    static void pedidosDoProduto() {
        Lista<Pedido> pedidosDoProduto;
        Produto produto = localizarProdutoID(produtosBalanceadosPorId);
        
        if (produto == null) {
            System.out.println("Produto não encontrado.");
            return;
        }

        String nomeArquivo = "RelatorioProduto" + produto.hashCode() + ".txt";  
        
        try {
            pedidosDoProduto = pedidosPorProduto.pesquisar(produto);
            FileWriter arquivoRelatorio = new FileWriter(nomeArquivo, Charset.forName("UTF-8"));
            arquivoRelatorio.append(pedidosDoProduto.toString() + "\n");
            arquivoRelatorio.close();
            System.out.println("Dados salvos em " + nomeArquivo);
        } catch (NoSuchElementException e) {
            System.out.println("Nenhum pedido encontrado para este produto.");
        } catch(IOException excecao) {
            System.out.println("Problemas para criar o arquivo " + nomeArquivo + ". Tente novamente");        	
        }
    }
    
    public static void main(String[] args) {
        teclado = new Scanner(System.in, Charset.forName("UTF-8"));
        nomeArquivoDados = "produtos.txt";
        produtosBalanceadosPorId = lerProdutos(nomeArquivoDados, Produto::hashCode);
        
        if (produtosBalanceadosPorId != null) {
            produtosBalanceadosPorNome = new AVL<>(produtosBalanceadosPorId, produto -> produto.descricao, String::compareTo);
            pedidosPorProduto = new TabelaHash<>((int)(quantosProdutos * 1.50));
            
            System.out.println("Gerando pedidos...");
            gerarPedidos(25_000);
            System.out.println("Pedidos gerados.");
        
            int opcao = -1;
            do {
                opcao = menu();
                switch (opcao) {
                    case 1 -> mostrarProduto(localizarProdutoID(produtosBalanceadosPorId));
                    case 2 -> pedidosDoProduto(); 
                }
                pausa();
            } while(opcao != 0);       
        } else {
            System.out.println("Erro ao carregar produtos. Verifique o arquivo " + nomeArquivoDados);
        }

        teclado.close();    
    }
}