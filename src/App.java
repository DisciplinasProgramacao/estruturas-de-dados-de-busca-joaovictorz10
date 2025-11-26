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

    // As variáveis abaixo agora armazenarão instâncias de AVL (devido ao polimorfismo na leitura)
    static ABB<String, Produto> produtosCadastradosPorNome;
    static ABB<Integer, Produto> produtosCadastradosPorId;
    
    // Estas variáveis estavam no código original, mas o PDF pede para carregar AVL nas variáveis principais.
    // O código abaixo usa as variáveis genéricas ABB para armazenar a AVL.
    
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
    
    /** Imprime o menu principal */
    static int menu() {
        cabecalho();
        System.out.println("1 - Carregar produtos por nome/descrição (AVL)");
        System.out.println("2 - Carregar produtos por id (AVL)");
        System.out.println("3 - Procurar produto, por nome");
        System.out.println("4 - Procurar produto, por id");
        System.out.println("5 - Remover produto, por nome");
        System.out.println("6 - Remover produto, por id");
        System.out.println("7 - Recortar a lista de produtos, por nome");
        System.out.println("8 - Recortar a lista de produtos, por id");
        System.out.println("0 - Sair");
        System.out.print("Digite sua opção: ");
        try {
            return Integer.parseInt(teclado.nextLine());
        } catch (NumberFormatException e) {
            return -1;
        }
    }
    
    static <K> ABB<K, Produto> lerProdutos(String nomeArquivoDados, Function<Produto, K> extratorDeChave) {
        
        Scanner arquivo = null;
        int numProdutos;
        String linha;
        Produto produto;
        ABB<K, Produto> produtosCadastrados;
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
            System.out.println("Produtos carregados em árvore AVL com sucesso!");
            
        } catch (IOException excecaoArquivo) {
            System.out.println("Erro ao ler arquivo: " + excecaoArquivo.getMessage());
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
        } catch (NoSuchElementException | NullPointerException excecao) {
            produto = null;
        }
        
        if (produtosCadastrados != null) {
            System.out.println("Número de comparações realizadas: " + produtosCadastrados.getComparacoes());
            System.out.println("Tempo de processamento da pesquisa: " + produtosCadastrados.getTempo() + " ms");
        }
        
        return produto;
    }
    
    static Produto localizarProdutoID(ABB<Integer, Produto> produtosCadastrados) {
        if(produtosCadastrados == null) return null;
        int idProduto = lerOpcao("Digite o identificador do produto desejado: ", Integer.class);
        return localizarProduto(produtosCadastrados, idProduto);
    }
    
    static Produto localizarProdutoNome(ABB<String, Produto> produtosCadastrados) {
        if(produtosCadastrados == null) return null;
        String descricao;
        System.out.println("Digite o nome ou a descrição do produto desejado:");
        descricao = teclado.nextLine();
        return localizarProduto(produtosCadastrados, descricao);
    }
    
    private static void mostrarProduto(Produto produto) {
        cabecalho();
        String mensagem = "Produto não encontrado ou dados inválidos!";
        if (produto != null){
            mensagem = String.format("Dados do produto:\n%s", produto);
        }
        System.out.println(mensagem);
    }
    
    static Produto removerProdutoId(ABB<Integer, Produto> produtosCadastrados) {
         if(produtosCadastrados == null) return null;
         cabecalho();
         System.out.println("Localizando o produto por id para remover");
         int id = lerOpcao("Digite o id do produto que deve ser removido", Integer.class);
         return removerProduto(produtosCadastrados, id);
    }

    static Produto removerProdutoNome(ABB<String, Produto> produtosCadastrados) {
        if(produtosCadastrados == null) return null;
        String descricao;
        cabecalho();
        System.out.println("Localizando o produto por nome para remover");
        System.out.print("Digite a descrição do produto que deve ser removido: ");
        descricao = teclado.nextLine();
        return removerProduto(produtosCadastrados, descricao);
    }

    static <K> Produto removerProduto(ABB<K, Produto> produtosCadastrados, K chave){
         cabecalho();
         try {
             Produto localizado = produtosCadastrados.remover(chave);
             return localizado;
         } catch (Exception e) {
             return null;
         }
    }
    
    

    private static void recortarProdutosNome(ABB<String, Produto> produtosCadastrados) {
        if (produtosCadastrados == null || produtosCadastrados.vazia()) {
            System.out.println("A árvore de produtos por nome ainda não foi carregada.");
            return;
        }
        
        cabecalho();
        System.out.println("RECORTAR POR NOME (Ordem Alfabética)");
        System.out.println("Digite o texto inicial (ex: 'Cadeira'): ");
        String inicio = teclado.nextLine();
        System.out.println("Digite o texto final (ex: 'Mesa'): ");
        String fim = teclado.nextLine();
        
        System.out.println("\nProcessando recorte...");
        Lista<Produto> resultado = produtosCadastrados.recortar(inicio, fim);
        
        System.out.println(resultado.toString());
    }
     
    private static void recortarProdutosId(ABB<Integer, Produto> produtosCadastrados) {
        if (produtosCadastrados == null || produtosCadastrados.vazia()) {
            System.out.println("A árvore de produtos por ID ainda não foi carregada.");
            return;
        }

        cabecalho();
        System.out.println("RECORTAR POR ID");
        Integer inicio = lerOpcao("Digite o ID inicial (ex: 10000): ", Integer.class);
        Integer fim = lerOpcao("Digite o ID final (ex: 20000): ", Integer.class);
        
        if (inicio != null && fim != null) {
            System.out.println("\nProcessando recorte...");
            Lista<Produto> resultado = produtosCadastrados.recortar(inicio, fim);
            System.out.println(resultado.toString());
        } else {
            System.out.println("IDs inválidos.");
        }
    }
    
    public static void main(String[] args) {
        teclado = new Scanner(System.in, Charset.forName("UTF-8"));
        nomeArquivoDados = "produtos.txt";
        
        int opcao = -1;
      
        do{
            opcao = menu();
            switch (opcao) {
                case 1 -> produtosCadastradosPorNome = lerProdutos(nomeArquivoDados, (p -> p.descricao));
                case 2 -> produtosCadastradosPorId = lerProdutos(nomeArquivoDados, (p -> p.idProduto));
                case 3 -> mostrarProduto(localizarProdutoNome(produtosCadastradosPorNome));
                case 4 -> mostrarProduto(localizarProdutoID(produtosCadastradosPorId));
                case 5 -> mostrarProduto(removerProdutoNome(produtosCadastradosPorNome));
                case 6 -> mostrarProduto(removerProdutoId(produtosCadastradosPorId));
                case 7 -> recortarProdutosNome(produtosCadastradosPorNome); 
                case 8 -> recortarProdutosId(produtosCadastradosPorId); 
                case 0 -> System.out.println("Saindo...");
                default -> System.out.println("Opção inválida!");
            }
            pausa();
        } while(opcao != 0);       

        teclado.close();    
    }
}