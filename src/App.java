import java.nio.charset.Charset;
import java.util.NoSuchElementException;
import java.util.Scanner;
import java.util.function.Function;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.InvocationTargetException;

public class App {

    /** Nome do arquivo de dados. O arquivo deve estar localizado na raiz do projeto */
    static String nomeArquivoDados;
    
    /** Scanner para leitura de dados do teclado */
    static Scanner teclado;

    /** Quantidade de produtos cadastrados atualmente na lista */
    static int quantosProdutos = 0;

    static ABB<String, Produto> produtosCadastradosPorNome;
    
    static ABB<Integer, Produto> produtosCadastradosPorId;
    
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
     * @return Um inteiro com a opção do usuário.
    */
    static int menu() {
        cabecalho();
        System.out.println("1 - Listar todos os produtos");
        System.out.println("2 - Carregar produtos por nome/descrição");
        System.out.println("3 - Carregar produtos por id");
        System.out.println("4 - Procurar produto, por nome");
        System.out.println("5 - Procurar produto, por id");
        System.out.println("0 - Sair");
        System.out.print("Digite sua opção: ");
        try {
            return Integer.parseInt(teclado.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    /**
     * Lê os dados de um arquivo-texto e retorna uma árvore de produtos.
     * @param nomeArquivoDados Nome do arquivo de dados a ser aberto.
     * @param extratorDeChave Função para extrair a chave (ID ou Nome) do produto.
     * @return Uma árvore com os produtos carregados.
     */
    static <K> ABB<K, Produto> lerProdutos(String nomeArquivoDados, Function<Produto, K> extratorDeChave) {
        
        Scanner arquivo = null;
        ABB<K, Produto> produtosCadastrados = new ABB<>();
        
        try {
            // Abre o arquivo usando UTF-8
            arquivo = new Scanner(new File(nomeArquivoDados), Charset.forName("UTF-8"));
            
            // Lê a quantidade de produtos da primeira linha (se existir)
            if (arquivo.hasNextLine()) {
                arquivo.nextLine(); 
            }
            
            // Lê linha por linha
            while (arquivo.hasNextLine()) {
                String linha = arquivo.nextLine();
                
                // Ignora linhas vazias para evitar erros bobos
                if (linha.trim().isEmpty()) {
                    continue;
                }

                try {
                    Produto produto = Produto.criarDoTexto(linha);
                    K chave = extratorDeChave.apply(produto);
                    produtosCadastrados.inserir(chave, produto);
                } catch (Exception e) {
                    // Se uma linha específica der erro, avisa mas continua carregando o resto
                    System.out.println("Erro ao processar linha: " + linha);
                }
            }
            
            quantosProdutos = produtosCadastrados.tamanho();
            System.out.println("Sucesso! Produtos carregados: " + quantosProdutos);
            
        } catch (IOException e) {
            System.out.println("Erro ao abrir o arquivo: " + e.getMessage());
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
    
    /** Localiza um produto na árvore de produtos organizados por id, a partir do código de produto informado pelo usuário, e o retorna. 
     * Em caso de não encontrar o produto, retorna null */
    static Produto localizarProdutoID(ABB<Integer, Produto> produtosCadastrados) {
        if (produtosCadastrados == null || produtosCadastrados.vazia()) {
            System.out.println("Árvore de produtos por ID não foi carregada ou está vazia.");
            return null;
        }
        
        System.out.print("Digite o ID do produto: ");
        try {
            int id = Integer.parseInt(teclado.nextLine());
            return localizarProduto(produtosCadastrados, id);
        } catch (NumberFormatException e) {
            System.out.println("ID inválido.");
            return null;
        }
    }
    
    /** Localiza um produto na árvore de produtos organizados por nome, a partir do nome de produto informado pelo usuário, e o retorna. 
     * A busca não é sensível ao caso. Em caso de não encontrar o produto, retorna null */
    static Produto localizarProdutoNome(ABB<String, Produto> produtosCadastrados) {
        if (produtosCadastrados == null || produtosCadastrados.vazia()) {
            System.out.println("Árvore de produtos por Nome não foi carregada ou está vazia.");
            return null;
        }

        System.out.print("Digite o Nome do produto: ");
        String nome = teclado.nextLine();
        return localizarProduto(produtosCadastrados, nome);
    }
    
    private static void mostrarProduto(Produto produto) {
        
        cabecalho();
        String mensagem = "Produto não localizado!";
        
        if (produto != null){
            mensagem = String.format("Dados do produto:\n%s", produto);
        }
        
        System.out.println(mensagem);
    }
    
    /** Lista todos os produtos cadastrados, numerados, um por linha */
    static <K> void listarTodosOsProdutos(ABB<K, Produto> produtosCadastrados) {
        
        cabecalho();
        if (produtosCadastrados != null && !produtosCadastrados.vazia()) {
            System.out.println("\nPRODUTOS CADASTRADOS:");
            System.out.println(produtosCadastrados.toString());
        } else {
            System.out.println("Nenhum produto cadastrado ou árvore não carregada.");
        }
    }
    
    public static void main(String[] args) {
        teclado = new Scanner(System.in, Charset.forName("UTF-8"));
        nomeArquivoDados = "produtos.txt";
        
        int opcao = -1;
      
        do{
            opcao = menu();
            switch (opcao) {
                case 1 -> listarTodosOsProdutos(produtosCadastradosPorNome); // Assume listagem por nome como padrão se disponível
                case 2 -> produtosCadastradosPorNome = lerProdutos(nomeArquivoDados, (p -> p.descricao));
                case 3 -> produtosCadastradosPorId = lerProdutos(nomeArquivoDados, (p -> p.idProduto));
                case 4 -> mostrarProduto(localizarProdutoNome(produtosCadastradosPorNome));
                case 5 -> mostrarProduto(localizarProdutoID(produtosCadastradosPorId));
            }
            pausa();
        } while(opcao != 0);       

        teclado.close();    
    }
}