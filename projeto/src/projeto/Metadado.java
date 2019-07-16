package projeto;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.RandomAccessFile;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;
import java.util.Properties;

public class Metadado {
	private static  final int TAM_PECA = 1400;
	public static String nome;
	public static long tamanhoArquivo;
	public static int tamanhoPeca;
	public static long qtdPeca;
	public static String peca;
	public static int tamPecaFinal;

	public static void montarMetadado(String diretorio, String metadado) {
		long i;
		byte[] peca = new byte[TAM_PECA];
		Properties property = new Properties();
		File arquivo = new File(diretorio);
		tamanhoArquivo = arquivo.length();
		tamanhoPeca = TAM_PECA;
		qtdPeca = qtdPecas(tamanhoArquivo);
		tamPecaFinal= (int) (TAM_PECA - (qtdPeca * TAM_PECA - tamanhoArquivo));
		byte[] pecaUltimo = new byte[tamPecaFinal];
		try {
			property.setProperty("Nome", diretorio);
			property.setProperty("Tamanho_Arquivo", String.valueOf(tamanhoArquivo));
			property.setProperty("Tamanho_Peca", String.valueOf(tamanhoPeca));
			property.setProperty("Quantidade_Peca", String.valueOf(qtdPeca));
			property.setProperty("Tamanho_Ultima_Peca", String.valueOf(tamPecaFinal));
			for (i = 0; i < qtdPeca - 1; i++) {
				peca = lerArquivo(diretorio, i);
				property.setProperty(String.valueOf(i), sha1(peca));
			}
			pecaUltimo = lerArquivo(diretorio, qtdPeca - 1);
			property.setProperty(String.valueOf(i), sha1(pecaUltimo));
			OutputStream output = new FileOutputStream(metadado);
			property.store(output, null);
			output.close();
			System.out.println("Metadado criado com sucesso");
		} catch (IOException | NoSuchAlgorithmException io) {
			System.out.println("ERRO: " + io.getMessage());
		}
	}
		
	public static byte[] lerArquivo(String diretorio, long pecaRequerida) throws IOException {//pedir a peca do cliente (long pecaRequerida)
	   	byte[] bytes = new byte[tamanhoPeca];
	   	byte[] byteUltimo = new byte[tamPecaFinal];
	   	long pecaAtual = (pecaRequerida) * tamanhoPeca;
	   	long ultimaPeca = qtdPeca - 1;
	   	RandomAccessFile arquivo = new RandomAccessFile(new File(diretorio), "rw");
	    try {    	       
	        if (pecaRequerida != ultimaPeca) {
	        	arquivo.seek(pecaAtual);
	        	arquivo.readFully(bytes);
	    	}
	        else {
	        	arquivo.seek(pecaAtual);
	        	arquivo.readFully(byteUltimo);
	        	return(byteUltimo);
	    	}        
        } catch (IOException e) {
	    	System.out.println("ERRO: " + e.getMessage());
	    } catch (Exception e) {
	    	System.out.println("ERRO: " + e.getCause());
	    } finally {
	    	arquivo.close();
	    }
	    return(bytes);
	}	
	
	public static int qtdPecas(long tam){
		if((tam % TAM_PECA) > 0){
			return((int) ((tam / TAM_PECA) + 1)); 
		}		
		return((int) (tam / TAM_PECA));
	}
	
	public static String sha1(byte[] input) throws NoSuchAlgorithmException {
		MessageDigest mDigest = MessageDigest.getInstance("SHA1");
	    byte[] result = mDigest.digest(input);
	    StringBuffer sb = new StringBuffer();
	    for (int i = 0; i < result.length; i++) {
	    	sb.append(Integer.toString((result[i] & 0xff) + 0x100, 16).substring(1));
	    }
	    return sb.toString();
	}
	
	public static void carregarCabecalhoMetadado(String diretorio) {
		Properties property = new Properties();	
		try {			
			InputStream input = new FileInputStream(diretorio);
			property.load(input);
			nome = property.getProperty("Nome");
			tamanhoArquivo = Long.parseLong(property.getProperty("Tamanho_Arquivo"));
			tamanhoPeca = Integer.parseInt(property.getProperty("Tamanho_Peca"));
			qtdPeca = Integer.parseInt(property.getProperty("Quantidade_Peca"));
			tamPecaFinal = Integer.parseInt(property.getProperty("Tamanho_Ultima_Peca"));
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
	}
	
	public static String carregarPecaMetadado(String diretorio,long id_Peca) {
		Properties property = new Properties();	
		try {			
			InputStream input = new FileInputStream(diretorio);
			property.load(input);
			peca = property.getProperty(String.valueOf(id_Peca));//INTEIRO PARA STRING id_Peca;
			input.close();
		} catch (IOException e) {
			e.printStackTrace();
		}
		return(peca);
	}
}