import java.util.NoSuchElementException;

public class TabelaHash<K, V> implements IMapeamento<K, V> {

    private Entrada<K, V>[] tabela;
    private int capacidade;
    private int tamanho;

    @SuppressWarnings("unchecked")
    public TabelaHash(int capacidade) {
        this.capacidade = capacidade;
        this.tabela = new Entrada[capacidade];
        this.tamanho = 0;
    }

    private int hash(K chave) {
        return Math.abs(chave.hashCode()) % capacidade;
    }

    @Override
    public int inserir(K chave, V item) {
        int posicao = hash(chave);
        int posInicial = posicao;

        while (tabela[posicao] != null) {
            if (tabela[posicao].getChave().equals(chave)) {
                tabela[posicao].setValor(item);
                return posicao;
            }
            posicao = (posicao + 1) % capacidade;
            
            if (posicao == posInicial) {
                throw new IllegalStateException("Tabela Hash cheia");
            }
        }

        tabela[posicao] = new Entrada<>(chave, item);
        tamanho++;
        return posicao;
    }

    @Override
    public V pesquisar(K chave) {
        int posicao = hash(chave);
        int posInicial = posicao;

        while (tabela[posicao] != null) {
            if (tabela[posicao].getChave().equals(chave)) {
                return tabela[posicao].getValor();
            }
            posicao = (posicao + 1) % capacidade;

            if (posicao == posInicial) {
                break;
            }
        }
        
        throw new NoSuchElementException("Item não encontrado");
    }

    @Override
    public V remover(K chave) {
        throw new UnsupportedOperationException("Remoção não suportada nesta implementação");
    }

    @Override
    public int tamanho() {
        return tamanho;
    }
    
    @Override
    public long getComparacoes() { 
        return 0; 
    }
    
    @Override
    public double getTempo() { 
        return 0; 
    }

    @Override
    public String percorrer() {
        StringBuilder sb = new StringBuilder();
        for (Entrada<K, V> entrada : tabela) {
            if (entrada != null) {
                sb.append(entrada.toString()).append("\n");
            }
        }
        return sb.toString();
    }
}