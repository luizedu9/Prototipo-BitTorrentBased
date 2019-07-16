package projeto;

public class Main {

	public static void main(String[] args) {
		//EXEMPLO - montar C:\\Users\\NOME\\Desktop\\ARQUIVO.EXTENSAO C:\\Users\\NOME\\Desktop\\NOME_METADADO.metadado
		if (args[0].equals("montar")) { //Argumento 0 = montar - Cria o arquivo metadado
			Metadado.montarMetadado(args[1], args[2]); //Argumento 1 e 2 - Diretorio do Arquivo, Diretorio para salvar o metadado .extensao 
		}
		//EXEMPLO - seeder/leecher C:\\Users\\NOME\\Desktop\\ARQUIVO.EXTENSAO C:\\Users\\NOME\\Desktop\\NOME_METADADO.metadado
		if (args[0].equals("seeder") || args[0].equals("leecher")) { //Argumento 0 = seeder/leecher - Inicia o envio/recebimento de arquivo
			new ConexaoP2P(args[0], args[1], args[2]).iniciaPar(); //Argumento 1, 2 - Diretorio Arquivo, Diretorio do metadado;
		}
	}
	
}