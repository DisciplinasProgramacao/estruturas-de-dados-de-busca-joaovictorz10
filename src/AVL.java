import java.util.Comparator;

public class AVL<K, V> extends ABB<K, V> {

    public AVL() {
        super();
    }
    
    public AVL(Comparator<K> comparador) {
    	super(comparador);
    }
    
    @Override
    protected No<K, V> inserir(No<K, V> raizArvore, K chave, V item) {
    	return balancear(super.inserir(raizArvore, chave, item));
    }
    
    @Override
    protected No<K, V> removerNoAntecessor(No<K, V> itemRetirar, No<K, V> raizArvore) {
        return balancear(super.removerNoAntecessor(itemRetirar, raizArvore));
    }

    @Override
    protected No<K, V> remover(No<K, V> raizArvore, K chaveRemover) {
    	return balancear(super.remover(raizArvore, chaveRemover));
    }

    private No<K, V> balancear(No<K, V> raizArvore) {
		int fatorBalanceamento;
		int fatorBalanceamentoFilho;
		
		if (raizArvore != null) {
			fatorBalanceamento = raizArvore.getFatorBalanceamento();
			
			if (fatorBalanceamento == 2) {
				fatorBalanceamentoFilho = raizArvore.getEsquerda().getFatorBalanceamento();
				if (fatorBalanceamentoFilho == -1)
					raizArvore.setEsquerda(rotacionarEsquerda(raizArvore.getEsquerda()));
				raizArvore = rotacionarDireita(raizArvore);
			} else if (fatorBalanceamento == -2) {
				fatorBalanceamentoFilho = raizArvore.getDireita().getFatorBalanceamento();
				if (fatorBalanceamentoFilho == 1)
					raizArvore.setDireita(rotacionarDireita(raizArvore.getDireita()));
				raizArvore = rotacionarEsquerda(raizArvore);
			} else
				raizArvore.setAltura();
		}
		return raizArvore;
	}
	
	private No<K, V> rotacionarDireita(No<K, V> p) {
		No<K, V> u;
		No<K, V> filhoEsquerdaDireita;
		
		u = p.getEsquerda();
		filhoEsquerdaDireita = u.getDireita();
		
		p.setEsquerda(filhoEsquerdaDireita);
		u.setDireita(p);
		
		p.setAltura();
		u.setAltura();
		
		return u;
	}
	
	private No<K, V> rotacionarEsquerda(No<K, V> p) {
		No<K, V> z;
		No<K, V> filhoDireitaEsquerda;
		
		z = p.getDireita();
		filhoDireitaEsquerda = z.getEsquerda();
		
		p.setDireita(filhoDireitaEsquerda);
		z.setEsquerda(p);
		
		p.setAltura();
		z.setAltura();
		
		return z;
	}
}